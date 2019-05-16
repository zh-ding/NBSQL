import Database.Database;
import Exceptions.BPlusTreeException;
import Table.Table;

import java.io.IOException;
import java.util.ArrayList;

public class test {
    public static void main(String[] args) throws BPlusTreeException,IOException {

//        ArrayList arr = new ArrayList<>();
//        arr.add(1);
//        arr.add("testa");
//        table.InsertRow(arr);
//        ArrayList arr1 = new ArrayList<>();
//        arr1.add(2);
//        arr1.add("twew1");
//        table.InsertRow(arr1);
//        ArrayList arr2 = new ArrayList<>();
//        arr2.add(3);
//        arr2.add("twew2");
//        table.InsertRow(arr2);
//        ArrayList arr3 = new ArrayList<>();
//        arr3.add(4);
//        arr3.add("twew3");
//        table.InsertRow(arr3);
//        ArrayList arr4 = new ArrayList<>();
//        arr4.add(5);
//        arr4.add("twew4");
//        table.InsertRow(arr4);
//        ArrayList arr5 = new ArrayList<>();
//        arr5.add(6);
//        arr5.add("twew5");
//        table.InsertRow(arr5);
        String path = "./dat/";
        Database db = new Database("test", 0);
        int[] a = new int[2];
        a[0] = -1;
        a[1] = 8;
        String[] s = new String[2];
        s[0] = "m_id";
        s[1] = "name";
        String[] p = new String[1];
        p[0] = "m_id";
        db.createTable(s, a, p, "test");
        Database db1 = new Database("test", 1);
        for(int i = 0; i<db1.tables.size(); i++){
            System.out.println(db1.tables.get(i).table_name);
        }

        ArrayList arr = new ArrayList<>();
        Table table = db1.tables.get(0);
        arr.add(1);
        arr.add("testa");
        table.InsertRow(arr);
        table.index_forest.get(0).printBPlusTree();
        System.out.println();

        ArrayList arr1 = new ArrayList<>();
        arr1.add(2);
        arr1.add("twew1");
        table.InsertRow(arr1);
        table.index_forest.get(0).printBPlusTree();
        System.out.println();

        ArrayList arr2 = new ArrayList<>();
        arr2.add(3);
        arr2.add("twew2");
        table.InsertRow(arr2);
        table.index_forest.get(0).printBPlusTree();
        System.out.println();

        ArrayList arr3 = new ArrayList<>();
        arr3.add(4);
        arr3.add("twew3");
        table.InsertRow(arr3);
        table.index_forest.get(0).printBPlusTree();
        System.out.println();

        ArrayList arr4 = new ArrayList<>();
        arr4.add(5);
        arr4.add("twew4");
        table.InsertRow(arr4);
        table.index_forest.get(0).printBPlusTree();
        System.out.println();

        ArrayList arr5 = new ArrayList<>();
        arr5.add(6);
        arr5.add("twew5");
        table.InsertRow(arr5);
        table.index_forest.get(0).printBPlusTree();
        System.out.println();

        Database db2 = new Database("test", 1);

        table = db2.tables.get(0);

        table.index_forest.get(0).printBPlusTree();
        System.out.println();

//        File db = new File(path);
//        if(db.exists()){
//            File[] tmplist = db.listFiles();
//            for(File f: tmplist){
//                System.out.print(f.getName().substring(0,f.getName().lastIndexOf(".")));
//                System.out.print("\n");
//            }
//        }

    }
}