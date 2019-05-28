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
        int num = 10; // data
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
            s[1] = "name";
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
            //ArrayList<ArrayList> tmp =  table.SelectRows(null, null);
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
            //ArrayList<ArrayList> tmp =  table.SelectRows(null, null);
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
        ArrayList test = new ArrayList();
        test.add("m_id");
        test.add(4);
        test.add(num/2);
        test.add(true);
        ArrayList<ArrayList> test_test = new ArrayList();
        test_test.add(test);
        ArrayList<ArrayList<ArrayList>> test_test_test = new ArrayList();
        test_test_test.add(test_test);
        for(int i = 0; i<table_num; ++i){
            Table table = db.tables.get(i);
            table.DeleteRows(test_test_test);
            Generator<ArrayList> tmp =  table.SelectRows(null, null);
            //ArrayList<ArrayList> tmp =  table.SelectRows(null, null);
            int number = 0;
            for(ArrayList tmpres: tmp){
                number++;
            }
            if(number == num/2){
                System.out.println("table " + Integer.toString(i) + " delete success");
            }
            else {
                System.out.println("table " + Integer.toString(i) + " delete fail");
            }
        }

        System.out.println("----------------------start testing update-----------------");
        ArrayList col_name = new ArrayList();
        col_name.add("name");
        ArrayList new_row = new ArrayList();
        new_row.add("test");
        ArrayList test1 = new ArrayList();
        test1.add("m_id");
        test1.add(1);
        test1.add(num/2);
        test1.add(true);
        ArrayList<ArrayList> test_test1 = new ArrayList();
        test_test1.add(test1);
        ArrayList<ArrayList<ArrayList>> test_test_test1 = new ArrayList();
        test_test_test1.add(test_test1);
        ArrayList test2 = new ArrayList();
        test2.add("name");
        test2.add(0);
        test2.add("test                ");
        test2.add(true);
        ArrayList<ArrayList> test_test2 = new ArrayList();
        test_test2.add(test2);
        ArrayList<ArrayList<ArrayList>> test_test_test2 = new ArrayList();
        test_test_test2.add(test_test2);
        for(int i = 0; i<table_num; ++i){
            Table table = db.tables.get(i);
            table.UpdateRow(test_test_test1, col_name, new_row);
            Generator<ArrayList> tmp =  table.SelectRows(test_test_test2, null);
            int number = 0;
            for(ArrayList tmpres: tmp){
                number++;
            }
            if(number == num/2){
                System.out.println("table " + Integer.toString(i) + " update success");
            }
            else {
                System.out.println("table " + Integer.toString(i) + " update fail");
            }
        }

        for(int i = 0; i<table_num; i++){
            db.dropTable("test_"+Integer.toString(i));
        }
        db.dropDB("test");
        db.dropDB("test_for_use");

        System.out.println("----------------------start testing join-----------------");
        db.newDB("test");
        db.useDB("test");
        for(int i =0; i<table_num; ++i){
            s[1] = Integer.toString(i)+"_name";
            db.createTable(s, a, p, "test_"+Integer.toString(i), isNotNull);
        }

        for(int i = 0; i<table_num; ++i){
            Table table = db.tables.get(i);
            for(int j = 0; j<num; j++){
                ArrayList arr = new ArrayList<>();
                arr.add(j);
                arr.add(Integer.toString(j));
                table.InsertRow(arr);
            }
        }

        ArrayList conditions = new ArrayList();
        for(int i = 0; i<table_num-1; i++){
            conditions.add(null);
        }

        Set<ArrayList> res = db.joinTables(db.tables, conditions);
        int number = 0;
        for(ArrayList tmpres: res){
            number++;
        }
        if(number == num){
            System.out.println(Integer.toString(table_num)+" tables natural join success");
        }
        else{
            System.out.println(Integer.toString(table_num)+" tables natural join fail");
        }
        System.out.println("----------------------start testing select from join-----------------");
        ArrayList wheretest = new ArrayList();
        wheretest.add("test_1");
        wheretest.add("m_id");
        wheretest.add(4);
        wheretest.add(num/2);
        wheretest.add(null);
        wheretest.add(true);
        ArrayList wheretest_test = new ArrayList();
        wheretest_test.add(wheretest);
        ArrayList wheretest_test_test = new ArrayList();
        wheretest_test_test.add(wheretest_test);
        ArrayList<ArrayList> finalres = new ArrayList<>();
        finalres = db.selectFromTables(db.tables, conditions, wheretest_test_test, null);
        number = 0;
        for(ArrayList tmpres: finalres){
            System.out.println(tmpres);
            number ++;
        }
        number -= 2;
        if(number == num/2){
            System.out.println(Integer.toString(table_num)+" tables select join success");
        }
        else{
            System.out.println(Integer.toString(table_num)+" tables select join fail");
        }
        for(int i = 0; i<table_num; i++){
            db.dropTable("test_"+Integer.toString(i));
        }
        db.dropDB("test");
    }
}