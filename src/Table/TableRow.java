package Table;

import java.util.ArrayList;

public class TableRow<TKey extends Comparable<TKey> > {
    private int[] int_columns;
    private float[] float_columns;
    private double[] double_columns;
    private long[] long_columns;
    private String[] string_columns;

    TableRow(){
        ArrayList a = new ArrayList();
        a.add(5);
        a.add("ss");
    }
}
