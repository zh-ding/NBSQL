package Parser;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;

public class SQLVisitorEvalColumns extends SQLBaseVisitor<Void> {

    ArrayList<String> columns;
    ArrayList<String> tableNames;
    ArrayList<ArrayList<String>> columnNames;
    boolean multiTable;

    public SQLVisitorEvalColumns(ArrayList<String> columns, ArrayList<String> tableNames, ArrayList<ArrayList<String>> columnNames, boolean multiTable)
    {
        this.columns = columns;
        this.tableNames = tableNames;
        this.columnNames = columnNames;
        this.multiTable = multiTable;
    }

    @Override
    public Void visitExpr(SQLParser.ExprContext ctx) {
        if (ctx.column_name() != null)
        {
            String table_name = "";
            String column_name = "";
            int index = 0;
            if (ctx.table_name() != null)
            {
                table_name = ctx.table_name().getText().toUpperCase();
                index = tableNames.indexOf(table_name);
                if(index < 0)
                    throw new ParseCancellationException("!Table " + table_name + " doesn't exist\n");
            }
            column_name = ctx.column_name().getText().toUpperCase();
            if(columnNames.get(index).indexOf(column_name) < 0)
                throw new ParseCancellationException("!Column " + column_name + " doesn't exist\n");
            if(multiTable && (ctx.column_name() != null))
                column_name = table_name + "." + column_name;
            this.columns.add(column_name);
        }
        else
        {
            if(ctx.literal_value() != null)
                return null;
            for(int i = 0; i < ctx.expr().size(); i++)
            {
                ctx.expr(i).accept(new SQLVisitorEvalColumns(columns, tableNames, columnNames, multiTable));
            }
        }
        return null;
    }
}
