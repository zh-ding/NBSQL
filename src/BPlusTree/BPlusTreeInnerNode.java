package BPlusTree;

//public class BPlusTreeInnerNode extends BPlusTreeNode {
//
//    public BPlusTreeInnerNode(ArrayList<ArrayList> keys, ArrayList<Integer> pointers, int parent,
//                              int leftSibling, int rightSibling, int keyNum, int location,
//                              boolean isLeafNode, int id){
//        super(keys, pointers, parent, leftSibling, rightSibling, keyNum, location, isLeafNode, id);
//    }
//
//    public BPlusTreeInnerNode(FileManager fm, int id)throws IOException {
//        super(fm, id, false);
//    }
//
//    public int search(ArrayList key)
//            throws BPlusTreeException {
//
//        int i = 0;
//        for(i = 0; i < this.keys.size(); ++i){
//            int cmp = this.compare(key, this.keys.get(i));
//            if(cmp == 2)
//                throw new BPlusTreeException("key null");
//            if(cmp == 0)
//                return i + 1;
//            else if(cmp < 0)
//                return i;
//        }
//
//        return i;
//    }
//
//
//    protected BPlusTreeNode pushUpKey(FileManager fm, ArrayList key,
//                                      BPlusTreeNode leftChild, BPlusTreeNode rightNode)
//            throws BPlusTreeException, IOException{
//
//        int i = this.search(key);
//        this.insertAt(fm, i, key, leftChild, rightNode);
//
//        if(this.keyNum > BPlusTree.ORDER)
//            return this.dealOverflow(fm);
//        else{
//            fm.updateNode(this);
//            if(this.parent == -1)
//                return this;
//            else
//                return null;
//        }
//
//    }
//
//    protected BPlusTreeNode split(FileManager fm) throws IOException{
//        int midIndex = this.keyNum / 2;
//
//        BPlusTreeInnerNode newRNode = new BPlusTreeInnerNode(fm, this.id);
//        for (int i = midIndex + 1; i < this.keyNum; ++i) {
//            newRNode.keys.add(this.keys.get(i));
//        }
//
//        for (int i = midIndex + 1; i < this.keyNum; ++i)
//            this.keys.remove(midIndex + 1);
//
//        for (int i = midIndex + 1; i <= this.keyNum; ++i) {
//            newRNode.pointers.add(this.pointers.get(i));
//
//            BPlusTreeNode node = fm.readNode(this.pointers.get(i), this.id);
//            node.setParent(fm, newRNode.location);
//        }
//
//        for(int i = midIndex + 1; i <= this.keyNum; ++i)
//            this.pointers.remove(midIndex + 1);
//
//        this.keys.remove(midIndex);
//        newRNode.keyNum = this.keyNum - midIndex - 1;
//        this.keyNum = midIndex;
//
//        fm.updateNode(newRNode);
//        fm.updateNode(this);
//
//        return newRNode;
//    }
//
//    private void insertAt(FileManager fm, int index, ArrayList key, BPlusTreeNode leftChild, BPlusTreeNode rightChild)
//            throws IOException{
//        if(this.keys.size() > index)
//            this.keys.remove(index);
//        this.keys.add(index, key);
//        if(this.pointers.size() > index)
//            this.pointers.remove(index);
//        this.pointers.add(index, leftChild.location);
//        BPlusTreeNode node = fm.readNode(leftChild.location, this.id);
//        if(node != null) {
//            node.parent = this.location;
//            fm.updateNode(node);
//        }
//        this.pointers.add(index + 1, rightChild.location);
//        node = fm.readNode(rightChild.location, this.id);
//        if(node != null) {
//            node.parent = this.location;
//            fm.updateNode(node);
//        }
//        ++this.keyNum;
//    }
//
//    protected void processChildrenTransfer(FileManager fm, BPlusTreeNode borrower, BPlusTreeNode lender, int borrowIndex)
//            throws IOException, BPlusTreeException{
//        int borrowerChildIndex = 0;
//        while (borrowerChildIndex < this.keyNum + 1 && this.pointers.get(borrowerChildIndex) != borrower.location)
//            ++borrowerChildIndex;
//
//        if (borrowIndex == 0) {
//            ArrayList upKey = borrower.transferFromSibling(fm, this.keys.get(borrowerChildIndex), lender, borrowIndex);
//            if(borrowerChildIndex < this.keyNum)
//                this.keys.set(borrowerChildIndex, upKey);
//            else
//                throw new BPlusTreeException("borrowIndex error");
//        }
//        else {
//            ArrayList upKey = borrower.transferFromSibling(fm, this.keys.get(borrowerChildIndex - 1), lender, borrowIndex);
//            if(borrowerChildIndex < this.keyNum)
//                this.keys.set(borrowerChildIndex - 1, upKey);
//            else
//                throw new BPlusTreeException("borrowIndex error");
//        }
//        fm.updateNode(this);
//    }
//
//    private BPlusTreeNode getChild(FileManager fm, int index)
//            throws IOException{
//        return fm.readNode(this.pointers.get(index), this.id);
//    }
//
//    protected BPlusTreeNode processChildrenFusion(FileManager fm, BPlusTreeNode leftChild, BPlusTreeNode rightChild)
//            throws IOException, BPlusTreeException{
//
//        int index = 0;
//        while (index < this.keyNum && this.pointers.get(index) != leftChild.location)
//            ++index;
//        ArrayList sinkKey = this.keys.get(index);
//
//       leftChild.fusionWithSibling(fm, sinkKey, rightChild);
//
//        this.deleteAt(index);
//        fm.updateNode(this);
//
//        if (this.keyNum < Math.floor(BPlusTree.ORDER / 2.0) - 1) {
//            if (this.parent == -1) {
//                if (this.keyNum == 0) {
//                    leftChild.parent = -1;
//                    fm.updateNode(leftChild);
//                    return leftChild;
//                }
//                else {
//                    return null;
//                }
//            }
//
//            return this.dealUnderflow(fm);
//        }
//
//        return null;
//    }
//
//    private void deleteAt(int index) {
//        this.keys.remove(index);
//        this.pointers.remove(index + 1);
//        --this.keyNum;
//    }
//
//    protected void fusionWithSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode rightSibling)
//            throws IOException, BPlusTreeException {
//        BPlusTreeInnerNode rightSiblingNode = (BPlusTreeInnerNode)rightSibling;
//
//        if (this.keyNum + 1 + rightSibling.keyNum > BPlusTree.ORDER)
//            return;
//
//        int j = this.keyNum;
//        while(this.keys.size() < j + 1)this.keys.add(new ArrayList());
//        this.keys.set(j++, sinkKey);
//
//        for (int i = 0; i < rightSiblingNode.keyNum; ++i) {
//            while(this.keys.size() < j + i + 1)this.keys.add(new ArrayList());
//            this.keys.set(j + i, rightSiblingNode.keys.get(i));
//        }
//        for (int i = 0; i < rightSiblingNode.keyNum + 1; ++i) {
//            while(this.pointers.size() < j + i + 1)this.pointers.add(-1);
//            this.pointers.set(j + i, rightSiblingNode.pointers.get(i));
//            if(rightSiblingNode.pointers.get(i) == -1)
//                continue;
//            BPlusTreeNode node = fm.readNode(rightSibling.pointers.get(i), this.id);
//            node.parent = this.location;
//            fm.updateNode(node);
//        }
//        this.keyNum += 1 + rightSiblingNode.keyNum;
//
//        this.rightSibling = rightSiblingNode.rightSibling;
//        fm.updateNode(this);
//        if (rightSiblingNode.rightSibling != -1){
//            BPlusTreeNode node = fm.readNode(rightSiblingNode.rightSibling, this.id);
//            node.leftSibling = this.location;
//            fm.updateNode(node);
//        }
//    }
//
//    protected ArrayList transferFromSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode sibling, int borrowIndex)
//            throws IOException {
//        BPlusTreeInnerNode siblingNode = (BPlusTreeInnerNode) sibling;
//
//        ArrayList upKey = null;
//        if (borrowIndex == 0) {
//            while(this.keys.size() < this.keyNum + 1)this.keys.add(new ArrayList());
//            this.keys.set(this.keyNum, sinkKey);
//            while(this.pointers.size() < this.keyNum + 2)this.pointers.add(-1);
//            this.pointers.set(this.keyNum + 1, siblingNode.getChild(fm, borrowIndex).location);
//            BPlusTreeNode node = fm.readNode(siblingNode.getChild(fm, borrowIndex).location, this.id);
//            if(node != null){
//                node.parent = this.location;
//                fm.updateNode(node);
//            }
//
//            this.keyNum += 1;
//
//            upKey = siblingNode.keys.get(0);
//            siblingNode.deleteAt(borrowIndex);
//        }
//        else {
//            this.insertAt(fm, 0, sinkKey, siblingNode.getChild(fm,borrowIndex + 1), this.getChild(fm,0));
//            upKey = siblingNode.keys.get(borrowIndex);
//            siblingNode.deleteAt(borrowIndex);
//        }
//
//        fm.updateNode(this);
//        fm.updateNode(siblingNode);
//
//        return upKey;
//    }
//}

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

    BPlusTreeInnerNode(FileManager fm, int id)throws IOException {
        super(fm, id, false);
    }

    public BPlusTreeNode search(FileManager fm, ArrayList key)throws IOException, BPlusTreeException{
        for(int i = 0; i < this.keyNum; ++i){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == -1)
                return fm.readNode(this.pointers.get(i), this.id);
        }
        return fm.readNode(this.pointers.get(this.keyNum), this.id);
    }

    public BPlusTreeNode insert(FileManager fm, ArrayList key, BPlusTreeNode node)throws BPlusTreeException, IOException {

        int i = 0;

        for(i = 0; i < this.keyNum; ++i){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == -1)
                break;
            else if(cmp == 2)
                throw new BPlusTreeException("key cannot be null");
        }

        this.keys.add(i, key);
        this.pointers.add(i + 1, node.location);
        ++this.keyNum;

        fm.updateNode(this);

        if(this.keyNum == BPlusTree.M)
            return this.split(fm);
        return null;
    }

    public BPlusTreeNode split(FileManager fm)throws BPlusTreeException, IOException{
        assert this.keyNum == BPlusTree.M;



        BPlusTreeInnerNode newNode = new BPlusTreeInnerNode(fm, this.id);
        int m = (int)Math.ceil(BPlusTree.M / 2.0);
        ArrayList key = this.keys.get(m);

        for(int i = m + 1; i < this.keyNum; ++i){
            newNode.keys.add(this.keys.get(i));
            newNode.pointers.add(this.pointers.get(i));

            BPlusTreeNode node = fm.readNode(this.pointers.get(i), this.id);
            node.parent = newNode.location;
            fm.updateNode(node);
        }

        newNode.pointers.add(this.pointers.get(this.keyNum));

        BPlusTreeNode node = fm.readNode(this.pointers.get(this.keyNum), this.id);
        node.parent = newNode.location;
        fm.updateNode(node);

        newNode.keyNum = this.keyNum - m - 1;

        for(int i = m; i < this.keyNum; ++i){
            this.keys.remove(m);
            this.pointers.remove(m + 1);
        }
        this.keyNum = m;

        newNode.parent = this.parent;

        fm.updateNode(this);
        fm.updateNode(newNode);

        if(this.parent == -1){
            BPlusTreeInnerNode newRoot = new BPlusTreeInnerNode(fm, this.id);
            newRoot.keys.add(key);
            newRoot.keyNum = 1;
            newRoot.pointers.add(this.location);
            newRoot.pointers.add(newNode.location);
            this.parent = newRoot.location;
            newNode.parent = newRoot.location;

            fm.updateNode(this);
            fm.updateNode(newNode);
            fm.updateNode(newRoot);

            return newRoot;
        }

        BPlusTreeInnerNode parent = (BPlusTreeInnerNode) fm.readNode(this.parent, this.id);
        return parent.insert(fm, key, newNode);
    }

    public BPlusTreeNode delete(FileManager fm, int index, int pindex)throws IOException, BPlusTreeException{

        this.keys.remove(index + 0);
        this.pointers.remove(pindex);
        --this.keyNum;

        if(this.parent == -1 && this.keyNum == 0)
            return fm.readNode(this.pointers.get(0), this.id);

        if(this.keyNum < (int)Math.ceil((BPlusTree.M - 1) / 2.0) - 1)
            return this.combine(fm);
        return null;
    }

    public BPlusTreeNode combine(FileManager fm)throws IOException, BPlusTreeException{
        if(this.parent == -1)
            return null;

        int i;

        BPlusTreeInnerNode parent = (BPlusTreeInnerNode)fm.readNode(this.parent, this.id);

        for(i = 0; i < parent.keyNum + 1; ++i) {
            if (parent.pointers.get(i) == this.location)
                break;
        }

        BPlusTreeInnerNode left = null;
        if(i - 1 >= 0)
            left = (BPlusTreeInnerNode)fm.readNode(parent.pointers.get(i-1), this.id);
        BPlusTreeInnerNode right = null;
        if(i + 1 < parent.keyNum + 1)
            right = (BPlusTreeInnerNode)fm.readNode(parent.pointers.get(i+1), this.id);


        if(left != null && left.keyNum > (int)Math.ceil((BPlusTree.M - 1) / 2.0) - 1){

            this.keys.add(0, parent.keys.get(i-1));
            this.pointers.add(0, left.pointers.get(left.keyNum));
            BPlusTreeNode node = fm.readNode(left.pointers.get(left.keyNum), this.id);
            node.parent = this.location;
            ++this.keyNum;

            parent.keys.set(i - 1, left.keys.get(left.keyNum - 1));

            left.keys.remove(left.keyNum - 1);
            left.pointers.remove(left.keyNum);
            --left.keyNum;

            fm.updateNode(left);
            fm.updateNode(this);
            fm.updateNode(node);
            fm.updateNode(parent);
            return null;

        }else if(right != null && right.keyNum > (int)Math.ceil((BPlusTree.M - 1) / 2.0) - 1){

            this.keys.add(this.keyNum, parent.keys.get(i));
            this.pointers.add(this.keyNum + 1, right.pointers.get(0));
            BPlusTreeNode node = fm.readNode(right.pointers.get(0), this.id);
            node.parent = this.location;
            ++this.keyNum;

            parent.keys.set(i, right.keys.get(0));

            right.keys.remove(0);
            right.pointers.remove(0);
            --right.keyNum;

            fm.updateNode(right);
            fm.updateNode(this);
            fm.updateNode(node);
            fm.updateNode(parent);

            return null;
        }

        if(left != null){

//            int k;
//            for(k = 0; k < parent.keyNum; ++k){
//                int cmp = this.compare(parent.keys.get(k), left.keys.get(0));
//
//                if(cmp == 1)
//                    break;
//            }

            //assert k > 0;

            this.keys.add(0, parent.keys.get(i - 1));

            for(int j = 0; j < left.keyNum; ++j){
                this.keys.add(j, left.keys.get(j));
                this.pointers.add(j, left.pointers.get(j));
                BPlusTreeNode node = fm.readNode(left.pointers.get(j), this.id);
                node.parent = this.location;
                fm.updateNode(node);
            }

            this.pointers.add(left.keyNum, left.pointers.get(left.keyNum));

            BPlusTreeNode node = fm.readNode(left.pointers.get(left.keyNum), this.id);
            node.parent = this.location;

            this.keyNum += 1 + left.keyNum;

            //delete left


            fm.updateNode(this);
            fm.updateNode(node);

            return parent.delete(fm, i - 1, i - 1);

        }


//        int k;
//        for(k = 0; k < parent.keyNum; ++k){
//            int cmp = this.compare(parent.keys.get(k), right.keys.get(0));
//            if(cmp == 1)
//                break;
//        }

        //assert k < this.parent.keyNum;

        this.keys.add(parent.keys.get(i));

        for(int j = 0; j < right.keyNum; ++j){
            this.keys.add(right.keys.get(j));
            this.pointers.add(right.pointers.get(j));
            BPlusTreeNode node = fm.readNode(right.pointers.get(j), this.id);
            node.parent = this.location;
            fm.updateNode(node);
        }

        this.pointers.add(right.pointers.get(right.keyNum));

        BPlusTreeNode node = fm.readNode(right.pointers.get(right.keyNum), this.id);
        node.parent = this.location;

        this.keyNum += 1 + right.keyNum;

        //delete right

        fm.updateNode(this);
        fm.updateNode(node);

        return parent.delete(fm, i, i + 1);
    }
}
