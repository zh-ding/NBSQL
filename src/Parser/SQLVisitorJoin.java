package Parser;

import Database.Database;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;
import java.util.ArrayList;

public class SQLVisitorJoin extends SQLBaseVisitor<SQLVisitorJoinConditions>{

    Database db;

    public SQLVisitorJoin(Database db)
    {
        this.db = db;
    }

    @Override
    public SQLVisitorJoinConditions visitJoin_clause(SQLParser.Join_clauseContext ctx) throws ParseCancellationException {
        try {
            ArrayList<String> tableNames = new ArrayList<String>();
            tableNames.add(ctx.table_name().getText().toUpperCase());
            ArrayList<ArrayList<String>> columnNames = new ArrayList<>();
            ArrayList<ArrayList<Integer>> columnTypes = new ArrayList<>();

            if(db.getTable(tableNames.get(0)) == null)
            {
                throw new ParseCancellationException("Table " + tableNames.get(0) + " doesn't exist\n");
            }
            columnNames.add(db.getTable(tableNames.get(0)).getColumnName());
            columnTypes.add(db.getTable(tableNames.get(0)).getColumnType());
            ArrayList<Integer> joinTypes = new ArrayList<>();
            ArrayList<ArrayList<ArrayList<ArrayList>>> conditions = new ArrayList<>();
            for(SQLParser.Join_defContext c:ctx.join_def())
            {
                String name = c.table_name().getText().toUpperCase();
                tableNames.add(name);
                if(db.getTable(name) == null)
                {
                    throw new ParseCancellationException("Table " + name + " doesn't exist\n");
                }
                columnNames.add(db.getTable(name).getColumnName());
                columnTypes.add(db.getTable(name).getColumnType());
                if(c.K_OUTER() != null)
                {
                    if(c.K_LEFT() != null)
                        joinTypes.add(2);
                    else if(c.K_RIGHT() != null)
                        joinTypes.add(3);
                    else
                        joinTypes.add(1);

                }
                else
                    joinTypes.add(0);
                if((c.K_NATURAL() != null && c.K_ON() != null) || (c.K_NATURAL() == null && c.K_ON() == null))
                {
                    throw new ParseCancellationException("Join clause invalid\n");
                }
                if(c.K_NATURAL() != null)
                {
                    conditions.add(null);
                    continue;
                }
                ArrayList<ArrayList<ArrayList>> condition = c.expr().accept(new SQLVisitorJoinOn(tableNames,columnNames,columnTypes));
                conditions.add(condition);
            }
            return new SQLVisitorJoinConditions(tableNames,joinTypes,conditions);
        }catch (IOException e){
            System.out.println(e);
        }
        return null;
    }
}
