package Server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59898)) {
            System.out.println("The capitalization server is running...");
            newFixedThreadPool pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Worker(listener.accept()));
            }
        }
    }

    private static class Worker implements Runnable {
        private Socket socket;

        Worker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println("Connected: " + socket);

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                String line = "";
                while (!line.equals("Over")) {
                    try {
                        line = in.readUTF();
                        System.out.println(line);

                        out.writeUTF("Received: " + line);

                    } catch (IOException i) {
                        System.out.println(i);
                        break;
                    }
                }
                socket.close();
            }catch (IOException e){
                System.out.println(e);
            }
        }
    }
}
