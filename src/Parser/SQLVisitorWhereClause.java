package Parser;

import java.util.ArrayList;

public class SQLVisitorWhereClause extends SQLBaseVisitor<ArrayList<ArrayList<ArrayList>>> {
    ArrayList<ArrayList<ArrayList>> curExp;
    int orPos;
    public SQLVisitorWhereClause(ArrayList<ArrayList<ArrayList>> curExp, int orPos)
    {
        this.curExp = curExp;
        this.orPos = orPos;
    }

    @Override
    public ArrayList<ArrayList<ArrayList>> visitExpr(SQLParser.ExprContext ctx) {
        if(ctx.K_OR() != null)
        {
            this.curExp.add(new ArrayList<ArrayList>());
            ctx.expr(0).accept(new SQLVisitorWhereClause(this.curExp, this.orPos));
            ctx.expr(1).accept(new SQLVisitorWhereClause(this.curExp, this.orPos+1));
        }
        else if(ctx.K_AND() != null)
        {
            ctx.expr(0).accept(new SQLVisitorWhereClause(this.curExp, this.orPos));
            ctx.expr(1).accept(new SQLVisitorWhereClause(this.curExp, this.orPos));
        }
        else
        {
            int type = resolveType(ctx);
            String colomn_name1 = "";
            String colomn_name2 = "";
            DataTypes data = null;
            ArrayList condition = new ArrayList();
            //todo:交换顺序后调整符号
            if(ctx.expr(0).column_name() != null)
            {
                if(ctx.expr(0).table_name() != null)
                    colomn_name1.concat(ctx.expr(0).table_name().getText()).concat(".");
                colomn_name1.concat(ctx.expr(0).column_name().getText());
            }
            else
            {
                data = ctx.expr(0).literal_value().accept(new SQLVisitorLiteralValue());
            }
            if(ctx.expr(1).column_name() != null)
            {
                if(ctx.expr(1).table_name() != null)
                    colomn_name2.concat(ctx.expr(1).table_name().getText()).concat(".");
                colomn_name2.concat(ctx.expr(1).column_name().getText());
            }
            else
            {
                data = ctx.expr(1).literal_value().accept(new SQLVisitorLiteralValue());
            }
            if(data == null)
            {
                condition.add(colomn_name1);
                condition.add(type);
                condition.add(colomn_name2);
                condition.add(false);
            }
            else
            {
                condition.add((colomn_name1 == "") ? colomn_name2 : colomn_name1);
                condition.add(type);
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
                condition.add(true);
            }
            this.curExp.get(this.orPos).add(condition);
        }
        return this.curExp;
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
}
