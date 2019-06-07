
package Server;

import Database.Database;
import Exceptions.DatabaseException;
import Parser.SQLLexer;
import Parser.SQLParser;
import Parser.SQLVisitorStmt;
import Parser.ThrowingErrorListener;
import Utils.CacheTimer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

public class Server {
    public static ConcurrentHashMap<String, Lock> G_lock = new ConcurrentHashMap<>();

    public static final int maxNodeLength = 10000;
    public static ConcurrentHashMap<String, Map<Integer, ArrayList>> node_cache = new ConcurrentHashMap<>();

    public static final int maxDataCache = 10000;
    public static ConcurrentHashMap<String, Map<Integer, ArrayList>> data_cache = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> auto_id = new ConcurrentHashMap<>();

    private static final long interval = 600*1000;

    public static void main(String[] args) throws Exception {
        CacheTimer task = new CacheTimer();
        Timer timer = new Timer();
        timer.schedule(task, new Date(), interval);
        int port;
        if(args.length == 0)
            port = 59898;
        else
            port = Integer.parseInt(args[0]);
        try (ServerSocket listener = new ServerSocket(port)) {
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
            Database db = null;
            try {
                System.out.println("Connected: " + socket);

                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                db = new Database("TEST");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                StringBuffer output = new StringBuffer();
                StringBuffer dbName = new StringBuffer("TEST");

                String line = "";
                while (!line.equals("quit")) {
                    try {
                        line = in.readUTF();
                        System.out.println(socket + line);
                        try {
                            SQLLexer lexer = new SQLLexer(CharStreams.fromString(line));
                            lexer.removeErrorListeners();
                            lexer.addErrorListener(new ThrowingErrorListener());
                            SQLParser parser = new SQLParser(new CommonTokenStream(lexer));
                            parser.removeErrorListeners();
                            parser.addErrorListener(new ThrowingErrorListener());
                            SQLParser.Sql_stmtContext stmt = parser.sql_stmt();
                            SQLVisitorStmt visitor = new SQLVisitorStmt(db, out);
                            stmt.accept(visitor);
                        }catch (Exception e){
                            try {
                                System.out.println("1: " + e.toString());
                                out.writeUTF("!" + e.getMessage() + "\n");
                            }catch (Exception e1){
                                out.writeUTF("!" + e.toString() + "\n");
                            }
                        }

                        out.writeUTF("over");

                    } catch (IOException i) {
                        System.out.println("2: " + i);
                        out.writeUTF("over");
                    }
                }
                socket.close();
            }catch (IOException e){
                System.out.println("3 " + e);
            } catch (DatabaseException e){
                System.out.println("4: " + e);
            }finally {
                db.quitDB();
            }
        }
    }
}
