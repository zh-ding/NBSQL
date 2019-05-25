import Database.Database;
import Exceptions.BPlusTreeException;
import Exceptions.TableException;
import Table.Table;
import generator.Generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class test {
    public static void main(String[] args) throws BPlusTreeException,IOException, TableException {
        int num = 100; // data
        int table_num = 10;

        Database db = new Database("test", 0);
        db.newDB("test_for_use");
        db.useDB("test");
        int[] a = new int[2];
        a[0] = -1;
        a[1] = 20;
        String[] s = new String[2];
        s[0] = "m_id";
        String[] p = new String[1];
        p[0] = "m_id";
        boolean[] isNotNull = new boolean[2];
        isNotNull[0] = true;
        isNotNull[1] = false;

        for(int i =0; i<table_num; ++i){
            s[1] = Integer.toString(i)+"_name";
            db.createTable(s, a, p, "test_"+Integer.toString(i), isNotNull);
        }

        db.useDB("test");

        for(int i = 0; i<table_num; ++i){
            Table table = db.tables.get(i);
            for(int j = 0; j<num; j++){
                ArrayList arr = new ArrayList<>();
                arr.add(j);
                arr.add(Integer.toString(j));
                table.InsertRow(arr);
            }
        }

        System.out.println("----------------------start testing insert-----------------");
        for(int i = 0; i<table_num; ++i){
            Table table = db.tables.get(i);
            Generator<ArrayList> tmp =  table.SelectRows(null, null);
            int number = 0;
            for(ArrayList tmpres: tmp){
                number++;
            }
            if(number == num){
                System.out.println("table " + Integer.toString(i) + " insert success");
            }
            else {
                System.out.println("table " + Integer.toString(i) + " insert fail");
            }
        }

        System.out.println("----------------------start testing Persistent storage-----------------");

        db.useDB("test_for_use");
        db.useDB("test");

        for(int i = 0; i<table_num; ++i){
            Table table = db.tables.get(i);
            Generator<ArrayList> tmp =  table.SelectRows(null, null);
            int number = 0;
            for(ArrayList tmpres: tmp){
                number++;
            }
            if(number == num){
                System.out.println("table " + Integer.toString(i) + " reload success");
            }
            else {
                System.out.println("table " + Integer.toString(i) + " reload fail");
            }
        }

        System.out.println("----------------------start testing delete-----------------");
//        ArrayList test = new ArrayList();
//
//        for(int i = 0; i<table_num; ++i){
//            Table table = db.tables.get(i);
//            Generator<ArrayList> tmp =  table.SelectRows(null, null);
//            int number = 0;
//            for(ArrayList tmpres: tmp){
//                number++;
//            }
//            if(number == num){
//                System.out.println("table " + Integer.toString(i) + " reload success");
//            }
//            else {
//                System.out.println("table " + Integer.toString(i) + " reload fail");
//            }
//        }

//        table = db.tables.get(0);
//        table1 = db.tables.get(1);
//        table2 = db.tables.get(2);
//
//
//        ArrayList test = new ArrayList();
//        ArrayList test1 = new ArrayList();
//        ArrayList test2 = new ArrayList();
//        ArrayList test_test = new ArrayList();
//        ArrayList test_test1 = new ArrayList();
//        ArrayList test_test_test = new ArrayList();
//        ArrayList test_test_test1 = new ArrayList();
//        ArrayList test_test_test_test = new ArrayList();
//        test.add("test");
//        test.add("m_id");
//        test.add(0);
//        test.add("test1");
//        test.add("m_id");
//        test.add(false);
//        test1.add("test");
//        test1.add("m_id");
//        test1.add(0);
//        test1.add(2);
//        test1.add(null);
//        test1.add(true);
//        test_test.add(test);
//        test_test.add(test1);
//
//        test_test_test.add(test_test);
//        test_test_test_test.add(test_test_test);
//        test2.add("test");
//        test2.add("m_id");
//        test2.add(0);
//        test2.add("test2");
//        test2.add("m_id");
//        test2.add(false);
//        test_test1.add(test2);
//        test_test_test1.add(test_test1);
//        test_test_test_test.add(test_test_test1);
//        ArrayList<Table> tmp = new ArrayList<Table>();
//        tmp.add(table);
//        tmp.add(table1);
//        tmp.add(table2);
//        Set<ArrayList> res = db.joinTables(tmp, test_test_test_test);
//        System.out.print(res);
//        System.out.println();
//        for (ArrayList tmpres : res) {
//            System.out.print(table.file.readData((int)tmpres.get(0)));
//            System.out.print(table1.file.readData((int)tmpres.get(1)));
//            System.out.print(table2.file.readData((int)tmpres.get(2)));
//            System.out.println();
//        }

        for(int i = 0; i<table_num; i++){
            db.dropTable("test_"+Integer.toString(i));
        }
        db.dropDB("test");
        db.dropDB("test_for_use");
    }
}