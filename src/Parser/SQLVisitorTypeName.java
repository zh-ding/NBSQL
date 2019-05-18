package Parser;

public class SQLVisitorTypeName extends SQLBaseVisitor<Integer> {
    @Override
    public Integer visitType_name(SQLParser.Type_nameContext ctx) {
        Integer type = -5;
        if(ctx.K_INT() != null)
        {
            type = -1;
        }
        else if(ctx.K_LONG() != null)
        {
            type = -2;
        }
        else if(ctx.K_FLOAT() != null)
        {
            type = -3;
        }
        else if(ctx.K_DOUBLE() != null)
        {
            type = -4;
        }
        else if(ctx.K_STRING() != null)
        {
            type = new Integer(ctx.signed_number().getText());
        }
        return type;
    }
}
