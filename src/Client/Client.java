package Client;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("127.0.0.1", 59898);
        OutputStream outToServer = socket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        InputStream inFromServer = socket.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String sql = "";
            try {
                sql = br.readLine();
            } catch (IOException e) {
                System.out.println(e);
            }

            out.writeUTF(sql);
            if(sql.equals("quit"))
                break;
            String line = "";
            while(true){
                line = in.readUTF();
                if(line.equals("over")) {
                    break;
                }
                System.out.println(line);
            }
        }
    }
}
