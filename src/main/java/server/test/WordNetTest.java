package server.test;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

/**
 * Created by tomcat on 12/6/16.
 */
public class WordNetTest {

    private static ILexicalDatabase db = new NictWordNet();
    /*
    //available options of metrics
    private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
            new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
            new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };
    */
    private static double compute(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return s;
    }

//    public static void main(String[] args) {
//        String[] words = {"add", "get", "filter", "remove", "check", "find", "collect", "create"};
//
//        for(int i=0; i<words.length-1; i++){
//            for(int j=i+1; j<words.length; j++){
//                double distance = compute(words[i], words[j]);
//                LogUtil.log(words[i] +" -  " +  words[j] + " = " + distance);
//            }
//        }
//    }

}
