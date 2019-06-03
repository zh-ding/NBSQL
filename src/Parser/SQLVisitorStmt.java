package Parser;

import Database.Database;
import Exceptions.DatabaseException;
import Table.Table;
import generator.Generator;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SQLVisitorStmt extends SQLBaseVisitor<Void>{
    Database db = null;
    DataOutputStream output;

    public SQLVisitorStmt(Database db, DataOutputStream output)
    {
        this.output = output;
        this.db = db;
    }

    private Void writeStr(String s)
    {
        try {
            output.writeBytes(s);
        }
        catch (IOException e)
        {
            System.out.println("Datastream write fail: " + e.getMessage());
        }
        return null;
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
                writeStr("create table " + tableName + " success\n");
            }
            catch (Exception e)
            {
                writeStr("create table "  + tableName + " fail: " + e.getMessage() + '\n');
            }
        }
        else
        {
            writeStr("create table fail\n");
        }
        return null;
    }

    @Override
    public Void visitDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx) {
        if(this.db != null)
        {
            String tableName = ctx.table_name().getText().toUpperCase();
            this.db.dropTable(tableName);
            writeStr("drop table success\n");
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
                    writeStr(column_names.get(i) + "\t");
                    switch (column_types.get(i))
                    {
                        case -1:
                            writeStr("INT");
                            break;
                        case -2:
                            writeStr("LONG");
                            break;
                        case -3:
                            writeStr("FLOAT");
                            break;
                        case -4:
                            writeStr("DOUBLE");
                            break;
                        default:
                            writeStr("STRING(" + column_types.get(i) + ")");
                            break;
                    }
                    writeStr("\n");
                }
            }
            else
            {
                writeStr("Table does not exist\n");
            }
        }
        return null;
    }



    @Override
    public Void visitCreate_database_stmt(SQLParser.Create_database_stmtContext ctx) {
        String name = ctx.database_name().getText().toUpperCase();
        try {
            this.db.newDB(name);
            this.writeStr("create database " + name + " success\n");
        }
        catch (Exception e)
        {
            this.writeStr("create database " + name + " fail: " + e.getMessage() + '\n');
        }
        return null;
    }

    @Override
    public Void visitDrop_database_stmt(SQLParser.Drop_database_stmtContext ctx)
    {
        String name = ctx.database_name().getText().toUpperCase();
        try {
            db.dropDB(name);
            this.writeStr("drop database " + name + " success\n");
        }
        catch (Exception e)
        {
            this.writeStr("drop database " + name +  " fail: " + e.getMessage() + '\n');
        }
        return null;
    }

    @Override
    public Void visitUse_database_stmt(SQLParser.Use_database_stmtContext ctx)
    {
        String name = ctx.database_name().getText().toUpperCase();
        try {
            this.db.useDB(name);
            this.writeStr("use database " + name + " success\n");
        }
        catch (Exception e)
        {
            this.writeStr("use database " + name + " fail: " + e.getMessage() + '\n');
        }
        return null;
    }

    @Override
    public Void visitShow_database_stmt(SQLParser.Show_database_stmtContext ctx) {
        String name = ctx.database_name().getText().toUpperCase();
        ArrayList<String> tables = this.db.showDbTable(name);
        if(tables != null)
        {
            for(String n:tables)
            {
                writeStr(n.toUpperCase());
                writeStr("\t");
            }
            writeStr("\n");
        }
        else
        {
            writeStr("Database " + name + " not exists\n");
        }
        return null;
    }

    @Override
    public Void visitShow_databases_stmt(SQLParser.Show_databases_stmtContext ctx)
    {
        ArrayList<String> dbs = this.db.showDbs();
        for(String s:dbs)
        {
            this.writeStr(s.toUpperCase() + "\t");
        }
        this.writeStr("\n");
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
        for(ArrayList data:dataAll) {
            ArrayList row = new ArrayList();
            for (int i = 0; i < column_names.size(); ++i) {
                int index = columns.indexOf(column_names.get(i));
                if(index >= 0)
                {
                    row.add(data.get(index));
                }
                else
                {
                    row.add(null);
                }
            }
            try {
                t.InsertRow(row);
            } catch (Exception a) {
                writeStr(a.getMessage() + "\n");
                return null;
            }
        }
        writeStr("insert success\n");
        return null;
    }

    @Override
    public Void visitSelect_stmt(SQLParser.Select_stmtContext ctx) {
        if(ctx.join_clause() == null)
        {
            simple_Select(ctx);
        }
        else
        {
            select_with_join(ctx);
        }
        return null;
    }

    private Void select_with_join(SQLParser.Select_stmtContext ctx)
    {
        SQLVisitorJoinConditions joinCondition = ctx.join_clause().accept(new SQLVisitorJoin(this.db));
        ArrayList<ArrayList<ArrayList>> conditions;
        ArrayList<Table> tables = new ArrayList<Table>();
        ArrayList<ArrayList<String>> tableColumnNames = new ArrayList<>();
        ArrayList<ArrayList<Integer>> tableColumnTypes = new ArrayList<>();
        for(String n:joinCondition.tableNames)
        {
            Table t = this.db.getTable(n);
            tables.add(t);
            tableColumnNames.add(t.getColumnName());
            tableColumnTypes.add(t.getColumnType());
        }

        //获得每一列名称
        ArrayList<String> column_names = new ArrayList<String>();
        for(int i = 0; i < ctx.result_column().size(); i++)
        {
            if(ctx.result_column(i).STAR() != null)
            {
                String t = ctx.result_column(i).table_name().accept(new SQLVisitorNames());
                ArrayList<String> name_temp = tableColumnNames.get(joinCondition.tableNames.indexOf(t));
                for(String c:name_temp)
                {
                    c = t + "." + c;
                    column_names.add(c);
                }
            }
            else if(ctx.result_column(i).column_alias() != null)
                column_names.add(ctx.result_column(i).column_alias().getText().toUpperCase());
            else
                column_names.add(ctx.result_column(i).expr().getText().toUpperCase());
        }

        //获得所有要查询的列
        ArrayList<String> column_queries = new ArrayList<>();
        for(int i = 0; i < ctx.result_column().size(); i++) {
            if (ctx.result_column(i).STAR() != null)
            {
                String t = ctx.result_column(i).table_name().accept(new SQLVisitorNames());
                ArrayList<String> name_temp = tableColumnNames.get(joinCondition.tableNames.indexOf(t));
                for(String c:name_temp)
                {
                    c = t + "." + c;
                    column_names.add(c);
                }
            }
            else {
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
            String[] temp = c.split("\\.");
            int index = joinCondition.tableNames.indexOf(temp[0]);
            ArrayList<String> name_temp = tableColumnNames.get(index);
            ArrayList<Integer> type_temp = tableColumnTypes.get(index);
            column_types_queries.add(type_temp.get(name_temp.indexOf(temp[1])));
        }

        //condition
        if(ctx.K_WHERE() != null)
            conditions = ctx.expr().accept(new SQLVisitorWhereClause(tableColumnNames, tableColumnTypes, true));
        else
            conditions = null;

        try {
            Generator<ArrayList> result = this.db.selectFromTables(tables,joinCondition.joinTypes,joinCondition.conditions,conditions,column_queries);
            for(String c:column_names)
            {
                writeStr(c + "\t");
            }
            writeStr("\n");
            for(ArrayList r:result) {
                ArrayList result_output = new ArrayList();
                for (int i = 0; i < ctx.result_column().size(); i++) {
                    if (ctx.result_column(i).STAR() != null) {
                        String t = ctx.result_column(i).table_name().accept(new SQLVisitorNames());
                        ArrayList<String> name_temp = tableColumnNames.get(joinCondition.tableNames.indexOf(t));
                        for (String c : name_temp) {
                            result_output.add(r.get(column_queries.indexOf(c)));
                        }
                    } else {
                        DataTypes data = ctx.result_column(i).expr().accept(new SQLVisitorEvalValue(column_queries, column_types_queries, r));
                        if (data == null) {
                            result_output.add(null);
                            continue;
                        }
                        switch (data.type) {
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
                                result_output.add(data.bool_data ? "TRUE" : "FALSE");
                                break;
                        }
                    }
                }
                for (Object o : result_output) {
                    if (o != null)
                        writeStr(o.toString() + "\t");
                    else
                        writeStr("NULL" + "\t");
                }
                writeStr("\n");
            }
        }
        catch (Exception e)
        {
            writeStr("select fail: " + e.getMessage() + '\n');
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
        ArrayList<ArrayList<ArrayList>> conditions;
        if(ctx.K_WHERE() != null)
            conditions = ctx.expr().accept(new SQLVisitorWhereClause(tableColumnNames, tableColumnTypes));
        else
            conditions = null;
        Generator<ArrayList> result;
        try {
            result = this.db.getTable(tableName).SelectRows(conditions, column_queries);
            for(String c:column_names)
            {
                writeStr(c + "\t");
            }
            writeStr("\n");
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
                            case -1:
                                result_output.add(null);
                                break;
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
                        writeStr(o.toString() + "\t");
                    else
                        writeStr("NULL" + "\t");
                }
                writeStr("\n");
            }
        } catch (Exception e)
        {

            writeStr("select fail: " + e.getMessage() + '\n');
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
            ctx.expr().accept(new SQLVisitorWhereClause(tableColumnNames, tableColumnTypes));
        else
            conditions = null;
        //Generator<ArrayList> result;
        ArrayList<ArrayList> result;
        try {
            this.db.getTable(tableName).DeleteRows(conditions);
            this.writeStr("delete rows success\n");
        } catch (Exception e)
        {
            this.writeStr("delete rows fail " + e.getMessage() + '\n');
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
            ctx.expr().get(ctx.expr().size() - 1).accept(new SQLVisitorWhereClause(tableColumnNames, tableColumnTypes));
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
            this.writeStr("update rows success\n");
        } catch (Exception e)
        {
            this.writeStr("update rows fail: " + e.getMessage() + '\n');
        }
        return null;

    }
}
