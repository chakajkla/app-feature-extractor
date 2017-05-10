package server;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tomcat on 5/11/17.
 */
public class RequestControllerUtil {


    public static <T> T[] convertToArray(List<T> list) {

        T[] array =  (T[]) Array.newInstance(list.getClass(), list.size());

        for (int i = 0; i < list.size(); i++) {
            array[i] = (T)list.get(i);
        }

        return array;

    }

    public static void main(String[] args){

        List<Float> list = new ArrayList<>();

        list.add(1.45f);
        list.add(2.34f);
        list.add(3.45f);

        System.out.println(Arrays.toString(convertToArray(list)));

    }

}
