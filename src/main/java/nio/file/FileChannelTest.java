package nio.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import org.junit.Test;

public class FileChannelTest {

    @Test
    public void testRead() throws IOException {

        RandomAccessFile aFile = new RandomAccessFile("D:\\sql script\\scott.sql", "rw");
        FileChannel inChannel = aFile.getChannel();

        ByteBuffer buf = ByteBuffer.allocate(48);

        while (inChannel.read(buf) != -1) {
            buf.flip();

            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }

            buf.clear();
        }
        aFile.close();
    }

    @Test
    public void testTransfer() throws IOException {
        RandomAccessFile fromFile = new RandomAccessFile("D:/普华签名服务器环境配置.txt", "rw");
        FileChannel fromChannel = fromFile.getChannel();

        RandomAccessFile toFile = new RandomAccessFile("D:/hadoop-1.2.1配置.txt", "rw");
        FileChannel toChannel = toFile.getChannel();

        toChannel.transferFrom(fromChannel, fromChannel.size(), fromChannel.size());
        fromFile.close();
        toChannel.close();
        toFile.close();
    }

    @Test
    public void testSelector() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("119.75.218.70", 80));
        socketChannel.configureBlocking(false);
        ByteBuffer buf = ByteBuffer.allocate(48);
        while (socketChannel.read(buf) != -1) {
            buf.flip();
            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }
            buf.clear();
        }
        /*Selector selector = Selector.open();
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
        while (key.isReadable()) {
        }*/

    }
}
