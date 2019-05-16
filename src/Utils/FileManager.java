package Utils;

import BPlusTree.BPlusTreeInnerNode;
import BPlusTree.BPlusTreeLeafNode;
import BPlusTree.BPlusTreeNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class FileManager {

    private RandomAccessFile file;
    private String inputFile;
    private FileChannel fc;
    private ByteBuffer bb;
    private final int blockSize = 1024;
    private int size = 0;

    /*
    -1: int
    -2: long
    -3: float
    -4: double
     */
    private static final int page_size = 1024 * 4;
    private static final int header_page_num = 4;
    private static final int page_header_len = 8;

    public FileManager(String name) throws IOException{
        this.inputFile = name + ".dat";
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
        long test = this.file.length();
        for(i = header_page_num; i*page_size<this.file.length(); i++){
            this.file.seek(i*page_size);
            int tmp = this.file.readInt();
            if(len <= page_size-tmp){
                return calPos(i, tmp);
            }
        }
        this.file.seek(i*page_size);
        this.file.writeInt(page_header_len);
        this.file.writeInt(0);
        return calPos(i,page_header_len);
    }

    public ArrayList<Integer> getValueType() throws IOException{
        ArrayList<Integer> valueType = new ArrayList<Integer>();
        this.file.seek(4);
        int col_num = this.file.readInt();
        for(int i = 0; i<col_num; i++){
            this.file.readUTF();
            valueType.add(this.file.readInt());
        }
        return valueType;
    }

    public int writeValue(ArrayList value) throws IOException{
        ArrayList<Integer> valueType = getValueType();
        ArrayList<Integer> num = this.calNodeLen(valueType);
        int len  = num.get(0);
        int pos = this.findBlock(len);
        this.file.seek(pos);
        for(int i = 0; i<valueType.size(); i++){
            switch (valueType.get(i)) {
                case -1:
                    this.file.writeInt((int)value.get(i));
                    break;
                case -2:
                    this.file.writeLong((long)value.get(i));
                    break;
                case -3:
                    this.file.writeFloat((float)value.get(i));
                    break;
                case -4:
                    this.file.writeDouble((double)value.get(i));
                    break;
                default:
                    this.file.writeUTF(formatStr(value.get(i).toString(), valueType.get(i)));
                    break;
            }
        }
        /*
        change page header
         */
        this.file.seek((pos/page_size)*page_size);
        int block_len = this.file.readInt()+len;
        int still_len = this.file.readInt() + len;
        this.file.seek((pos/page_size)*page_size);
        this.file.writeInt(block_len);
        this.file.writeInt(still_len);
        return pos;
    }

    private String formatStr(String str, int len){
        for(int i = str.length(); i<len; i++){
            str += " ";
        }
        return str;
    }

    public void updateNode(BPlusTreeNode node) throws IOException{
        ArrayList<Integer> valueType = getKeyType(node.id);
        this.file.seek(node.location);
        this.file.writeBoolean(node.isLeafNode);
        this.file.writeInt(node.keyNum);
        int len = 0;
        for(int i = 0; i<node.keyNum; i++){
            this.file.writeInt(node.pointers.get(i));
            len = 0;
            for(int j = 0; j<node.keys.get(0).size(); j++){
                switch (valueType.get(j)) {
                    case -1:
                        this.file.writeInt((int)node.keys.get(i).get(j));
                        len += 4;
                        break;
                    case -2:
                        this.file.writeLong((long)node.keys.get(i).get(j));
                        len += 8;
                        break;
                    case -3:
                        this.file.writeFloat((float)node.keys.get(i).get(j));
                        len += 4;
                        break;
                    case -4:
                        this.file.writeDouble((double)node.keys.get(i).get(j));
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
        if(node.isLeafNode == false){
            this.file.writeInt(node.pointers.get(node.keyNum));
        }
        else {
            this.file.writeInt(-1);
        }
        this.file.seek(node.location+1+4+4*4+3*len);
        this.file.writeInt(node.parent);
        this.file.writeInt(node.leftSibling);
        this.file.writeInt(node.rightSibling);

    }

    public int writeTableHeader(int col_num, int index_num, int size, ArrayList<String> column_name,
                                ArrayList<Integer> column_type, ArrayList<Integer> key_index, int auto_id)
            throws IOException{
        this.file.seek(0);
        this.file.writeInt(auto_id);
        this.file.writeInt(col_num);
        for(int i = 0; i < col_num; ++i){
            this.file.writeUTF(column_name.get(i));
            this.file.writeInt(column_type.get(i));
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
        len = 1+4+4*4+3*4+3*(len);
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
        int total = len*3+4*4+4+1+4*3;
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
        this.file.seek(pos+1+4+4*4+3*len);
        this.file.writeInt(-1);
        this.file.writeInt(-1);
        this.file.writeInt(-1);
        this.file.seek((pos/page_size)*page_size);
        int block_len = this.file.readInt()+total;
        int still_len = this.file.readInt()+total;
        this.file.seek((pos/page_size)*page_size);
        this.file.writeInt(block_len);
        this.file.writeInt(still_len);
        return pos;
    }

    public BPlusTreeNode readNode(int offset, int id) throws IOException{
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
                        tmpKey.add(this.file.readUTF());
                        break;
                }
            }
            keys.add(tmpKey);
            pointers.add(this.file.readInt());
        }
        this.file.seek(offset+1+4+4*4+3*len);
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
        return node;
    }

    public ArrayList readData(int offset) throws IOException{
        ArrayList<Integer> valueType = this.getValueType();
        ArrayList data = new ArrayList();
        this.file.seek(offset);
        for(int i = 0; i<valueType.size(); i++){
            switch (valueType.get(i)) {
                case -1:
                    data.add(this.file.readInt());
                    break;
                case -2:
                    data.add(this.file.readLong());
                    break;
                case -3:
                    data.add(this.file.readFloat());
                    break;
                case -4:
                    data.add(this.file.readDouble());
                    break;
                default:
                    data.add(this.file.readUTF());
                    break;
            }
        }
        return data;
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
            for(int j = 0; j<tmp; j++){
                num.add(this.file.readInt());
            }
        }
        return num;
    }

    public int readTableHeader(ArrayList<String> column_name, ArrayList<Integer> column_type) throws IOException{
        this.file.seek(0);
        int col_num = this.file.readInt();
        for(int i = 0; i < col_num; ++i){
            column_name.add(this.file.readUTF());
            column_type.add(this.file.readInt());
        }

        return col_num;
    }

    public int deleteRow() throws IOException{

        return 0;
    }

    public int updateRow() throws IOException{

        return 0;
    }

    public void write(byte[] bytes, int position) throws IOException {
        if(position < 0 || position > size){
            //error
        }else{
            writeBytes(bytes);
            if(position == size)
                size++;
        }
    }

    public int write(byte[] bytes) throws IOException{
        writeBytes(bytes);
        return ++size;
    }

    private void writeBytes(byte[] bytes) throws IOException{
        bb = ByteBuffer.allocate(blockSize);
        bb.put(bytes);
        bb.rewind();
        fc.write(bb, size*blockSize);
    }

    public byte[] read(int blockPosition) throws IOException{
        if(blockPosition < 0 || blockPosition > size){
            throw new IndexOutOfBoundsException();
        }

        bb = ByteBuffer.allocate(blockSize);
        fc.read(bb, blockPosition*blockSize);
        return bb.array();
    }

    public int getBlockSize(){
        return blockSize;
    }

    public int getSize(){
        return size;
    }
    public long getNumberOfReadWrites(){
        return 0;
    }

    public void resetReadWriteCounter(){
    }

    private void deleteData(int offset, int total) throws IOException{
        this.file.seek((offset/page_size)*page_size);
        int start = this.file.readInt();
        int len = this.file.readInt();
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

    public void deleteNode(int offset, int id) throws IOException{
        ArrayList<Integer> keyType = getKeyType(id);
        ArrayList<Integer> node_len = this.calNodeLen(keyType);
        int total = node_len.get(1);
        this.deleteData(offset, total);
    }

    public void deleteValue(int offset) throws IOException{
        ArrayList<Integer> valueType = getValueType();
        ArrayList<Integer> node_len = this.calNodeLen(valueType);
        int total = node_len.get(0);
        this.deleteData(offset, total);
    }


    public void deleteFile(){
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        file.close();
    }
}
