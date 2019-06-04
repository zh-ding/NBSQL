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

    public BPlusTreeNode search(FileManager fm, ArrayList key){
        System.out.println("search in leaf node");
        return null;
    }

    public BPlusTreeNode insert(FileManager fm, ArrayList key, int data)throws BPlusTreeException, IOException {

        int i = 0;

        for(i = 0; i < this.keyNum; ++i){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == -1)
                break;
            if(cmp == 2)
                throw new BPlusTreeException("key cannot be null");
        }

        this.keys.add(i, key);
        this.pointers.add(i, data);
        ++this.keyNum;
        fm.updateNode(this);

        if(this.keyNum == BPlusTree.M)
            return this.split(fm);

        return null;
    }

    public BPlusTreeNode delete(FileManager fm, ArrayList key)throws BPlusTreeException, IOException{
        int i;

        for(i = 0; i < this.keyNum; ++i){
            int cmp = this.compare(key, this.keys.get(i));
            if(cmp == 0)
                break;
        }

        if(i == this.keyNum)
            throw new BPlusTreeException("the key to be deleted doesn't exist");

        this.keys.remove(i);
        this.pointers.remove(i);
        --this.keyNum;
        fm.updateNode(this);


        if(this.keyNum < (int)Math.ceil((BPlusTree.M - 1) / 2.0) - 1)
            return this.combine(fm);

        return null;
    }


    public BPlusTreeNode split(FileManager fm)throws IOException, BPlusTreeException{
        assert this.keyNum == BPlusTree.M;

        BPlusTreeLeafNode newNode = new BPlusTreeLeafNode(fm, this.id);
        int m = (int)Math.ceil(BPlusTree.M / 2.0);
        for(int i = m; i < this.keyNum; ++i){
            newNode.keys.add(this.keys.get(i));
            newNode.pointers.add(this.pointers.get(i));
        }

        newNode.keyNum = this.keyNum - m;

        for(int i = m; i < this.keyNum; ++i){
            this.keys.remove(m);
            this.pointers.remove(m);
        }

        this.keyNum = m;

        newNode.parent = this.parent;
        newNode.leftSibling = this.location;
        newNode.rightSibling = this.rightSibling;
        if(this.rightSibling != -1) {
            BPlusTreeLeafNode node = (BPlusTreeLeafNode) fm.readNode(this.rightSibling, this.id);
            node.leftSibling = newNode.location;
            fm.updateNode(node);
        }
        this.rightSibling = newNode.location;

        fm.updateNode(this);
        fm.updateNode(newNode);

        if(this.parent == -1){
            BPlusTreeInnerNode newRoot = new BPlusTreeInnerNode(fm, this.id);
            newRoot.keys.add(newNode.keys.get(0));
            newRoot.keyNum = 1;
            newRoot.pointers.add(this.location);
            newRoot.pointers.add(newNode.location);
            this.parent = newRoot.location;
            newNode.parent = newRoot.location;

            fm.updateNode(newRoot);
            fm.updateNode(this);
            fm.updateNode(newNode);

            return newRoot;
        }


        BPlusTreeInnerNode node = (BPlusTreeInnerNode) fm.readNode(this.parent, this.id);
        return node.insert(fm, newNode.keys.get(0), newNode);
    }

    public BPlusTreeNode combine(FileManager fm)throws IOException, BPlusTreeException{
        if(this.parent == -1)
            return null;

        int i;

        BPlusTreeInnerNode parent = (BPlusTreeInnerNode) fm.readNode(this.parent, this.id);

        for(i = 0; i < parent.keyNum + 1; ++i) {
            if (parent.pointers.get(i) == this.location)
                break;
        }

        BPlusTreeLeafNode left = null;
        if(i - 1 >= 0)
            left = (BPlusTreeLeafNode) fm.readNode(parent.pointers.get(i - 1), this.id);

        BPlusTreeLeafNode right = null;
        if(i + 1 < parent.keyNum + 1)
            right = (BPlusTreeLeafNode) fm.readNode(parent.pointers.get(i + 1), this.id);

        if(left != null && left.keyNum > (int)Math.ceil((BPlusTree.M - 1) / 2.0) - 1){

            this.keys.add(0, left.keys.get(left.keyNum - 1));
            this.pointers.add(0, left.pointers.get(left.keyNum - 1));
            ++this.keyNum;

            parent.keys.set(i - 1, left.keys.get(left.keyNum - 1));

            left.keys.remove(left.keyNum - 1);
            left.pointers.remove(left.keyNum - 1);
            --left.keyNum;

            fm.updateNode(left);
            fm.updateNode(this);
            fm.updateNode(parent);
            return null;

        }else if(right != null && right.keyNum > (int)Math.ceil((BPlusTree.M - 1) / 2.0) - 1){

            this.keys.add(this.keyNum, right.keys.get(0));
            this.pointers.add(this.keyNum, right.pointers.get(0));
            ++this.keyNum;

            parent.keys.set(i, right.keys.get(1));

            right.keys.remove(0);
            right.pointers.remove(0);
            --right.keyNum;

            fm.updateNode(right);
            fm.updateNode(this);
            fm.updateNode(parent);

            return null;
        }

        if(i - 1 >= 0){
            for(int j = 0; j < this.keyNum; ++j){
                left.keys.add(this.keys.get(j));
                left.pointers.add(left.keyNum + j, this.pointers.get(j));
            }

            left.keyNum += this.keyNum;
            left.rightSibling = this.rightSibling;
            if(this.rightSibling != -1) {
                BPlusTreeNode node = fm.readNode(this.rightSibling, this.id);
                node.leftSibling = left.location;
                fm.updateNode(node);
            }

            fm.updateNode(left);
            fm.deleteNode(this.location, this.id);
            return parent.delete(fm, i - 1, i);

        }

        for(int j = 0; j < right.keyNum; ++j){
            this.keys.add(right.keys.get(j));
            this.pointers.add(right.keyNum + j, right.pointers.get(j));
        }

        this.keyNum += right.keyNum;
        this.rightSibling = right.rightSibling;
        if(right.rightSibling != -1) {
            BPlusTreeNode node = fm.readNode(right.rightSibling, this.id);
            node.leftSibling = this.location;
            fm.updateNode(node);
        }

        fm.updateNode(this);
        fm.deleteNode(right.location, this.id);

        return parent.delete(fm, i, i + 1);
    }
}
