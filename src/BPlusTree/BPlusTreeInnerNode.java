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
            if(cmp == 2)
                throw new BPlusTreeException("key null");
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
        this.insertAt(fm, i, key, leftChild, rightNode);

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

    private void insertAt(FileManager fm, int index, ArrayList key, BPlusTreeNode leftChild, BPlusTreeNode rightChild)
            throws IOException{
        if(this.keys.size() > index)
            this.keys.remove(index);
        this.keys.add(index, key);
        if(this.pointers.size() > index)
            this.pointers.remove(index);
        this.pointers.add(index, leftChild.location);
        BPlusTreeNode node = fm.readNode(leftChild.location, this.id);
        if(node != null) {
            node.parent = this.location;
            fm.updateNode(node);
        }
        this.pointers.add(index + 1, rightChild.location);
        node = fm.readNode(rightChild.location, this.id);
        if(node != null) {
            node.parent = this.location;
            fm.updateNode(node);
        }
        ++this.keyNum;
    }

    protected void processChildrenTransfer(FileManager fm, BPlusTreeNode borrower, BPlusTreeNode lender, int borrowIndex)
            throws IOException, BPlusTreeException{
        int borrowerChildIndex = 0;
        while (borrowerChildIndex < this.keyNum + 1 && this.pointers.get(borrowerChildIndex) != borrower.location)
            ++borrowerChildIndex;

        if (borrowIndex == 0) {
            ArrayList upKey = borrower.transferFromSibling(fm, this.keys.get(borrowerChildIndex), lender, borrowIndex);
            if(borrowerChildIndex < this.keyNum)
                this.keys.set(borrowerChildIndex, upKey);
            else
                throw new BPlusTreeException("borrowIndex error");
        }
        else {
            ArrayList upKey = borrower.transferFromSibling(fm, this.keys.get(borrowerChildIndex - 1), lender, borrowIndex);
            if(borrowerChildIndex < this.keyNum)
                this.keys.set(borrowerChildIndex - 1, upKey);
            else
                throw new BPlusTreeException("borrowIndex error");
        }
        fm.updateNode(this);
    }

    private BPlusTreeNode getChild(FileManager fm, int index)
            throws IOException{
        return fm.readNode(this.pointers.get(index), this.id);
    }

    protected BPlusTreeNode processChildrenFusion(FileManager fm, BPlusTreeNode leftChild, BPlusTreeNode rightChild)
            throws IOException, BPlusTreeException{

        int index = 0;
        while (index < this.keyNum && this.pointers.get(index) != leftChild.location)
            ++index;
        ArrayList sinkKey = this.keys.get(index);

       leftChild.fusionWithSibling(fm, sinkKey, rightChild);

        this.deleteAt(index);
        fm.updateNode(this);

        if (this.keyNum < Math.floor(BPlusTree.ORDER / 2.0) - 1) {
            if (this.parent == -1) {
                if (this.keyNum == 0) {
                    leftChild.parent = -1;
                    fm.updateNode(leftChild);
                    return leftChild;
                }
                else {
                    return null;
                }
            }

            return this.dealUnderflow(fm);
        }

        return null;
    }

    private void deleteAt(int index) {
        this.keys.remove(index);
        this.pointers.remove(index + 1);
        --this.keyNum;
    }

    protected void fusionWithSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode rightSibling)
            throws IOException, BPlusTreeException {
        BPlusTreeInnerNode rightSiblingNode = (BPlusTreeInnerNode)rightSibling;

        if (this.keyNum + 1 + rightSibling.keyNum > BPlusTree.ORDER)
            return;

        int j = this.keyNum;
        while(this.keys.size() < j + 1)this.keys.add(new ArrayList());
        this.keys.set(j++, sinkKey);

        for (int i = 0; i < rightSiblingNode.keyNum; ++i) {
            while(this.keys.size() < j + i + 1)this.keys.add(new ArrayList());
            this.keys.set(j + i, rightSiblingNode.keys.get(i));
        }
        for (int i = 0; i < rightSiblingNode.keyNum + 1; ++i) {
            while(this.pointers.size() < j + i + 1)this.pointers.add(-1);
            this.pointers.set(j + i, rightSiblingNode.pointers.get(i));
            if(rightSiblingNode.pointers.get(i) == -1)
                continue;
            BPlusTreeNode node = fm.readNode(rightSibling.pointers.get(i), this.id);
            node.parent = this.location;
            fm.updateNode(node);
        }
        this.keyNum += 1 + rightSiblingNode.keyNum;

        this.rightSibling = rightSiblingNode.rightSibling;
        fm.updateNode(this);
        if (rightSiblingNode.rightSibling != -1){
            BPlusTreeNode node = fm.readNode(rightSiblingNode.rightSibling, this.id);
            node.leftSibling = this.location;
            fm.updateNode(node);
        }
    }

    protected ArrayList transferFromSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode sibling, int borrowIndex)
            throws IOException {
        BPlusTreeInnerNode siblingNode = (BPlusTreeInnerNode) sibling;

        ArrayList upKey = null;
        if (borrowIndex == 0) {
            while(this.keys.size() < this.keyNum + 1)this.keys.add(new ArrayList());
            this.keys.set(this.keyNum, sinkKey);
            while(this.pointers.size() < this.keyNum + 2)this.pointers.add(-1);
            this.pointers.set(this.keyNum + 1, siblingNode.getChild(fm, borrowIndex).location);
            BPlusTreeNode node = fm.readNode(siblingNode.getChild(fm, borrowIndex).location, this.id);
            if(node != null){
                node.parent = this.location;
                fm.updateNode(node);
            }

            this.keyNum += 1;

            upKey = siblingNode.keys.get(0);
            siblingNode.deleteAt(borrowIndex);
        }
        else {
            this.insertAt(fm, 0, sinkKey, siblingNode.getChild(fm,borrowIndex + 1), this.getChild(fm,0));
            upKey = siblingNode.keys.get(borrowIndex);
            siblingNode.deleteAt(borrowIndex);
        }

        fm.updateNode(this);
        fm.updateNode(siblingNode);

        return upKey;
    }
}
