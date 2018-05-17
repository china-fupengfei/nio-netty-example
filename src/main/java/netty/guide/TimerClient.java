package netty.guide;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TimerClient implements Runnable {

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean isStop;

    public TimerClient(String host, int port) {
        try {
            this.host = host == null ? InetAddress.getLocalHost().getHostAddress() : host;
            this.port = port;
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        while (!isStop) {
            try {
                selector.select(1000);
                for (Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext(); iter.remove()) {
                    handleInput(iter.next());
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

    }

    private void handleInput(SelectionKey key) throws IOException {
        if (!key.isValid()) return;

        try {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else {
                    System.exit(1);
                }
            } 
            if (key.isReadable()) {
                ByteBuffer readBuff = ByteBuffer.allocate(1024);
                int count = sc.read(readBuff);
                if (count > 0) {
                    readBuff.flip();
                    byte[] bytes = new byte[readBuff.remaining()];
                    readBuff.get(bytes);
                    System.out.println("client receive: " + new String(bytes, "UTF-8"));
                    this.isStop = true;
                } else if (count < 0) {
                    key.cancel();
                    sc.close();
                } else {
                    // ignored
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (key != null) {
                key.cancel();
                if (key.channel() != null) {
                    key.channel().close();
                }
            }
        }
    }

    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(host, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel sc) throws IOException {
        byte[] req = "time".getBytes();
        ByteBuffer buff = ByteBuffer.allocate(req.length);
        buff.put(req);
        buff.flip();
        sc.write(buff);
        if (!buff.hasRemaining()) {
            System.out.println("request successed.");
        }
    }

    public static void main(String[] args) {
        new Thread(new TimerClient("192.168.1.2", 1234)).start();
    }

}
