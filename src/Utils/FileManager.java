package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class FileManager {

    private RandomAccessFile file;
    private File inputFile;
    private FileChannel fc;
    private ByteBuffer bb;
    private final int blockSize;
    private int size = 0;
    private long numReadWrites = 0;

    /*
    0: int
    1: long
    2: float
    3: double
    4: String
     */
    private static final int page_size = 1024 * 4;
    private static final int header_page_num = 4;
    private static final int page_header_len = 4;

    public FileManager(String name, int blockSize) throws IOException{
        this.blockSize = blockSize;
        this.numReadWrites = 0;
        this.inputFile = new File(name);
        try {
            this.file = new RandomAccessFile(inputFile, "rw");
            this.fc = file.getChannel();
            this.bb = ByteBuffer.allocate(blockSize);
            if (inputFile.exists()){
                this.size  = (int) (this.inputFile.length() / blockSize);
                if (this.inputFile.length() % blockSize != 0){
                    this.file.setLength(size*blockSize);
                }
            }else{
                this.size  = 0;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int calPos(int page, int offset){
        return page * page_size + offset;
    }
    // 返回可以写入的位置
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
                case 0:
                    this.file.writeInt((int)data.next());
                    break;
                case 1:
                    this.file.writeLong((long)data.next());
                    break;
                case 2:
                    this.file.writeFloat((float)data.next());
                    break;
                case 3:
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
                case 0:
                    row.add((int)this.file.readInt());
                    break;
                case 1:
                    row.add((long)this.file.readLong());
                    break;
                case 2:
                    row.add((float)this.file.readFloat());
                    break;
                case 3:
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
        this.numReadWrites++;
    }

    public byte[] read(int blockPosition) throws IOException{
        if(blockPosition < 0 || blockPosition > size){
            throw new IndexOutOfBoundsException();
        }

        bb = ByteBuffer.allocate(blockSize);
        fc.read(bb, blockPosition*blockSize);
        this.numReadWrites++;
        return bb.array();
    }

    public int getBlockSize(){
        return blockSize;
    }

    public int getSize(){
        return size;
    }
    public long getNumberOfReadWrites(){
        return this.numReadWrites;
    }

    public void resetReadWriteCounter(){
        this.numReadWrites = 0;
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
