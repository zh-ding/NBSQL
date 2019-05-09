package Table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import Utils.FileManager;
import BPlusTree.BPlusTree;

public class Table {
    private ArrayList<String> column_name;
    /*
    -1: int
    -2: long
    -3: float
    -4: double
    n (n > 0): String, max_length = 0
     */
    private ArrayList<BPlusTree> index_forest;
    private ArrayList<Integer> column_type;
    private int primary_key_index = 0;
    private int col_num = 0;
    private int index_num = 1;
    private FileManager file;


    Table(String[] names, int[] types, String primary_key, String table_name)
        throws IOException {

        this.file = new FileManager(table_name);

        this.col_num = names.length+1;
        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();
        this.column_name.add("id");
        this.column_type.add(0);

        for(int i = 0; i < this.col_num; ++i) {
            this.column_name.add(names[i]);
            this.column_type.add(types[i]);
            if (names[i] == primary_key)
                primary_key_index = i + 1;
        }

        int pos = this.file.writeTableHeader(this.col_num, this.index_num, column_name, column_type);
        BPlusTree index_tree = new BPlusTree(file, pos, true);
        index_forest.add(index_tree);

    }

    Table(String table_name)
        throws IOException{

        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();
        this.col_num = this.file.readTableHeader(this.column_name, this.column_type);
        ArrayList<Integer> tmp = this.file.readIndexForest();
        this.index_num = tmp.get(0);
        for(int i = 0; i<this.index_num; i++){
            BPlusTree tmp_tree = new BPlusTree(file, tmp.get(i+1), false);
            index_forest.add(tmp_tree);
        }
    }

    void InsertRow(){

    }

    void DeleteRow(){

    }

    void SelectRows(){

    }
}
