import java.io.IOException;
import java.util.ArrayList;

import Exceptions.BPlusTreeException;
import Table.Table;


public class test {
    public static void main(String[] args) throws BPlusTreeException,IOException {
        int[] a = new int[2];
        a[0] = -1;
        a[1] = 8;
        String[] s = new String[2];
        s[0] = "m_id";
        s[1] = "name";
        String[] p = new String[1];
        p[0] = "m_id";
        Table table = new Table(s, a, p, "test");
        ArrayList arr = new ArrayList<>();
        arr.add(1);
        arr.add("testa");
        table.InsertRow(arr);
        ArrayList arr1 = new ArrayList<>();
        arr1.add(2);
        arr1.add("twew1");
        table.InsertRow(arr1);
        ArrayList arr2 = new ArrayList<>();
        arr2.add(3);
        arr2.add("twew2");
        table.InsertRow(arr2);
        ArrayList arr3 = new ArrayList<>();
        arr3.add(4);
        arr3.add("twew3");
        table.InsertRow(arr3);
        ArrayList arr4 = new ArrayList<>();
        arr4.add(5);
        arr4.add("twew4");
        table.InsertRow(arr4);
        ArrayList arr5 = new ArrayList<>();
        arr5.add(6);
        arr5.add("twew5");
        table.InsertRow(arr5);
    }
}