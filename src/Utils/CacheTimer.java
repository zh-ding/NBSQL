package Utils;

import java.io.IOException;
import java.util.TimerTask;
import Server.Server;
import Table.Table;

public class CacheTimer extends TimerTask {
    @Override
    public void run(){
        try {
            for(String tmp : Server.node_cache.keySet()){
                String name = tmp.split("/")[3];
                String tb_name = name.substring(0, name.indexOf("."));
                String db_name = tmp.split("/")[2];
                System.out.println(tb_name);
                System.out.println(db_name);
                Table t = new Table(tb_name, db_name);
                t.file.resetCache();
            }
        }catch (IOException e){
            System.out.println(e);
        }

    }
}
