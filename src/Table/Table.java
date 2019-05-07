package Table;

import java.io.IOException;
import java.io.RandomAccessFile;
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
    private ArrayList<Integer> column_type;
    private int primary_key_index = 0;
    private int col_num = 0;
    private RandomAccessFile file;
    private int index_num = 1;

    private static final int page_size = 1024 * 4;
    private static final int header_page_num = 4;

    Table(String[] names, int[] types, String primary_key, String table_name)
        throws IOException {

        this.col_num = names.length;

        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();
        this.column_name.add("id");
        this.column_type.add(0);


        for(int i = 0; i < names.length; ++i) {
            this.column_name.add(names[i]);
            this.column_type.add(types[i]);
            if (names[i] == primary_key)
                primary_key_index = i + 1;
        }

        String path = table_name + ".dat";
        this.file = new RandomAccessFile(path, "rw");

        writeTableHeader();
    }

    Table(String table_name)
        throws IOException{

        this.column_name = new ArrayList<>();
        this.column_type = new ArrayList<>();

        readTableHeader();

    }

    void writeTableHeader() throws IOException{

        this.file.seek(0);

        this.file.writeInt(this.col_num);
        for(int i = 0; i < this.col_num; ++i){
            this.file.writeUTF(this.column_name.get(i));
            this.file.writeInt(this.column_type.get(i));
        }

        this.file.writeInt(this.index_num);

    }

    void readTableHeader() throws IOException{

        this.file.seek(0);

        this.col_num = this.file.readInt();
        for(int i = 0; i < this.col_num; ++i){
            this.column_name.add(this.file.readUTF());
            this.column_type.add(this.file.readInt());
        }

        this.index_num = this.file.readInt();

    }

    void InsertRow(){

    }

    void DeleteRow(){

    }

    void SelectRows(){

    }
}
