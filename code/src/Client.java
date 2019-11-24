import java.io.IOException;
import java.net.Socket;

/**
 * 客户端
 *
 * @author Alvin
 * @date 2019/11/4 15:14
 * @description
 */
class Client {
    private Socket s;

    void connect(String serverHost) throws IOException {
        s = new Socket(serverHost, Config.PORT);
    }

    void send(String msg) throws IOException {
        System.out.println("发送消息：" + msg);
        s.getOutputStream().write(msg.getBytes());
        s.getOutputStream().flush();
    }

    String read() throws IOException {
        byte[] buffer = new byte[Config.MAX_MSG_LEN];
        int len;
        String msg;
        if ((len = s.getInputStream().read(buffer)) != -1) {
            System.out.println("接收消息：" + (msg = new String(buffer, 0, len)));
        } else {
            msg = null;
        }

        return msg;
    }

    void close() throws IOException {
        if (s != null && !s.isClosed()) {
            send(Config.DOWNLINE_COMMAND);
            s.close();
        }
    }
}
