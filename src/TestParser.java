import Exceptions.BPlusTreeException;
import Parser.SQLLexer;
import Parser.SQLParser;
import Parser.SQLVisitorStmt;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import Database.Database;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestParser {
    public static void main(String[] args) throws BPlusTreeException, IOException {
        StringBuffer dbName = new StringBuffer("TEST");
        Database db = new Database("TEST", 0);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in ));
        while(true) {
            String sql = "quit";
            try {
                sql = br.readLine();
            } catch (IOException e) {
            }
            if (sql.compareTo("quit") == 0)
                break;
            StringBuffer output = new StringBuffer();
            SQLParser parser = new SQLParser(new CommonTokenStream(new SQLLexer(CharStreams.fromString(sql))));
            SQLParser.Sql_stmtContext stmt = parser.sql_stmt();
            SQLVisitorStmt visitor = new SQLVisitorStmt(dbName, output);
            stmt.accept(visitor);
            System.out.println(output.toString());
        }
    }
}

// create table test(attr1 int primary key, attr2 long, attr3 float, attr4 double, attr5 string(10));
