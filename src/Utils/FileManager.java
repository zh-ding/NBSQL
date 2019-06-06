package Utils;

import BPlusTree.BPlusTree;
import BPlusTree.BPlusTreeInnerNode;
import BPlusTree.BPlusTreeLeafNode;
import BPlusTree.BPlusTreeNode;
import Server.Server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private RandomAccessFile file;
    private String inputFile;

    /*
    -1: int
    -2: long
    -3: float
    -4: double
     */
    private static final int page_size = 1024 * 4;
    private static final int header_page_num = 4;
    private static final int page_header_len = 8;
    private static final int maxTreeKeyNum = BPlusTree.M - 1;
    /*
    to do cache limit
     */
    /*
    offset, [ boolean, node  ]
    */
    private ArrayList<ArrayList> keyType = new ArrayList<>();
    private ArrayList valueType = new ArrayList();


    public FileManager(String name) throws IOException{
        this.inputFile = name + ".dat";
        if(!Server.node_cache.containsKey(inputFile)){
            Map<Integer, ArrayList> node_node_cache = new HashMap<>();
            Server.node_cache.put(inputFile, node_node_cache);
        }
        if(!Server.data_cache.containsKey(inputFile)){
            Map<Integer, ArrayList> data_data_cache = new HashMap<>();
            Server.data_cache.put(inputFile, data_data_cache);
        }



        try {
            this.file = new RandomAccessFile(inputFile, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int calPos(int page, int offset){
        return page * page_size + offset;
    }

    private int findBlock(int len) throws IOException {
        int i;
        for(i = header_page_num; i*page_size<this.file.length(); i++){
            this.file.seek(i*page_size);
            int tmp = this.file.readInt();
            if(tmp == -1){
                int number = this.file.readInt();
                i += number;
                --i;
            }
            else {
                if(len <= page_size-tmp){
                    return calPos(i, tmp);
                }
            }
        }
        if(len > page_size - page_header_len){
            this.file.seek(i*page_size);
            this.file.writeInt(-1);
            int tmp = len - (page_size - page_header_len);
            int num = 0;
            if(tmp % page_size == 0){
                num = tmp/page_size + 1;
            }
            else{
                num = tmp/page_size + 2;
            }
            this.file.writeInt(num);
            return calPos(i, page_header_len);
        }
        else{
            this.file.seek(i*page_size);
            this.file.writeInt(page_header_len);
            this.file.writeInt(0);
            return calPos(i,page_header_len);
        }
    }

    public ArrayList<Integer> getValueType() throws IOException{
        if(this.valueType.size() == 0) {
            ArrayList<Integer> valueType = new ArrayList<Integer>();
            this.file.seek(4);
            int col_num = this.file.readInt();
            for (int i = 0; i < col_num; i++) {
                this.file.readUTF();
                valueType.add(this.file.readInt());
            }
            this.valueType = valueType;
            return valueType;
        }
        else{
            return this.valueType;
        }
    }

    public int writeValue(ArrayList value) throws IOException{
        ArrayList<Integer> valueType = getValueType();
        ArrayList<Integer> num = this.calNodeLen(valueType);
        int len  = num.get(0);
        len += valueType.size();
        int pos = this.findBlock(len);
        this.file.seek(pos);
        /*
        cache
         */
        ArrayList tmpdata = new ArrayList();
        tmpdata.add(false);
        tmpdata.add(value);
        Server.data_cache.get(inputFile).put(pos, tmpdata);
        if(Server.data_cache.get(inputFile).size()>Server.maxDataCache){
            Server.data_cache.get(inputFile).clear();
        }

        for(int i = 0; i<valueType.size(); i++){
            int tmp = 0;
            if(value.get(i) == null){
                this.file.writeBoolean(true);
                tmp = 1;
            }
            else{
                this.file.writeBoolean(false);
            }
            switch (valueType.get(i)) {
                case -1:
                    if(tmp == 1){
                        this.file.writeInt(0);
                    }
                    else
                        this.file.writeInt((int)value.get(i));
                    break;
                case -2:
                    if(tmp == 1){
                        this.file.writeLong(0);
                    }
                    else
                        this.file.writeLong((long)value.get(i));
                    break;
                case -3:
                    if(tmp == 1){
                        this.file.writeFloat(0);
                    }
                    else
                        this.file.writeFloat((float)value.get(i));
                    break;
                case -4:
                    if(tmp == 1){
                        this.file.writeDouble(0);
                    }
                    else
                        this.file.writeDouble((double)value.get(i));
                    break;
                default:
                    if(tmp == 1){
                        this.file.writeUTF("");
                    }
                    else
                        this.file.writeUTF(formatStr(value.get(i).toString(), valueType.get(i)));
                    break;
            }
        }
        /*
        change page header
         */
        if(len > page_size-page_header_len){
            return pos;
        }
        else{
            this.file.seek((pos/page_size)*page_size);
            int block_len = this.file.readInt()+len;
            int still_len = this.file.readInt() + len;
            this.file.seek((pos/page_size)*page_size);
            this.file.writeInt(block_len);
            this.file.writeInt(still_len);
            return pos;
        }
    }

    private String formatStr(String str, int len){
        for(int i = str.length(); i<len; i++){
            str += " ";
        }
        return str;
    }

    public void resetCache() throws IOException{
        this.file.seek(0);
        this.file.writeInt(Server.auto_id.get(this.inputFile));
        this.resetNodeCache();
        file.close();
    }

    public void resetNodeCache() throws IOException{
        for(Integer offset: Server.node_cache.get(inputFile).keySet()){
            if((boolean)Server.node_cache.get(inputFile).get(offset).get(0)){
                this.resetNode((BPlusTreeNode) Server.node_cache.get(inputFile).get(offset).get(1));
            }
        }
        Server.node_cache.get(inputFile).clear();
    }

    private void resetNode(BPlusTreeNode node) throws IOException{
        ArrayList<Integer> valueType = getKeyType(node.id);
        this.file.seek(node.location);
        this.file.writeBoolean(node.isLeafNode);
        this.file.writeInt(node.keyNum);
        int len = 0;
        for (int i = 0; i < node.keyNum; i++) {
            this.file.writeInt(node.pointers.get(i));
            len = 0;
            for (int j = 0; j < node.keys.get(0).size(); j++) {
                switch (valueType.get(j)) {
                    case -1:
                        this.file.writeInt((int) node.keys.get(i).get(j));
                        len += 4;
                        break;
                    case -2:
                        this.file.writeLong((long) node.keys.get(i).get(j));
                        len += 8;
                        break;
                    case -3:
                        this.file.writeFloat((float) node.keys.get(i).get(j));
                        len += 4;
                        break;
                    case -4:
                        this.file.writeDouble((double) node.keys.get(i).get(j));
                        len += 8;
                        break;
                    default:
                        this.file.writeUTF(formatStr(node.keys.get(i).get(j).toString(), valueType.get(i)));
                        len += 2;
                        len += valueType.get(i);
                        break;
                }
            }
        }
        if (node.isLeafNode == false) {
            this.file.writeInt(node.pointers.get(node.keyNum));
        } else {
            this.file.writeInt(-1);
        }
        this.file.seek(node.location + 1 + 4 + (this.maxTreeKeyNum+1) * 4 + (this.maxTreeKeyNum) * len);
        this.file.writeInt(node.parent);
        this.file.writeInt(node.leftSibling);
        this.file.writeInt(node.rightSibling);
    }

    public void updateNode(BPlusTreeNode node) throws IOException{

        if(!Server.node_cache.get(inputFile).containsKey(node.location)) {
            this.resetNode(node);
            /*
            cache
             */
            ArrayList data = new ArrayList();
            data.add(false);
            data.add(node);
            Server.node_cache.get(inputFile).put(node.location, data);
            if(Server.node_cache.get(inputFile).size() > Server.maxNodeLength){
                this.resetNodeCache();
            }
        }
        else {
            ArrayList data = new ArrayList();
            data.add(0, true);
            data.add(1, node);
            Server.node_cache.get(inputFile).remove(node.location);
            Server.node_cache.get(inputFile).put(node.location, data);
        }
    }

    public int writeTableHeader(int col_num, int index_num, int size, ArrayList<String> column_name,
                                ArrayList<Integer> column_type, ArrayList<Integer> key_index, int auto_id, ArrayList<Boolean> column_isNotNull)
            throws IOException{
        this.file.seek(0);
        this.file.writeInt(auto_id);
        this.file.writeInt(col_num);
        for(int i = 0; i < col_num; ++i){
            this.file.writeUTF(column_name.get(i));
            this.file.writeInt(column_type.get(i));
        }
        for(int i = 0; i<col_num; ++i){
            this.file.writeBoolean(column_isNotNull.get(i));
        }
        this.file.seek(2*page_size);
        this.file.writeInt(index_num);
        int len = 0;
        for(int i = 0; i<key_index.size(); i++){
            switch (column_type.get(key_index.get(i))){
                case -1:
                    len += 4;
                    break;
                case -2:
                    len += 8;
                    break;
                case -3:
                    len += 4;
                    break;
                case -4:
                    len += 8;
                    break;
                default:
                    len += 2;
                    len += column_type.get(key_index.get(i));
                    break;
            }
        }
        len = 1+4+(this.maxTreeKeyNum+1)*4+3*4+(this.maxTreeKeyNum)*(len);
        int pos = this.findBlock(len);
        this.file.seek(2*page_size+4);
        this.file.writeInt(pos);
        this.file.writeInt(size);
        for(int i = 0; i<size; i++){
            this.file.writeInt(key_index.get(i));
        }
        return pos;
    }

    private ArrayList<Integer> getKeyType(int id) throws IOException{
        if(id >= this.keyType.size()){
            ArrayList<Integer> num = new ArrayList<Integer>();
            int offset = 2*page_size;
            this.file.seek(2*page_size);
            int index_num = this.file.readInt();
            offset += 4;
            for(int i = 0; i<index_num; i++){
                this.file.readInt();
                int tmp = this.file.readInt();
                if(i == id){
                    for(int j = 0; j<tmp; j++){
                        num.add(this.file.readInt());
                    }
                    break;
                }
                offset += 8;
                offset += tmp*4;
                this.file.seek(offset);
            }
            ArrayList<Integer> col_type = new ArrayList<Integer>();
            this.file.seek(4);
            int col_num = this.file.readInt();
            for(int i = 0; i<col_num; i++){
                this.file.readUTF();
                col_type.add(this.file.readInt());
            }
            ArrayList<Integer> type = new ArrayList<Integer>();
            for(int i = 0; i<num.size(); i++){
                type.add(col_type.get(num.get(i)));
            }
            return type;
        }
        else{
            ArrayList<Integer> num = this.keyType.get(id);
            ArrayList<Integer> col_type = new ArrayList<Integer>();
            if(this.valueType.size() == 0){
                this.file.seek(4);
                int col_num = this.file.readInt();
                for(int i = 0; i<col_num; i++){
                    this.file.readUTF();
                    col_type.add(this.file.readInt());
                }
                this.valueType = col_type;
            }
            else {
                col_type = this.valueType;
            }
            ArrayList<Integer> type = new ArrayList<Integer>();
            for(int i = 0; i<num.size(); i++){
                type.add(col_type.get(num.get(i)));
            }
            return type;
        }
    }

    private ArrayList<Integer> calNodeLen(ArrayList<Integer> keyType){
        ArrayList<Integer> num = new ArrayList<>();
        int len = 0;
        for(int i = 0; i<keyType.size(); i++){
            switch (keyType.get(i)){
                case -1:
                    len += 4;
                    break;
                case -2:
                    len += 8;
                    break;
                case -3:
                    len += 4;
                    break;
                case -4:
                    len += 8;
                    break;
                default:
                    len += 2;
                    len += keyType.get(i);
                    break;
            }
        }
        num.add(len);
        int total = len*(this.maxTreeKeyNum)+(this.maxTreeKeyNum+1)*4+4+1+4*3;
        num.add(total);
        return num;
    }

    public int writeNewNode(int id, boolean isleafnode) throws IOException {
        ArrayList<Integer> keyType = this.getKeyType(id);
        ArrayList<Integer> node_len = this.calNodeLen(keyType);
        int len = node_len.get(0);
        int total = node_len.get(1);
        int pos = findBlock(total);
        this.file.seek(pos);
        this.file.writeBoolean(isleafnode);
        this.file.writeInt(0);
        this.file.seek(pos+1+4+(this.maxTreeKeyNum+1)*4+(this.maxTreeKeyNum)*len);
        this.file.writeInt(-1);
        this.file.writeInt(-1);
        this.file.writeInt(-1);
        if(total > page_size - page_header_len){
            return pos;
        }
        else{
            this.file.seek((pos/page_size)*page_size);
            int block_len = this.file.readInt()+total;
            int still_len = this.file.readInt()+total;
            this.file.seek((pos/page_size)*page_size);
            this.file.writeInt(block_len);
            this.file.writeInt(still_len);
            return pos;
        }
    }

    public BPlusTreeNode readNode(int offset, int id) throws IOException{

        if(!Server.node_cache.get(inputFile).containsKey(offset)){
            if(offset == -1){
                return null;
            }
            ArrayList<Integer> keyType = getKeyType(id);
            ArrayList<Integer> node_len = this.calNodeLen(keyType);
            int len = node_len.get(0);
            ArrayList<ArrayList> keys = new ArrayList<ArrayList>();
            ArrayList<Integer> pointers = new ArrayList<Integer>();
            int parent;
            int leftSibling;
            int rightSibling;
            int keyNum;
            int location;
            boolean isLeafNode;
            this.file.seek(offset);
            isLeafNode = this.file.readBoolean();
            keyNum = this.file.readInt();
            if(keyNum != 0){
                pointers.add(this.file.readInt());
            }
            for(int i = 0; i<keyNum; i++){
                ArrayList tmpKey = new ArrayList();
                for(int j = 0; j<keyType.size(); j++){
                    switch (keyType.get(j)) {
                        case -1:
                            tmpKey.add(this.file.readInt());
                            break;
                        case -2:
                            tmpKey.add(this.file.readLong());
                            break;
                        case -3:
                            tmpKey.add(this.file.readFloat());
                            break;
                        case -4:
                            tmpKey.add(this.file.readDouble());
                            break;
                        default:
                            tmpKey.add(Rtrim(this.file.readUTF()));
                            break;
                    }
                }
                keys.add(tmpKey);
                pointers.add(this.file.readInt());
            }
            this.file.seek(offset+1+4+(this.maxTreeKeyNum+1)*4+(this.maxTreeKeyNum)*len);
            parent = this.file.readInt();
            leftSibling = this.file.readInt();
            rightSibling = this.file.readInt();
            location = offset;
            BPlusTreeNode node = null;
            if(isLeafNode ==true){
                node = new BPlusTreeLeafNode(keys, pointers, parent, leftSibling, rightSibling, keyNum, location, isLeafNode, id);
            }
            else{
                node = new BPlusTreeInnerNode(keys, pointers, parent, leftSibling, rightSibling, keyNum, location, isLeafNode, id);
            }

            /*
            cache
             */
            ArrayList data = new ArrayList();
            data.add(false);
            data.add(node);
            Server.node_cache.get(inputFile).put(node.location, data);
            if(Server.node_cache.get(inputFile).size() > Server.maxNodeLength){
                this.resetNodeCache();
            }
            return node;
        }
        else {
            return (BPlusTreeNode)(Server.node_cache.get(inputFile).get(offset).get(1));
        }
    }

    private String Rtrim(String str){
        String tmpstr= str;
        int len = tmpstr.length()-1;
        while(tmpstr.charAt(len) == ' '){
            len --;
            if(len == 0){
                break;
            }
        }
        tmpstr = tmpstr.substring(0,len+1);
        return tmpstr;
    }

    public ArrayList readData(int offset) throws IOException{
        if(offset == -1){
            return null;
        }
        if(!Server.data_cache.get(inputFile).containsKey(offset)){
            ArrayList<Integer> valueType = this.getValueType();
            ArrayList data = new ArrayList();
            this.file.seek(offset);
            for(int i = 0; i<valueType.size(); i++){
                int tmp = 0;
                if(this.file.readBoolean() == true){
                    data.add(null);
                    tmp = 1;
                }
                switch (valueType.get(i)) {
                    case -1:
                        if(tmp == 1){
                            this.file.readInt();
                            break;
                        }
                        else
                            data.add(this.file.readInt());
                        break;
                    case -2:
                        if(tmp == 1){
                            this.file.readLong();
                            break;
                        }
                        else
                            data.add(this.file.readLong());
                        break;
                    case -3:
                        if(tmp == 1){
                            this.file.readFloat();
                            break;
                        }
                        else
                            data.add(this.file.readFloat());
                        break;
                    case -4:
                        if(tmp == 1){
                            this.file.readDouble();
                            break;
                        }
                        else
                            data.add(this.file.readDouble());
                        break;
                    default:
                        if(tmp == 1){
                            this.file.readUTF();
                            break;
                        }
                        else
                            data.add(Rtrim(this.file.readUTF()));
                        break;
                }
            }
            /*
            cache
             */
            ArrayList tmpdata = new ArrayList();
            tmpdata.add(false);
            tmpdata.add(data);
            Server.data_cache.get(inputFile).put(offset, tmpdata);
            if(Server.data_cache.get(inputFile).size()>Server.maxDataCache){
                Server.data_cache.get(inputFile).clear();
            }
            return data;
        }
        else{
            return (ArrayList)(Server.data_cache.get(inputFile).get(offset).get(1));
        }
    }

    public void updateRoot(int id, int location) throws IOException{
        ArrayList<Integer> num = new ArrayList<Integer>();
        int offset = 2*page_size;
        this.file.seek(2*page_size);
        int index_num = this.file.readInt();
        offset += 4;
        for(int i = 0; i<index_num; i++){
            if(i == id){
                this.file.writeInt(location);
                break;
            }
            this.file.readInt();
            int tmp = this.file.readInt();
            offset += 8;
            offset += tmp*4;
            this.file.seek(offset);
        }
    }

    public ArrayList<Integer> readIndexForest() throws IOException {

        ArrayList<Integer> num = new ArrayList<Integer>();
        this.file.seek(2*page_size);
        int index_num = this.file.readInt();
        num.add(index_num);
        for(int i = 0; i<index_num; i++){
            num.add(this.file.readInt());
            int tmp = this.file.readInt();
            num.add(tmp);
            ArrayList tmpIndex = new ArrayList();
            for(int j = 0; j<tmp; j++){
                int index = this.file.readInt();
                num.add(index);
                tmpIndex.add(index);
            }
            this.keyType.add(tmpIndex);
        }
        return num;
    }

    public ArrayList<Integer> readTableHeader(ArrayList<String> column_name, ArrayList<Integer> column_type, ArrayList<Boolean> column_isNotNull) throws IOException{
        ArrayList<Integer> num = new ArrayList<>();
        this.file.seek(0);
        num.add(this.file.readInt());
        int col_num = this.file.readInt();
        num.add(col_num);
        for(int i = 0; i < col_num; ++i){
            column_name.add(this.file.readUTF());
            column_type.add(this.file.readInt());
        }
        for(int i = 0; i<col_num; i++){
            column_isNotNull.add(this.file.readBoolean());
        }
        return num;
    }

    private void deleteData(int offset, int total) throws IOException{
        this.file.seek((offset/page_size)*page_size);
        int start = this.file.readInt();
        int len = this.file.readInt();
        if(start == -1){
            int begin = (offset/page_size)*page_size;
            for(int i = 0; i<len; i++){
                begin = begin + i*page_size;
                this.file.seek(begin);
                this.file.writeInt(page_header_len);
                this.file.writeInt(0);
            }
        }
        else{
            if(len == total){
                this.file.seek((offset/page_size)*page_size);
                this.file.writeInt(page_header_len);
                this.file.writeInt(0);
            }
            else{
                len = len - total;
                if(start == offset + total){
                    start = offset;
                }
                this.file.seek((offset/page_size)*page_size);
                this.file.writeInt(start);
                this.file.writeInt(len);
            }
        }
    }

    public void deleteNode(int offset, int id) throws IOException{
        if(Server.node_cache.get(inputFile).containsKey(offset)){
            Server.node_cache.remove(offset);
        }
        ArrayList<Integer> keyType = getKeyType(id);
        ArrayList<Integer> node_len = this.calNodeLen(keyType);
        int total = node_len.get(1);
        this.deleteData(offset, total);
    }

    public void deleteValue(int offset) throws IOException{
        if(Server.data_cache.get(inputFile).containsKey(offset)){
            Server.data_cache.get(inputFile).remove(offset);
        }
        ArrayList<Integer> valueType = getValueType();
        ArrayList<Integer> node_len = this.calNodeLen(valueType);
        int total = node_len.get(0);
        total += valueType.size();
        this.deleteData(offset, total);
    }

    public void deleteFile(){
        try {
            this.file.seek(0);
            this.file.writeInt(Server.auto_id.get(this.inputFile));
            this.resetNodeCache();
            Server.data_cache.get(inputFile).clear();
            Server.data_cache.remove(inputFile);
            Server.node_cache.remove(inputFile);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
