package Table;

import BPlusTree.BPlusTree;
import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;

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
    private int auto_id = 0;
    private int col_num = 0;
    private int index_num = 1;
    private FileManager file;


    public Table(String[] names, int[] types, String[] primary_key, String table_name)
        throws IOException {

        this.file = new FileManager(table_name);

        this.col_num = names.length+1;
        this.index_key = new ArrayList<>();
        this.index_forest = new ArrayList<>();
        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();
        this.column_name.add("id");
        this.column_type.add(-1);
        ArrayList<Integer> tmp = new ArrayList<>();
        for(int i = 0; i < names.length; ++i) {
            this.column_name.add(names[i]);
            this.column_type.add(types[i]);
            for(int j = 0; j<primary_key.length; j++){
                if(names[i] == primary_key[j]){
                    tmp.add(i+1);
                }
            }
        }
        if(tmp.size() == 0){
            tmp.add(0);
        }
        this.index_key.add(tmp);
        int pos = this.file.writeTableHeader(this.col_num, this.index_num, tmp.size() ,column_name, column_type, tmp, auto_id);
        BPlusTree index_tree = new BPlusTree(file, pos, true, 0);
        index_forest.add(index_tree);
    }

    public Table(String table_name)
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
                m_tmp.add(tmp.get(i+j+1));
            }
            i = i+j;
            index_key.add(m_tmp);
        }
    }

    public void InsertRow(ArrayList row)
            throws IOException, BPlusTreeException {
        auto_id ++;
        row.add(0, auto_id);
        int offset = file.writeValue(row);
        for(int i = 0; i < this.index_forest.size(); ++i){
            ArrayList key = new ArrayList();
            for(int j = 0; j < this.index_key.get(i).size(); ++j)
                key.add(row.get(this.index_key.get(i).get(j)));
            index_forest.get(i).insert(key, offset);
        }
    }

    void DeleteRow(){

    }

    /*
    0: =
    1: <
    2: >
    3: <=
    4: >=
    5: <>
    [
      [
        ['column1', 0, 'name', True],
        ['column2', 1, column3, False],
      ],
      [
      ],
    ]
    */
//    void SelectRows(ArrayList<ArrayList<ArrayList>> conditions, ArrayList column_names){
//        for (ArrayList<ArrayList> arr_or: conditions) {
//            if(IsKeyMatch(arr_or)){
//
//            }
//
//            for(ArrayList arr_and: arr_or){
//                String attr1 = (String)arr_and.get(0);
//                int relation = (Integer)arr_and.get(1);
//                String attr2 = (String)arr_and.get(2);
//                boolean isPrimitive = (Boolean)arr_and.get(3);
//
//            }
//        }
//    }
//
//    private int IsKeyMatch(ArrayList<ArrayList> arr){
//
//    }
}
