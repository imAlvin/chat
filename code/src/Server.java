import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;

/**
 * 服务端
 *
 * @author Alvin
 * @date 2019/11/4 15:08
 * @description
 */
public class Server {
    private Selector selector;

    /**
     * 客户端列表
     */
    private List<SocketChannel> clients;
    /**
     * 消息队列
     */
    private LinkedTransferQueue<String> msgQueue;

    public static void main(String[] args) {
        try {
            new Server().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        init();
        listen();
    }

    /**
     * 初始化
     *
     * @throws IOException
     */
    private void init() throws IOException {
        //初始化群发工作线程、客户端列表、消息队列
        ExecutorService service = Executors.newSingleThreadExecutor();
        clients = new LinkedList<>();
        msgQueue = new LinkedTransferQueue<>();

        //群发消息
        service.submit(() -> {
            while (true) {
                //客户端消息
                String msg = msgQueue.take();

                //当前消息
                Date date = new Date(System.currentTimeMillis());
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CHINA);
                String time = df.format(date);
                String[] str = msg.split(Config.SPLIT);
                for (SocketChannel sc : clients) {
                    try {
                        msg = str[0] + time + str[1];
                        sc.write(ByteBuffer.wrap(msg.getBytes()));
                        System.out.println("群发消息给 " + sc.getRemoteAddress());
                    } catch (IOException e) {
                        //客户端已经断开连接
                        clientDownLine(sc);
                    }
                }
            }
        });

        //绑定端口
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(new InetSocketAddress(Config.PORT));
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server started, listening on :" + ssc.getLocalAddress());
    }

    /**
     * 监听端口
     *
     * @throws IOException
     */
    private void listen() throws IOException {
        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                handle(key);
            }
        }
    }

    private void handle(SelectionKey key) {
        if (key.isAcceptable()) {
            try {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);

                //添加到客户端列表
                clients.add(sc);
                System.out.println(sc.getRemoteAddress() + "连接到服务器");
                //注册读事件
                sc.register(key.selector(), SelectionKey.OP_READ);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (key.isReadable()) {
            SocketChannel sc = null;
            try {
                sc = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(Config.MAX_MSG_LEN);
                buffer.clear();
                int len;
                if ((len = sc.read(buffer)) != -1) {
                    //读取到客户端发来的消息
                    String msg = new String(buffer.array(), 0, len);
                    if (Config.DOWNLINE_COMMAND.equals(msg)) {
                        //客户端下线通知
                        clientDownLine(sc);
                    } else {
                        System.out.println("server receives：" + msg);
                        //添加到消息队列
                        msgQueue.put(msg);
                    }
                }
            } catch (IOException e) {
                clientDownLine(sc);

            }
        }
    }

    private void clientDownLine(SocketChannel sc) {
        try {
            if (sc != null) {
                clients.remove(sc);
                System.out.println(sc.getRemoteAddress() + "已下线");
                sc.close();
            }
        } catch (IOException ignored) {

        }
    }
}
