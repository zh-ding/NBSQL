import Database.Database;
import Exceptions.BPlusTreeException;
import Parser.SQLLexer;
import Parser.SQLParser;
import Parser.SQLVisitorStmt;
import Parser.ThrowingErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestParser {
    public static void main(String[] args) throws BPlusTreeException, IOException {
        File dat = new File("./dat/");
        if(!dat.exists())
        {
            dat.mkdir();
        }
        StringBuffer dbName = new StringBuffer("TEST");
        Database db = new Database("TEST", 0);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            String sql = "quit";
            try {
                sql = br.readLine();
            } catch (IOException e) {
            }
            if (sql.compareTo("quit") == 0)
                break;
            StringBuffer output = new StringBuffer();
            try {
                SQLLexer lexer = new SQLLexer(CharStreams.fromString(sql));
                lexer.removeErrorListeners();
                lexer.addErrorListener(new ThrowingErrorListener());
                SQLParser parser = new SQLParser(new CommonTokenStream(lexer));
                parser.removeErrorListeners();
                parser.addErrorListener(new ThrowingErrorListener());
                SQLParser.Sql_stmtContext stmt = parser.sql_stmt();
                SQLVisitorStmt visitor = new SQLVisitorStmt(dbName, output);
                stmt.accept(visitor);
                System.out.println(output.toString());
            } catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }

        db.dropDB("TEST");
    }
}

// create table test(attr1 int, attr2 long, attr3 float not null, attr4 double, attr5 string(10), primary key(attr1), not null(attr5));
