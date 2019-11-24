import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketException;

/**
 * 客户端启动类
 *
 * @author Alvin
 * @date 2019/11/4 15:49
 * @description
 */
public class ClientFrame extends Frame {
    public static void main(String[] args) {
        new ClientFrame().launch();
    }

    private Client client;
    /**
     * 聊天记录
     */
    private JTextPane tpRecord = new JTextPane();
    /**
     * 聊天记录容器
     */
    private JScrollPane pane = new JScrollPane(tpRecord);
    /**
     * 昵称输入框
     */
    private TextField tfName = new TextField();
    /**
     * 服务器地址输入框
     */
    private TextField tfServerHost = new TextField("127.0.0.1");
    /**
     * 发送消息输入框
     */
    private TextField txMsg = new TextField();
    /**
     * 按钮
     */
    private JButton bSend = new JButton("发送");
    private JButton bConnect = new JButton("连接");
    /**
     * 底层按钮容器
     */
    private JPanel panelSetting = new JPanel(new GridLayout(2, 2));
    private JPanel panelControl = new JPanel(new GridLayout(1, 2));
    private JPanel panel = new JPanel(new BorderLayout());

    /**
     * 标志是否连接服务器
     */
    private boolean isConnect = false;
    /**
     * 接收消息线程
     */
    private Thread recv;

    /**
     * 初始化窗口
     */
    private void launch() {
        client = new Client();

        //界面显示初始化
        this.setSize(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        this.setTitle("聊天室");
        add(pane, BorderLayout.NORTH);
        panelSetting.add(new JLabel("昵称："));
        panelSetting.add(tfName);
        panelSetting.add(new JLabel("服务器地址："));
        panelSetting.add(tfServerHost);
        panelControl.add(bSend);
        panelControl.add(bConnect);
        tpRecord.setPreferredSize(new Dimension(Config.RECORD_PANE_WIDTH, Config.RECORD_PANE_HEIGHT));
        panel.add(panelSetting, BorderLayout.NORTH);
        panel.add(txMsg, BorderLayout.CENTER);
        panel.add(panelControl, BorderLayout.SOUTH);
        add(panel, BorderLayout.SOUTH);
        pack();

        //窗口关闭事件
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    disconnect();
                    System.exit(0);
                } catch (IOException e1) {
                    //断开连接失败处理
                    JOptionPane.showConfirmDialog(null, e1.getMessage(), "断开连接失败", JOptionPane.DEFAULT_OPTION);
                }
            }
        });

        //修改昵称事件
        tfName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                ClientFrame.this.setTitle("本机：" + tfName.getText());
            }
        });

        //发送事件
        txMsg.addActionListener(e -> ClientFrame.this.send());
        bSend.addActionListener(e -> ClientFrame.this.send());

        //连接事件
        bConnect.addActionListener(e -> {
            if (isConnect) {
                System.out.println("已连接服务器，无需重复请求");
            } else {
                ClientFrame.this.connect();
            }
        });

        //显示窗口
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();
        this.setLocation(width / 2 - 200, height / 2 - 300);
        this.setVisible(true);
    }

    /**
     * 发送消息
     */
    private void send() {
        String msg = txMsg.getText().trim();
        if (msg.length() > 0) {
            try {
                client.send(tfName.getText() + " " + Config.SPLIT + "\n" + msg);
                txMsg.setText("");
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    isConnect = false;
                    if (JOptionPane.YES_OPTION ==
                            JOptionPane.showConfirmDialog(null, "请重新连接", "连接异常", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        ClientFrame.this.connect();
                    }
                } else {
                    //发送失败提示
                    JOptionPane.showConfirmDialog(null, e.getMessage(), "发送消息失败", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NullPointerException e) {
                //需要先连接服务器
                JOptionPane.showConfirmDialog(null, "尚未连接服务器", "发送消息失败", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * 连接服务器
     */
    private void connect() {
        try {
            client.connect(tfServerHost.getText());
            isConnect = true;
            //开始接收消息
            recv = new Thread(new Recv());
            recv.start();
            appendStrToTpRecord("Connected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开与服务器的连接
     */
    private void disconnect() throws IOException {
        client.close();
        isConnect = false;
    }

    /**
     * 接收消息
     */
    private class Recv implements Runnable {
        @Override
        public void run() {
            try {
                while (isConnect) {
                    String str = client.read();
                    if (null != str) {
                        appendStrToTpRecord(str);
                    }
                }
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    isConnect = false;
                }
            }
        }
    }

    private void appendStrToTpRecord(String str) {
        String strO = tpRecord.getText();
        tpRecord.setText(strO + (strO.length() > 0 ? "\n" : "") + str);
        //自动滚动到底部（有问题：最新的消息没有显示）
        pane.getVerticalScrollBar().setValue(pane.getVerticalScrollBar().getMaximum());
    }
}
