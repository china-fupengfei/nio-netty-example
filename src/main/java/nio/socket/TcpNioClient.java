package nio.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;

public class TcpNioClient {
    // 信道选择器
    private Selector selector;

    // 与服务器通信的信道
    SocketChannel socketChannel;

    public TcpNioClient(String host, int port) {
        try {
            // 打开监听信道并设置为非阻塞模式
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);

            // 打开并注册选择器到信道
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    // select()方法只能使用一次，用了之后就会自动删除,每个连接到服务器的选择器都是独立的
                    while (selector.select() > 0) {
                        // 遍历每个有可用IO操作Channel对应的SelectionKey
                        for (Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext(); iter.remove()) {
                            SelectionKey key = iter.next();
                            try {
                                // 如果该SelectionKey对应的Channel中有可读的数据
                                if (key.isReadable()) {
                                    // 使用NIO读取Channel中的数据
                                    SocketChannel sc = (SocketChannel) key.channel();// 获取通道信息
                                    ByteBuffer buffer = ByteBuffer.allocate(1024);// 分配缓冲区大小
                                    sc.read(buffer);// 读取通道里面的数据放在缓冲区内
                                    buffer.flip();// 调用此方法为一系列通道写入或相对获取 操作做好准备
                                    // 将字节转化为为UTF-16的字符串
                                    String receivedString = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
                                    // 控制台打印出来
                                    System.out.println("接收到来自服务器[" + sc.socket().getRemoteSocketAddress() + "]的信息: " + receivedString);
                                    // 为下一次读取作准备
                                    key.interestOps(SelectionKey.OP_READ);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                key.cancel();
                                key.channel().close();
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 发送字符串到服务器
     * @param message
     * @throws IOException
     */
    public void sendMsg(String message) throws IOException {
        ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        socketChannel.write(writeBuffer);
    }

    public static void main(String[] args) throws IOException {
        TcpNioClient client = new TcpNioClient("localhost", 1000);
        client.sendMsg("hello server, i am nio client!");

        Scanner scan = new Scanner(System.in); // 键盘输入数据
        while (scan.hasNextLine()) {
            client.sendMsg(scan.nextLine());
        }
        scan.close();
    }
}
