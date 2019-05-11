package Table;

import BPlusTree.BPlusTree;
import BPlusTree.BPlusTreeNode;
import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    ArrayList<ArrayList> SelectRows(ArrayList<ArrayList<ArrayList>> conditions, ArrayList column_names)
            throws BPlusTreeException, IOException{
        Set<Integer> result = new HashSet<>();
        int offset;
        for (ArrayList<ArrayList> arr_or: conditions) {
            int index = IsKeyMatch(arr_or);
            if(index >= 0){
                ArrayList key = new ArrayList();
                for(ArrayList arr_and: arr_or)
                    key.add(arr_and.get(2));
                offset = this.index_forest.get(index).search(key);

                BPlusTreeNode node = this.file.readNode(offset, index);

                addResult(node, arr_or, result, index);
            }else{
                for(ArrayList arr_and: arr_or){
                    String attr1 = (String)arr_and.get(0);
                    int relation = (Integer)arr_and.get(1);
                    String attr2 = (String)arr_and.get(2);
                    boolean isPrimitive = (Boolean)arr_and.get(3);

                    
                }
            }

        }
    }

    void addResult(BPlusTreeNode node, ArrayList<ArrayList> arr, Set<Integer> result, int index)
            throws BPlusTreeException, IOException{
        for(int i = 0; i < node.keys.size(); ++i) {
            if (node.compare(arr, node.keys.get(i)) == 0){
                result.add(node.pointers.get(i));
                if(i == 0 && node.leftSibling != -1) {
                    BPlusTreeNode n = file.readNode(node.leftSibling, index);
                    addResult(n, arr, result, index);
                } else if(i == BPlusTree.ORDER && node.rightSibling != -1){
                    BPlusTreeNode n = file.readNode(node.leftSibling, index);
                    addResult(n, arr, result, index);
                }
            }

        }

    }

    private int IsKeyMatch(ArrayList<ArrayList> arr){
        for(int i = 0; i < this.index_key.size(); ++i){

            ArrayList<Integer> keys = this.index_key.get(i);

            if(keys.size() != arr.size())
                continue;

            ArrayList<Integer> tmp = new ArrayList<>();
            for(Integer index: keys)
                tmp.add(index);

            for(ArrayList arr_and: arr){
                if(!(Boolean)arr_and.get(3) || (Integer)arr_and.get(1) != 0)
                    return -1;
                int index = this.column_name.indexOf((String)arr_and.get(0));
                tmp.remove(index);
            }

            if(tmp.size() == 0)
                return i;
        }

        return -1;

    }

}
