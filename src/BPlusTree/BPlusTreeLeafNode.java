package BPlusTree;

import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;


public class BPlusTreeLeafNode extends BPlusTreeNode {

    public BPlusTreeLeafNode(ArrayList<ArrayList> keys, ArrayList<Integer> pointers, int parent,
                             int leftSibling, int rightSibling, int keyNum, int location,
                             boolean isLeafNode, int id){
        super(keys, pointers, parent, leftSibling, rightSibling, keyNum, location, isLeafNode, id);
    }

    BPlusTreeLeafNode(FileManager fm, int id) throws IOException {
        super(fm, id, true);
    }

    public int search(ArrayList key)
            throws BPlusTreeException {

        for(int i = 0; i < this.keys.size(); ++i){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == 2)
                throw new BPlusTreeException("key null");
            if(cmp == 0)
                return i;
            else if(cmp < 0)
                return -1;
        }

        return -1;
    }

    public boolean contains(ArrayList key)
            throws BPlusTreeException{
        for(ArrayList tkey: this.keys){
            int cmp = this.compare(key, tkey);
            if(cmp == 2)
                throw new BPlusTreeException("key null");
            if(cmp == 0)
                return true;

        }
        return false;
    }

    protected BPlusTreeNode split(FileManager fm) throws IOException{
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
            throws BPlusTreeException, IOException{

        int i = 0;
        while(i < this.keyNum){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == 2)
                throw new BPlusTreeException("key null");
            if(cmp <= 0)
                break;
            ++i;
        }
        this.insertAt(i, key, offset);
    }

    private void insertAt(int i, ArrayList key, int offset){
        this.keys.add(i, key);
        this.pointers.add(i, offset);

        ++this.keyNum;
    }

    public boolean delete(FileManager fm, ArrayList key)
            throws BPlusTreeException, IOException {
        int index = 0;
        for(index = 0; index < this.keyNum; ++index){
            int cmp = this.compare(key, this.keys.get(index));
            if(cmp == 2)
                throw new BPlusTreeException("key null");
            if(cmp == 0)
                break;
        }

        if (index == this.keyNum)
            return false;

        this.deleteAt(index);
        fm.updateNode(this);
        return true;
    }

    private void deleteAt(int index) {
        this.keys.remove(index);
        this.pointers.remove(index);
        --this.keyNum;
    }

    protected BPlusTreeNode pushUpKey(FileManager fm, ArrayList key,
                                      BPlusTreeNode leftChild, BPlusTreeNode rightNode)
            throws BPlusTreeException{

        throw new BPlusTreeException("pushUpKey in leaf node");
    }

    protected void processChildrenTransfer(FileManager fm, BPlusTreeNode borrower, BPlusTreeNode lender, int borrowIndex)
            throws IOException, BPlusTreeException{
        throw new BPlusTreeException("processChildrenTransfer in leaf node");
    }

    protected BPlusTreeNode processChildrenFusion(FileManager fm, BPlusTreeNode leftChild, BPlusTreeNode rightChild)
            throws BPlusTreeException{
        throw new BPlusTreeException("processChildrenFusion in leaf node");
    }

    protected void fusionWithSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode rightSibling)
            throws IOException {
        BPlusTreeLeafNode siblingLeaf = (BPlusTreeLeafNode)rightSibling;

        int j = this.keyNum;
        for (int i = 0; i < siblingLeaf.keyNum; ++i) {
            while(this.keys.size() < j + i + 1)
                this.keys.add(new ArrayList());
            while(this.pointers.size() < j + i + 1)
                this.pointers.add(-1);
            this.keys.set(j + i, siblingLeaf.keys.get(i));
            this.pointers.set(j + i, siblingLeaf.pointers.get(i));
        }
        this.keyNum += siblingLeaf.keyNum;


        this.rightSibling = siblingLeaf.rightSibling;
        if (siblingLeaf.rightSibling != -1){
            BPlusTreeNode node = fm.readNode(siblingLeaf.rightSibling, this.id);
            node.leftSibling = this.location;
            fm.updateNode(node);
        }

        fm.updateNode(this);
    }

    protected ArrayList transferFromSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode sibling, int borrowIndex)
            throws BPlusTreeException, IOException {
        BPlusTreeLeafNode siblingNode = (BPlusTreeLeafNode) sibling;

        this.insertKey(fm, siblingNode.keys.get(borrowIndex), siblingNode.pointers.get(borrowIndex));
        siblingNode.deleteAt(borrowIndex);

        return borrowIndex == 0 ? sibling.keys.get(0) : this.keys.get(0);
    }

}