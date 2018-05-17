package netty.guide;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

public class TimerServer implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverChannel;

    public TimerServer(int port) {
        try {
            selector = Selector.open();

            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false); // 非阻塞
            //serverChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), port), 1024);
            serverChannel.socket().bind(new InetSocketAddress(port), 1024);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    selector.select(1000); // 1 seconds timeout
                    for (Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext(); iter.remove()) {
                        handleInput(iter.next());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            if (selector != null) try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleInput(SelectionKey key) throws IOException {
        if (!key.isValid()) return;

        try {
            if (key.isAcceptable()) {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }

            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer buff = ByteBuffer.allocate(1024);
                int count = sc.read(buff);
                if (count > 0) {
                    buff.flip();
                    byte[] bytes = new byte[buff.remaining()];
                    buff.get(bytes);
                    String receive = new String(bytes, "UTF-8");
                    System.out.println("time server receive data: " + receive);
                    String resp = "time".equalsIgnoreCase(receive) ? new Date().toString() : "unknow";

                    // response
                    byte[] respBytes = resp.getBytes();
                    ByteBuffer write = ByteBuffer.allocate(respBytes.length);
                    write.put(respBytes);
                    write.flip();
                    sc.write(write);
                } else if (count < 0) {
                    key.cancel();
                    sc.close();
                } else {
                    // ignored
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
            if (key != null) {
                key.cancel();
                if (key.channel() != null) {
                    key.channel().close();
                }
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new TimerServer(1234), "time-server-01").start();
    }
}
