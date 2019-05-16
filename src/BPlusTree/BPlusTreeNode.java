package BPlusTree;

import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;

public abstract class BPlusTreeNode{

    public ArrayList<ArrayList> keys;
    public ArrayList<Integer> pointers;
    public int parent;
    public int leftSibling;
    public int rightSibling;
    public boolean isLeafNode;
    public int keyNum;
    public int location;
    public int id;


    public BPlusTreeNode(ArrayList<ArrayList> keys, ArrayList<Integer> pointers, int parent,
                         int leftSibling, int rightSibling, int keyNum, int location,
                         boolean isLeafNode, int id){
        this.keys = keys;
        this.pointers = pointers;
        this.parent = parent;
        this.leftSibling = leftSibling;
        this.rightSibling = rightSibling;
        this.keyNum = keyNum;
        this.location = location;
        this.isLeafNode = isLeafNode;
        this.id = id;
    }

    BPlusTreeNode(FileManager fm, int id, boolean isLeafNode) throws IOException {
        this.keys = new ArrayList<>();
        this.pointers = new ArrayList<>();
        parent = -1;
        leftSibling = -1;
        rightSibling = -1;
        this.isLeafNode = isLeafNode;
        this.location = fm.writeNewNode(id, isLeafNode);
    }

    public void setParent(FileManager fm, int offset)throws IOException{
        this.parent = offset;
        fm.updateNode(this);
    }

    public void setLeftSibling(FileManager fm, int offset)throws IOException{
        this.leftSibling = offset;
        fm.updateNode(this);
    }

    public void setRightSibling(FileManager fm, int offset)throws IOException{
        this.rightSibling = offset;
        fm.updateNode(this);
    }

    public abstract int search(ArrayList key) throws BPlusTreeException;

    protected abstract BPlusTreeNode split(FileManager fm) throws IOException;

    protected abstract BPlusTreeNode pushUpKey(FileManager fm, ArrayList key,
                                               BPlusTreeNode leftChild, BPlusTreeNode rightNode)
            throws BPlusTreeException, IOException;

    public BPlusTreeNode dealOverflow(FileManager fm)
            throws BPlusTreeException, IOException{

        int midIndex = this.keyNum / 2;
        ArrayList upKey = this.keys.get(midIndex);

        BPlusTreeNode newRNode = this.split(fm);

        if (this.parent == -1) {
            BPlusTreeInnerNode node = new BPlusTreeInnerNode(fm, this.id);
            this.setParent(fm, node.location);
        }
        newRNode.parent = this.parent;

        newRNode.setLeftSibling(fm, this.location);
        newRNode.setRightSibling(fm, this.rightSibling);
        fm.updateNode(newRNode);
        if (this.rightSibling != -1){
            BPlusTreeNode node = fm.readNode(this.rightSibling, this.id);
            node.setLeftSibling(fm, newRNode.location);
            fm.updateNode(node);
        }
        this.setRightSibling(fm, newRNode.location);

        fm.updateNode(this);

        BPlusTreeNode node = fm.readNode(this.parent, this.id);
        return node.pushUpKey(fm, upKey, this, newRNode);
    }

    public BPlusTreeNode dealUnderflow(FileManager fm)
            throws IOException, BPlusTreeException {
        if (this.parent == -1)
            return null;

        BPlusTreeNode leftSibling = fm.readNode(this.leftSibling, this.id);
        BPlusTreeNode parent = fm.readNode(this.parent, this.id);
        if (leftSibling != null && leftSibling.keyNum > Math.ceil(BPlusTree.ORDER / 2.0)) {
            parent.processChildrenTransfer(fm, this, leftSibling, leftSibling.keyNum - 1);
            return null;
        }

        BPlusTreeNode rightSibling = fm.readNode(this.rightSibling, this.id);
        if (rightSibling != null && rightSibling.keyNum > Math.ceil(BPlusTree.ORDER / 2.0)) {
            parent.processChildrenTransfer(fm, this, rightSibling, 0);
            return null;
        }

        if (leftSibling != null) {
            return parent.processChildrenFusion(fm, leftSibling, this);
        }
        else {
            return parent.processChildrenFusion(fm,this, rightSibling);
        }
    }

    protected abstract void processChildrenTransfer(FileManager fm, BPlusTreeNode borrower, BPlusTreeNode lender, int borrowIndex)
            throws IOException, BPlusTreeException;


    protected abstract BPlusTreeNode processChildrenFusion(FileManager fm, BPlusTreeNode leftChild, BPlusTreeNode rightChild)
            throws IOException, BPlusTreeException;

    protected abstract void fusionWithSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode rightSibling)
            throws BPlusTreeException, IOException;

    protected abstract ArrayList transferFromSibling(FileManager fm, ArrayList sinkKey, BPlusTreeNode sibling, int borrowIndex)
            throws IOException, BPlusTreeException;


    public void print(){
        System.out.print(this.location);
        System.out.print(' ');
        if(isLeafNode)
            System.out.print("leaf");
        else
            System.out.print("inner");
        System.out.print(' ');
        System.out.print(this.keyNum);
        System.out.print(" [");
        for(ArrayList arr: this.keys){
            System.out.print('[');
            for(Object o:arr){
                System.out.print(o);
                System.out.print(',');
            }
            System.out.print("],");
        }
        System.out.print("] [");
        for(Integer offset: this.pointers){
            System.out.print(offset);
            System.out.print(',');
        }
        System.out.print("] ");
        System.out.print(this.parent);
        System.out.print(" ");
        System.out.print(this.leftSibling);
        System.out.print(" ");
        System.out.print(this.rightSibling);
        System.out.println();
    }

    /*
    0: key1 == key2
    -1: key1 < key2
    1: key1 > key2
    */
    public int compare(ArrayList key1, ArrayList key2)
            throws BPlusTreeException{
        int len = key1.size();
        if(len != key2.size())
            throw new BPlusTreeException("cmp: key size not match");
        for(int i = 0; i < len; ++i){
            Object k1 = key1.get(i);
            Object k2 = key2.get(i);
            if(k1 instanceof Integer && k2 instanceof Integer){
                if((Integer)k1 < (Integer)k2)
                    return -1;
                else if((Integer)k1 > (Integer)k2)
                    return 1;
            }else if(k1 instanceof Long && k2 instanceof Long){
                if((Long)k1 < (Long)k2)
                    return -1;
                else if((Long)k1 > (Long)k2)
                    return 1;
            }else if(k1 instanceof Float && k2 instanceof Float){
                if((Float)k1 < (Float) k2)
                    return -1;
                else if((Float)k1 > (Float) k2)
                    return 1;
            }else if(k1 instanceof Double && k2 instanceof Double){
                if((Double)k1 < (Double) k2)
                    return -1;
                else if((Double)k1 > (Double)k2)
                    return 1;
            }else if(k1 instanceof String && k2 instanceof String){
                if(((String)k1).compareTo((String)k2) < 0)
                    return -1;
                else if(((String)k1).compareTo((String)k2) > 0)
                    return 1;
            }else
                throw new BPlusTreeException("cmp: key type not match");
        }

        return 0;
    }
}