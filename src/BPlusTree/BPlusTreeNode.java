package BPlusTree;

import Exceptions.BPlusTreeException;
import Utils.FileManager;

import java.io.IOException;
import java.util.ArrayList;

public abstract class BPlusTreeNode {

    public boolean isLeafNode;
    public ArrayList<ArrayList> keys;
    public int keyNum;
    public ArrayList<Integer> pointers;
    public int parent;
    public int leftSibling;
    public int rightSibling;
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

    abstract public BPlusTreeNode split(FileManager fm)throws IOException, BPlusTreeException;

    abstract public BPlusTreeNode search(FileManager fm, ArrayList key)throws IOException, BPlusTreeException;

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
    2: null
    */
    public int compare(ArrayList key1, ArrayList key2)
            throws BPlusTreeException {
        int len = key1.size();
        if(len != key2.size())
            throw new BPlusTreeException("cmp: key size not match");
        for(int i = 0; i < len; ++i){
            Object k1 = key1.get(i);
            Object k2 = key2.get(i);
            if(k1 == null || k2 == null)
                return 2;
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
