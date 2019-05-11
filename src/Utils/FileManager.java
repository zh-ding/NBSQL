package Utils;

import BPlusTree.BPlusTree;
import BPlusTree.BPlusTreeNode;
import BPlusTree.BPlusTreeLeafNode;
import BPlusTree.BPlusTreeInnerNode;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

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
    private static final int page_header_len = 4;

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
        for(i = header_page_num; i*page_size<this.file.length(); i+=1){
            this.file.seek(i*page_size);
            if(len <= page_size-this.file.readInt()){
                return calPos(i, this.file.readInt());
            }
        }
        return calPos(i,page_header_len);
    }

    public ArrayList<Integer> getType(ArrayList value){
        ArrayList<Integer> valueType = new ArrayList<Integer>();
        for(int i = 0; i<valueType.size(); i++){
            if(value.get(i) instanceof Integer){
                valueType.add(-1);
            }
            if(value.get(i) instanceof Long){
                valueType.add(-2);
            }
            if(value.get(i) instanceof Float){
                valueType.add(-3);
            }
            if(value.get(i) instanceof Double){
                valueType.add(-4);
            }
            if(value.get(i) instanceof String){
                int tmp = value.get(i).toString().length();
                valueType.add(tmp);
            }
        }
        return valueType;
    }

    public int writeValue(ArrayList value) throws IOException{
        int len = 0;
        ArrayList<Integer> valueType = new ArrayList<Integer>();
        for(int i = 0; i<valueType.size(); i++){
            if(value.get(i) instanceof Integer){
                valueType.add(-1);
                len += 4;
            }
            if(value.get(i) instanceof Long){
                valueType.add(-2);
                len += 8;
            }
            if(value.get(i) instanceof Float){
                valueType.add(-3);
                len += 4;
            }
            if(value.get(i) instanceof Double){
                valueType.add(-4);
                len += 8;
            }
            if(value.get(i) instanceof String){
                int tmp = value.get(i).toString().length();
                valueType.add(tmp);
                len = len + tmp +2;
            }
        }
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
                    this.file.writeUTF(value.get(i).toString());
                    break;
            }
        }
        /*
        change page header
         */
        this.file.seek((pos/page_size)*page_size);
        int block_len = this.file.readInt()+len;
        this.file.writeInt(block_len);
        return pos;
    }

    public void updateNode(BPlusTreeNode node) throws IOException{
        ArrayList<Integer> valueType = getType(node.keys.get(0));
        this.file.seek(node.location);
        this.file.writeBoolean(node.isLeafNode);
        this.file.writeInt(node.keyNum);
        for(int i = 0; i<node.keyNum; i++){
            this.file.writeInt(node.pointers.get(i));
            for(int j = 0; j<node.keys.size(); j++){
                switch (valueType.get(i)) {
                    case -1:
                        this.file.writeInt((int)node.keys.get(i).get(j));
                        break;
                    case -2:
                        this.file.writeLong((long)node.keys.get(i).get(j));
                        break;
                    case -3:
                        this.file.writeFloat((float)node.keys.get(i).get(j));
                        break;
                    case -4:
                        this.file.writeDouble((double)node.keys.get(i).get(j));
                        break;
                    default:
                        this.file.writeUTF(node.keys.get(i).get(j).toString());
                        break;
                }
            }
        }
        this.file.writeInt(node.pointers.get(node.keyNum));
    }

    public int writeTableHeader(int col_num, int index_num, int size, ArrayList<String> column_name,
                                ArrayList<Integer> column_type, ArrayList<Integer> key_index)
            throws IOException{
        this.file.seek(0);
        this.file.writeInt(col_num);
        for(int i = 0; i < col_num; ++i){
            this.file.writeUTF(column_name.get(i));
            this.file.writeInt(column_type.get(i));
        }
        this.file.seek(2*page_size);
        this.file.writeInt(index_num);
        int pos = this.findBlock(10);
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
            if(i+1 == id){
                for(int j = 0; j<tmp; j++){
                    num.add(this.file.readInt());
                }
                break;
            }
            offset += 8;
            offset += tmp*4;
            this.file.seek(offset);
        }
        return num;
    }

    public int writeNewNode(int id) throws IOException {
        ArrayList<Integer> keyType = this.getKeyType(id);
        int len = 0;
        for(int i = 0; i<keyType.size(); i++){
            switch (i){
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
                    len += i;
                    break;
            }
        }
        int pos = findBlock(len*3+4*4+4+1+4*3);
        return pos;
    }

    public BPlusTreeNode readNode(int offset, int id) throws IOException{
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
        pointers.add(this.file.readInt());
        ArrayList<Integer> keyType = getKeyType(id);
        for(int i = 0; i<keyNum; i++){
            ArrayList tmpKey = new ArrayList();
            for(int j = 0; j<keyType.size(); j++){
                switch (keyType.get(i)) {
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

    public void updateRoot(int id, int location) throws IOException{
        ArrayList<Integer> num = new ArrayList<Integer>();
        int offset = 2*page_size;
        this.file.seek(2*page_size);
        int index_num = this.file.readInt();
        offset += 4;
        for(int i = 0; i<index_num; i++){
            if(i+1 == id){
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
