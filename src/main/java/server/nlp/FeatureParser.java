package server.nlp;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import server.nlp.IndexBuilder.TYPE;
import server.nlp.featureCluster.FeatureGroup;
import server.objects.AppFeatureDataPoint;
import server.objects.AppFeatureDescriptor;
import server.objects.Bigram;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class FeatureParser {

    public static AppFeatureDescriptor clusterFeatureMap(
            AppFeatureDescriptor appFeatureDescriptor) throws Exception {

        if (appFeatureDescriptor == null || appFeatureDescriptor.getFunctionList() == null) {
            return null;
        }

        ArrayList<AppFeatureDataPoint> allFeatures = new ArrayList<>();

//		for (String key : appFeatureMap.keySet()) {
//			allFeatures.addAll(appFeatureMap.get(key).getFunctionList());
//		}

        allFeatures.addAll(appFeatureDescriptor.getFunctionList());

        Collections.shuffle(allFeatures);

        System.out.println("Number of features before filter: " + allFeatures.size());


        Collections.sort(allFeatures);

        //filter by average score
        double av = 0;
        for (AppFeatureDataPoint fe : allFeatures) {
            av += fe.getScore();
        }
        av /= (double) allFeatures.size();
        System.out.println("Average score = " + av);
        ArrayList<AppFeatureDataPoint> newFeatures = new ArrayList<>();
        for (int i = 0; i < allFeatures.size(); i++) {
            AppFeatureDataPoint fe = allFeatures.get(i);
            if (fe.getScore() >= av) {
                newFeatures.add(fe);
            }
        }
        allFeatures.clear();
        allFeatures.addAll(newFeatures);

        System.out.println("Number of features before reduction: " + allFeatures.size());


        ArrayList<FeatureGroup> clusterList = new ArrayList<>();
        while (!allFeatures.isEmpty()) {
            // System.out.println(allFeatures.size());
            // remove first feature
            AppFeatureDataPoint fea = allFeatures.remove(0);
            FeatureGroup fg = new FeatureGroup();
            fg.addGroupMember(fea);
            for (int i = 0; i < allFeatures.size(); i++) {
                AppFeatureDataPoint other_fea = allFeatures.get(i);

                if (fg.canAddMember(other_fea)) {
                    fg.addGroupMember(other_fea);
                    allFeatures.remove(i);
                }
            }

            clusterList.add(fg);

            // Collections.shuffle(allFeatures);
        }

        //at this point we clear the old list
        appFeatureDescriptor.clearFunctionList();


        //score proportion trick
        double totalScore = 0;
        for (FeatureGroup fg : clusterList) {
            totalScore += fg.getAverageScore();
        }

        for (FeatureGroup fg : clusterList) {
            int pickSize = (int) ((fg.getAverageScore() / totalScore) * fg.getGroupMembers().size());
            if (pickSize <= 0) {
                pickSize = 1;
            }
            for (AppFeatureDataPoint fe : fg.getAdditionalMembers(pickSize)) {
                appFeatureDescriptor.addFunctionList(fe);
            }
        }


        // process FG
        // Unique_Key -> AppFeatureDataPoint
//        Map<String, AppFeatureDataPoint> featureReducedMap = new HashMap<String, AppFeatureDataPoint>();
//        for (FeatureGroup fg : clusterList) {
////			for (AppFeatureDataPoint dp : fg.getGroupMembers()) {
////				featureReducedMap.put(dp.getUniqueName(), fg.getGroupLeader());
////			}
//            appFeatureDescriptor.addFunctionList(fg.getGroupLeader());
//
//            for (AppFeatureDataPoint fe : fg.getAdditionalMembers()) {
//                appFeatureDescriptor.addFunctionList(fe);
//            }
//        }

//		for (String key : appFeatureMap.keySet()) {
//			for (AppFeatureDataPoint dp : appFeatureMap.get(key)
//					.getFunctionList()) {
//				if (featureReducedMap.get(dp.getUniqueName()) == null) {
//					throw new Exception("Clustering fault");
//				}
//
//				// if (!dp.toString().equals(
//				// featureReducedMap.get(dp.getUniqueName()).toString()))
//				// System.out.println(dp.toString()
//				// + " __ "
//				// + featureReducedMap.get(dp.getUniqueName())
//				// .toString() + " __ " + dp.getUniqueName());
//			}
//		}


        return appFeatureDescriptor;

    }

    private static MaxentTagger tagger = null;
    private static DependencyParser parser = null;

    public static String tagString(String input) {
        if (tagger == null) {
            tagger = new MaxentTagger(
                    "data/postagger/english-bidirectional-distsim.tagger");
        }
        return tagger.tagString(input);
    }

    public static AppFeatureDescriptor preprocessAppFeature(
            List<Bigram> bigrams, String description, String name) {

        AppFeatureDescriptor ap = new AppFeatureDescriptor();
        ap.setDescription(description);
        ap.setName(name);


        if (parser == null) {
            parser = DependencyParser
                    .loadFromModelFile(DependencyParser.DEFAULT_MODEL);
        }

        HashSet<String> negVerbDict = findNegativeVerbs(tagger, parser,
                description);

        for (Bigram bg : bigrams) {

            double NgramScore = getBigramScore(bg); //colocation score calculated separately using Python

            boolean status = checkFeature(bg, negVerbDict);

//            /**
//             * Chi-Square Pearson check at 0.005
//             */
//            if (NgramScore < 7.88)

            //average 3.4077
//            if (NgramScore <= 3.4077)
//                status = false;

            if (status) {

                // search using only noun
                double indexScore = 0;
                try {
                    indexScore += IndexBuilder.getIndexScore(bg.toString(), name, TYPE.android);
                    if (!bg.isSingleVerb()) {
                        indexScore += IndexBuilder.getIndexScore(bg.toVerbString(), name, TYPE.android);
                        indexScore /= 2;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    indexScore = 0;
                }
                // System.out.println(status + "__" + tagged
                // + "  __ NGramScore: " + NgramScore
                // + " __ IndexScore: " + indexScore);

                AppFeatureDataPoint app_feature = new AppFeatureDataPoint();
                app_feature.setName(name);
                app_feature.setVerb(bg.getVerb());
                app_feature.setNoun(bg.getNoun());
                app_feature.setParticle(bg.getParticle());
                app_feature.setPreposition(bg.getPreposition());
                app_feature.setNgramScore(NgramScore);
                app_feature.setTfScore(indexScore);

                ap.addFunctionList(app_feature);

            }

        }

        //re-scoring based on most frequent NN
        setNNScore(ap);
        setVBScore(ap);

        //normalize scores
        // Apply softmax to feature scores
        //ap = FeatureParser.applyScoreFilter(ap);

        //clustering of features
//        try {
//            return clusterFeatureMap(ap);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return ap;

    }

    private static void setNNScore(AppFeatureDescriptor ap) {
        Map<String, Double> histogram = new HashMap<>();
        for (AppFeatureDataPoint fe : ap.getFunctionList()) {
            if (!histogram.containsKey(fe.getNoun())) {
                histogram.put(fe.getNoun(), 1d);
            } else {
                histogram.put(fe.getNoun(), histogram.get(fe.getNoun()) + 1);
            }
        }


        for (AppFeatureDataPoint fe : ap.getFunctionList()) {
            fe.setNnFreqScore(histogram.get(fe.getNoun()));
        }

    }

    private static void setVBScore(AppFeatureDescriptor ap) {
        Map<String, Double> histogram = new HashMap<>();
        for (AppFeatureDataPoint fe : ap.getFunctionList()) {
            if (!histogram.containsKey(fe.getVerb())) {
                histogram.put(fe.getVerb(), 1d);
            } else {
                histogram.put(fe.getVerb(), histogram.get(fe.getVerb()) + 1);
            }
        }

        for (AppFeatureDataPoint fe : ap.getFunctionList()) {
            fe.setVbFreqScore(histogram.get(fe.getVerb()));
        }

    }

    private static double getBigramScore(Bigram bg) {

        return NLPUtil.getBigramColocScore(bg);
    }

    private static HashSet<String> findNegativeVerbs(MaxentTagger tagger,
                                                     DependencyParser parser, String text) {

        HashSet<String> badVerbs = new HashSet<>();

        DocumentPreprocessor tokenizer = new DocumentPreprocessor(
                new StringReader(text));

        for (List<HasWord> sentence : tokenizer) {

            List<HasWord> p_sentence = processSentence(sentence);
            List<TaggedWord> tagged = tagger.tagSentence(p_sentence);

            GrammaticalStructure gs = parser.predict(tagged);

            // Print typed dependencies

            for (TypedDependency dp : gs.allTypedDependencies()) {

                if (dp.toString().contains("neg(")) {
                    // System.out.println(dp.toString());

                    badVerbs.add(dp.toString().substring(4,
                            dp.toString().indexOf(",") - 2));
                }
            }

        }

        // System.out.println(badVerbs.toString());
        return badVerbs;
    }

    private static List<HasWord> processSentence(List<HasWord> sentence) {

        List<HasWord> processList = new ArrayList<>();
        for (int i = 0; i < sentence.size(); i++) {
            HasWord w = sentence.get(i);
            String newWord = NLPUtil.removeNonCharacters(w.toString()
                    .toLowerCase());
            // System.out.println(w.toString().toLowerCase() + " __ " +
            // newWord);
            Pattern p = Pattern.compile("[^a-z]");
            boolean hasSpecialChar = p.matcher(w.toString()).find();

            if (!newWord.equals("") && newWord.length() >= 2 && !hasSpecialChar) {
                // System.out.println(newWord);
                w.setWord(newWord);
                processList.add(w);
            }

        }

        // System.out.println(processList);

        return processList;
    }

    private static boolean checkFeature(Bigram bg, HashSet<String> negVerbDict) {

        if (bg.getAdverb() != null) {
            return false;
        }

        String verb = bg.getVerb();
        String noun = bg.getNoun();
        String rawVerbString = bg.toRawVerbString();

        String tagged = tagString(bg.toString());
        String taggedVerb = tagString(bg.toVerbString());
        String taggedRawVerb = rawVerbString != null ? tagString(rawVerbString) : null;

        if (noun.length() <= 1 || verb.length() <= 1) {
            return false;
        }

        if (negVerbDict.contains(verb)) {
            return false;
        }

        boolean status = false;
        String path = "data" + File.separator + "dict";
        URL url;
        // construct the dictionary object and open it
        IDictionary dict = null;
        try {
            url = new URL("file", null, path);

            dict = new Dictionary(url);

            dict.open();

            // get verb
            IIndexWord idxWord = dict.getIndexWord(verb, POS.VERB);
            idxWord.getWordIDs().get(0);

            // get noun
            idxWord = dict.getIndexWord(noun, POS.NOUN);
            idxWord.getWordIDs().get(0);

            status = true;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {

        } finally {
            if (dict != null) {
                dict.close();
            }
        }

        // check if second component is NN
        String[] sp = tagged.split("\\s");
        String[] verbSp = taggedVerb.split("\\s");
        String[] rawVerbSp = null;
        if (taggedRawVerb != null) {
            rawVerbSp = taggedRawVerb.split("\\s");
        }

        //strictly VB-NN pair or VB-Particle/Preposition pair
        if (((sp[1].contains("NN") && sp[0].contains("VB")) //normal case
                || (sp[1].contains("NN") && verbSp[0].contains("VB")) // phrasal verb case
                || (rawVerbSp != null && rawVerbSp.length == 2 ? sp[1].contains("NN") && rawVerbSp[0].contains("VB") : false) //raw verb case
        )
                || status) {
            return true;
        }

        return false;
    }

    public static AppFeatureDescriptor applyScoreNormalization(
            AppFeatureDescriptor featurelist) {

        if (featurelist == null || featurelist.getFunctionList() == null) {
            return null;
        }
        //normalize to 0-1
        double min1 = Double.MAX_VALUE;
        double min2 = Double.MAX_VALUE;
        double min3 = Double.MAX_VALUE;
        double max1 = 0;
        double max2 = 0;
        double max3 = 0;
        for (AppFeatureDataPoint dp : featurelist.getFunctionList()) {
            min1 = Math.min(min1, dp.getTfScore());
            max1 = Math.max(max1, dp.getTfScore());
            min2 = Math.min(min2, dp.getNgramScore());
            max2 = Math.max(max2, dp.getNgramScore());
            min3 = Math.min(min3, dp.getNnFreqScoreScore());
            max3 = Math.max(max3, dp.getNnFreqScoreScore());
        }
        for (AppFeatureDataPoint dp : featurelist.getFunctionList()) {
            dp.setTfScore((dp.getTfScore() - min1) / (max1 - min1));
            dp.setNgramScore((dp.getNgramScore() - min2) / (max2 - min2));
            dp.setNnFreqScore((dp.getNnFreqScoreScore() - min3) / (max3 - min3));
        }

        return featurelist;
    }

    public static AppFeatureDescriptor applyScoreFilter(
            AppFeatureDescriptor featurelist) {

        if (featurelist == null || featurelist.getFunctionList() == null) {
            return null;
        }

        double[] tfscore = new double[featurelist.getFunctionList().size()];
        double[] ngramscore = new double[featurelist.getFunctionList().size()];
        double[] nnCtscore = new double[featurelist.getFunctionList().size()];

        for (int i = 0; i < featurelist.getFunctionList().size(); i++) {

            AppFeatureDataPoint dp = featurelist.getFunctionList().get(i);
            tfscore[i] = dp.getTfScore();
            ngramscore[i] = dp.getNgramScore();
            nnCtscore[i] = dp.getNnFreqScoreScore();
            // System.out.println(dp.toString() + "\n\n");

        }

        // softmax
        tfscore = softmax(tfscore);
        ngramscore = softmax(ngramscore);
        nnCtscore = softmax(nnCtscore);

        for (int i = 0; i < featurelist.getFunctionList().size(); i++) {

            AppFeatureDataPoint dp = featurelist.getFunctionList().get(i);
            dp.setTfScore(tfscore[i]);
            dp.setNgramScore(ngramscore[i]);
            dp.setNnFreqScore(nnCtscore[i]);

            // System.out.println(dp.toString() + "\n");

        }

        return featurelist;
    }

    private static double[] softmax(double[] inputs) {

        if (inputs.length <= 1) {
            return inputs;
        }

        double[] outputs = new double[inputs.length];
        double expSum = 0.0;
        for (int i = 0; i < inputs.length; i++) {
            expSum += Math.exp(Math.log(inputs[i]));
        }

        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = Math.exp(Math.log(inputs[i])) / expSum;
        }
        return outputs;
    }
}
