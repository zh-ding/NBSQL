package Parser;

import java.util.ArrayList;

public class SQLVisitorColumnDef extends SQLBaseVisitor<ArrayList> {

    @Override
    public ArrayList visitColumn_def(SQLParser.Column_defContext ctx) {
        ArrayList def = new ArrayList();
        def.add(ctx.column_name().getText().toUpperCase());
        def.add(ctx.type_name().accept(new SQLVisitorTypeName()));
        if(ctx.column_constraint().size() > 0)
        {
            if(ctx.column_constraint(0).K_PRIMARY() != null && ctx.column_constraint(0).K_KEY() != null)
                def.add(new Integer(0));
            else if(ctx.column_constraint(0).K_NOT() != null && ctx.column_constraint(0).K_NULL() != null)
                def.add(new Integer(1));
            else
                def.add(null);
        }
        else
        {
            def.add(null);
        }
        return def;
    }

    @Override
    public ArrayList<String> visitTable_constraint(SQLParser.Table_constraintContext ctx) {
        ArrayList<String> columns = new ArrayList<>();
        for(int i = 0; i < ctx.indexed_column().size(); i++)
        {
            columns.add(ctx.indexed_column(i).getText().toUpperCase());
        }
        return columns;
    }
}
