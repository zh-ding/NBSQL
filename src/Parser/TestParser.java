package Parser;

import Exceptions.BPlusTreeException;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import Database.Database;

import java.io.IOException;

public class TestParser {
    public static void main(String[] args) throws BPlusTreeException, IOException {
        StringBuffer dbName = new StringBuffer("DEFAULT");
        while(true) {
            String sql = System.console().readLine();
            if(sql == "quit")
                break;
            StringBuffer output = new StringBuffer();
            SQLParser parser = new SQLParser(new CommonTokenStream(new SQLLexer(CharStreams.fromString(sql))));
            SQLParser.Sql_stmtContext stmt = parser.sql_stmt();
            SQLVisitorStmt visitor = new SQLVisitorStmt(dbName,output);
            stmt.accept(visitor);
            System.out.println(output.toString());
        }
    }
}
