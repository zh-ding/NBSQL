package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileManager {

    private RandomAccessFile file;
    private File inputFile;
    private FileChannel fc;
    private ByteBuffer bb;
    private final int blockSize;
    private int size = 0;
    private long numReadWrites = 0;

    public FileManager(String name, int blockSize){
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
        } catch (IOException ie){
            ie.printStackTrace();
        }
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
