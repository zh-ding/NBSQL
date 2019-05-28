package Parser;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;

public class SQLVisitorJoinOn extends SQLBaseVisitor<ArrayList<ArrayList<ArrayList>>> {

    ArrayList<String> tableNames;
    ArrayList<ArrayList<String>> columnNames;
    ArrayList<ArrayList<Integer>> columnTypes;
    
    public SQLVisitorJoinOn(ArrayList<String> tableNames,
            ArrayList<ArrayList<String>> columnNames,
            ArrayList<ArrayList<Integer>> columnTypes)
    {
        this.tableNames = tableNames;
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
    }

    @Override
    public ArrayList<ArrayList<ArrayList>> visitExpr(SQLParser.ExprContext ctx) {
        if(ctx.OPEN_PAR() != null && ctx.CLOSE_PAR() != null)
            return ctx.expr(0).accept(new SQLVisitorJoinOn(tableNames,columnNames,columnTypes));
        if(ctx.K_OR() != null)
        {
            ArrayList<ArrayList<ArrayList>> l = ctx.expr(0).accept(new SQLVisitorJoinOn(tableNames,columnNames,columnTypes));
            ArrayList<ArrayList<ArrayList>> r = ctx.expr(1).accept(new SQLVisitorJoinOn(tableNames,columnNames,columnTypes));
            l.addAll(r);
            return l;
        }
        else if(ctx.K_AND() != null)
        {
            ArrayList<ArrayList<ArrayList>> l = ctx.expr(0).accept(new SQLVisitorJoinOn(tableNames,columnNames,columnTypes));
            ArrayList<ArrayList<ArrayList>> r = ctx.expr(1).accept(new SQLVisitorJoinOn(tableNames,columnNames,columnTypes));
            ArrayList<ArrayList<ArrayList>> mix = new ArrayList<>();
            for(int i = 0; i < l.size(); i++)
            {
                for(int j = 0; j < r.size(); j++)
                {
                    ArrayList<ArrayList> temp = new ArrayList<>();
                    temp.addAll(l.get(i));
                    temp.addAll(r.get(j));
                    mix.add(temp);
                }
            }
            return mix;
        }
        else
        {
            ArrayList<ArrayList<ArrayList>> singleCondition = new ArrayList<ArrayList<ArrayList>>();
            singleCondition.add(new ArrayList<ArrayList>());
            int type = resolveType(ctx);
            String column_name1 = "";
            String table_name1 = "";
            String column_name2 = "";
            String table_name2 = "";
            DataTypes data = null;
            ArrayList condition = new ArrayList();
            if(ctx.expr(0).column_name() == null && ctx.expr(1).column_name() == null)
            {
                throw new ParseCancellationException("Invalid Expression");
            }
            if(ctx.expr(0).column_name() != null)
            {
                if(ctx.expr(0).table_name() != null)
                    table_name1 = ctx.expr(0).table_name().getText().toUpperCase();
                column_name1 = column_name1.concat(ctx.expr(0).column_name().accept(new SQLVisitorNames()));
            }
            if(ctx.expr(1).column_name() != null)
            {
                if(ctx.expr(1).table_name() != null)
                    table_name2 = ctx.expr(1).table_name().getText().toUpperCase();
                column_name2 = column_name2.concat(ctx.expr(1).column_name().accept(new SQLVisitorNames()));
            }
            if(ctx.expr(0).literal_value() != null)
            {
                int index = tableNames.indexOf(table_name2);
                int dataType = columnTypes.get(index).get(columnNames.get(index).indexOf(column_name2));
                data = ctx.expr(0).literal_value().accept(new SQLVisitorLiteralValue(dataType));
            }
            else
            {
                int index = tableNames.indexOf(table_name1);
                int dataType = columnTypes.get(index).get(columnNames.get(index).indexOf(column_name1));
                data = ctx.expr(1).literal_value().accept(new SQLVisitorLiteralValue(dataType));
            }
            if(!column_name1.equals("") && !column_name2.equals(""))
            {
                condition.add(table_name1);
                condition.add(column_name1);
                condition.add(type);
                condition.add(table_name2);
                condition.add(column_name2);
                condition.add(false);
            }
            else
            {
                if(column_name2.equals(""))
                {
                    condition.add(table_name1);
                    condition.add(column_name1);
                    condition.add(type);
                }
                else
                {
                    condition.add(table_name2);
                    condition.add(column_name2);
                    condition.add(switchType(type));
                }

                if(data != null)
                {
                    switch (data.type)
                    {
                        case 0:
                            condition.add(data.int_data);
                            break;
                        case 1:
                            condition.add(data.long_data);
                            break;
                        case 2:
                            condition.add(data.float_data);
                            break;
                        case 3:
                            condition.add(data.double_data);
                            break;
                        case 4:
                            condition.add(data.string_data);
                            break;
                    }
                }
                else
                {
                    condition.add(null);
                }
                condition.add(null);
                condition.add(true);
            }
            singleCondition.get(0).add(condition);
            return singleCondition;
        }
    }

    private int resolveType(SQLParser.ExprContext ctx)
    {
        if(ctx.ASSIGN() != null)
            return 0;
        else if(ctx.LT() != null)
            return 1;
        else if(ctx.GT() != null)
            return 2;
        else if(ctx.LT_EQ() != null)
            return 3;
        else if(ctx.GT_EQ() != null)
            return 4;
        else if(ctx.NOT_EQ2() != null)
            return 5;
        return -1;
    }

    private int switchType(int type)
    {
        switch (type)
        {
            case 0:
                return 0;
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 4;
            case 4:
                return 3;
            case 5:
                return 5;
        }
        return -1;
    }
}
