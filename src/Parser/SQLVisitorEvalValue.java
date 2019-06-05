package Parser;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;

public class SQLVisitorEvalValue extends SQLBaseVisitor<DataTypes>{

    ArrayList<String> columns;
    ArrayList<Integer> types;
    ArrayList data;

    public SQLVisitorEvalValue(ArrayList<String> columns, ArrayList<Integer> types, ArrayList data)
    {
        this.columns = columns;
        this.types = types;
        this.data = data;
    }

    @Override
    public DataTypes visitExpr(SQLParser.ExprContext ctx) throws ParseCancellationException {
        //字面值
        if(ctx.literal_value() != null)
        {
            return ctx.literal_value().accept(new SQLVisitorLiteralValue());
        }
        //括号
        if(ctx.OPEN_PAR() != null && ctx.CLOSE_PAR() != null)
        {
            return ctx.expr(0).accept(new SQLVisitorEvalValue(columns,types,data));
        }
        //表中数据
        if(ctx.column_name() != null)
        {
            String column_name = ctx.column_name().getText().toUpperCase();
            if(ctx.table_name() != null)
                column_name = ctx.table_name().getText().toUpperCase() + "." + column_name;
            int index = columns.indexOf(column_name);
            int type = types.get(index);
            DataTypes column_data;
            if(data.get(index) == null)
                return new DataTypes(null);
            switch (type)
            {
                case -1:
                    column_data = new DataTypes((int)data.get(index));
                    break;
                case -2:
                    column_data = new DataTypes((long)data.get(index));
                    break;
                case -3:
                    column_data = new DataTypes((float)data.get(index));
                    break;
                case -4:
                    column_data = new DataTypes((double)data.get(index));
                    break;
                default:
                    column_data = new DataTypes((String)data.get(index));
                    break;
            }
            return column_data;
        }
        try {
            //一元运算符
            if (ctx.unary_operator() != null) {
                if (ctx.unary_operator().MINUS() != null) {
                    DataTypes value = ctx.expr(0).accept(new SQLVisitorEvalValue(columns, types, data));
                    value.neg();
                    return value;
                }
                else if(ctx.unary_operator().K_NOT() != null)
                {
                    DataTypes value = ctx.expr(0).accept(new SQLVisitorEvalValue(columns, types, data));
                    value.not();
                    return value;
                }
                else if(ctx.unary_operator().PLUS() != null)
                    return ctx.expr(0).accept(new SQLVisitorEvalValue(columns, types, data));
            }
            //二元运算符
            DataTypes a = ctx.expr(0).accept(new SQLVisitorEvalValue(columns, types, data));
            DataTypes b = ctx.expr(1).accept(new SQLVisitorEvalValue(columns, types, data));
            if(ctx.PLUS() != null)
                return a.add(b);
            else if(ctx.MINUS() != null)
                return a.minus(b);
            else if(ctx.STAR() != null)
                return a.multiply(b);
            else if(ctx.DIV() != null)
                return a.divide(b);
            else if(ctx.MOD() != null)
                return a.mod(b);
            else if(ctx.LT() != null)
                return a.lower(b);
            else if(ctx.LT_EQ() != null)
                return a.lowerEqual(b);
            else if(ctx.GT() != null)
                return a.greater(b);
            else if(ctx.GT_EQ() != null)
                return a.greaterEqual(b);
            else if(ctx.NOT_EQ2() != null)
                return a.notEqual(b);
            else if(ctx.ASSIGN() != null)
                return a.equal(b);
            else if(ctx.K_AND() != null)
                return a.and(b);
            else if(ctx.K_OR() != null)
                return a.or(b);

        }catch (Exception e)
        {
            throw new ParseCancellationException("!" + e.getMessage() + "\n");
        }
        return null;
    }
}
