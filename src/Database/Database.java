package Database;

import BPlusTree.BPlusTreeLeafNode;
import Exceptions.BPlusTreeException;
import Utils.FileManager;
import Table.Table;
import javafx.scene.control.Tab;

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
                [table_name, col_name, 0, value, null, True],
                [table_name, col_name, 0, table_name, col_name, False]
            ],
            [
            ]
        ]
    ]
    or null
     */
    // 先做两个无条件的join
    public ArrayList<ArrayList> joinTables(ArrayList<Table> tabs, ArrayList<ArrayList<ArrayList<ArrayList>>> conditions) throws IOException, BPlusTreeException{
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

    private ArrayList<ArrayList> makeSimpleCross(Table a, Table b) throws IOException{
        ArrayList<ArrayList> res = new ArrayList<>();
        BPlusTreeLeafNode node = a.index_forest.get(0).getMostLeftLeafNode();
        BPlusTreeLeafNode node1 = b.index_forest.get(0).getMostLeftLeafNode();
        ArrayList tmp1 = new ArrayList();
        ArrayList tmp2 = new ArrayList();
        while(node != null) {
            for (int i = 0; i < node.keyNum; ++i) {
                int offset = node.pointers.get(i);
                tmp1.add(offset);
            }
            node = (BPlusTreeLeafNode) a.file.readNode(node.rightSibling,0);
        }
        while(node1 != null) {
            for (int i = 0; i < node1.keyNum; ++i) {
                int offset = node1.pointers.get(i);
                tmp2.add(offset);
            }
            node1 = (BPlusTreeLeafNode) b.file.readNode(node1.rightSibling,0);
        }
        for(int i = 0; i<tmp1.size(); ++i){
            for(int j = 0; j<tmp2.size(); ++j){
                ArrayList tmp = new ArrayList();
                tmp.add(tmp1.get(i));
                tmp.add(tmp2.get(j));
                res.add(tmp);
            }
        }
        return res;
    }

    private Map<String, ArrayList> makeHashMap(Table a, ArrayList<Integer> index) throws IOException{
        if(index == null){
            return null;
        }
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
                key.hashCode();
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

    private boolean isObey(Object value1, int operation, Object value2) throws BPlusTreeException{
        if(value1 instanceof Integer && value2 instanceof Integer){
            Integer tmpvalue1 = (Integer)value1;
            Integer tmpvalue2 = (Integer)value2;
            switch (operation){
                case 0:
                    if(tmpvalue1 == tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 1:
                    if(tmpvalue1 < tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 2:
                    if(tmpvalue1 > tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 3:
                    if(tmpvalue1 <= tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 4:
                    if(tmpvalue1 >= tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 5:
                    if(tmpvalue1 != tmpvalue2){
                        return true;
                    }
                    else
                        return false;
            }
        }else if(value1 instanceof Float && value2 instanceof Float){
            Float tmpvalue1 = (Float)value1;
            Float tmpvalue2 = (Float)value2;
            switch (operation){
                case 0:
                    if(tmpvalue1 == tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 1:
                    if(tmpvalue1 < tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 2:
                    if(tmpvalue1 > tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 3:
                    if(tmpvalue1 <= tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 4:
                    if(tmpvalue1 >= tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 5:
                    if(tmpvalue1 != tmpvalue2){
                        return true;
                    }
                    else
                        return false;
            }
        }else if(value1 instanceof Long && value2 instanceof Long){
            switch (operation){
                case 0:
                    if((Long)value1 == (Long)value2){
                        return true;
                    }
                    else
                        return false;
                case 1:
                    if((Long)value1 < (Long)value2){
                        return true;
                    }
                    else
                        return false;
                case 2:
                    if((Long)value1 > (Long)value2){
                        return true;
                    }
                    else
                        return false;
                case 3:
                    if((Long)value1 <= (Long)value2){
                        return true;
                    }
                    else
                        return false;
                case 4:
                    if((Long)value1 >= (Long)value2){
                        return true;
                    }
                    else
                        return false;
                case 5:
                    if((Long)value1 != (Long)value2){
                        return true;
                    }
                    else
                        return false;
            }
        }else if(value1 instanceof Double && value2 instanceof Double){
            Double tmpvalue1 = (Double)value1;
            Double tmpvalue2 = (Double)value2;
            switch (operation){
                case 0:
                    if(tmpvalue1 == tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 1:
                    if(tmpvalue1 < tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 2:
                    if(tmpvalue1 > tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 3:
                    if(tmpvalue1 <= tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 4:
                    if(tmpvalue1 >= tmpvalue2){
                        return true;
                    }
                    else
                        return false;
                case 5:
                    if(tmpvalue1 != tmpvalue2){
                        return true;
                    }
                    else
                        return false;
            }
        }else if(value1 instanceof String && value2 instanceof String){
            switch (operation){
                case 0:
                    if(value1.toString().compareTo(value2.toString()) == 0){
                        return true;
                    }
                    else
                        return false;
                case 1:
                    if(value1.toString().compareTo(value2.toString()) < 0){
                        return true;
                    }
                    else
                        return false;
                case 2:
                    if(value1.toString().compareTo(value2.toString()) > 0){
                        return true;
                    }
                    else
                        return false;
                case 3:
                    if(value1.toString().compareTo(value2.toString()) <= 0){
                        return true;
                    }
                    else
                        return false;
                case 4:
                    if(value1.toString().compareTo(value2.toString()) >= 0){
                        return true;
                    }
                    else
                        return false;
                case 5:
                    if(value1.toString().compareTo(value2.toString()) != 0){
                        return true;
                    }
                    else
                        return false;
            }
        }else{
            throw new BPlusTreeException("cmp: key type not match");
        }
        return true;
    }

    private boolean isObeyConditions(ArrayList<ArrayList> condition, Table tab1, Table tab2, ArrayList tmp1, ArrayList tmp2)
            throws BPlusTreeException{
        if(condition.size() == 0){
            return true;
        }
        for(int i = 0; i<condition.size(); i++){
            if((boolean)condition.get(i).get(5)){
                if(condition.get(i).get(0).toString().compareTo(tab1.table_name) == 0){
                    if(!isObey(tmp1.get(tab1.getColumnName().indexOf(condition.get(i).get(1))), (int)condition.get(i).get(2), condition.get(i).get(3))){
                        return false;
                    }
                }
                else{
                    if(!isObey(tmp2.get(tab2.getColumnName().indexOf(condition.get(i).get(1))), (int)condition.get(i).get(2), condition.get(i).get(3))){
                        return false;
                    }
                }
            }
            else{
                if(condition.get(i).get(0).toString().compareTo(tab1.table_name) == 0){
                    if(condition.get(i).get(3).toString().compareTo(tab1.table_name) == 0){
                        if(!isObey(tmp1.get(tab1.getColumnName().indexOf(condition.get(i).get(1))), (int)condition.get(i).get(2), tmp1.get(tab1.getColumnName().indexOf(condition.get(i).get(3))))){
                            return false;
                        }
                    }
                    else{
                        if(!isObey(tmp1.get(tab1.getColumnName().indexOf(condition.get(i).get(1))), (int)condition.get(i).get(2), tmp2.get(tab2.getColumnName().indexOf(condition.get(i).get(3))))){
                            return false;
                        }
                    }
                }
                else{
                    if(condition.get(i).get(3).toString().compareTo(tab1.table_name) == 0){
                        if(!isObey(tmp2.get(tab2.getColumnName().indexOf(condition.get(i).get(1))), (int)condition.get(i).get(2), tmp1.get(tab1.getColumnName().indexOf(condition.get(i).get(3))))){
                            return false;
                        }
                    }
                    else{
                        if(!isObey(tmp2.get(tab2.getColumnName().indexOf(condition.get(i).get(1))), (int)condition.get(i).get(2), tmp2.get(tab2.getColumnName().indexOf(condition.get(i).get(3))))){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    public Set<ArrayList> joinTwoTables(ArrayList<Table> tabs, ArrayList<ArrayList<ArrayList>> conditions) throws IOException, BPlusTreeException{
        Set<ArrayList> result = new HashSet<>();
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
                    for(int i = 0; i<(hash[0].get(key).size()); ++i){
                        for(int j = 0; j<hash[1].get(key).size(); ++j){
                            ArrayList tmp1 = tabs.get(0).file.readData((int)hash[0].get(key).get(i));
                            ArrayList tmp2 = tabs.get(1).file.readData((int)hash[1].get(key).get(j));
                            boolean flag = true;
                            for(int k = 0; k<index.size(); ++k){
                                if(tmp1.get((int)index.get(k).get(0)).toString().compareTo(tmp2.get((int)index.get(k).get(1)).toString())!=0){
                                    flag = false;
                                }
                            }
                            if(flag){
                                ArrayList tmpitem = new ArrayList();
                                tmpitem.add(hash[0].get(key).get(i));
                                tmpitem.add(hash[1].get(key).get(j));
                                result.add(tmpitem);
                            }
                        }
                    }
                }
            }
        }
        else{
            for(int i = 0; i<conditions.size(); ++i){
                ArrayList<ArrayList> tmpCond = new ArrayList<>();
                ArrayList<Integer> tmp1 = new ArrayList<>();
                ArrayList<Integer> tmp2 = new ArrayList<>();
                Map<String, ArrayList> hash[] = new HashMap[2];
                for(int j = 0; j<conditions.get(i).size(); ++j){
                    if(((boolean)conditions.get(i).get(j).get(5)==false) && ((int)conditions.get(i).get(j).get(2)==0) &&
                            (conditions.get(i).get(j).get(0).toString().compareTo(conditions.get(i).get(j).get(3).toString())!=0)){
                        if(conditions.get(i).get(j).get(0).toString().compareTo(tabs.get(0).table_name) == 0){
                            tmp1.add(tabs.get(0).getColumnName().indexOf(conditions.get(i).get(j).get(1).toString()));
                            tmp2.add(tabs.get(1).getColumnName().indexOf(conditions.get(i).get(j).get(4).toString()));
                        }
                        else{
                            tmp1.add(tabs.get(0).getColumnName().indexOf(conditions.get(i).get(j).get(4).toString()));
                            tmp2.add(tabs.get(1).getColumnName().indexOf(conditions.get(i).get(j).get(1).toString()));
                        }
                    }
                    else{
                        tmpCond.add(conditions.get(i).get(j));
                    }
                }
                if(tmp1.size() != 0){
                    hash[0] = makeHashMap(tabs.get(0), tmp1);
                    hash[1] = makeHashMap(tabs.get(1), tmp2);
                    for(String key : hash[0].keySet()){
                        if(hash[1].containsKey(key)){
                            for(int l = 0; l<(hash[0].get(key).size()); ++l){
                                for(int j = 0; j<hash[1].get(key).size(); ++j){
                                    ArrayList tmp1value = tabs.get(0).file.readData((int)hash[0].get(key).get(l));
                                    ArrayList tmp2value = tabs.get(1).file.readData((int)hash[1].get(key).get(j));
                                    boolean flag = true;
                                    for(int k = 0; k<tmp1.size(); ++k){
                                        if(tmp1value.get((int)tmp1.get(k)).toString().compareTo(tmp2value.get((int)tmp2.get(k)).toString())!=0){
                                            flag = false;
                                        }
                                    }
                                    if(flag && isObeyConditions(tmpCond, tabs.get(0), tabs.get(1), tmp1value, tmp2value)){
                                        ArrayList tmpitem = new ArrayList();
                                        tmpitem.add(hash[0].get(key).get(l));
                                        tmpitem.add(hash[1].get(key).get(j));
                                        result.add(tmpitem);
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    // to do brute-force
                    ArrayList<ArrayList> tmp = makeSimpleCross(tabs.get(0), tabs.get(1));
                    for(int j = 0; j<tmp.size(); j++){
                        for(int k = 0; k<conditions.get(i).size(); ++k){
                            ArrayList tmp1value = tabs.get(0).file.readData((int)tmp.get(j).get(0));
                            ArrayList tmp2value = tabs.get(1).file.readData((int)tmp.get(j).get(1));
                            if(isObeyConditions(conditions.get(i), tabs.get(0), tabs.get(1), tmp1value, tmp2value)){
                                result.add(tmp.get(j));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
