package Parser;
import Database.Database;
import Table.Table;
import generator.Generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SQLVisitorStmt extends SQLBaseVisitor<Void>{
    Database db = null;
    StringBuffer dbName;
    StringBuffer output;
    public SQLVisitorStmt(StringBuffer dbName, StringBuffer output) throws IOException
    {
        this.dbName = dbName;
        this.output = output;
        this.db = new Database(this.dbName.toString(),1);
    }

    @Override
    public Void visitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx) {
        if(db != null)
        {
            String tableName = ctx.table_name().getText().toUpperCase();
            ArrayList<String> names = new ArrayList<>();
            int[] types = new int[ctx.column_def().size()];
            ArrayList<String> primary_key = new ArrayList<>();
            boolean[] not_null = new boolean[ctx.column_def().size()];
            for(int i = 0; i < ctx.column_def().size(); i++)
            {
                ArrayList def = ctx.column_def(i).accept(new SQLVisitorColumnDef());
                names.add((String)def.get(0));
                types[i] = ((Integer)def.get(1)).intValue();
                if(def.get(2) == null)
                {
                    not_null[i] = false;
                }
                else if((Integer)def.get(2) == 0)
                {
                    primary_key.add((String)def.get(0));
                    not_null[i] = false;
                }
                else if((Integer)def.get(2) == 1)
                {
                    not_null[i] = true;
                }
            }
            for(int i = 0; i < ctx.table_constraint().size(); i++)
            {
                ArrayList<String> columns = ctx.table_constraint(i).accept(new SQLVisitorColumnDef());
                if(ctx.table_constraint(i).K_PRIMARY() != null && ctx.table_constraint(i).K_KEY() != null)
                {
                    primary_key.addAll(columns);
                }
                else if(ctx.table_constraint(i).K_NOT() != null && ctx.table_constraint(i).K_NULL() != null)
                {
                    for(String s:columns)
                    {
                        not_null[names.indexOf(s)] = true;
                    }
                }
            }
            try {
                this.db.createTable(names.toArray(new String[names.size()]), types, primary_key.toArray(new String[primary_key.size()]), tableName, not_null);
                output.append("create table success");
            }
            catch (IOException e)
            {
                output.append("create table fail");
            }
        }
        else
        {
            output.append("create table fail");
        }
        return null;
    }

    @Override
    public Void visitDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx) {
        if(this.db != null)
        {
            String tableName = ctx.table_name().getText().toUpperCase();
            this.db.dropTable(tableName);
            output.append("drop table success");
        }
        return null;
    }

    @Override
    public Void visitShow_table_stmt(SQLParser.Show_table_stmtContext ctx)
    {
        if(this.db != null)
        {
            String tableName = ctx.table_name().getText().toUpperCase();
            Table t = this.db.getTable(tableName);
            if(t != null) {
                ArrayList<String> column_names = t.getColumnName();
                ArrayList<Integer> column_types = t.getColumnType();
                for (int i = 1; i < column_names.size(); i++) {
                    output.append(column_names.get(i)).append("\t");
                    switch (column_types.get(i))
                    {
                        case -1:
                            output.append("INT");
                            break;
                        case -2:
                            output.append("LONG");
                            break;
                        case -3:
                            output.append("FLOAT");
                            break;
                        case -4:
                            output.append("DOUBLE");
                            break;
                        default:
                            output.append("STRING(").append(column_types.get(i)).append(")");
                            break;
                    }
                    output.append("\n");
                }
            }
            else
            {
                output.append("Table does not exist");
            }
        }
        return null;
    }



    @Override
    public Void visitCreate_database_stmt(SQLParser.Create_database_stmtContext ctx) {
        String name = ctx.database_name().getText().toUpperCase();
        try {
            Database temp = new Database(name,0);
            this.output.append("create database success");
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            this.output.append("create database fail");
        }
        return null;
    }

    @Override
    public Void visitDrop_database_stmt(SQLParser.Drop_database_stmtContext ctx)
    {
        String name = ctx.database_name().getText().toUpperCase();
        if(name.equals(this.dbName.toString()))
        {
            this.output.append("Database is being used");
            return null;
        }
        try {
            Database temp = new Database(name,2);
            this.output.append("drop database success");
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            this.output.append("drop database fail");
        }
        return null;
    }

    @Override
    public Void visitUse_database_stmt(SQLParser.Use_database_stmtContext ctx)
    {
        String name = ctx.database_name().getText().toUpperCase();
        try {
            this.db = new Database(name,1);
            this.dbName.setLength(0);
            this.dbName.append(name);
            this.output.append("use database ").append(name).append(" success");
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            this.output.append("use database ").append(name).append(" fail");
        }
        return null;
    }

    @Override
    public Void visitShow_database_stmt(SQLParser.Show_database_stmtContext ctx) {
        String name = ctx.database_name().getText().toUpperCase();
        Database temp;
        try {
            if(name.equals(this.dbName.toString()))
                temp = this.db;
            else
                temp = new Database(name,0);
            for(int i = 0; i < temp.tables.size(); i++)
            {
                output.append(temp.tables.get(i).table_name);
                output.append("\t");
            }
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            this.output.append("show database fail");
        }
        return null;
    }

    @Override
    public Void visitShow_databases_stmt(SQLParser.Show_databases_stmtContext ctx)
    {
        String path = "./dat/";
        File dir = new File(path);
        if(dir.exists()) {
            File[] dirList = dir.listFiles();
            for (File f : dirList) {
                if (f.isDirectory()) {
                    this.output.append(f.getName().toUpperCase()).append("\t");
                }
                this.output.append("\n");
            }
        }
        else
        {
            this.output.append("Data directory not exists");
        }
        return null;
    }

    @Override
    public Void visitInsert_stmt(SQLParser.Insert_stmtContext ctx){
        String tableName = ctx.table_name().getText().toUpperCase();
        Table t = this.db.getTable(tableName);
        ArrayList<String> column_names = this.db.getTable(tableName).getColumnName();
        column_names = new ArrayList<String>(column_names.subList(1,column_names.size()));
        ArrayList<Integer> column_types = this.db.getTable(tableName).getColumnType();
        column_types = new ArrayList<Integer>(column_types.subList(1,column_types.size()));

        ArrayList<String> columns = new ArrayList<String>();
        for(int i = 0; i < ctx.column_name().size(); i++)
        {
            columns.add(ctx.column_name(i).getText().toUpperCase());
        }
        if(columns.size() == 0)
        {
            columns.addAll(column_names);
        }

        ArrayList<ArrayList> dataAll = new ArrayList<ArrayList>();
        for(int i = 0; i < ctx.insert_values().size(); i++)
        {
            ArrayList data = new ArrayList();
            for(int j = 0; j < ctx.insert_values(i).expr().size(); j++) {
                int type = column_types.get(column_names.indexOf(columns.get(j)));
                DataTypes dataTmp = ctx.insert_values(i).expr(j).literal_value().accept(new SQLVisitorLiteralValue(type));
                if (dataTmp != null) {
                    switch (dataTmp.type) {
                        case 0:
                            data.add(dataTmp.int_data);
                            break;
                        case 1:
                            data.add(dataTmp.long_data);
                            break;
                        case 2:
                            data.add(dataTmp.float_data);
                            break;
                        case 3:
                            data.add(dataTmp.double_data);
                            break;
                        case 4:
                            data.add(dataTmp.string_data);
                            break;
                        default:
                            break;
                    }
                } else {
                    data.add(null);
                }
            }
            dataAll.add(data);
        }
//        ArrayList<String> col_name = t.getColumnName();
//        ArrayList row = new ArrayList();
//        if(columns.size() == 0){
//            int k = 0;
//            for(int i = 0; i<column_names.size(); ++i){
////                if(col_name.get(i).toString().compareTo("id")==0){
////                    continue;
////                }
//                if(k < data.size()){
//                    row.add(data.get(k));
//                    k++;
//                    continue;
//                }
//                row.add(null);
//            }
//        }
//        else{
        for(ArrayList data:dataAll) {
            ArrayList row = new ArrayList();
            for (int i = 0; i < column_names.size(); ++i) {
//                if(col_name.get(i).toString().compareTo("id")==0){
//                    continue;
//                }
                int index = columns.indexOf(column_names.get(i));
                if(index >= 0)
                {
                    row.add(data.get(index));
                }
                else
                {
                    row.add(null);
                }
//                for (int j = 0; j < columns.size(); ++j) {
//                    if (column_names.get(i).equals(columns.get(j))) {
//                        row.add(data.get(j));
//                        continue;
//                    }
//                }
//                row.add(null);
            }
            try {
                t.InsertRow(row);
            } catch (Exception a) {
                output.append(a.getMessage());
                return null;
            }
        }
        output.append("insert success");
        return null;
    }

    @Override
    public Void visitSelect_stmt(SQLParser.Select_stmtContext ctx) {
        if(ctx.join_clause() == null)
        {
            simple_Select(ctx);
        }
        return null;
    }

    private Void simple_Select(SQLParser.Select_stmtContext ctx) {
        String tableName = ctx.table_name().getText().toUpperCase();
        ArrayList<String> tableColumnNames = this.db.getTable(tableName).getColumnName();
        tableColumnNames = new ArrayList<String>(tableColumnNames.subList(1,tableColumnNames.size()));
        ArrayList<Integer> tableColumnTypes = this.db.getTable(tableName).getColumnType();
        tableColumnTypes = new ArrayList<Integer>(tableColumnTypes.subList(1,tableColumnTypes.size()));
        //获得每一列名称
        ArrayList<String> column_names = new ArrayList<String>();
        for(int i = 0; i < ctx.result_column().size(); i++)
        {
            if(ctx.result_column(i).STAR() != null)
            {
                column_names.addAll(tableColumnNames);
            }
            else if(ctx.result_column(i).column_alias() != null)
                column_names.add(ctx.result_column(i).column_alias().getText().toUpperCase());
            else
                column_names.add(ctx.result_column(i).expr().getText().toUpperCase());
        }
        //获得所有要查询的列
        ArrayList<String> column_queries = new ArrayList<>();
        for(int i = 0; i < ctx.result_column().size(); i++) {
            if (ctx.result_column(i).STAR() != null) {
                column_queries.addAll(tableColumnNames);
            } else {
                ArrayList<String> temp = new ArrayList<>();
                ctx.result_column(i).expr().accept(new SQLVisitorEvalColumns(temp));
                column_queries.addAll(temp);
            }
        }
        Set<String> column_queries_set = new HashSet<String>(column_queries);
        column_queries = new ArrayList<>(column_queries_set);
        ArrayList<Integer> column_types_queries = new ArrayList<>();
        for(String c:column_queries)
        {
            column_types_queries.add(tableColumnTypes.get(tableColumnNames.indexOf(c)));
        }
        //condition
        ArrayList<ArrayList<ArrayList>> conditions = new ArrayList<ArrayList<ArrayList>>();
        conditions.add(new ArrayList<ArrayList>());
        if(ctx.K_WHERE() != null)
            ctx.expr().accept(new SQLVisitorWhereClause(conditions,0, tableColumnNames, tableColumnTypes));
        else
            conditions = null;
        Generator<ArrayList> result;
        try {
            result = this.db.getTable(tableName).SelectRows(conditions, column_queries);
            for(String c:column_names)
            {
                output.append(c).append("\t");
            }
            output.append("\n");
            for(ArrayList r:result)
            {
                ArrayList result_output = new ArrayList();
                for(int i = 0; i < ctx.result_column().size(); i++)
                {
                    if(ctx.result_column(i).STAR() != null)
                    {
                        for(String c:tableColumnNames)
                        {
                            result_output.add(r.get(column_queries.indexOf(c)));
                        }
                    }
                    else
                    {
                        DataTypes data = ctx.result_column(i).expr().accept(new SQLVisitorEvalValue(column_queries,column_types_queries,r));
                        if(data == null)
                        {
                            result_output.add(null);
                            continue;
                        }
                        switch (data.type)
                        {
                            case 0:
                                result_output.add(data.int_data);
                                break;
                            case 1:
                                result_output.add(data.long_data);
                                break;
                            case 2:
                                result_output.add(data.float_data);
                                break;
                            case 3:
                                result_output.add(data.double_data);
                                break;
                            case 4:
                                result_output.add(data.string_data);
                                break;
                            case 5:
                                result_output.add(data.bool_data?"TRUE":"FALSE");
                                break;
                        }
                    }
                }
                for(Object o:result_output)
                {
                    if(o != null)
                        output.append(o.toString()).append("\t");
                    else
                        output.append("NULL").append("\t");
                }
                output.append("\n");
            }
        } catch (Exception e)
        {

            output.append("select fail");
        }
        return null;
    }
    @Override
    public Void visitDelete_stmt(SQLParser.Delete_stmtContext ctx) {
        String tableName = ctx.table_name().getText().toUpperCase();
        ArrayList<String> tableColumnNames = this.db.getTable(tableName).getColumnName();
        tableColumnNames = new ArrayList<String>(tableColumnNames.subList(1,tableColumnNames.size()));
        ArrayList<Integer> tableColumnTypes = this.db.getTable(tableName).getColumnType();
        tableColumnTypes = new ArrayList<Integer>(tableColumnTypes.subList(1,tableColumnTypes.size()));

        ArrayList<ArrayList<ArrayList>> conditions = new ArrayList<ArrayList<ArrayList>>();
        conditions.add(new ArrayList<ArrayList>());
        if(ctx.K_WHERE() != null)
            ctx.expr().accept(new SQLVisitorWhereClause(conditions,0,tableColumnNames, tableColumnTypes));
        else
            conditions = null;
        Generator<ArrayList> result;
        try {
            result = this.db.getTable(tableName).SelectRows(conditions,tableColumnNames);
            for(ArrayList row:result)
            {
                this.db.getTable(tableName).DeleteRow(row);
            }
            this.output.append("delete rows success");
        } catch (Exception e)
        {
            this.output.append("delete rows fail");
        }
        return null;
    }

    @Override
    public Void visitUpdate_stmt(SQLParser.Update_stmtContext ctx) {
        String tableName = ctx.table_name().getText().toUpperCase();
        ArrayList<String> tableColumnNames = this.db.getTable(tableName).getColumnName();
        tableColumnNames = new ArrayList<String>(tableColumnNames.subList(1,tableColumnNames.size()));
        ArrayList<Integer> tableColumnTypes = this.db.getTable(tableName).getColumnType();
        tableColumnTypes = new ArrayList<Integer>(tableColumnTypes.subList(1,tableColumnTypes.size()));

        ArrayList<String> column_names = new ArrayList<>();
        ArrayList data = new ArrayList();
        ArrayList<ArrayList<ArrayList>> conditions = new ArrayList<ArrayList<ArrayList>>();
        conditions.add(new ArrayList<ArrayList>());

        if(ctx.K_WHERE() != null) {
            ctx.expr().get(ctx.expr().size() - 1).accept(new SQLVisitorWhereClause(conditions, 0, tableColumnNames, tableColumnTypes));
        }
        else
        {
            conditions = null;
        }

        for(int i = 0; i < ctx.column_name().size(); i++)
        {
            String columnName = ctx.column_name(i).getText().toUpperCase();
            int columnType = tableColumnTypes.get(tableColumnNames.indexOf(columnName));
            column_names.add(columnName);
            DataTypes dataTmp = ctx.expr(i).accept(new SQLVisitorLiteralValue(columnType));
            if(dataTmp != null)
            {
                switch (dataTmp.type){
                    case 0:
                        data.add(dataTmp.int_data);
                        break;
                    case 1:
                        data.add(dataTmp.long_data);
                        break;
                    case 2:
                        data.add(dataTmp.float_data);
                        break;
                    case 3:
                        data.add(dataTmp.double_data);
                        break;
                    case 4:
                        data.add(dataTmp.string_data);
                        break;
                    default:
                        break;
                }
            }
            else
            {
                data.add(null);
            }
        }
        try {
            this.db.getTable(tableName).UpdateRow(conditions,column_names,data);
            this.output.append("update rows success");
        } catch (Exception e)
        {
            this.output.append("update rows fail");
        }
        return null;

    }
}
