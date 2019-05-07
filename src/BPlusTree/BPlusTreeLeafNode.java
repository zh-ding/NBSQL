package BPlusTree;

import Utils.Bytes;

import java.io.IOException;
import java.util.Arrays;

public class BPlusTreeLeafNode extends BPlusTreeNode {

    public BPlusTreeLeafNode(int ID) {
        super(ID, true);
        this.keys = new byte[BPlusTree.getKeySize()*(BPlusTree.getOrder())+BPlusTree.getKeySize()];
        this.pointers = new byte[BPlusTree.getPointerSize()*(BPlusTree.getOrder()+1)+BPlusTree.getPointerSize()];
    }

    public BPlusTreeLeafNode(byte[] data){
        super();
        this.header = Arrays.copyOfRange(data, 0, 9);
        this.keys = new byte[BPlusTree.getKeySize()*(BPlusTree.getOrder())+BPlusTree.getKeySize()];
        this.pointers = new byte[BPlusTree.getPointerSize()*(BPlusTree.getOrder()+1)+BPlusTree.getPointerSize()];
        byte[] temporaryKeys = Arrays.copyOfRange(data, 9, 9 + BPlusTree.getKeySize()*(BPlusTree.getOrder()));
        System.arraycopy(temporaryKeys, 0, this.keys, 0, temporaryKeys.length);
        byte[] temporaryPointers = Arrays.copyOfRange(data,
                9 + BPlusTree.getKeySize()*(BPlusTree.getOrder()),
                9 + BPlusTree.getKeySize()*(BPlusTree.getOrder()) +
                        BPlusTree.getPointerSize()*BPlusTree.getOrder());
        System.arraycopy(temporaryPointers, 0, this.pointers, 0, temporaryPointers.length);
    }

    public void insert(int key, int pointer, BPlusTree tree) throws IOException{
        BPlusTreeLeafNode leftNode = this;
        BPlusTreeLeafNode rightNode = null;
        BPlusTreeInnerNode newInternal = null;
        byte[] keys = leftNode.getKeys();
        byte[] pointers = leftNode.getPointers();
        boolean inserted = false;
        byte[] temporaryKeys = new byte[keys.length];
        byte[] temporaryPointers = new byte[pointers.length];
        if(Bytes.bytesToInt(keys, 0) == 0){

            Bytes.intToBytes(key, keys, 0);
            Bytes.intToBytes(pointer, pointers, 0);
            inserted = true;
        }else{
            for( int i = 0; i < keys.length-4 ; i+=4){
                if(key < Bytes.bytesToInt(keys, i)){
                    System.arraycopy(keys, i, temporaryKeys, 0, temporaryKeys.length-i);
                    System.arraycopy(pointers, i, temporaryPointers, 0, temporaryPointers.length-i);

                    Bytes.intToBytes(key, keys, i);
                    Bytes.intToBytes(pointer, pointers, i);

                    System.arraycopy(temporaryKeys, 0, keys, i+4, temporaryKeys.length-(i+4));
                    System.arraycopy(temporaryPointers, 0, pointers, i+4, temporaryPointers.length-(i+4));
                    inserted = true;
                    break;
                }else if(key == Bytes.bytesToInt(keys, i)){
                    Bytes.intToBytes(pointer, pointers, i);
                    inserted = true;
                    break;
                }
            }
            if(!inserted){
                Bytes.appendInt(keys, key);
                Bytes.appendInt(pointers, pointer);
            }
        }
        if(Bytes.getLastIndex(keys) > BPlusTree.getKeySize() * BPlusTree.getOrder()){
            rightNode = new BPlusTreeLeafNode(tree.getFileManager().getSize());
            byte[] keysForRightNode = new byte[BPlusTree.getKeySize()*BPlusTree.getOrder()];
            byte[] pointersForRightNode = new byte[BPlusTree.getPointerSize()*(BPlusTree.getOrder()+1)];
            int byteIndexToSplitAt = (int) (BPlusTree.getKeySize()*Math.ceil((double)(BPlusTree.getOrder()+1)/2.0));
            System.arraycopy(keys, byteIndexToSplitAt, keysForRightNode, 0, keys.length-byteIndexToSplitAt);
            System.arraycopy(pointers, byteIndexToSplitAt, pointersForRightNode, 0, pointers.length-byteIndexToSplitAt);
            for(int i = 0; i < Bytes.getLastIndex(keysForRightNode); i+=4){
                int movingKey = Bytes.bytesToInt(keysForRightNode, i);
                int movingPointer = Bytes.bytesToInt(pointersForRightNode, i);
                rightNode.insert(movingKey, movingPointer, tree);
            }
            Bytes.clearBytesFromPosition(keys, byteIndexToSplitAt);
            Bytes.clearBytesFromPosition(pointers, byteIndexToSplitAt);
            if(leftNode.getRightLeaf() != 0){
                rightNode.setRightLeaf(leftNode.getRightLeaf());
            }
            leftNode.setRightLeaf(rightNode.getID());
            if(leftNode.isRoot()){

                int id = tree.getFileManager().getSize();
                newInternal = new BPlusTreeInnerNode(id);
                tree.setRoot(newInternal.getID(), 0);
                newInternal.setSmallestPointer(leftNode.getID());
                newInternal.insert(Bytes.bytesToInt(rightNode.getKeys(), 0), rightNode.getID(), tree);
                tree.getFileManager().write(newInternal.toBytes());
            }else{
                // Push the new right node ID to parent
                newInternal = (BPlusTreeInnerNode) leftNode.parent;
                newInternal.insert(Bytes.bytesToInt(rightNode.getKeys(), 0), rightNode.getID(), tree);
                tree.getFileManager().write(newInternal.toBytes(), newInternal.getID());
            }
        }
        if(rightNode != null) tree.getFileManager().write(rightNode.toBytes(), rightNode.getID());
        leftNode.setKeys(keys);
        leftNode.setPointers(pointers);
        tree.getFileManager().write(leftNode.toBytes(), leftNode.getID());
    }

    public int get(int key){
        byte[] keys = this.getKeys();
        byte[] pointers = this.getPointers();
        for(int i = 0; i < keys.length; i+=4){
            if(Bytes.bytesToInt(keys, i) == key){
                return Bytes.bytesToInt(pointers, i);
            }
        }
        return 0;
    }

    public int getRightLeaf(){
        return Bytes.bytesToInt(this.header, this.header.length-4);
    }

    public void setRightLeaf(int rightLeafID){
        Bytes.intToBytes(rightLeafID, this.header, this.header.length-4);
    }
}