package Client;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private OutputStream outToServer;
    private DataOutputStream out;
    private InputStream inFromServer;
    private DataInputStream in;

    public Client(String host, int port) throws IOException{
        this.socket = new Socket(host, port);
        this.outToServer = socket.getOutputStream();
        this.inFromServer = socket.getInputStream();
        this.out = new DataOutputStream(this.outToServer);
        this.in = new DataInputStream(this.inFromServer);
    }

    void sendSql(String sql)throws IOException{
        this.out.writeUTF(sql);
    }

    String receiveText()throws IOException{
        String line = this.in.readUTF();
        return line;
    }

    void importFile(File f){
        RandomAccessFile writer = null;
        try {
            writer = new RandomAccessFile("result.txt", "rw");
            writer.setLength(0);
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            FileInputStream fis = new FileInputStream(f);
            char current;
            while(fis.available() > 0) {
                String sql = "";
                while (fis.available() > 0) {
                    current = (char) fis.read();
                    if(current == '\n')
                        continue;
                    sql = sql + current;
                    if (current == ';')
                        break;
                }
                sendSql(sql);
                while(true){
                    try {
                        String line = receiveText();
                        if(line.equals("over"))
                            break;
                        writer.writeBytes(line);
                    }catch (IOException e){
                        System.out.println(e);
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void close(){
        try {
            socket.close();
        }catch (IOException e){
            System.out.println(e);
        }
    }

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
