package Utils;

import BPlusTree.BPlusTree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class FileManager {

    private RandomAccessFile file;
    private File inputFile;
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
        this.inputFile = new File(name);
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

    public int writeRow(ArrayList column_type, ArrayList row, int len) throws IOException{
        int pos = findBlock(len);
        this.file.seek(pos);
        Iterator type = column_type.iterator();
        Iterator data = row.iterator();
        while(data.hasNext()){
            switch ((Integer)type.next()) {
                case -1:
                    this.file.writeInt((int)data.next());
                    break;
                case -2:
                    this.file.writeLong((long)data.next());
                    break;
                case -3:
                    this.file.writeFloat((float)data.next());
                    break;
                case -4:
                    this.file.writeDouble((double)data.next());
                    break;
                case 4:
                    this.file.writeUTF(data.next().toString());
                    break;
            }
        }
        /*
        change page header
         */
        this.file.seek((pos/page_size)*page_size);
        int block_len = this.file.readInt()+len;
        this.file.writeInt(block_len);

        return 0;
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

    public ArrayList readRow(int pos, ArrayList column_type) throws IOException{
        ArrayList row = new ArrayList();
        Iterator type = column_type.iterator();
        this.file.seek(pos);
        while(type.hasNext()){
            switch ((Integer)type.next()) {
                case -1:
                    row.add((int)this.file.readInt());
                    break;
                case -2:
                    row.add((long)this.file.readLong());
                    break;
                case -3:
                    row.add((float)this.file.readFloat());
                    break;
                case -4:
                    row.add((double)this.file.readDouble());
                    break;
                case 4:
                    row.add(this.file.readUTF().toString());
                    break;
            }
        }
        return row;
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

    public boolean deleteFile(){
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputFile.delete();
    }

    public void close() throws IOException {
        file.close();
    }
}
