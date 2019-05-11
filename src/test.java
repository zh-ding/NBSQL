import java.io.IOException;
import java.util.ArrayList;

import Exceptions.BPlusTreeException;
import Table.Table;


public class test {
    public static void main(String[] args) throws BPlusTreeException,IOException {
        int[] a = new int[2];
        a[0] = -1;
        a[1] = -1;
        String[] s = new String[2];
        s[0] = "m_id";
        s[1] = "name";
        String[] p = new String[1];
        p[0] = "m_id";
        Table table = new Table(s, a, p, "test");
        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(1);
        arr.add(2);
        table.InsertRow(arr);
    }
}