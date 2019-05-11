package BPlusTree;

import Utils.FileManager;

import java.util.ArrayList;

public class BPlusTreeLeafNode extends BPlusTreeNode {

    public BPlusTreeLeafNode(ArrayList<ArrayList> keys, ArrayList<Integer> pointers, int parent,
                             int leftSibling, int rightSibling, int keyNum, int location,
                             boolean isLeafNode, int id){
        super(keys, pointers, parent, leftSibling, rightSibling, keyNum, location, isLeafNode, id);
    }

    BPlusTreeLeafNode(FileManager fm, int id){
        super(fm, id);
    }

    public int search(ArrayList key)
            throws BPlusTreeException{

        for(int i = 0; i < this.keys.size(); ++i){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == 0)
                return i;
            else if(cmp < 0)
                return -1;
        }

        return -1;
    }

    protected BPlusTreeNode split(FileManager fm) {
        int midIndex = this.keyNum / 2;

        BPlusTreeLeafNode newRNode = new BPlusTreeLeafNode(fm, this.id);
        for (int i = midIndex; i < this.keyNum; ++i) {
            newRNode.keys.add(this.keys.get(i));
            newRNode.pointers.add(this.pointers.get(i));
        }

        for (int i = midIndex; i < this.keyNum; ++i){
            this.keys.remove(midIndex);
            this.pointers.remove(midIndex);
        }
        newRNode.keyNum = this.keyNum - midIndex;
        this.keyNum = midIndex;

        fm.updateNode(newRNode);
        fm.updateNode(this);

        return newRNode;
    }

    public void insertKey(FileManager fm, ArrayList key, int offset)
            throws BPlusTreeException{

        int i = 0;
        while (i < this.keyNum && this.compare(key, this.keys.get(i)) > 0)
            ++i;
        this.insertAt(i, key, offset);

        fm.updateNode(this);
    }

    private void insertAt(int i, ArrayList key, int offset){
        this.keys.add(i, key);
        this.pointers.add(i, offset);

        ++this.keyNum;
    }

    protected BPlusTreeNode pushUpKey(FileManager fm, ArrayList key,
                                      BPlusTreeNode leftChild, BPlusTreeNode rightNode)
            throws BPlusTreeException{

        throw new BPlusTreeException("pushUpKey in leaf node");
    }

}