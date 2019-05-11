import java.io.IOException;
import Table.Table;


public class test {
    public static void main(String[] args) throws IOException {
        int[] a = new int[2];
        a[0] = -1;
        a[1] = -1;
        String[] s = new String[2];
        s[0] = "m_id";
        s[1] = "name";
        String[] p = new String[1];
        p[0] = "m_id";
        Table table = new Table(s, a, p, "test");

    }
}