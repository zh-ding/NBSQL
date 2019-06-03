package Parser;

import org.antlr.v4.runtime.misc.ParseCancellationException;

public class SQLVisitorLiteralValue extends SQLBaseVisitor<DataTypes> {

    private int type = -5;

    public SQLVisitorLiteralValue(int type)
    {
        this.type = type;
    }

    public SQLVisitorLiteralValue()
    {

    }

    @Override
    public DataTypes visitLiteral_value(SQLParser.Literal_valueContext ctx) {
        if(ctx.K_NULL() != null)
            return null;
        if(ctx.NUMERIC_LITERAL() != null)
        {
            String number = ctx.NUMERIC_LITERAL().getText();
            if(number.contains("."))
            {
                switch (type) {
                    case -3:
                        float dataFloat = Float.parseFloat(number);
                        return new DataTypes(dataFloat);
                    case -4:
                    case -5:
                        double dataDouble = Double.parseDouble(number);
                        return new DataTypes(dataDouble);
                    default:
                        throw new ParseCancellationException("!type mismatch\n");
                }
            }
            else
            {
                switch (type) {
                    case -1:
                        int dataInt = Integer.parseInt(number);
                        return new DataTypes(dataInt);
                    case -2:
                    case -5:
                        long dataLong = Long.parseLong(number);
                        return new DataTypes(dataLong);
                    case -3:
                        float dataFloat = Float.parseFloat(number);
                        return new DataTypes(dataFloat);
                    case -4:
                        double dataDouble = Double.parseDouble(number);
                        return new DataTypes(dataDouble);
                    default:
                        throw new ParseCancellationException("!type mismatch\n");
                }
            }
        }
        else
        {
            String data = ctx.STRING_LITERAL().getText();
            data = data.substring(1,data.length()-1);
            return new DataTypes(data);
        }
    }
}
