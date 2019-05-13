package BPlusTree;

import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;

public class BPlusTreeInnerNode extends BPlusTreeNode {

    public BPlusTreeInnerNode(ArrayList<ArrayList> keys, ArrayList<Integer> pointers, int parent,
                              int leftSibling, int rightSibling, int keyNum, int location,
                              boolean isLeafNode, int id){
        super(keys, pointers, parent, leftSibling, rightSibling, keyNum, location, isLeafNode, id);
    }

    public BPlusTreeInnerNode(FileManager fm, int id)throws IOException {
        super(fm, id, false);
    }

    public int search(ArrayList key)
            throws BPlusTreeException {

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
            throws BPlusTreeException, IOException{

        int i = this.search(key);
        this.insertAt(i, key, leftChild, rightNode);

        if(this.keyNum > BPlusTree.ORDER)
            return this.dealOverflow(fm);
        else{
            fm.updateNode(this);
            if(this.parent == -1)
                return this;
            else
                return null;
        }

    }

    protected BPlusTreeNode split(FileManager fm) throws IOException{
        int midIndex = this.keyNum / 2;

        BPlusTreeInnerNode newRNode = new BPlusTreeInnerNode(fm, this.id);
        for (int i = midIndex + 1; i < this.keyNum; ++i) {
            newRNode.keys.add(this.keys.get(i));
        }

        for (int i = midIndex + 1; i < this.keyNum; ++i)
            this.keys.remove(midIndex + 1);

        for (int i = midIndex + 1; i <= this.keyNum; ++i) {
            newRNode.pointers.add(this.pointers.get(i));

            BPlusTreeNode node = fm.readNode(this.pointers.get(i), this.id);
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
