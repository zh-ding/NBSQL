package BPlusTree;

import Utils.FileManager;

import java.io.File;
import java.util.ArrayList;

public abstract class BPlusTreeNode{

    protected ArrayList<ArrayList> keys;
    protected ArrayList<Integer> pointers;
    protected int parent;
    protected int leftSibling;
    protected int rightSibling;
    public boolean isLeafNode;
    protected int keyNum;
    protected int location;

    BPlusTreeNode(FileManager fm){
        this.keys = new ArrayList<>();
        this.pointers = new ArrayList<>();
        parent = -1;
        leftSibling = -1;
        rightSibling = -1;
        this.location = fm.writeNewNode();
    }

    public void setParent(FileManager fm, int offset){
        this.parent = offset;
        fm.updateNode(this);
    }

    public void setLeftSibling(FileManager fm, int offset){
        this.leftSibling = offset;
        fm.updateNode(this);
    }

    public void setRightSibling(FileManager fm, int offset){
        this.rightSibling = offset;
        fm.updateNode(this);
    }

    public abstract int search(ArrayList key) throws BPlusTreeException;

    protected abstract BPlusTreeNode split(FileManager fm);

    protected abstract BPlusTreeNode pushUpKey(FileManager fm, ArrayList key,
                                               BPlusTreeNode leftChild, BPlusTreeNode rightNode)
            throws BPlusTreeException;

    public BPlusTreeNode dealOverflow(FileManager fm)
            throws BPlusTreeException{

        int midIndex = this.keyNum / 2;
        ArrayList upKey = this.keys.get(midIndex);

        BPlusTreeNode newRNode = this.split(fm);

        if (this.parent == -1) {
            BPlusTreeInnerNode node = new BPlusTreeInnerNode(fm);
            this.setParent(fm, node.location);
        }
        newRNode.parent = this.parent;

        newRNode.setLeftSibling(fm, this.location);
        newRNode.setRightSibling(fm, this.rightSibling);
        if (this.rightSibling != -1){
            BPlusTreeNode node = fm.readNode(this.rightSibling);
            node.setLeftSibling(fm, newRNode.location);
        }
        this.setRightSibling(fm, newRNode.location);

        BPlusTreeNode node = fm.readNode(this.parent);
        return node.pushUpKey(fm, upKey, this, newRNode);
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