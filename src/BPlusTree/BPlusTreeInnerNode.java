package BPlusTree;

import Utils.FileManager;

import java.util.ArrayList;

public class BPlusTreeInnerNode extends BPlusTreeNode {

    public BPlusTreeInnerNode(ArrayList<ArrayList> keys, ArrayList<Integer> pointers, int parent,
                             int leftSibling, int rightSibling, int keyNum, int location, boolean isLeafNode){
        super(keys, pointers, parent, leftSibling, rightSibling, keyNum, location, isLeafNode);
    }

    public BPlusTreeInnerNode(FileManager fm){
        super(fm);
    }

    public int search(ArrayList key)
            throws BPlusTreeException{

        int i = 0;
        for(i = 0; i < this.keys.size(); ++i){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == 0)
                return i + 1;
            else if(cmp < 0)
                return i;
        }

        return i;
    }

    protected BPlusTreeNode pushUpKey(FileManager fm, ArrayList key,
                                      BPlusTreeNode leftChild, BPlusTreeNode rightNode)
            throws BPlusTreeException{

        int i = this.search(key);
        this.insertAt(i, key, leftChild, rightNode);
        fm.updateNode(this);

        if(this.keyNum > BPlusTree.ORDER)
            return this.dealOverflow(fm);
        else{
            if(this.parent == -1)
                return null;
            else
                return fm.readNode(this.parent);
        }

    }

    protected BPlusTreeNode split(FileManager fm) {
        int midIndex = this.keyNum / 2;

        BPlusTreeInnerNode newRNode = new BPlusTreeInnerNode(fm);
        for (int i = midIndex + 1; i < this.keyNum; ++i) {
            newRNode.keys.add(this.keys.get(i));
        }

        for (int i = midIndex + 1; i < this.keyNum; ++i)
            this.keys.remove(midIndex + 1);

        for (int i = midIndex + 1; i <= this.keyNum; ++i) {
            newRNode.pointers.add(this.pointers.get(i));

            BPlusTreeNode node = fm.readNode(this.pointers.get(i));
            node.setParent(fm, newRNode.location);
        }

        for(int i = midIndex + 1; i <= this.keyNum; ++i)
            this.pointers.remove(midIndex + 1);

        this.keys.remove(midIndex);
        newRNode.keyNum = this.keyNum - midIndex - 1;
        this.keyNum = midIndex;

        fm.updateNode(newRNode);
        fm.updateNode(this);

        return newRNode;
    }

    private void insertAt(int index, ArrayList key, BPlusTreeNode leftChild, BPlusTreeNode rightChild){
        this.keys.add(index, key);
        this.pointers.add(index, leftChild.location);
        this.pointers.add(index + 1, rightChild.location);
        ++this.keyNum;
    }

}
