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

public class NLPUtil {

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

        tokenizer = NLPUtil.removeBadWords(tokenizer);

        ArrayList<String> cleanSentences = new ArrayList<>();

        for (String sentence : tokenizer) {

            String featureString = NLPUtil.constructString(NLPUtil
                    .lemmatize(sentence));

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
                    new FileReader("data/stopwords_ranksnl.txt"));

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

        // check quality
        if (w1_verb.equals(w2_verb) && w1_noun.equals(w2_noun)) {
            return true;
        }

        if (checkSynonymn(w1_noun, w2_noun, POS.NOUN)
                && checkSynonymn(w1_verb, w2_verb, POS.VERB)) {
            return true;
        }

        return false;
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

        return DataAccess.getColocScore(bg.toKey());

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
