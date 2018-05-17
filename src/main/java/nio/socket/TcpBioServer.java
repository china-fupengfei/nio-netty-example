package nio.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TcpBioServer implements Runnable {
    private Socket socket;

    public TcpBioServer(Socket socket) {
        this.socket = socket;
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1000);
        while (true) {
            new Thread(new TcpBioServer(ss.accept())).start();
        }
    }

    @Override
    public void run() {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            int readCount;
            while (true) {
                if ((readCount = socket.getInputStream().available()) > 0) {
                    byte[] buff = new byte[readCount];
                    socket.getInputStream().read(buff);
                    String received = new String(buff);
                    String now = now();
                    System.out.println("[" + now + "]接收到来自[" + socket.getRemoteSocketAddress() + "]的信息: [" + received + "]");
                    writer.println("[" + now + "]hello client, i am bio server，已经收到你的信息：[" + received + "]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
