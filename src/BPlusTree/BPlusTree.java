package BPlusTree;

//public class BPlusTree {
//
//    private FileManager fm;
//    private BPlusTreeNode root;
//    private int ID;
//    public static final int ORDER = 4;
//
//    public BPlusTree(FileManager file, int offset, boolean is_empty, int id) throws IOException {
//
//        this.fm = file;
//        this.ID = id;
//
//        if(is_empty){
//            this.root = new BPlusTreeLeafNode(fm, ID);
//        }else {
//            this.root = fm.readNode(offset, this.ID);
//        }
//    }
//
//    public void insert(ArrayList key, int offset)
//            throws BPlusTreeException, IOException{
//
//        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);
//        if(this.ID == 0 && leaf.contains(key))
//            throw new BPlusTreeException("Primary key error");
//
//        leaf.insertKey(fm, key, offset);
//
//        if(leaf.keyNum > BPlusTree.ORDER){
//            BPlusTreeNode node = leaf.dealOverflow(fm);
//            if(node != null){
//                this.root = node;
//                this.fm.updateRoot(this.ID, this.root.location);
//            }
//        }else{
//            fm.updateNode(leaf);
//        }
//
//        //this.root = fm.readNode(this.root.location, this.ID);
//    }
//
//    public void delete(ArrayList key, int id)
//            throws IOException, BPlusTreeException {
//
//        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);
//
//
//        if (leaf.delete(this.fm, key) && leaf.keyNum < Math.floor(this.ORDER / 2.0) - 1) {
//            BPlusTreeNode n = leaf.dealUnderflow(fm);
//            if (n != null) {
//                this.root = n;
//                this.fm.updateRoot(this.ID, this.root.location);
//            }
//        }else{
//            fm.updateNode(leaf);
//        }
//
//
//        this.root = fm.readNode(this.root.location, this.ID);
//    }
//
//    //return leaf offset
//    public int search(ArrayList key)
//            throws BPlusTreeException, IOException{
//        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);
//
//        return leaf.location;
//    }
//
//    public void printBPlusTree()
//            throws IOException{
//
//        printBPlusTree(this.root);
//        System.out.println();
//    }
//
//    public BPlusTreeLeafNode getMostLeftLeafNode()
//            throws IOException{
//        return getMostLeftLeafNode(this.root);
//    }
//
//    private void printBPlusTree(BPlusTreeNode node)
//            throws IOException{
//
//        node.print();
//        if(node.isLeafNode)
//            return;
//        for(int i = 0; i < node.keyNum + 1; ++i){
//            BPlusTreeNode n = fm.readNode(node.pointers.get(i), this.ID);
//            printBPlusTree(n);
//        }
//    }
//
//    private BPlusTreeLeafNode getMostLeftLeafNode(BPlusTreeNode node)
//            throws IOException{
//        if(node.isLeafNode)
//            return (BPlusTreeLeafNode) node;
//        node = fm.readNode(node.pointers.get(0), this.ID);
//        return getMostLeftLeafNode(node);
//    }
//
//
//    private BPlusTreeLeafNode findLeafNodeToInsert(ArrayList key)
//            throws BPlusTreeException, IOException{
//
//        BPlusTreeNode node = this.root;
//        while (!node.isLeafNode) {
//            node = fm.readNode(node.pointers.get(node.search(key)), this.ID);
//        }
//        return (BPlusTreeLeafNode)node;
//    }
//
//
//}


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

        for (int i = 0; i < leaf.keyNum; ++i)
            if(leaf.compare(key, leaf.keys.get(i)) == 0)
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

//    public void print(){
//        System.out.println(this.root.keys);
//        printNode(this.root);
//    }
//
//    private void printNode(BPlusTreeNode node){
//        if(node.isLeafNode)
//            return;
//        for(int i = 0; i < node.keyNum + 1; ++i)
//            System.out.print(node.pointers.get(i).keys);
//        System.out.println();
//
//        for(int i = 0; i < node.keyNum + 1; ++i)
//            printNode(node.pointers.get(i));
//    }

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


