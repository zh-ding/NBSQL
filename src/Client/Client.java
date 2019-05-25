package Client;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {

        Socket[] sockets = new Socket[10];
        OutputStream[] outToServer = new OutputStream[10];
        DataOutputStream[] out = new DataOutputStream[10];
        InputStream[] inFromServer = new InputStream[10];
        DataInputStream[] in = new DataInputStream[10];
        for(int i = 0; i < 10; ++i){
            sockets[i] = new Socket("127.0.0.1", 59898);
            outToServer[i] = sockets[i].getOutputStream();
            out[i] = new DataOutputStream(outToServer[i]);
            inFromServer[i] = sockets[i].getInputStream();
            in[i] = new DataInputStream(inFromServer[i]);
        }

        for(Integer i = 0; i < 10; ++i){
            out[i].writeUTF("hhh" + i.toString());
            System.out.println(in[i].readUTF());
        }

    }
}
