import Utils.FileManager;
import Table.TableRow;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;


public class test {
    public static void main(String[] args) throws IOException {

        ArrayList b = new ArrayList();
        b.add(5);
        b.add("s");
        Iterator data = b.iterator();
        while(data.hasNext()){
            System.out.print( data.next().toString());
            System.out.print(1);
        }
        RandomAccessFile file = new RandomAccessFile("test.txt","rw");
        file.seek(0);
        file.writeInt(1);
        file.writeInt(2);
        file.seek(0);
        System.out.println(file.readInt());
        System.out.println(file.readInt());
    }
}