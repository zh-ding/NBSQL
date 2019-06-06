import Database.Database;
import Exceptions.BPlusTreeException;
import Exceptions.DatabaseException;
import Exceptions.TableException;
import Table.Table;

import java.io.IOException;
import java.util.ArrayList;

public class testMultipleIndex {

    public static void main(String[] args)throws IOException, BPlusTreeException, DatabaseException, TableException {

        int num = 1000;

        Database db = new Database("test");
        db.useDB("test");
        int[] a = new int[2];
        a[0] = -1;
        a[1] = 20;
        String[] s = new String[2];
        s[0] = "id";
        s[1] = "name";
        String[] p = new String[0];
        boolean[] isNotNull = new boolean[2];
        isNotNull[0] = true;
        isNotNull[1] = false;

        db.createTable(s, a, p, "test", isNotNull);


        // insert data

        Table t = db.tables.get(0);
        for(int i = 0; i < num; ++i){
            ArrayList arr = new ArrayList<>();
            arr.add(i);
            arr.add(Integer.toString(i));
            t.InsertRow(arr);
        }

        // select without index

        ArrayList test = new ArrayList();
        test.add("id");
        test.add(0);
        test.add(num/2);
        test.add(true);
        ArrayList<ArrayList> test_test = new ArrayList();
        test_test.add(test);
        ArrayList<ArrayList<ArrayList>> test_test_test = new ArrayList();
        test_test_test.add(test_test);

        long start = System.nanoTime();

        for(ArrayList re: t.SelectRows(test_test_test, null)){
            System.out.println(re);
        }

        long end = System.nanoTime();

        System.out.println("select without index: " + (end - start) / 1000000000.0);

        // create index

        ArrayList<String> index= new ArrayList<>();
        index.add("id");
        t.createIndex(index);

        // select with index

        start = System.nanoTime();

        for(ArrayList re: t.SelectRows(test_test_test, null)){
            System.out.println(re);
        }

        end = System.nanoTime();

        System.out.println("select with index: " + (end - start) / 1000000000.0);


    }


}
