import Database.Database;
import Exceptions.BPlusTreeException;
import Exceptions.DatabaseException;
import Exceptions.TableException;
import Table.Table;
import generator.Generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class test {
    public static void main(String[] args) throws BPlusTreeException,IOException, TableException, DatabaseException {


        int num = 1000; // data to be even
        int table_num = 2; // >= 2

        Database db = new Database("test");
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

        System.out.println("----------------------start testing insert-----------------");
        long starTime=System.nanoTime();
        for(int i = 0; i<table_num; ++i){
            Table table = db.tables.get(i);
            for(int j = 0; j<num; j++){
                ArrayList arr = new ArrayList<>();
                arr.add(j);
                arr.add(Integer.toString(j));
                table.InsertRow(arr);
            }
        }

        long endTime=System.nanoTime();
        System.out.println((endTime-starTime)/1000000.0+"ms");

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
        starTime=System.currentTimeMillis();
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

        //db.tables.get(0).index_forest.get(0).printBPlusTree();


        System.out.println("----------------------start testing delete-----------------");


        starTime = System.nanoTime();

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
        endTime = System.nanoTime();
        System.out.println((endTime - starTime) / 1000000.0 + "ms");

        System.out.println("----------------------start testing update-----------------");

//        ArrayList arr1 = new ArrayList<>();
//        arr1.add(0);
//        arr1.add(Integer.toString(0));
//        db.tables.get(0).InsertRow(arr1);
        starTime=System.currentTimeMillis();
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
        test2.add("test");
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
        db.dropDB("test_for_use");

        System.out.println("----------------------start testing join-----------------");
        db.newDB("testnew");
        db.useDB("testnew");
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
        ArrayList isOuterOrNot = new ArrayList();
        for(int i = 0; i<table_num-1; i++){
            isOuterOrNot.add(0);
        }
        Set<ArrayList> res = db.joinTables(db.tables, conditions, isOuterOrNot);
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
        wheretest.add("test_0");
        wheretest.add("m_id");
        wheretest.add(4);
        wheretest.add(num/2);
        wheretest.add(null);
        wheretest.add(true);
        ArrayList wheretest_test = new ArrayList();
        wheretest_test.add(wheretest);
        ArrayList wheretest_test_test = new ArrayList();
        wheretest_test_test.add(wheretest_test);

        Generator<ArrayList> finalres = db.selectFromTables(db.tables, isOuterOrNot, conditions, wheretest_test_test, null);
//        ArrayList<ArrayList> finalres = db.selectFromTables(db.tables, isOuterOrNot, conditions, wheretest_test_test, null);
        number = 0;
        for(ArrayList tmpres: finalres){
            number ++;
        }
        if(number == num/2){
            System.out.println(Integer.toString(table_num)+" tables select join success");
        }
        else{
            System.out.println(Integer.toString(table_num)+" tables select join fail");
        }

        System.out.println("----------------------start testing select from outer join-----------------");

        ArrayList jointest = new ArrayList();
        jointest.add("m_id");
        jointest.add(0);
        jointest.add(num/2+1);
        jointest.add(true);
        ArrayList join_jointest = new ArrayList();
        join_jointest.add(jointest);
        ArrayList join_join_jointest = new ArrayList();
        join_join_jointest.add(join_jointest);
        db.tables.get(table_num-1).DeleteRows(join_join_jointest);
        ArrayList<ArrayList> jointest_test = new ArrayList();
        test_test.add(jointest);
        ArrayList<ArrayList<ArrayList>> jointest_test_test = new ArrayList();
        jointest_test_test.add(jointest_test);
        Table table = db.tables.get(table_num-1);
        table.DeleteRows(jointest_test_test);
        ArrayList onconditions = new ArrayList();
        for(int i = 0; i<table_num-2; i++){
            onconditions.add(null);
        }
        ArrayList tmp1 = new ArrayList();
        tmp1.add("test_1");
        tmp1.add("m_id");
        tmp1.add(0);
        tmp1.add("test_9");
        tmp1.add("m_id");
        tmp1.add(false);
        ArrayList tmp_tmp1 = new ArrayList();
        tmp_tmp1.add(tmp1);
        ArrayList tmp_tmp_tmp1 = new ArrayList();
        tmp_tmp_tmp1.add(tmp_tmp1);
        onconditions.add(tmp_tmp_tmp1);
        ArrayList outer = new ArrayList();
        for(int i = 0; i<table_num-2; i++){
            outer.add(0);
        }
        outer.add(1);
        finalres = db.selectFromTables(db.tables, outer, onconditions, wheretest_test_test, null);
        number = 0;
        for(ArrayList tmpres: finalres){
            number ++;
        }
        if(number == num/2){
            System.out.println(Integer.toString(table_num)+" tables select outer join success");
        }
        else{
            System.out.println(Integer.toString(table_num)+" tables select outer join fail");
        }

        for(int i = 0; i<table_num; i++){
            db.dropTable("test_"+Integer.toString(i));
        }
        db.dropDB("test");
    }
}