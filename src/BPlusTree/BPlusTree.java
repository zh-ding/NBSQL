package BPlusTree;

import Utils.Bytes;
import Utils.FileManager;

import java.io.IOException;

public class BPlusTree {

    private FileManager fm;
    private static byte[] headerBytes;
    static BPlusTreeNode temporaryParent;
    private static int BLOCK_SIZE, ORDER, KEY_SIZE, POINTER_SIZE, ROOT_ID, ROOT_IS_LEAF;
    private int TREE_LEVEL;

    public BPlusTree(FileManager file){

        //temp
        int blockSize = 1024;

        this.fm = file;

        try {
            if(fm.getSize() == 0){
                BLOCK_SIZE = blockSize;
                KEY_SIZE = 4;
                POINTER_SIZE = 4;
                ROOT_ID = 0;
                ROOT_IS_LEAF = 0;
                TREE_LEVEL = 1;
                ORDER = calculateOrder();
                if(ORDER < 3){
                    System.out.println("The block size was calculated to less than 3, exiting.");
                    System.exit(1);
                }
                headerBytes = new byte[BLOCK_SIZE];
                writeDataToHeader(headerBytes);
            }else{
                // Read header block and set info
                headerBytes = fm.read(0);
                readDataFromHeader(headerBytes);
                if(BLOCK_SIZE != blockSize){
                    System.out.println("The block size contained in header block did not match input block size, exiting.");
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insert(int key, int value){
        try {
            headerBytes = fm.read(0);
            readDataFromHeader(headerBytes);
            if(ROOT_ID != 0){
                // There is a root, find it and start inserting from there
                if(ROOT_IS_LEAF == 1){
                    // Root was leaf, just insert the key value.
                    BPlusTreeLeafNode root = new BPlusTreeLeafNode(fm.read(ROOT_ID));
                    root.insert(key, value, this);
                }else{
                    // Root is internal, find the correct leaf and write to it
                    BPlusTreeLeafNode node = get(key, ROOT_ID);
                    node.insert(key, value, this);
                }
            }else{
                BPlusTreeLeafNode root = new BPlusTreeLeafNode(1);
                root.insert(key, value, this);
                this.setRoot(root.getID(), 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getDiskPointer(int key){
        TREE_LEVEL = 0;
        return this.get(key, this.getRoot()).get(key);
    }

    public BPlusTreeLeafNode get(int key, int blockID){
        BPlusTreeNode node = null;
        if(blockID == 0) return null;
        try {
            byte[] block = fm.read(blockID);
            boolean isLeaf = Bytes.byteToBoolean(block[4]);
            if(isLeaf){
                node = new BPlusTreeLeafNode(block);
            }else{
                node = new BPlusTreeInnerNode(block);
            }
            node.setParent(temporaryParent);
            temporaryParent = node;
            TREE_LEVEL++; // Used for calculation of tree height
            if(node.isLeaf()){
                temporaryParent = null;
                return (BPlusTreeLeafNode) node;
            }else{
                return get(key, ((BPlusTreeInnerNode)node).get(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void setRoot(int rootID, int isLeaf){
        try {
            headerBytes = fm.read(0);
            ROOT_ID = rootID;
            ROOT_IS_LEAF = isLeaf;
            TREE_LEVEL++;
            Bytes.intToBytes(ROOT_ID, headerBytes, 6);
            headerBytes[10] = Bytes.intToByte(ROOT_IS_LEAF);
            writeDataToHeader(headerBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRoot(){
        try {
            headerBytes = fm.read(0);
            readDataFromHeader(headerBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ROOT_ID;
    }
    public static int getRootIsLeaf(){
        return ROOT_IS_LEAF;
    }
    public static int getOrder(){
        return ORDER;
    }
    public static int getKeySize(){
        return KEY_SIZE;
    }
    public static int getPointerSize(){
        return POINTER_SIZE;
    }
    public static int getBlockSize(){
        return BLOCK_SIZE;
    }
    public int getTreeLevel(){
        return TREE_LEVEL;
    }
    public void resetTreeLevel(){
        this.TREE_LEVEL = 0;
    }
    public FileManager getFileManager(){
        return fm;
    }

    private static void readDataFromHeader(byte[] headerBytes){
        BLOCK_SIZE = Bytes.bytesToInt(headerBytes, 0);
        KEY_SIZE = Bytes.byteToInt(headerBytes[4]);
        POINTER_SIZE = Bytes.byteToInt(headerBytes[5]);
        ROOT_ID = Bytes.bytesToInt(headerBytes, 6);
        ROOT_IS_LEAF = Bytes.byteToInt(headerBytes[10]);
        ORDER = calculateOrder();
    }

    private void writeDataToHeader(byte[] headerBytes){
        Bytes.intToBytes(BLOCK_SIZE, headerBytes, 0);
        headerBytes[4] = Bytes.intToByte(KEY_SIZE);
        headerBytes[5] = Bytes.intToByte(POINTER_SIZE);
        Bytes.intToBytes(ROOT_ID, headerBytes, 6);
        headerBytes[10] = Bytes.intToByte(ROOT_IS_LEAF);
        try {
            fm.write(headerBytes, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* Method for printing the information stored in the header */
    private void printHeader(){
        System.out.println("--- HEADER INFO ---");
        System.out.println("Block size: " + Bytes.bytesToInt(headerBytes,0));
        System.out.println("Key size: " + Bytes.byteToInt(headerBytes[4]));
        System.out.println("Pointer size: " + Bytes.byteToInt(headerBytes[5]));
        System.out.println("Root ID: " + Bytes.bytesToInt(headerBytes, 6));
        System.out.println("Root is Leaf: " + Bytes.byteToBoolean(headerBytes[10]));
        System.out.println("Tree Order: (not stored in header): " + ORDER);
        System.out.println("--- STOP HEADER INFO ---");
        System.out.flush();
    }

    public String readAndPrintHeaderToConsole(){
        try {
            headerBytes = fm.read(0);
            String result = "--- HEADER INFO ---\n";
            result += "Block size: " + Bytes.bytesToInt(headerBytes,0) + "\n";
            result += "Key size: " + Bytes.byteToInt(headerBytes[4]) + "\n";
            result += "Pointer size: " + Bytes.byteToInt(headerBytes[5]) + "\n";
            result += "Root ID: " + Bytes.bytesToInt(headerBytes, 6) + "\n";
            result += "Root is Leaf: " + Bytes.byteToBoolean(headerBytes[10]) + "\n";
            result += "Tree Order: (not stored in header): " + ORDER + "\n";
            result += "--- STOP HEADER INFO ---" + "\n";
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static int calculateOrder(){
        return (BLOCK_SIZE - 9 - POINTER_SIZE)/(KEY_SIZE + POINTER_SIZE);
    }

    public void dumpIndex(){
        int blockNumber = 0;
        while(blockNumber < fm.getSize()){
            try {
                byte[] data = fm.read(blockNumber);
                if(blockNumber == 0){
                    readDataFromHeader(data);
                    printHeader();
                }else{
                    boolean isLeaf = Bytes.byteToBoolean(data[4]);
                    if(isLeaf){
                        BPlusTreeLeafNode leaf = new BPlusTreeLeafNode(data);
                    }else{
                        BPlusTreeInnerNode internal = new BPlusTreeInnerNode(data);
                    }
                }
                blockNumber++;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
