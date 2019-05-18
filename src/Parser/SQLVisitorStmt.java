package Parser;
import Database.Database;
import Table.Table;
import com.sun.tools.javac.util.List;

import java.io.IOException;
import java.util.ArrayList;

public class SQLVisitorStmt extends SQLBaseVisitor<Void>{
    Database db = null;
    StringBuffer dbName;
    StringBuffer output;
    public SQLVisitorStmt(StringBuffer dbName, StringBuffer output) throws IOException
    {
        this.dbName = dbName;
        this.output = output;
        this.db = new Database(this.dbName.toString(),1);
    }

    @Override
    public Void visitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx) {
        if(db != null)
        {
            String tableName = ctx.table_name().getText();
            String[] names;
            int[] types;
            String[] primary_key;
            for(int i = 0; i < ctx.column_def().size(); i++)
            {
                
            }
        }
        else
        {
            output.append("create table fail");
        }
        return null;
    }

    @Override
    public Void visitDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx) {
        if(this.db != null)
        {
            String tableName = ctx.table_name().getText();
        }
        return null;
    }

    @Override
    public Void visitCreate_database_stmt(SQLParser.Create_database_stmtContext ctx) {
        String name = ctx.getChild(2).accept(new SQLVisitorNames());
        try {
            Database temp = new Database(name,0);
            this.output.append("create database success");
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            this.output.append("create database fail");
        }
        return null;
    }

    @Override
    public Void visitShow_database_stmt(SQLParser.Show_database_stmtContext ctx) {
        String name = ctx.getChild(2).accept(new SQLVisitorNames());
        Database temp;
        try {
            if(name == this.dbName.toString())
                temp = this.db;
            else
                temp = new Database(name,0);
            for(int i = 0; i < temp.tables.size(); i++)
            {
                output.append(temp.tables.get(i).table_name);
                output.append(" ");
            }
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            this.output.append("show database fail");
        }
        return null;
    }

    @Override
    public Void visitInsert_stmt(SQLParser.Insert_stmtContext ctx) {
        String tableName = ctx.table_name().getText();
        Table t = this.db.getTable(tableName);
        ArrayList<String> colomns= new ArrayList<String>();
        for(int i = 0; i < ctx.column_name().size(); i++)
        {
            colomns.add(ctx.column_name(i).getText());
        }

        int count = 0;
        if(colomns.size())
        for(int i = 0; i < ctx.expr().size(); i++)
        {

        }
    }

    @Override
    public Void visitSelect_stmt(SQLParser.Select_stmtContext ctx) {

        if(ctx.join_clause() == null)
        {

        }
        return null;
    }

    private ArrayList<String> simple_Select(SQLParser.Select_stmtContext ctx) {
        String tableName = ctx.table_name().getText();
        ArrayList<String> colomns = new ArrayList<String>();
        for(int i = 0; i < ctx.result_column().size(); i++)
        {

        }
        ArrayList<ArrayList<ArrayList>> conditions = new ArrayList<ArrayList<ArrayList>>();
        conditions.add(new ArrayList<ArrayList>());
        ctx.expr().accept(new SQLVisitorWhereClause(conditions,0));
        this.db.getTable(tableName).SelectRows(conditions,);
    }
}
