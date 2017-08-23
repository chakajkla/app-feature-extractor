package server.nlp;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import server.log.LogUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tomcat on 3/8/17.
 */
public class WordNet {


    private static String path = "data/dict";
    private static URL url;
    private static IDictionary dict;

    private static final double ALPHA = 0.2;

    static {
        try {
            url = new URL("file", null, path);
            dict = new Dictionary(url);
            dict.open();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve a set of hypernyms for a word.
     */
    public static List<String> getHypernyms(IDictionary iDictionary, ISynset iSynset) {
        ArrayList<String> aHypernyms = new ArrayList<>();

        // multiple hypernym chains are possible for a synset
        for (ISynsetID iSynsetId : iSynset.getRelatedSynsets(Pointer.HYPERNYM)) {
            List<IWord> iWords = iDictionary.getSynset(iSynsetId).getWords();
            for (IWord iWord2 : iWords) {
                String sLemma = iWord2.getLemma();
                aHypernyms.add(sLemma);
            }
        }
        return aHypernyms;
    }

    public static List<ISynsetID> getAncestors(ISynset synset) {
        List<ISynsetID> list = new ArrayList<ISynsetID>();
        list.addAll(synset.getRelatedSynsets(Pointer.HYPERNYM));
        list.addAll(synset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE));
        return list;
    }

    public static List<List<ISynset>> getPathsToRoot(ISynset synset) {
        List<List<ISynset>> pathsToRoot = null;
        List<ISynsetID> ancestors = getAncestors(synset);

        if (ancestors.isEmpty()) {
            pathsToRoot = new ArrayList<List<ISynset>>();
            List<ISynset> pathToRoot = new ArrayList<ISynset>();
            pathToRoot.add(synset);
            pathsToRoot.add(pathToRoot);

        } else if (ancestors.size() == 1) {
            pathsToRoot = getPathsToRoot(dict.getSynset(ancestors.get(0)));

            for (List<ISynset> pathToRoot : pathsToRoot) {
                pathToRoot.add(0, synset);
            }

        } else {
            pathsToRoot = new ArrayList<List<ISynset>>();
            for (ISynsetID ancestor : ancestors) {
                ISynset ancestorSynset = dict.getSynset(ancestor);
                List<List<ISynset>> pathsToRootLocal = getPathsToRoot(ancestorSynset);

                for (List<ISynset> pathToRoot : pathsToRootLocal) {
                    pathToRoot.add(0, synset);
                }

                pathsToRoot.addAll(pathsToRootLocal);
            }
        }

        return pathsToRoot;
    }

    private static ISynset findClosestCommonParent(List<ISynset> pathToRoot1,
                                                   List<ISynset> pathToRoot2) {
        int i = 0;
        int j = 0;

        if (pathToRoot1.size() > pathToRoot2.size()) {
            i = pathToRoot1.size() - pathToRoot2.size();
            j = 0;

        } else if (pathToRoot1.size() < pathToRoot2.size()) {
            i = 0;
            j = pathToRoot2.size() - pathToRoot1.size();
        }

        do {
            ISynset synset1 = pathToRoot1.get(i++);
            ISynset synset2 = pathToRoot2.get(j++);

            if (synset1.equals(synset2)) {
                return synset1;
            }

        } while (i < pathToRoot1.size());

        return null;
    }

    public static ISynset findClosestCommonParent(ISynset synset1, ISynset synset2) {
        if ((synset1 == null) || (synset2 == null)) {
            return null;
        }
        if (synset1.equals(synset2)) {
            return synset1;
        }

        List<List<ISynset>> pathsToRoot1 = getPathsToRoot(synset1);
        List<List<ISynset>> pathsToRoot2 = getPathsToRoot(synset2);
        ISynset resultSynset = null;
        int i = 0;

        for (List<ISynset> pathToRoot1 : pathsToRoot1) {
            for (List<ISynset> pathToRoot2 : pathsToRoot2) {

                ISynset synset = findClosestCommonParent(pathToRoot1, pathToRoot2);

                if (synset != null) {
                    int j = pathToRoot1.size() - (pathToRoot1.indexOf(synset) + 1);
                    if (j >= i) {
                        i = j;
                        resultSynset = synset;
                    }
                }
            }
        }

        return resultSynset;
    }


    /**
     * maxDepth
     *
     * @param synset
     * @return The length of the longest hypernym path from this synset to the
     * root.
     */
    public static int maxDepth(ISynset synset) {
        if (synset == null) {
            return 0;
        }

        List<ISynsetID> ancestors = getAncestors(synset);
        if (ancestors.isEmpty()) {
            return 0;
        }

        int i = 0;
        for (ISynsetID ancestor : ancestors) {
            ISynset ancestorSynset = dict.getSynset(ancestor);
            int j = maxDepth(ancestorSynset);
            i = (i > j) ? i : j;
        }

        return i + 1;
    }

    public static Integer shortestPathDistance(ISynset synset1, ISynset synset2) {
        Integer distance = null;
        if (synset1.equals(synset2)) {
            return 0;
        }

        ISynset ccp = findClosestCommonParent(synset1, synset2);
        if (ccp != null) {
            distance = maxDepth(synset1) + maxDepth(synset2) - 2 * maxDepth(ccp);

            // Debug
            String w1 = synset1.getWords().get(0).getLemma();
            String w2 = synset2.getWords().get(0).getLemma();
            String w3 = ccp.getWords().get(0).getLemma();
            LogUtil.log("maxDepth(" + w1 + "): " + maxDepth(synset1));
            LogUtil.log("maxDepth(" + w2 + "): " + maxDepth(synset2));
            LogUtil.log("maxDepth(" + w3 + "): " + maxDepth(ccp));
            LogUtil.log("distance(" + w1 + "," + w2 + "): " + distance);
        }
        return distance;
    }

    /***
     * Shortest path (based on common hypernymns) between best synsets
     *
     * @param w1
     * @param w2
     * @return
     */
    public static double calculateShortestPathDistance(String w1, String w2, POS pos) {
        ISynset[] pair = getBestSynsetsPair(w1, w2, pos);
        Integer l_dist = shortestPathDistance(pair[0], pair[1]);

        if (l_dist != null) {
            return Math.exp(-ALPHA * l_dist);
        }

        return -1;
    }

    /***
     * get best synsets pairs based on their path distance
     *
     * @param w1
     * @param w2
     * @param pos
     * @return
     */
    public static ISynset[] getBestSynsetsPair(String w1, String w2, POS pos) {

        ISynset[] pair = new ISynset[2];

        int minDistance = Integer.MAX_VALUE;

        //list all synsets of w1,w2
        List<ISynset> list1 = getSynsets(w1, pos);
        List<ISynset> list2 = getSynsets(w2, pos);

        for (ISynset syn1 : list1) {
            for (ISynset syn2 : list2) {
                Integer distance = shortestPathDistance(syn1, syn2);
                distance = distance != null ? distance : Integer.MAX_VALUE;
                if (distance < minDistance) {
                    minDistance = distance;
                    pair[0] = syn1;
                    pair[1] = syn2;
                }
            }
        }

        return pair;
    }


    public static List<ISynset> getSynsets(String w, POS pos) {
        ArrayList<ISynset> list = new ArrayList<>();
        IIndexWord idxWordNoun = dict.getIndexWord(w, pos);
        if (idxWordNoun != null) {

            List<IWordID> listWordNoun = idxWordNoun.getWordIDs();

            for (Iterator<IWordID> ite = listWordNoun.iterator(); ite.hasNext(); ) {
                IWordID wordID1 = ite.next();
                IWord word = dict.getWord(wordID1);
                ISynset synset = word.getSynset();
                list.add(synset);
            }
        }


        return list;
    }


    public static List<ISynsetID> getRelatedSynsets(String noun, POS pos) {

        List<ISynsetID> list = new ArrayList<>();

        IIndexWord idxWordNoun = dict.getIndexWord(noun, pos);
        if (idxWordNoun != null) {

            List<IWordID> listWordNoun = idxWordNoun.getWordIDs();

            for (Iterator<IWordID> ite = listWordNoun.iterator(); ite.hasNext(); ) {
                IWordID wordID1 = ite.next();
                IWord word = dict.getWord(wordID1);
                ISynset synset = word.getSynset();
                list.addAll(synset.getRelatedSynsets());
            }

        }

        return list;

    }


    public static boolean checkSynonymn(String w1, String w2, POS pos) {


        // check w1 against w2
        IIndexWord idxWord = dict.getIndexWord(w1, pos);

        if (idxWord != null) {
            List<IWordID> listWordNoun = idxWord.getWordIDs();

            for (Iterator<IWordID> ite = listWordNoun.iterator(); ite.hasNext(); ) {
                IWordID wordID1 = ite.next();
                IWord word = dict.getWord(wordID1);
                ISynset synset = word.getSynset();

                HashSet<String> synonymnList = new HashSet<>();
                for (IWord w : synset.getWords())
                    synonymnList.add(w.getLemma());

                if (synonymnList.contains(w2)) {

                    //dict.close();
                    return true;
                }
            }

        }

        // check w2 against w1
        idxWord = dict.getIndexWord(w2, pos);

        if (idxWord != null) {
            List<IWordID> listWordNoun = idxWord.getWordIDs();

            for (Iterator<IWordID> ite = listWordNoun.iterator(); ite.hasNext(); ) {
                IWordID wordID1 = ite.next();
                IWord word = dict.getWord(wordID1);
                ISynset synset = word.getSynset();

                HashSet<String> synonymnList = new HashSet<>();
                for (IWord w : synset.getWords())
                    synonymnList.add(w.getLemma());

                if (synonymnList.contains(w1)) {

                    //dict.close();
                    return true;
                }
            }

        }


        //dict.close();


        return false;
    }


    public static void main3(String[] args) {
//        double score = calculateSemanticSimilarity("compose new email","create new email");
//        //score = calculateSemanticSimilarity("add image","delete image");
//        LogUtil.log(score);


        IIndexWord idxWordNoun = dict.getIndexWord("dog", POS.NOUN);
        if (idxWordNoun != null) {

            List<IWordID> listWordNoun = idxWordNoun.getWordIDs();

            for (Iterator<IWordID> ite = listWordNoun.iterator(); ite.hasNext(); ) {
                IWordID wordID1 = ite.next();
                IWord word = dict.getWord(wordID1);
                ISynset synset = word.getSynset();
                LogUtil.log(synset.toString());
                //LogUtil.log(synset.getRelatedSynsets());
            }

        }

    }

}
