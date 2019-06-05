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

        int i;

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

            fm.deleteNode(left.location, this.id);

            fm.updateNode(this);
            fm.updateNode(node);

            return parent.delete(fm, i - 1, i - 1);

        }

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

        fm.deleteNode(right.location, this.id);

        fm.updateNode(this);
        fm.updateNode(node);

        return parent.delete(fm, i, i + 1);
    }
}
