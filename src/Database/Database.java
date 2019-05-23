package Database;

import BPlusTree.BPlusTreeLeafNode;
import Utils.FileManager;
import Table.Table;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
    /*
    0: =
    1: <
    2: >
    3: <=
    4: >=
    5: <>
    conditions
    [
        [
            [
                [table_id, col_name, 0, value, null, True],
                [table_id, col_name, 0, table_id, col_name, False]
            ],
            [
            ]
        ]
    ]
    or null
     */
    // 先做两个无条件的join
    public ArrayList<ArrayList> joinTables(ArrayList<Table> tabs, ArrayList<ArrayList<ArrayList<ArrayList>>> conditions) throws IOException{
        ArrayList result = new ArrayList();
        ArrayList tmp = new ArrayList();
        tmp.add(tabs.get(0));
        tmp.add(tabs.get(1));
        joinTwoTables(tmp, conditions.get(0));
        return result;
    }

    private ArrayList<ArrayList> getCommonColIndex(Table a, Table b){
        ArrayList res = new ArrayList();
        ArrayList col_name1 = a.getColumnName();
        ArrayList col_name2 = b.getColumnName();
        for(int i = 1; i<col_name1.size(); ++i){
            for(int j = 1; j<col_name2.size(); ++j){
                if(col_name1.get(i).toString().compareTo(col_name2.get(j).toString()) == 0){
                    ArrayList tmp = new ArrayList();
                    tmp.add(i);
                    tmp.add(j);
                    res.add(tmp);
                }
            }
        }
        return res;
    }

    private Map<String, ArrayList> makeHashMap(Table a, ArrayList<Integer> index) throws IOException{
        Map<String, ArrayList> res = new HashMap<String, ArrayList>();
        BPlusTreeLeafNode node = a.index_forest.get(0).getMostLeftLeafNode();
        while(node != null){
            for(int i = 0; i<node.keyNum; ++i){
                int offset = node.pointers.get(i);
                ArrayList tmp = a.file.readData(offset);
                String key = "";
                for(int j = 0; j<index.size(); ++j){
                    if(tmp.get(index.get(j)) != null){
                        key += tmp.get(index.get(j)).toString();
                    }
                }
                if(!res.containsKey(key)){
                    ArrayList tmpHash = new ArrayList();
                    tmpHash.add(offset);
                    res.put(key, tmpHash);
                }
                else{
                    ArrayList tmpHash = (ArrayList) res.get(key);
                    tmpHash.add(offset);
                    res.put(key, tmpHash);
                }
            }
            node = (BPlusTreeLeafNode) a.file.readNode(node.rightSibling,0);
        }
        return res;
    }

    public Map<String, ArrayList> joinTwoTables(ArrayList<Table> tabs, ArrayList<ArrayList<ArrayList>> conditions) throws IOException{
        Map<String, ArrayList> result = new HashMap<String, ArrayList>();

        if(conditions == null){
            ArrayList<ArrayList> index = getCommonColIndex(tabs.get(0), tabs.get(1));
            Map<String, ArrayList> hash[] = new HashMap[2];
            for(int i = 0; i<2; i++){
                ArrayList tmp = new ArrayList();
                for(int j = 0; j<index.size(); ++j){
                    tmp.add(index.get(j).get(i));
                }
                hash[i] = makeHashMap(tabs.get(i), tmp);
            }
            for(String key : hash[0].keySet()){
                if(hash[1].containsKey(key)){
                    ArrayList tmp = new ArrayList();
                    for(int i = 0; i<(hash[0].get(key).size()); ++i){
                        for(int j = 0; j<hash[1].get(key).size(); ++j){
                            ArrayList tmpitem = new ArrayList();
                            tmpitem.add(hash[0].get(key).get(i));
                            tmpitem.add(hash[1].get(key).get(j));
                            tmp.add(tmpitem);
                        }
                    }
                    result.put(key, tmp);
                }
            }
        }
        else{

        }
        return result;
    }
}
