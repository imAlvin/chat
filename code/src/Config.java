/**
 * 配置类
 *
 * @author Alvin
 * @date 2019/11/5 01:37
 * @description
 */
public class Config {
    /**
     * 服务器端口
     */
    public static final int PORT = 8888;
    /**
     * 昵称消息分隔符
     */
    public static final String SPLIT = "\0";
    /**
     * 下线消息指令
     */
    public static final String DOWNLINE_COMMAND = "\0\0";
    /**
     * 窗口初始大小
     */
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 600;
    /**
     * 聊天记录窗口初始大小
     */
    public static final int RECORD_PANE_WIDTH = 400;
    public static final int RECORD_PANE_HEIGHT = 400;
    /**
     * 消息最大长度
     */
    public static final int MAX_MSG_LEN = 1024;
}
