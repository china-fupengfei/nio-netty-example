package nio.socket;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TcpBioClient {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 1000);

        new Thread() {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            @Override
            public void run() {
                writer.write("hello server, i am bio client!");
                writer.flush();
                Scanner scan = new Scanner(System.in); // 键盘输入数据
                while (scan.hasNextLine()) {
                    writer.write(scan.nextLine());
                    writer.flush();
                }
            }
        }.start();

        int readCound;
        while (true) {
            if ((readCound = socket.getInputStream().available()) > 0) {
                byte[] buff = new byte[readCound];
                socket.getInputStream().read(buff);
                System.out.println(new String(buff));
            }

            //会阻塞
            /*BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String body = reader.readLine();
            if (body != null) System.out.println(body);*/
        }
    }

}
