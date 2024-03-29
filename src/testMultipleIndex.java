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

        Database db = new Database("TEST");
        db.useDB("TEST");
        int[] a = new int[2];
        a[0] = -1;
        a[1] = 20;
        String[] s = new String[2];
        s[0] = "ID";
        s[1] = "NAME";
        String[] p = new String[1];
        p[0] = "ID";
        boolean[] isNotNull = new boolean[2];
        isNotNull[0] = true;
        isNotNull[1] = true;

        db.createTable(s, a, p, "TEST", isNotNull);


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
        test.add("NAME");
        test.add(0);
        test.add(Integer.toString(num/2));
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
        index.add("ID");
        t.createIndex(index);

        // select with index

        start = System.nanoTime();

        for(ArrayList re: t.SelectRows(test_test_test, null)){
            System.out.println(re);
        }

        end = System.nanoTime();

        System.out.println("select with int index: " + (end - start) / 1000000000.0);

        //create index on string

        ArrayList<String> index2 = new ArrayList<>();
        index2.add("NAME");
        t.createIndex(index2);

        // select with string index



        start = System.nanoTime();

        for(ArrayList re: t.SelectRows(test_test_test, null)){
            System.out.println(re);
        }

        end = System.nanoTime();

        System.out.println("select with string index: " + (end - start) / 1000000000.0);


    }


}
