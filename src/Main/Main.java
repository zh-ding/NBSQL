package Main;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args){
        ArrayList a = new ArrayList();
        int e1 = 1;
        long e2 = 2;
        float e3 = 3.0f;
        double e4 = 4.0;
        String e5 = "111";
        a.add(e1);
        a.add(e2);
        a.add(e3);
        a.add(e4);
        a.add(e5);
        for(int i = 0; i < 5; ++i){
            if(a.get(i) instanceof Integer)
                System.out.println(((Integer)a.get(i)).intValue());
            else if(a.get(i) instanceof Long)
                System.out.println(((Long)a.get(i)) == 2);
            else if(a.get(i) instanceof Float)
                System.out.println(((Float)a.get(i)).floatValue());
        }

    }
}
