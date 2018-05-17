package nio.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class TcpNioServer {
    private static final int BUFF_SIZE = 1024; // 缓冲区大小
    private static final int TIME_OUT = 3000; // 超时时间，单位毫秒

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel listenerChannel = ServerSocketChannel.open();
        listenerChannel.socket().bind(new InetSocketAddress(1000));
        listenerChannel.configureBlocking(false);
        // 将多路复用器绑定到监听信道,只有非阻塞信道才可以注册多路复用器.并在注册过程中指出该信道可以进行Accept操作
        listenerChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 反复循环,等待IO
        while (true) {
            // 等待某信道就绪(或超时)
            if (selector.select(TIME_OUT) == 0) {
                System.out.print("独自等待.");
                continue;
            }

            // 轮询准备就绪的key
            for (Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext(); iter.remove()) {
                SelectionKey key = iter.next();
                try {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    if (key.isValid() && key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException ex) {
                    //ex.printStackTrace(); // ignored
                    key.cancel();
                    key.channel().close();
                }
            }
        }
    }

    /**
     * 监听到有新的客户端请求，处理新的接入请求
     * 完成TCP三次握手，建立物理链路
     * @param key
     * @throws IOException
     */
    private static void handleAccept(SelectionKey key) throws IOException {
        // 接受客户端建立连接的请求
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        // 设置非阻塞
        clientChannel.configureBlocking(false);
        // 注册到selector，监听读操作，用来读取客户端发送的网络消息
        clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BUFF_SIZE));
    }

    /**
     * 异步读取客户端请求消息
     * @param key
     * @throws IOException
     */
    private static void handleRead(SelectionKey key) throws IOException {
        // 获得与客户端通信的信道
        SocketChannel clientChannel = (SocketChannel) key.channel();
        // 得到并清空缓冲区
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        // 读取信息获得读取的字节数
        long bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            // 没有读取到内容的情况
            clientChannel.close();
        } else {
            // 将缓冲区准备为数据传出状态
            buffer.flip();
            // 将字节转化为为UTF-8的字符串
            String received = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
            // 控制台打印出来
            String now = now();
            System.out.println("[" + now + "]接收到来自[" + clientChannel.socket().getRemoteSocketAddress() + "]的信息: [" + received + "]");
            // 准备发送的文本
            buffer = ByteBuffer.wrap(("[" + now + "]hello client, i am nio server，已经收到你的信息：[" + received + "]").getBytes("UTF-8"));
            clientChannel.write(buffer);
            // 设置为下一次读取或是写入做准备
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    private static void handleWrite(SelectionKey key) throws IOException {
        // do nothing
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
