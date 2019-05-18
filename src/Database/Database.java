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

    public void newDB(String db_name){
        this.db_name = db_name;
        db_name = this.path + db_name;
        File db = new File(db_name);
        db.mkdir();
    }

    public void useDB(String db_name) throws IOException{
        for(int i = 0; i<tables.size(); i++){
            tables.get(i).file.deleteFile();
        }
        this.tables = new ArrayList<Table>();
        this.db_name = db_name;
        db_name = this.path + db_name;
        File db = new File(db_name);
        File[] tmplist = db.listFiles();
        if(tmplist != null)
        {
            for(File f:tmplist){
                String tmp = f.getName();
                Table tmpTable = new Table(tmp.substring(0, tmp.lastIndexOf(".")), this.db_name);
                this.tables.add(tmpTable);
            }
        }
    }

    public void dropDB(String db_name){
        this.db_name = db_name;
        db_name = this.path + db_name;
        File db = new File(db_name);
        if(db.isFile() && db.exists()){
            db.delete();
        }
        else{
            for(int i = 0; i<tables.size(); i++){
                tables.get(i).file.deleteFile();
            }
            File[] tmplist = db.listFiles();
            for(File f: tmplist){
                if(f.exists() && f.isFile()){
                    f.delete();
                }
            }
            db.delete();
        }
    }

    public Database(String db_name, int operation) throws IOException{ ;
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

    public Table createTable (String[] names, int[] types, String[] primary_key, String table_name, boolean[] isNotNull)
        throws IOException {
        Table table = new Table(names, types, primary_key, table_name, this.db_name, isNotNull);
        this.tables.add(table);
        return table;
    }

    public Table getTable (String table_name){
        for(int i = 0; i<tables.size(); i++){
            if(tables.get(i).table_name.compareTo(table_name) == 0){
                return tables.get(i);
            }
        }
        return null;
    }

    public void dropTable (String table_name){
        ArrayList<Table> tmp = new ArrayList<Table>();
        for(int i = 0; i<tables.size(); i++){
            if(tables.get(i).table_name.compareTo(table_name)==0){
                String table_path = this.path + db_name + "/" + table_name+ ".dat";
                File db = new File(table_path);
                if(db.isFile()){
                    this.tables.get(i).file.deleteFile();
                    System.gc();
                    db.delete();
                }
            }
            else{
                tmp.add(tables.get(i));
            }
        }
        this.tables = tmp;
    }
}
