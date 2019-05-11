package Table;

import java.io.IOException;
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
    private ArrayList<ArrayList<Integer>> index_key;
    private ArrayList<BPlusTree> index_forest;
    private ArrayList<Integer> column_type;
    private int primary_key_index = 0;
    private int col_num = 0;
    private int index_num = 1;
    private FileManager file;


    Table(String[] names, int[] types, String[] primary_key, String table_name)
        throws IOException {

        this.file = new FileManager(table_name);

        this.col_num = names.length+1;
        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();
        this.column_name.add("id");
        this.column_type.add(0);
        ArrayList<Integer> tmp = new ArrayList<Integer>();
        for(int i = 0; i < this.col_num; ++i) {
            this.column_name.add(names[i]);
            this.column_type.add(types[i]);
            for(int j = 0; j<primary_key.length; j++){
                if(names[i] == primary_key[j]){
                    tmp.add(i);
                }
            }
        }
        if(tmp.size() == 0){
            tmp.add(0);
        }
        index_key.add(tmp);
        int pos = this.file.writeTableHeader(this.col_num, this.index_num, tmp.size() ,column_name, column_type, tmp);
        BPlusTree index_tree = new BPlusTree(file, pos, true, 1);
        index_forest.add(index_tree);

    }

    Table(String table_name)
        throws IOException{

        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();
        this.col_num = this.file.readTableHeader(this.column_name, this.column_type);
        ArrayList<Integer> tmp = this.file.readIndexForest();
        this.index_num = tmp.get(0);
        int i = 0;
        while(i<this.index_num) {
            i++;
            BPlusTree tmp_tree = new BPlusTree(file, tmp.get(i), false, i);
            index_forest.add(tmp_tree);
            i++;
            ArrayList<Integer> m_tmp = new ArrayList<Integer>();
            int j = 0;
            for( ; j<tmp.get(i); j++){
                m_tmp.add(i+j+1);
            }
            i = i+j;
            index_key.add(m_tmp);
        }
    }

    void InsertRow(){

    }

    void DeleteRow(){

    }

    void SelectRows(){

    }
}
