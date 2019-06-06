package BPlusTree;

import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;

public class BPlusTree {

    private BPlusTreeNode root;
    public static final int M = 20;
    public FileManager fm;
    public int ID;

    public BPlusTree(FileManager file, int offset, boolean is_empty, int id)throws IOException {
        this.fm = file;
        this.ID = id;

        if(is_empty){
            this.root = new BPlusTreeLeafNode(fm, ID);
        }else {
            this.root = fm.readNode(offset, this.ID);
        }
    }

    public void insert(ArrayList key, int data)throws IOException, BPlusTreeException {

        BPlusTreeLeafNode leaf = this.findLeafNode(key);

        for (ArrayList arr: leaf.keys)
            if(leaf.compare(key, arr) == 0)
                throw new BPlusTreeException("Key cannot be duplicated");

        BPlusTreeNode n = leaf.insert(this.fm, key, data);

        this.root = fm.readNode(this.root.location, this.ID);

        if(n != null) {
            this.root = n;
            this.root.parent = -1;
            fm.updateNode(this.root);
            fm.updateRoot(this.ID, this.root.location);
        }
    }

    public void delete(ArrayList key, int id)throws IOException, BPlusTreeException{
        BPlusTreeLeafNode leaf = this.findLeafNode(key);
        BPlusTreeNode n = leaf.delete(fm, key);
        this.root = fm.readNode(this.root.location, this.ID);
        if(n != null) {
            this.root = n;
            this.root.parent = -1;
            fm.updateNode(this.root);
            fm.updateRoot(this.ID, this.root.location);
        }
    }

    public int search(ArrayList key)
            throws BPlusTreeException, IOException{
        BPlusTreeLeafNode leaf = this.findLeafNode(key);

        return leaf.location;
    }

    public BPlusTreeLeafNode getMostLeftLeafNode()
            throws IOException{
        return getMostLeftLeafNode(this.root);
    }

    private BPlusTreeLeafNode getMostLeftLeafNode(BPlusTreeNode node)
            throws IOException{
        if(node.isLeafNode)
            return (BPlusTreeLeafNode) node;
        node = fm.readNode(node.pointers.get(0), this.ID);
        return getMostLeftLeafNode(node);
    }

    public void printBPlusTree()
            throws IOException{

        printBPlusTree(this.root);
        System.out.println();
    }

    private void printBPlusTree(BPlusTreeNode node)
            throws IOException{

        node.print();
        if(node.isLeafNode)
            return;
        for(int i = 0; i < node.keyNum + 1; ++i){
            BPlusTreeNode n = fm.readNode(node.pointers.get(i), this.ID);
            printBPlusTree(n);
        }
    }

    private BPlusTreeLeafNode findLeafNode(ArrayList key)throws IOException, BPlusTreeException {

        BPlusTreeNode node = this.root;
        while (!node.isLeafNode) {
            node = node.search(this.fm, key);
        }
        return (BPlusTreeLeafNode)node;
    }

}


