package Parser;

import java.util.ArrayList;

public class SQLVisitorJoin extends SQLBaseVisitor{

    @Override
    public Object visitJoin_clause(SQLParser.Join_clauseContext ctx) {
        ArrayList<String> tableNames = new ArrayList<String>();
        for(int i = 0; i < ctx.table_name().size(); i++)
            tableNames.add(ctx.table_name(i).getText());
        ArrayList<Integer> joinTypes = new ArrayList<>();
        for(int i = 0; i < ctx.join_operator().size(); i++)
        {
            if(ctx.join_operator(i).K_OUTER() != null)
                joinTypes.add(1);
            else
                joinTypes.add(0);
        }
        return null;
    }
}
