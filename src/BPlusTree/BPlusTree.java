package BPlusTree;

import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;

public class BPlusTree {

    private FileManager fm;
    private BPlusTreeNode root;
    private int ID;
    public static final int ORDER = 3;

    public BPlusTree(FileManager file, int offset, boolean is_empty, int id) throws IOException {

        this.fm = file;
        this.ID = id;

        if(is_empty){
            this.root = new BPlusTreeLeafNode(fm, ID);
        }else {
            this.root = fm.readNode(offset, this.ID);
        }
    }

    public void insert(ArrayList key, int offset)
            throws BPlusTreeException, IOException{

        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);
        if(this.ID == 0 && leaf.contains(key))
            throw new BPlusTreeException("Primary key error");

        leaf.insertKey(fm, key, offset);

        if(leaf.keyNum > ORDER){
            BPlusTreeNode node = leaf.dealOverflow(fm);
            if(node != null){
                this.root = node;
                this.fm.updateRoot(ID, this.root.location);
            }
        }else{
            fm.updateNode(leaf);
        }
    }

    //return leaf offset
    public int search(ArrayList key)
            throws BPlusTreeException, IOException{
        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);

        return leaf.location;
    }

    private BPlusTreeLeafNode findLeafNodeToInsert(ArrayList key)
            throws BPlusTreeException, IOException{

        BPlusTreeNode node = this.root;
        while (!node.isLeafNode) {
            node = fm.readNode(node.pointers.get(node.search(key)), this.ID);
        }

        return (BPlusTreeLeafNode)node;
    }

}
