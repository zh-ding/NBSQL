package Parser;

import java.util.ArrayList;

public class SQLVisitorEvalColumns extends SQLBaseVisitor<Void> {

    ArrayList<String> columns;
    public SQLVisitorEvalColumns(ArrayList<String> columns)
    {
        this.columns = columns;
    }

    @Override
    public Void visitExpr(SQLParser.ExprContext ctx) {
        if (ctx.column_name() != null)
        {
            String column_name = "";
            if (ctx.table_name() != null)
            {
                column_name = column_name.concat(ctx.table_name().getText().toUpperCase()).concat(".");
            }
            column_name = column_name.concat(ctx.column_name().getText().toUpperCase());
            this.columns.add(column_name);
        }
        else
        {
            if(ctx.literal_value() != null)
                return null;
            for(int i = 0; i < ctx.expr().size(); i++)
            {
                ctx.expr(i).accept(new SQLVisitorEvalColumns(this.columns));
            }
        }
        return null;
    }
}
