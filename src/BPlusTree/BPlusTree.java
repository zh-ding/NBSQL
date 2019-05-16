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

    public void delete(ArrayList key, int id)
            throws IOException, BPlusTreeException {

        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);


        if (leaf.delete(this.fm, key) && leaf.keyNum < Math.ceil(this.ORDER / 2.0)) {
            BPlusTreeNode n = leaf.dealUnderflow(fm);
            if (n != null)
                this.root = n;
        }

    }

    //return leaf offset
    public int search(ArrayList key)
            throws BPlusTreeException, IOException{
        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);

        return leaf.location;
    }

    public void printBPlusTree()
            throws IOException{

        printBPlusTree(this.root);
    }

    public BPlusTreeLeafNode getMostLeftLeafNode()
            throws IOException{
        return getMostLeftLeafNode(this.root);
    }

    private void printBPlusTree(BPlusTreeNode node)
            throws IOException{

        node.print();
        if(node.isLeafNode)
            return;
        for(Integer offset:node.pointers){
            BPlusTreeNode n = fm.readNode(offset, this.ID);
            printBPlusTree(n);
        }
    }

    private BPlusTreeLeafNode getMostLeftLeafNode(BPlusTreeNode node)
            throws IOException{
        if(node.isLeafNode)
            return (BPlusTreeLeafNode) node;
        node = fm.readNode(node.pointers.get(0), this.ID);
        return getMostLeftLeafNode(node);
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
