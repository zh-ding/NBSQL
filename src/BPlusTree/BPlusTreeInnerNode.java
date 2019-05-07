package BPlusTree;

import Utils.Bytes;

import java.io.IOException;
import java.util.Arrays;


public class BPlusTreeInnerNode extends BPlusTreeNode {

    public BPlusTreeInnerNode(int ID){
        super(ID, false);
        this.keys = new byte[BPlusTree.getKeySize()*BPlusTree.getOrder()+BPlusTree.getKeySize()];
        this.pointers = new byte[BPlusTree.getPointerSize()*(BPlusTree.getOrder()+1)+BPlusTree.getPointerSize()];
    }

    public BPlusTreeInnerNode(byte[] data){
        super();
        this.header = Arrays.copyOfRange(data, 0, 9);
        this.keys = new byte[BPlusTree.getKeySize()*(BPlusTree.getOrder())+BPlusTree.getKeySize()];
        this.pointers = new byte[BPlusTree.getPointerSize()*(BPlusTree.getOrder()+1)+BPlusTree.getPointerSize()];
        byte[] temporaryKeys = Arrays.copyOfRange(data, 9, 9 + BPlusTree.getKeySize()*(BPlusTree.getOrder()));
        System.arraycopy(temporaryKeys, 0, this.keys, 0, temporaryKeys.length);
        byte[] temporaryPointers = Arrays.copyOfRange(data,
                9 + BPlusTree.getKeySize()*(BPlusTree.getOrder()),
                9 + BPlusTree.getKeySize()*(BPlusTree.getOrder()) +
                        BPlusTree.getPointerSize()*(BPlusTree.getOrder()+1));
        System.arraycopy(temporaryPointers, 0, this.pointers, 0, temporaryPointers.length);
    }


    public BPlusTreeInnerNode insert(int key, int pointer, BPlusTree tree) throws IOException{
        BPlusTreeInnerNode leftNode = this;
        BPlusTreeInnerNode rightNode = null;
        BPlusTreeInnerNode newInternal = null;

        byte[] keys = leftNode.getKeys();
        byte[] pointers = leftNode.getPointers();
        boolean inserted = false;
        byte[] temporaryKeys = new byte[keys.length];
        byte[] temporaryPointers = new byte[pointers.length];

        if(Bytes.bytesToInt(keys, 0) == 0){
            Bytes.intToBytes(key, keys, 0);
            Bytes.intToBytes(pointer, pointers, 4);
        }else{
            for( int i = 0; i < keys.length-4 ; i+=4){
                if(key < Bytes.bytesToInt(keys, i)){
                    System.arraycopy(keys, i, temporaryKeys, 0, temporaryKeys.length-i);
                    System.arraycopy(pointers, i+4, temporaryPointers, 0, temporaryPointers.length-(i+4));

                    Bytes.intToBytes(key, keys, i);
                    Bytes.intToBytes(pointer, pointers, i+4);

                    System.arraycopy(temporaryKeys, 0, keys, i+4, temporaryKeys.length-(i+4));
                    System.arraycopy(temporaryPointers, 0, pointers, i+8, temporaryPointers.length-(i+8));
                    inserted = true;

                    break;

                }else if(key == Bytes.bytesToInt(keys, i) && !inserted){

                    Bytes.intToBytes(pointer, pointers, i+4);
                    this.setPointers(pointers);
                    inserted = true;
                    break;
                }
            }

            if(!inserted){

                Bytes.appendInt(keys, key);
                Bytes.appendInt(pointers, pointer);
            }
            this.keys = new byte[BPlusTree.getKeySize()*(BPlusTree.getOrder())+BPlusTree.getKeySize()];
            if(Bytes.getLastIndex(keys) > BPlusTree.getKeySize() * BPlusTree.getOrder()){
                rightNode = new BPlusTreeInnerNode(tree.getFileManager().getSize());
                byte[] keysForRightNode = new byte[BPlusTree.getKeySize()*BPlusTree.getOrder()];
                byte[] pointersForRightNode = new byte[BPlusTree.getPointerSize()*(BPlusTree.getOrder()+1)];

                int keyByteIndexToSplitAt = (int) (BPlusTree.getKeySize()*
                        Math.ceil(((double)(BPlusTree.getOrder()))/2.0));
                int pointerByteIndexToSplitAt = (int) (BPlusTree.getPointerSize()*
                        Math.ceil(((double)(BPlusTree.getOrder()+2))/2.0));

                System.arraycopy(keys, keyByteIndexToSplitAt, keysForRightNode, 0, keys.length-keyByteIndexToSplitAt);
                System.arraycopy(pointers, pointerByteIndexToSplitAt, pointersForRightNode, 0, pointers.length-pointerByteIndexToSplitAt);

                rightNode.setSmallestPointer(Bytes.bytesToInt(pointersForRightNode,0));
                for(int i = 4; i < Bytes.getLastIndex(keysForRightNode); i+=4){
                    int movingKey = Bytes.bytesToInt(keysForRightNode, i);
                    int movingPointer = Bytes.bytesToInt(pointersForRightNode, i);
                    rightNode.insert(movingKey, movingPointer, tree);
                }
                Bytes.clearBytesFromPosition(keys, keyByteIndexToSplitAt); // Removes the keys that was moved from left node
                Bytes.clearBytesFromPosition(pointers, pointerByteIndexToSplitAt); // Removes the pointers that was moved from left node
                tree.getFileManager().write(rightNode.toBytes(), rightNode.getID());
                if(leftNode.isRoot()){

                    int id = tree.getFileManager().getSize();

                    newInternal = new BPlusTreeInnerNode(id);
                    tree.setRoot(newInternal.getID(), 0);
                    newInternal.setSmallestPointer(leftNode.getID());
                    newInternal.insert(Bytes.bytesToInt(keysForRightNode,0), rightNode.getID(), tree);
                    tree.getFileManager().write(newInternal.toBytes());
                }else{
                    // Push the new right node ID to parent
                    newInternal = (BPlusTreeInnerNode) leftNode.parent;

                    newInternal.insert(Bytes.bytesToInt(keysForRightNode,0), rightNode.getID(), tree);
                    tree.getFileManager().write(newInternal.toBytes(), newInternal.getID());
                }
            }
            if(rightNode != null) tree.getFileManager().write(rightNode.toBytes(), rightNode.getID());
            leftNode.setKeys(keys);
            leftNode.setPointers(pointers);
            tree.getFileManager().write(leftNode.toBytes(), leftNode.getID());
        }
        return this;
    }

    public void setSmallestPointer(int smallestPointer){

        byte[] pointers = this.getPointers();
        byte[] temporaryPointers = new byte[pointers.length];
        System.arraycopy(pointers, 0, temporaryPointers, 0, temporaryPointers.length);
        Bytes.intToBytes(smallestPointer, pointers, 0);
        System.arraycopy(temporaryPointers, 0, pointers, 4, temporaryPointers.length-4);
        this.setPointers(pointers);
    }


    public int get(int key){
        byte[] keys = this.getKeys();
        byte[] pointers = this.getPointers();
        if(key < Bytes.bytesToInt(keys, 0)){
            return Bytes.bytesToInt(pointers, 0);
        }else if(key >= Bytes.bytesToInt(keys, Bytes.getLastIndex(keys)-4)){
            return Bytes.bytesToInt(pointers, Bytes.getLastIndex(pointers)-4);
        }else{
            for(int i = 0; i <= keys.length-4; i+=4){
                if(key >= Bytes.bytesToInt(keys, i) && key < Bytes.bytesToInt(keys, i+4)){
                    return Bytes.bytesToInt(pointers, i+4);
                }
            }
        }
        return 0;
    }

}
