package Table;

import BPlusTree.BPlusTree;
import BPlusTree.BPlusTreeLeafNode;
import BPlusTree.BPlusTreeNode;
import Exceptions.BPlusTreeException;
import Exceptions.TableException;
import Server.Server;
import Utils.FileManager;
import generator.Generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    public ArrayList<BPlusTree> index_forest;
    private ArrayList<Integer> column_type;
    private int col_num = 0;
    private int index_num = 1;
    public FileManager file;
    public String table_name;
    private String database_name;
    private ArrayList<Boolean> column_isNotNull;
    private String lock_name;


    public Table(String[] names, int[] types, String[] primary_key, String table_name, String database_name, boolean[] isNotNull)
        throws IOException {

        this.database_name = database_name;
        this.table_name = table_name;
        /*
        lock
         */
        this.lock_name = this.database_name+"@"+this.table_name;
        if(!Server.G_lock.containsKey(lock_name)){
            Lock my_lock = new ReentrantLock();
            Server.G_lock.put(lock_name, my_lock);
        }

        table_name = "./dat/"+database_name+"/"+table_name;
        this.file = new FileManager(table_name);
        if(!Server.auto_id.containsKey(table_name+".dat")){
            Server.auto_id.put(table_name+".dat", 0);
        }

        this.col_num = names.length+1;
        this.index_key = new ArrayList<>();
        this.index_forest = new ArrayList<>();
        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();
        this.column_isNotNull = new ArrayList<>();
        this.column_name.add("auto_id");
        this.column_type.add(-1);
        this.column_isNotNull.add(true);

        ArrayList<Integer> tmp = new ArrayList<>();
        for(int i = 0; i < names.length; ++i) {
            this.column_name.add(names[i]);
            this.column_type.add(types[i]);
            this.column_isNotNull.add(isNotNull[i]);

            for(int j = 0; j<primary_key.length; j++){
                if(names[i].equals(primary_key[j])){
                    tmp.add(i+1);
                }
            }
        }
        if(tmp.size() == 0){
            tmp.add(0);
        }
        this.index_key.add(tmp);
        int pos = this.file.writeTableHeader(this.col_num, this.index_num, tmp.size() ,column_name, column_type, tmp, Server.auto_id.get(table_name+".dat"), this.column_isNotNull);
        BPlusTree index_tree = new BPlusTree(file, pos, true, 0);
        index_forest.add(index_tree);
    }

    public Table(String table_name, String database_name)
        throws IOException{
        this.database_name = database_name;
        this.table_name = table_name;
        /*
        lock
         */
        this.lock_name = this.database_name+"@"+this.table_name;
        table_name = "./dat/"+database_name+"/"+table_name;
        this.file = new FileManager(table_name);
        this.index_key = new ArrayList<>();
        this.index_forest = new ArrayList<>();
        this.column_name = new ArrayList<String>();
        this.column_type = new ArrayList<Integer>();
        this.column_isNotNull = new ArrayList<>();
        ArrayList<Integer> m_num= this.file.readTableHeader(this.column_name, this.column_type, this.column_isNotNull);
        if(!Server.auto_id.containsKey(table_name+".dat")){
            Server.auto_id.put(table_name+".dat", m_num.get(0));
        }
        this.col_num = m_num.get(1);
        ArrayList<Integer> tmp = this.file.readIndexForest();
        this.index_num = tmp.get(0);
        int i = 0;
        int tree_id = 0;
        while(i<this.index_num) {
            i++;
            BPlusTree tmp_tree = new BPlusTree(file, tmp.get(i), false, tree_id);
            tree_id ++;
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

    public void createIndex(ArrayList<String> arr)throws IOException, BPlusTreeException{
        BPlusTree tree = new BPlusTree(file, 0, true, this.index_num);
        ArrayList<Integer> tmp = new ArrayList<>();
        for(String key: arr){
            tmp.add(this.column_name.indexOf(key));
        }
        this.index_key.add(tmp);
        this.file.addIndex(tree.root.location, tmp);
        BPlusTreeLeafNode node = this.index_forest.get(0).getMostLeftLeafNode();
        while(true){
            for(Integer offset: node.pointers){
                ArrayList data = file.readData(offset);
                ArrayList key = new ArrayList();
                for(Integer index: tmp){
                    key.add(data.get(index));
                }
                tree.insert(key, offset);
            }
            if(node.rightSibling == -1)
                break;
            node = (BPlusTreeLeafNode) file.readNode(node.rightSibling, 0);
        }

        this.index_forest.add(tree);

        ++this.index_num;
    }

    public ArrayList<String> getColumnName(){
        return this.column_name;
    }

    public ArrayList<Integer> getColumnType(){ return this.column_type; }

    public void InsertRow(ArrayList row)
            throws IOException, BPlusTreeException, TableException {
        Server.G_lock.get(this.lock_name).lock();
        try {
            int auto_id_tmp = Server.auto_id.get("./dat/"+database_name+"/"+table_name+".dat");
            Server.auto_id.put("./dat/"+database_name+"/"+table_name+".dat", auto_id_tmp+1);
            if(row.size() != this.column_name.size())
                row.add(0, Server.auto_id.get("./dat/"+database_name+"/"+table_name+".dat"));
            for(int i = 0; i<row.size(); i++){
                if(row.get(i) == null && this.column_isNotNull.get(i)){
                    throw new TableException("value can't be null");
                }
                if(column_type.get(i) >= 0){
                    if(column_type.get(i) < row.get(i).toString().length()){
                        throw new TableException("string is too long");
                    }
                }
            }

            int offset = file.writeValue(row);
            for(int i = 0; i < this.index_forest.size(); ++i){
                ArrayList key = new ArrayList();
                for(int j = 0; j < this.index_key.get(i).size(); ++j)
                    key.add(row.get(this.index_key.get(i).get(j)));
                index_forest.get(i).insert(key, offset);
            }
        }finally {
            Server.G_lock.get(this.lock_name).unlock();
        }
    }

    public void DeleteRows(ArrayList<ArrayList<ArrayList>> conditions) throws IOException, BPlusTreeException{
        Server.G_lock.get(this.lock_name).lock();
        try{
            for(ArrayList row: SelectRows(conditions, null)){
                this.DeleteRow(row);
            }
        }finally {
            Server.G_lock.get(this.lock_name).unlock();
        }
    }

    public void DeleteRow(ArrayList row)throws IOException, BPlusTreeException{

        for(int i = 0; i<this.index_forest.size(); ++i){
            ArrayList key = new ArrayList();
            for(int j = 0; j<this.index_key.get(i).size(); ++j)
                key.add(row.get(this.index_key.get(i).get(j)));
            /*
            key and auto_id
             */
            index_forest.get(i).delete(key, (int)row.get(0));
        }
//        this.file.resetNodeCache();
    }

    public void UpdateRow(ArrayList<ArrayList<ArrayList>> conditions, ArrayList column_name, ArrayList newRow) throws IOException, BPlusTreeException, TableException{

        Server.G_lock.get(this.lock_name).lock();
        try {
            ArrayList<Integer> index = new ArrayList<>();
            for(int k = 0; k<column_name.size(); ++k){
                for(int j = 0; j<this.column_name.size(); ++j){
                    if(this.column_name.get(j).compareTo(column_name.get(k).toString()) == 0){
                        index.add(j);
                    }
                }
            }
            for(ArrayList row: SelectRows(conditions, this.column_name)){
                this.DeleteRow(row);
                for(int k = 0; k<column_name.size(); ++k){
                    row.set(index.get(k), newRow.get(k));
                }
                this.InsertRow(row);
            }
        }finally {
            Server.G_lock.get(this.lock_name).unlock();
        }
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

    public Generator<ArrayList> SelectRows(ArrayList<ArrayList<ArrayList>> conditions, ArrayList<String> column_names)
            throws BPlusTreeException, IOException{
        Table table = this;

        if(conditions == null){
            Generator<ArrayList> allGenerator = new Generator<ArrayList>() {
                @Override
                protected void run() throws InterruptedException {
                    Set<Integer> result = new HashSet<>();
                    try {
                        addAllResult(table.index_forest.get(0).getMostLeftLeafNode(), result);
                    }catch (IOException e){
                        System.out.println(e);
                        throw new InterruptedException();
                    }

                    if(column_names == null){
                        for(int off: result){
                            try {
                                ArrayList row = table.file.readData(off);
                                yield(row);
                            }catch(IOException e){
                                System.out.println(e);
                                throw new InterruptedException();
                            }
                        }
                    }else{
                        ArrayList<Integer> idx = new ArrayList<>();
                        for(String col: column_names){
                            idx.add(table.column_name.indexOf(col));
                        }
                        for(int off: result){
                            try {
                                ArrayList row = table.file.readData(off);
                                ArrayList newRow = new ArrayList();
                                for(int i: idx)
                                    newRow.add(row.get(i));
                                yield(newRow);
                            }catch(IOException e){
                                System.out.println(e);
                                throw new InterruptedException();
                            }
                        }
                    }
                }
            };

            return allGenerator;
        }

        Generator<ArrayList> simpleGenerator = new Generator<ArrayList>() {
            public void run() throws InterruptedException {
                Set<Integer> result = new HashSet<>();
                int offset;

                for (ArrayList<ArrayList> arr_or: conditions) {
                    int index = IsKeyMatch(arr_or);
                    if(index >= 0){
                        ArrayList key = new ArrayList();
                        for(ArrayList arr_and: arr_or)
                            table.addKey(arr_and, key);

                        try {
                            offset = table.index_forest.get(index).search(key);

                            BPlusTreeNode node = table.file.readNode(offset, index);

                            addResult(node, key, result, index);
                        }catch (BPlusTreeException e){
                            System.out.println(e);
                        }catch (IOException e){
                            System.out.println(e);
                        }
                    }else{
                        Set<Integer> arr1 = new HashSet<>();
                        Set<Integer> arr2 = new HashSet<>();
                        Boolean isFirst = true;

                        for(ArrayList arr_and: arr_or){
                            String attr1 = (String)arr_and.get(0);
                            int relation = (Integer)arr_and.get(1);
                            boolean isPrimitive = (Boolean)arr_and.get(3);

                            if(isPrimitive && relation != 5){
                                int col = column_name.indexOf(attr1);
                                for(index = 0; index < table.index_key.size(); ++index)
                                    if(attr1.compareTo(table.column_name.get(table.index_key.get(index).get(0))) == 0)
                                        break;

                                if(index < table.index_key.size()){

                                    ArrayList key = new ArrayList();
                                    table.addKey(arr_and, key);
                                    table.addRandomKey(index, key);

                                    try {

                                        offset = table.index_forest.get(index).searchOne(key);
                                        BPlusTreeNode node = table.file.readNode(offset, index);

                                        int kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                        if(kk == 1) {


                                            while(true) {
                                                node = table.file.readNode(node.leftSibling, node.id);
                                                kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                                if (kk == 0 || kk == 10)
                                                    break;
                                            }
                                        }else if(kk == 10){


                                            while (true) {
                                                node = table.file.readNode(node.rightSibling, node.id);
                                                kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                                if (kk == 0 || kk == 1)
                                                    break;
                                            }
                                        }else{
                                            BPlusTreeNode tmp = node;



                                            while(true) {
                                                node = table.file.readNode(node.leftSibling, node.id);
                                                kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                                if (kk == 0 || kk == 10)
                                                    break;
                                            }

                                            node = tmp;

                                            while (true) {
                                                node = table.file.readNode(node.rightSibling, node.id);
                                                kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                                if (kk == 0 || kk == 1)
                                                    break;
                                            }
                                        }


                                    }catch (BPlusTreeException e){
                                        System.out.println(e);
                                        throw new InterruptedException();
                                    }catch (IOException e){
                                        System.out.println(e);
                                        throw new InterruptedException();
                                    }
                                }else {
                                    try {
                                        int next = addResultWoIndex(table.index_forest.get(0).getMostLeftLeafNode(), arr_and, arr1, arr2, isFirst, relation);
                                        while(next != -1){
                                            next = addResultWoIndex((BPlusTreeLeafNode) table.file.readNode(next, 0), arr_and, arr1, arr2, isFirst, relation);
                                        }
                                    }catch (BPlusTreeException e){
                                        System.out.println(e);
                                        throw new InterruptedException();
                                    }catch (IOException e){
                                        System.out.println(e);
                                        throw new InterruptedException();
                                    }
                                }
                            }else if(isPrimitive && relation == 5){
                                try{
                                    int next = addResultWoIndex(table.index_forest.get(0).getMostLeftLeafNode(), arr_and, arr1, arr2, isFirst, relation);
                                    while(next != -1){
                                        next = addResultWoIndex((BPlusTreeLeafNode) table.file.readNode(next, 0), arr_and, arr1, arr2, isFirst, relation);
                                    }
                                }catch (BPlusTreeException e){
                                    System.out.println(e);
                                    throw new InterruptedException();
                                }catch (IOException e){
                                    System.out.println(e);
                                    throw new InterruptedException();
                                }
                            }else{
                                try {
                                    addResultNotPrimitive(table.index_forest.get(0).getMostLeftLeafNode(), arr_and, arr1, arr2, isFirst, relation);
                                }catch (BPlusTreeException e){
                                    System.out.println(e);
                                    throw new InterruptedException();
                                }catch (IOException e){
                                    System.out.println(e);
                                    throw new InterruptedException();
                                }
                            }
                            arr1 = arr2;
                            arr2 = new HashSet<>();
                            isFirst = false;
                        }
                        result.addAll(arr1);
                    }
                }
                if(column_names == null){
                    for(int off: result){
                        try {
                            ArrayList row = table.file.readData(off);
                            yield(row);
                        }catch(IOException e){
                            System.out.println(e);
                            throw new InterruptedException();
                        }
                    }
                }else{
                    ArrayList<Integer> idx = new ArrayList<>();
                    for(String col: column_names){
                        idx.add(table.column_name.indexOf(col));
                    }
                    for(int off: result){
                        try {
                            ArrayList row = table.file.readData(off);
                            ArrayList newRow = new ArrayList();
                            for(int i: idx)
                                newRow.add(row.get(i));
                            yield(newRow);
                        }catch(IOException e){
                            System.out.println(e);
                            throw new InterruptedException();
                        }
                    }
                }

            }
        };

        return simpleGenerator;
    }


    /*
    public ArrayList<ArrayList> SelectRows(ArrayList<ArrayList<ArrayList>> conditions, ArrayList<String> column_names)
            throws BPlusTreeException, IOException{
        Table table = this;
        ArrayList<ArrayList> re = new ArrayList<>();

        if(conditions == null){

                    Set<Integer> result = new HashSet<>();
                    try {
                        addAllResult(table.index_forest.get(0).getMostLeftLeafNode(), result);
                    }catch (IOException e){
                        System.out.println(e);
                    }

                    if(column_names == null){
                        for(int off: result){
                            try {
                                ArrayList row = table.file.readData(off);
                                re.add(row);
                            }catch(IOException e){
                                System.out.println(e);
                            }
                        }
                    }else{
                        ArrayList<Integer> idx = new ArrayList<>();
                        for(String col: column_names){
                            idx.add(table.column_name.indexOf(col));
                        }
                        for(int off: result){
                            try {
                                ArrayList row = table.file.readData(off);
                                ArrayList newRow = new ArrayList();
                                for(int i: idx)
                                    newRow.add(row.get(i));
                                re.add(newRow);
                            }catch(IOException e){
                                System.out.println(e);
                            }
                        }
                    }


            return re;
        }




        Set<Integer> result = new HashSet<>();
        int offset;

        for (ArrayList<ArrayList> arr_or: conditions) {
            int index = IsKeyMatch(arr_or);
            if(index >= 0){
                ArrayList key = new ArrayList();
                for(ArrayList arr_and: arr_or)
                    table.addKey(arr_and, key);

                try {
                    offset = table.index_forest.get(index).search(key);

                    BPlusTreeNode node = table.file.readNode(offset, index);

                    addResult(node, key, result, index);
                }catch (BPlusTreeException e){
                    System.out.println(e);
                }catch (IOException e){
                    System.out.println(e);
                }
            }else{
                Set<Integer> arr1 = new HashSet<>();
                Set<Integer> arr2 = new HashSet<>();
                Boolean isFirst = true;

                for(ArrayList arr_and: arr_or){
                    String attr1 = (String)arr_and.get(0);
                    int relation = (Integer)arr_and.get(1);
                    boolean isPrimitive = (Boolean)arr_and.get(3);

                    if(isPrimitive && relation != 5){
                        int col = column_name.indexOf(attr1);
                        for(index = 0; index < table.index_key.size(); ++index)
                            if(attr1.compareTo(table.column_name.get(table.index_key.get(index).get(0))) == 0)
                                break;

                        if(index < table.index_key.size()){

                            ArrayList key = new ArrayList();
                            table.addKey(arr_and, key);
                            table.addRandomKey(index, key);

                            try {

                                offset = table.index_forest.get(index).searchOne(key);
                                BPlusTreeNode node = table.file.readNode(offset, index);

                                int kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                if(kk == 1) {


                                    while(true) {
                                        node = table.file.readNode(node.leftSibling, node.id);
                                        kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                        if (kk == 0 || kk == 10)
                                            break;
                                    }
                                }else if(kk == 10){


                                    while (true) {
                                        node = table.file.readNode(node.rightSibling, node.id);
                                        kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                        if (kk == 0 || kk == 1)
                                            break;
                                    }
                                }else{
                                    BPlusTreeNode tmp = node;



                                    while(true) {
                                        node = table.file.readNode(node.leftSibling, node.id);
                                        kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                        if (kk == 0 || kk == 10)
                                            break;
                                    }

                                    node = tmp;

                                    while (true) {
                                        node = table.file.readNode(node.rightSibling, node.id);
                                        kk = addResult(node, arr_and, arr1, arr2, isFirst, relation, index);
                                        if (kk == 0 || kk == 1)
                                            break;
                                    }
                                }


                            }catch (BPlusTreeException e){
                                System.out.println(e);
                                //throw new InterruptedException();
                            }catch (IOException e){
                                System.out.println(e);
                                //throw new InterruptedException();
                            }
                        }else {
                            try {
                                int next = addResultWoIndex(table.index_forest.get(0).getMostLeftLeafNode(), arr_and, arr1, arr2, isFirst, relation);
                                while(next != -1){
                                    next = addResultWoIndex((BPlusTreeLeafNode) table.file.readNode(next, 0), arr_and, arr1, arr2, isFirst, relation);
                                }
                            }catch (BPlusTreeException e){
                                System.out.println(e);
                                //throw new InterruptedException();
                            }catch (IOException e){
                                System.out.println(e);
                                //throw new InterruptedException();
                            }
                        }
                    }else if(isPrimitive && relation == 5){
                        try{
                            int next = addResultWoIndex(table.index_forest.get(0).getMostLeftLeafNode(), arr_and, arr1, arr2, isFirst, relation);
                            while(next != -1){
                                next = addResultWoIndex((BPlusTreeLeafNode) table.file.readNode(next, 0), arr_and, arr1, arr2, isFirst, relation);
                            }
                        }catch (BPlusTreeException e){
                            System.out.println(e);
                            //throw new InterruptedException();
                        }catch (IOException e){
                            System.out.println(e);
                            //throw new InterruptedException();
                        }
                    }else{
                        try {
                            addResultNotPrimitive(table.index_forest.get(0).getMostLeftLeafNode(), arr_and, arr1, arr2, isFirst, relation);
                        }catch (BPlusTreeException e){
                            System.out.println(e);
                            //throw new InterruptedException();
                        }catch (IOException e){
                            System.out.println(e);
                            //throw new InterruptedException();
                        }
                    }
                    arr1 = arr2;
                    arr2 = new HashSet<>();
                    isFirst = false;
                }
                result.addAll(arr1);
            }
        }
        if(column_names == null){
            for(int off: result){
                try {
                    ArrayList row = table.file.readData(off);
                    re.add(row);
                }catch(IOException e){
                    System.out.println(e);
                }
            }
        }else{
            ArrayList<Integer> idx = new ArrayList<>();
            for(String col: column_names){
                idx.add(table.column_name.indexOf(col));
            }
            for(int off: result){
                try {
                    ArrayList row = table.file.readData(off);
                    ArrayList newRow = new ArrayList();
                    for(int i: idx)
                        newRow.add(row.get(i));
                    re.add(newRow);
                }catch(IOException e){
                    System.out.println(e);
                }
            }
        }


        return re;
    }*/


    void addResult(BPlusTreeNode node, ArrayList<ArrayList> arr, Set<Integer> result, int index)
            throws BPlusTreeException, IOException{
        for(int i = 0; i < node.keys.size(); ++i) {
            int cmp = node.compare(arr, node.keys.get(i));
            if(cmp == 2)
                continue;
            if (cmp == 0){
                result.add(node.pointers.get(i));
                if(i == 0 && node.leftSibling != -1) {
                    BPlusTreeNode n = file.readNode(node.leftSibling, index);
                    addResult(n, arr, result, index);
                } else if(i == node.keys.size() - 1 && node.rightSibling != -1){
                    BPlusTreeNode n = file.readNode(node.rightSibling, index);
                    addResult(n, arr, result, index);
                }
            }

        }

    }

    int addResult(BPlusTreeNode node, ArrayList arr, Set<Integer> arr1, Set<Integer> arr2,
                   boolean isFirst, int relation, int index)
            throws BPlusTreeException, IOException{

        if(node == null)
            return 0;

        boolean left = false, right = false;

        for(int i = 0; i < node.keys.size(); ++i) {
            ArrayList key1 = new ArrayList();
            this.addKey(arr, key1);
            for(int j = 1; j < node.keys.get(i).size(); ++j)
                key1.add(node.keys.get(i).get(j));

            int cmp = node.compareOne(key1, node.keys.get(i));
            if(cmp == 2)
                continue;

            if(cmp == 0){
                if(relation == 0 || relation == 3 || relation == 4){
                    if(isFirst || arr1.contains(node.pointers.get(i)))
                        arr2.add(node.pointers.get(i));
                    if(i == 0 && node.leftSibling != -1)
                        left = true;
                    else if(i == node.keys.size() - 1 && node.rightSibling != -1)
                        right = true;
                }else if(relation == 1 && i == 0 && node.leftSibling != -1){
                    left = true;
                }else if(relation == 2 && i == node.keys.size() - 1 && node.rightSibling != -1){
                    right = true;
                }
            }else if(cmp < 0){
                if(relation == 0 && i == 0 && node.leftSibling != -1)
                    left = true;
                else if(relation == 1 || relation == 3){
                    if(i == 0 && node.leftSibling != -1)
                        left = true;
                }else if(relation == 2 || relation == 4){
                    if(isFirst || arr1.contains(node.pointers.get(i)))
                        arr2.add(node.pointers.get(i));
                    if(i == 0 && node.leftSibling != -1)
                        left = true;
                    else if(i == node.keys.size() - 1 && node.rightSibling != -1)
                        right = true;
                }
            }else if(cmp > 0){
                if(relation == 0 && i == node.keys.size() - 1 && node.rightSibling != -1)
                    right = true;
                else if(relation == 2 || relation == 4){
                    if(i == 0 && node.leftSibling != -1)
                        left = true;
                }else if(relation == 1 || relation == 3){
                    if(isFirst || arr1.contains(node.pointers.get(i)))
                        arr2.add(node.pointers.get(i));
                    if(i == 0 && node.leftSibling != -1)
                        left = true;
                    else if(i == node.keys.size() - 1 && node.rightSibling != -1)
                        right = true;
                }
            }
        }


        int re = 0;
        if(left == true)
            re = 1;
        if(right == true)
            re = re + 10;

        return  re;
    }

    private int addResultWoIndex(BPlusTreeLeafNode node, ArrayList arr, Set<Integer> arr1, Set<Integer> arr2,
                                  boolean isFirst, int relation)
            throws IOException, BPlusTreeException{

        if(node == null)
            return -1;

        ArrayList key1 = new ArrayList();
        this.addKey(arr, key1);

        ArrayList key2 = new ArrayList();
        key2.add(0);
        int index = this.column_name.indexOf((String)arr.get(0));

        for(int i = 0; i < node.keyNum; ++i){
            key2.set(0, this.file.readData(node.pointers.get(i)).get(index));
            int cmp = node.compare(key1, key2);
            if(cmp == 2)
                continue;

            if(cmp == 0 && (relation == 0 || relation == 3 || relation == 4) && (isFirst || arr1.contains(node.pointers.get(i)))){
                arr2.add(node.pointers.get(i));
            }else if(cmp < 0 && (relation == 2 || relation == 4 || relation == 5) && (isFirst || arr1.contains(node.pointers.get(i)))){
                arr2.add(node.pointers.get(i));
            }else if(cmp > 0 && (relation == 1 || relation == 3 || relation == 5) && (isFirst || arr1.contains(node.pointers.get(i)))){
                arr2.add(node.pointers.get(i));
            }

        }
        key1 = null;
        key2 = null;
        System.gc();
        return node.rightSibling;
    }

    private void addAllResult(BPlusTreeLeafNode node, Set<Integer> arr)
        throws IOException{

        while(true){

            for(int i =0; i < node.keyNum; ++i){
                arr.add(node.pointers.get(i));
            }

            if(node.rightSibling != -1)
                node = (BPlusTreeLeafNode) this.file.readNode(node.rightSibling, 0);
            else
                break;
        }
    }

    private void addResultNotPrimitive(BPlusTreeLeafNode node, ArrayList arr, Set<Integer> arr1, Set<Integer> arr2,
                                       boolean isFirst, int relation)
            throws BPlusTreeException, IOException{

        String attr1 = (String) arr.get(0);
        String attr2 = (String) arr.get(2);
        int index1 = this.column_name.indexOf(attr1);
        int index2 = this.column_name.indexOf(attr2);

        while(true) {

            for (int i = 0; i < node.keyNum; ++i) {
                ArrayList key1 = new ArrayList();
                ArrayList key2 = new ArrayList();
                key1.add(this.file.readData(node.pointers.get(i)).get(index1));
                key2.add(this.file.readData(node.pointers.get(i)).get(index2));

                int cmp = node.compare(key1, key2);
                if (cmp == 2)
                    continue;

                if (cmp == 0 && (relation == 0 || relation == 3 || relation == 4) && (isFirst || arr1.contains(node.pointers.get(i)))) {
                    arr2.add(node.pointers.get(i));
                } else if (cmp > 0 && (relation == 2 || relation == 4 || relation == 5) && (isFirst || arr1.contains(node.pointers.get(i)))) {
                    arr2.add(node.pointers.get(i));
                } else if (cmp < 0 && (relation == 1 || relation == 3 || relation == 5) && (isFirst || arr1.contains(node.pointers.get(i)))) {
                    arr2.add(node.pointers.get(i));
                }

            }

            if(node.rightSibling != -1)
                node = (BPlusTreeLeafNode) this.file.readNode(node.rightSibling, node.id);
            else
                break;
        }
    }

    private int IsKeyMatch(ArrayList<ArrayList> arr){

//        System.out.println(this.column_name);

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
                tmp.remove((Integer)index);
            }

            if(tmp.size() == 0)
                return i;
        }
        return -1;

    }


    private void addKey(ArrayList arr, ArrayList key){
        try {
            if (column_type.get(this.column_name.indexOf(arr.get(0))) == -1)
                key.add((Integer) arr.get(2));
            else if (column_type.get(this.column_name.indexOf(arr.get(0))) == -2)
                key.add((Long) arr.get(2));
            else if (column_type.get(this.column_name.indexOf(arr.get(0))) == -3)
                key.add((Float) arr.get(2));
            else if (column_type.get(this.column_name.indexOf(arr.get(0))) == -4)
                key.add((Double) arr.get(2));
            else
                key.add((String) arr.get(2));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addRandomKey(int index, ArrayList key){
        for(int i = 1; i < this.index_key.get(index).size(); ++i){
            if(column_type.get(this.index_key.get(index).get(i)) == -1)
                key.add((Integer)1);
            else if(column_type.get(this.index_key.get(index).get(i)) == -2)
                key.add((Long)1L);
            else if(column_type.get(this.index_key.get(index).get(i)) == -3)
                key.add((Float)1f);
            else if(column_type.get(this.index_key.get(index).get(i)) == -4)
                key.add((Double)1d);
            else
                key.add("1");
        }
    }

}
