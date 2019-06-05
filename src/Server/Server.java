
package Server;

import Database.Database;
import Exceptions.DatabaseException;
import Parser.SQLLexer;
import Parser.SQLParser;
import Parser.SQLVisitorStmt;
import Parser.ThrowingErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

public class Server {
    public static ConcurrentHashMap<String, Lock> G_lock = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59898)) {
            System.out.println("The capitalization server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(20);
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

                Database db = new Database("TEST");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                StringBuffer output = new StringBuffer();
                StringBuffer dbName = new StringBuffer("TEST");

                String line = "";
                while (!line.equals("quit")) {
                    try {
                        line = in.readUTF();
                        System.out.println(socket + line);
                        SQLLexer lexer = new SQLLexer(CharStreams.fromString(line));
                        lexer.removeErrorListeners();
                        lexer.addErrorListener(new ThrowingErrorListener());
                        SQLParser parser = new SQLParser(new CommonTokenStream(lexer));
                        parser.removeErrorListeners();
                        parser.addErrorListener(new ThrowingErrorListener());
                        SQLParser.Sql_stmtContext stmt = parser.sql_stmt();
                        SQLVisitorStmt visitor = new SQLVisitorStmt(db, out);
                        stmt.accept(visitor);
                        out.writeUTF("over");

                    } catch (Exception i) {
                        System.out.println(i);
                        out.writeUTF(i.getMessage());
                        out.writeUTF("over");
                        System.out.println(i);
                        //break;
                    }

                }
                socket.close();
            }catch (IOException e){
                System.out.println(e);
            }
            catch (DatabaseException e){
                System.out.println(e);
            }
        }
    }
}
