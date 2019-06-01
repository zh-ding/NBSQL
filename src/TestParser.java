import Database.Database;
import Exceptions.BPlusTreeException;
import Exceptions.DatabaseException;
import Parser.SQLLexer;
import Parser.SQLParser;
import Parser.SQLVisitorStmt;
import Parser.ThrowingErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;

public class TestParser {
    public static void main(String[] args) throws BPlusTreeException, IOException, DatabaseException {
        File dat = new File("./dat/");
        if(!dat.exists())
        {
            dat.mkdir();
        }
        Database db = new Database("TEST");
        DataOutputStream out = new DataOutputStream(System.out);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            String sql = "quit";
            try {
                sql = br.readLine();
            } catch (IOException e) {
            }
            if (sql.compareTo("quit") == 0)
                break;
            try {
                SQLLexer lexer = new SQLLexer(CharStreams.fromString(sql));
                lexer.removeErrorListeners();
                lexer.addErrorListener(new ThrowingErrorListener());
                SQLParser parser = new SQLParser(new CommonTokenStream(lexer));
                parser.removeErrorListeners();
                parser.addErrorListener(new ThrowingErrorListener());
                SQLParser.Sql_stmtContext stmt = parser.sql_stmt();
                SQLVisitorStmt visitor = new SQLVisitorStmt(db, out);
                stmt.accept(visitor);
            } catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
}

