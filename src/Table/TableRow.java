package Table;

import java.util.ArrayList;

public class TableRow<TKey extends Comparable<TKey> > {
    ArrayList data;

    TableRow(){
        ArrayList a = new ArrayList();
        a.add(5);
        a.add("ss");
    }
}
