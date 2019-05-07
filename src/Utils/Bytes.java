package Utils;

import java.util.Comparator;

public class Bytes{

    public static byte intToByte(int number){
        return (byte) ((number<0?-number:number) & 0xff);
    }

    public static int byteToInt(byte b){
        if (b >= 0) return (int) b & 0xff;
        else return b + 256;
    }

    public static byte[] intToBytes(int number){
        byte[] ans = new byte[4];
        ans[0] = (byte) (number>>>24);
        ans[1] = (byte) (number>>>16 & 0xff);
        ans[2] = (byte) (number>>>8 & 0xff);
        ans[3] = (byte) (number & 0xff);
        return ans;
    }

    public static void intToBytes(int number, byte[] block, int offset){
        block[offset]  =  (byte) (number>>>24);
        block[offset+1] = (byte) (number>>>16 & 0xff);
        block[offset+2] = (byte) (number>>>8 & 0xff);
        block[offset+3] = (byte) (number & 0xff);
    }

    public static int bytesToInt(byte[] bytes){
        return (bytes[0]<<24)
                | ((bytes[1]&0xff)<<16)
                | ((bytes[2]&0xff)<<8)
                | (bytes[3]&0xff);
    }

    public static int bytesToInt(byte[] bytes, int offset){
        if(offset+3 >= bytes.length) return 0;
        return (bytes[offset]<<24)
                | ((bytes[offset+1]&0xff)<<16)
                | ((bytes[offset+2]&0xff)<<8)
                | (bytes[offset+3]&0xff);
    }

    public static int bytesToInt(byte b1, byte b2, byte b3, byte b4){
        return ((int)b1<<24)
                | (((int)b2&0xff)<<16)
                | (((int)b3&0xff)<<8)
                | ((int)b4&0xff);
    }

    public static byte booleanToByte(boolean state){
        if(state){
            return intToByte(1);
        }else{
            return intToByte(0);
        }
    }

    public static boolean byteToBoolean(byte b){
        if(Bytes.byteToInt(b) == 1){
            return true;
        }else{
            return false;
        }
    }

    public static int compare(byte[] a, byte[] b){
        int shared = a.length;
        if (b.length<shared) shared = b.length;
        for (int i=0; i<shared; i++){
            if (a[i]+128 < b[i]+128) return -1;
            if (a[i]+128 > b[i]+128) return 1;
        }
        // they are the same up to index shared
        if (a.length < b.length) return -1;
        if (a.length > b.length) return 1;
        return 0;
    }

    public static void appendInt(byte[] dest, int k){
        int size=0;
        for(int i=0;i<=dest.length-4;i+=4){
//			System.out.println("Bytes["+i+"]:"+bytesToInt(dest,i));
//			System.out.flush();
            if(bytesToInt(dest, i) != 0){
                size+=4;
            }else{
                //System.out.println("Found end of dest at position " + size);
                break;
            }
        }
//		System.out.println("End: " + size);
//		System.out.println("Dest length: " + dest.length);
        if(size <= dest.length-4){
            //System.out.println("Appending " + k + " at position " + size + ".");
            intToBytes(k, dest, size);
        }
    }

    public static int getLastIndex(byte[] bytes){
        int size=0;
        for(int i=0;i<=bytes.length-4;i+=4){
            if(bytesToInt(bytes, i) != 0){
                size+=4;
            }else{
                return size;
            }
        }
        return bytes.length;
    }

    public static void clearBytesFromPosition(byte[] data, int index){
        if(index > data.length-4) return;
        for(int i=index; i <= data.length-4; i+=4){
            Bytes.intToBytes(0, data, i);
        }
    }

    public static class ByteArrayComparator implements Comparator<byte[]>{
        public int compare(byte[] a, byte[] b){
            return Bytes.compare(a, b);
        }
    }
}
