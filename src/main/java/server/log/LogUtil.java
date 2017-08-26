package server.log;

import java.sql.Timestamp;

/**
 * Created by tomcat on 8/23/17.
 */
public class LogUtil {

    public static void log(String message){
        System.out.println(new Timestamp(System.currentTimeMillis()) + " : " + message);
    }

}
