package BPlusTree;

import Utils.FileManager;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.util.ArrayList;

public class BPlusTree {

    private FileManager fm;
    private BPlusTreeNode root;
    private int ID;
    protected static final int ORDER = 3;

    public BPlusTree(FileManager file, int offset, boolean is_empty, int id) throws IOException {

        this.fm = file;
        this.ID = id;

        if(is_empty){
            this.root = new BPlusTreeLeafNode(fm, ID);
        }else {
            this.root = fm.readNode(offset, this.ID);
        }
    }

    public void insert(ArrayList key, ArrayList value)
            throws BPlusTreeException, IOException{

        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);
        int offset = fm.writeValue(value);
        leaf.insertKey(fm, key, offset);

        if(leaf.keyNum > ORDER){
            BPlusTreeNode node = leaf.dealOverflow(fm);
            if(node != null){
                this.root = node;
                this.fm.updateRoot(ID, this.root.location);
            }
        }
    }

    //return data offset
    public int search(ArrayList key)
            throws BPlusTreeException{
        BPlusTreeLeafNode leaf = this.findLeafNodeToInsert(key);

        int index = leaf.search(key);
        if(index == -1)
            return -1;
        else
            return leaf.pointers.get(index);
    }

    private BPlusTreeLeafNode findLeafNodeToInsert(ArrayList key)
            throws BPlusTreeException, IOException {

        BPlusTreeNode node = this.root;
        while (!node.isLeafNode) {
            node = fm.readNode(node.pointers.get(node.search(key)), this.ID);
        }

        return (BPlusTreeLeafNode)node;
    }

}
