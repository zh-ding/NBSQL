package Parser;

public class SQLVisitorLiteralValue extends SQLBaseVisitor<DataTypes> {

    @Override
    public DataTypes visitLiteral_value(SQLParser.Literal_valueContext ctx) {
        if(ctx.K_NULL() != null)
            return null;
        if(ctx.NUMERIC_LITERAL() != null)
        {
            String number = ctx.NUMERIC_LITERAL().getText();
            if(number.contains("."))
            {
                double data = new Double(number).doubleValue();
                return new DataTypes(data);
            }
            else
            {
                long data = new Long(number).longValue();
                return new DataTypes(data);
            }
        }
        else
        {
            String data = ctx.STRING_LITERAL().getText();
            data = data.substring(1,data.length());
            return new DataTypes(data);
        }
    }
}
