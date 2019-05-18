package Parser;

public class SQLVisitorNames  extends SQLBaseVisitor<String> {

    @Override
    public String visitDatabase_name(SQLParser.Database_nameContext ctx) {
        return ctx.getChild(0).accept(new SQLVisitorNames());
    }

    @Override
    public String visitAny_name(SQLParser.Any_nameContext ctx) {
        if(ctx.IDENTIFIER() != null)
            return ctx.IDENTIFIER().getText().toUpperCase();
        else if(ctx.STRING_LITERAL() != null)
            return ctx.STRING_LITERAL().getText();
        else if(ctx.any_name() != null)
            return ctx.any_name().accept(new SQLVisitorNames());
        return null;
    }

    @Override
    public String visitTable_name(SQLParser.Table_nameContext ctx) {
        return ctx.getChild(0).accept(new SQLVisitorNames());
    }

    @Override
    public String visitColumn_name(SQLParser.Column_nameContext ctx) {
        return ctx.getChild(0).accept(new SQLVisitorNames());
    }
}
