package server.nlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import server.database.DataAccess;

import server.objects.AppFeatureDataPoint;
import server.objects.Bigram;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


public class NLPUtil {

    private static final double semanticSimilarityThreshold = 0.65;
    private static ILexicalDatabase db = new NictWordNet();

    public static List<String> tokenizeString(String text) {

        ArrayList<String> sentenceList = new ArrayList<>();

        String[] sp = text.split("\\n");

        for (String s : sp) {

            String[] sentenceSp = s.split("\\.");

            for (String sentence : sentenceSp) {

                if (sentence.trim().length() > 5) {
                    sentenceList.add(sentence);

                }
            }

        }

        return sentenceList;
    }

    private static Properties props = new Properties();
    private static StanfordCoreNLP pipeline = null;

    public static List<String> preprocessString(String text) {

        props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");

        if (pipeline == null) {
            pipeline = new StanfordCoreNLP(props);
        }

        List<String> tokenizer = NLPUtil.tokenizeString(text);

        //we test to include stop words for detecting phrasal noun/verb
        tokenizer = NLPUtil.removeBadWords(tokenizer);

        ArrayList<String> cleanSentences = new ArrayList<>();

        for (String sentence : tokenizer) {

            String featureString = NLPUtil.constructString(NLPUtil
                    .lemmatize(sentence));

            //check that the string is not single worded
            if (!NLPUtil.checkDuplication(featureString)) {
                cleanSentences.add(featureString);
            }

        }

        return cleanSentences;
    }

    public static String assembleString(List<String> strList) {

        StringBuilder sb = new StringBuilder();
        for (String tokenSentence : strList) {
            sb.append(tokenSentence);
        }

        return sb.toString();
    }

    public static List<String> lemmatize(String documentText) {

        List<String> lemmas = new LinkedList<String>();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);

        // run all Annotators on this text
        pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of
                // lemmas
                lemmas.add(token.get(LemmaAnnotation.class));
            }
        }

        return lemmas;
    }

    public static List<String> removeBadWords(List<String> text) {

        Set<String> dict = new HashSet<>();
        BufferedReader br;
        try {
            br = new BufferedReader(
                    new FileReader("data/stopwords_ranksnl_2.txt"));

            String d = "";

            while ((d = br.readLine()) != null) {
                dict.add(d.trim());
            }

            br.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ArrayList<String> stopwordsRemovedText = new ArrayList<>();
        for (String sentence : text) {

            String[] sp = sentence.split("\\s+");

            StringBuffer proc_sentence = new StringBuffer();

            for (String word : sp) {

                String w = word.toLowerCase();
                // System.out.println("before " + w);
                w = removeNonCharacters(w);
                // System.out.println("after " + w);

                if (!dict.contains(w.trim()) && w.length() > 1) {

                    proc_sentence.append(w + " ");
                }

            }

            stopwordsRemovedText.add(proc_sentence.toString());
        }

        return stopwordsRemovedText;
    }

    public static String removeNonCharacters(String txt) {
        txt = txt.replaceAll("-[0-9a-fA-F]+-", "");
        txt = txt.replaceAll("u[0-9a-fA-F]{4}", "");
        txt = txt.replaceAll("\\n", ".");
        txt = txt.replaceAll("[^a-z]", "");
        return txt.replaceAll("[-0123456789/\"]", "");
    }

    public static String constructString(List<String> list) {
        StringBuilder bu = new StringBuilder();
        for (String s : list) {
            bu.append(s + " ");
        }
        return bu.toString();
    }

    public static boolean checkDuplication(String featureString) {

        String[] sp = featureString.split("\\s+");

        HashMap<String, Integer> dict = new HashMap<>();

        for (String word : sp) {
            if (!dict.containsKey(word)) {
                dict.put(word, 1);
            } else {
                dict.put(word, dict.get(word) + 1);
            }
        }

        if (dict.keySet().size() == 1) {

            for (String key : dict.keySet()) {
                if (dict.get(key) > 1) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean checksynonymnDistance(String w1_verb, String w1_noun,
                                                String w2_verb, String w2_noun) {

        // check equality
        if (w1_verb.equals(w2_verb) && w1_noun.equals(w2_noun)) {
            return true;
        }

        if ((checkSynonymn(w1_noun, w2_noun, POS.NOUN)
                && checkSynonymn(w1_verb, w2_verb, POS.VERB)) &&
                calculateSemanticSimilarity(w1_noun, w2_noun, w1_verb, w2_verb) >= semanticSimilarityThreshold) {

            return true;
        }

        return false;
    }

    private static double calculateSemanticSimilarity(String noun1, String noun2, String verb1, String verb2) {

        double sim = 0;

        double nounSimilarity = compute(noun1, noun2);

        double verbSimilarity = compute(verb1, verb2);

        if (nounSimilarity == 0 && verbSimilarity == 0) {
            return sim;
        }

        return (nounSimilarity + verbSimilarity) / 2;
    }

//    public static void main(String[] args){
//        double score = calculateSemanticSimilarity("compose new email","create new email");
//        //score = calculateSemanticSimilarity("add image","delete image");
//        System.out.println(score);
//    }

    private static double calculateSemanticSimilarity(String sen1, String sen2) {

        String[] words1 = sen1.split("\\s");
        String[] words2 = sen2.split("\\s");

        HashSet<String> wordSet = new HashSet<>();
        HashSet<String> w1Set = new HashSet<>();
        HashSet<String> w2Set = new HashSet<>();
        for (String w : words1) {
            wordSet.add(w);
            w1Set.add(w);
        }
        for (String w : words2) {
            wordSet.add(w);
            w2Set.add(w);
        }
        ArrayList<String> wordList = new ArrayList<>();
        wordList.addAll(wordSet);

        double threshold = 0.6;

        //extract for sen1
        double[] word1vector = new double[wordList.size()];
        for (int i = 0; i < wordList.size(); i++) {

            String w = wordList.get(i);
            if (w1Set.contains(w)) {
                word1vector[i] = 1;
            } else {
                double max_sim = Double.MIN_VALUE;

                for (String words : w1Set) {
                    max_sim = Math.max(max_sim, compute(words,w));
                }

                word1vector[i] = max_sim > threshold ? max_sim : 0;
            }

        }

        double[] word2vector = new double[wordList.size()];
        for (int i = 0; i < wordList.size(); i++) {

            String w = wordList.get(i);
            if (w2Set.contains(w)) {
                word2vector[i] = 1;
            } else {
                double max_sim = Double.MIN_VALUE;

                for (String words : w2Set) {
                    max_sim = Math.max(max_sim, compute(words,w));
                }

                word2vector[i] = max_sim > threshold ? max_sim : 0;
            }

        }


        return getCosineSimilarity(word1vector,word2vector);
    }

    private static double getCosineSimilarity(double[] vectorA, double[] vectorB) {

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));

    }

    private static double compute(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);

        if (db == null) {
            db = new NictWordNet();
        }

        double s = 0;
        try {
            s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return s;
    }


    public static boolean checkSynonymn(String w1, String w2, POS pos) {

        String path = "data/dict";
        URL url;
        try {
            url = new URL("file", null, path);

            // construct the dictionary object and open it
            IDictionary dict = new Dictionary(url);

            dict.open();

            // check w1 against w2
            IIndexWord idxWord = dict.getIndexWord(w1, pos);
            IWordID wordID = idxWord.getWordIDs().get(0);
            IWord word = dict.getWord(wordID);
            ISynset synset = word.getSynset();

            HashSet<String> synonymnList = new HashSet<>();
            for (IWord w : synset.getWords())
                synonymnList.add(w.getLemma());

            if (synonymnList.contains(w2)) {

                dict.close();
                return true;
            }

            // check w2 against w1
            idxWord = dict.getIndexWord(w2, pos);
            wordID = idxWord.getWordIDs().get(0);
            word = dict.getWord(wordID);
            synset = word.getSynset();

            synonymnList = new HashSet<>();
            for (IWord w : synset.getWords())
                synonymnList.add(w.getLemma());


            if (synonymnList.contains(w1)) {

                dict.close();
                return true;
            }


            dict.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            return false;
        }


        return false;
    }

    public static boolean checkFeatureEquality(AppFeatureDataPoint f1,
                                               AppFeatureDataPoint f2) {
        return NLPUtil.checksynonymnDistance(f1.getVerb(), f1.getNoun(),
                f2.getVerb(), f2.getNoun());
    }

    public static double getBigramColocScore(Bigram bg) {
        //we take average of both the phrasal verb + verb/noun pair
        String verbKey = bg.toVerbKey();
        double sum;
        if (verbKey != null) {
            sum = (DataAccess.getColocScore(bg.toKey()) + DataAccess.getColocScore(verbKey));
        } else {
            return DataAccess.getColocScore(bg.toKey());
        }
        return sum > 0 ? sum / 2 : 0;

    }

    public static int analyzeSentiment(String line) {

        int mainSentiment = 0;
        if (line != null && line.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(line);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }

            }
        }

        return mainSentiment;
    }

    public static boolean checkNegativeFeature(String feature) {

        feature = feature.replaceAll("#", " ");

        int sentimentScore = analyzeSentiment(feature);

        if (sentimentScore == 0 || sentimentScore == 1) {
            return true;
        }

        return false;
    }

}
