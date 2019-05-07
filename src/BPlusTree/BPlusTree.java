package BPlusTree;

import Table.TableFile;

import java.io.File;
import java.io.IOException;

public class BPlusTree {

    private BPlusTreeNode root;
    public TableFile tablefile;

    public BPlusTree(String name){
        String path = name + ".dat";
        File f = new File(path);
        if(f.exists()){

        }
    }

    public BPlusTree() throws IOException {

    }
}
