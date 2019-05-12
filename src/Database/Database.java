package Database;

import Utils.FileManager;
import Table.Table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.DoubleBinaryOperator;

public class Database {
    public ArrayList<Table> tables;
    private String db_name;
    private static String path = "./dat/";
    /*
    0 - new
    1 - use
    2 - drop
     */

    private void newDB(String db_name){
        File db = new File(db_name);
        db.mkdir();
    }

    private void useDB(String db_name) throws IOException{
        File db = new File(db_name);
        File[] tmplist = db.listFiles();
        for(File f:tmplist){
            String tmp = f.getName();
            Table tmpTable = new Table(tmp.substring(0, tmp.lastIndexOf(".")), this.db_name);
            this.tables.add(tmpTable);
        }
    }

    private void dropDB(String db_name){
        File db = new File(db_name);
        if(db.isFile()){
            db.delete();
        }
        else{
            File[] tmplist = db.listFiles();
            for(File f: tmplist){
                f.delete();
            }
        }
    }

    public Database(String db_name, int operation) throws IOException{
        this.db_name = db_name;
        db_name = this.path + db_name;
        this.tables = new ArrayList<Table>();
        switch (operation){
            case 0:
                newDB(db_name);
                break;
            case 1:
                useDB(db_name);
                break;
            case 2:
                dropDB(db_name);
                break;
        }
    }

    public Table createTable (String[] names, int[] types, String[] primary_key, String table_name)
        throws IOException {
        Table table = new Table(names, types, primary_key, table_name, this.db_name);
        this.tables.add(table);
        return table;
    }

    public Table getTable (String table_name){
        for(int i = 0; i<tables.size(); i++){
            if(tables.get(i).table_name == table_name){
                return tables.get(i);
            }
        }
        return null;
    }
}
