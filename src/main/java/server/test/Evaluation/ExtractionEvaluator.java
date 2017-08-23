package server.test.Evaluation;

import server.FeatureProcessor;
import server.log.LogUtil;
import server.nlp.FeatureParser;
import server.nlp.NLPUtil;
import server.objects.AppFeatureDataPoint;
import server.objects.AppFeatureDescriptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static server.nlp.FeatureParser.applyScoreNormalization;

public class ExtractionEvaluator {

    public static void main5(String[] args) {
        tagTest();
    }

    public static void main2(String[] args) {

        File descDir = new File("data/evaluation/apps");
        File goldenDir = new File("data/evaluation/apps/features");

        double global_ct = 0;
        double global_prec = 0;
        double global_rec = 0;

        double threshold = 0.6;

        for (File f : descDir.listFiles()) {

            if (!f.isDirectory()) {

                global_ct++;

                String packageName = f.getName().replaceAll(".txt", "");
                String description = readFileContent(f);

                File goldenFile = new File(goldenDir + "/" + f.getName());
                String goldenFeatures = readFileContent(goldenFile);
                ArrayList<AppFeatureDataPoint> goldenList = new ArrayList<>();
                for (String feature : goldenFeatures.split("\\n")) {
                    String[] sp = feature.split("\\s");
                    if (sp.length == 2) {
                        AppFeatureDataPoint af = new AppFeatureDataPoint();
                        LogUtil.log(feature);
                        af.setVerb(sp[0]);
                        af.setNoun(sp[1]);
                        goldenList.add(af);
                    } else {
                        AppFeatureDataPoint af = new AppFeatureDataPoint();
                        LogUtil.log(feature);
                        af.setVerb(sp[0]);
                        af.setNoun("thing");
                        goldenList.add(af);
                    }
                }

                AppFeatureDescriptor ap = FeatureProcessor.extractFeatures(packageName, description);

                ap = applyScoreNormalization(ap);
                ArrayList<AppFeatureDataPoint> scoredList = new ArrayList<>();
                for (AppFeatureDataPoint fe : ap.getFunctionList()) {
                    LogUtil.log(fe.toString() + " " + fe.getScore());
                    //0.20 24 , 41
                    if (fe.getScore() >= threshold) {
                        scoredList.add(fe);
                    }
                }

                double localPrec = calculatePrecision(scoredList, goldenList);;
                double localRec = calculateRecall(scoredList, goldenList);

                LogUtil.log("local prec + " + localPrec);
                LogUtil.log("local rec + " + localRec);

                global_prec += localPrec;
                global_rec += localRec;

            }

        }

        LogUtil.log("Precision "+threshold+" : " + global_prec / global_ct);
        LogUtil.log("Recall "+threshold+" : " + global_rec / global_ct);

    }

    private static String readFileContent(File f) {

        try {
            return new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static double calculatePrecision(List<AppFeatureDataPoint> extractedList, List<AppFeatureDataPoint> goldenList) {

        double correctFeatures = 0;
        for (AppFeatureDataPoint fe1 : extractedList) {
            for (AppFeatureDataPoint fe2 : goldenList) {
                if (NLPUtil.checksynonymnDistance(fe1.getVerb(), fe1.getNoun(), fe2.getVerb(), fe2.getNoun()) || checkSimilarity(fe1, fe2)) {
                    correctFeatures++;
                }
            }
        }

        if (correctFeatures > 0) {
            return correctFeatures / (double) extractedList.size();
        }

        return 0;
    }

    public static double calculateRecall(List<AppFeatureDataPoint> extractedList, List<AppFeatureDataPoint> goldenList) {

        double correctFeatures = 0;
        for (AppFeatureDataPoint fe1 : extractedList) {
            for (AppFeatureDataPoint fe2 : goldenList) {
                if (NLPUtil.checksynonymnDistance(fe1.getVerb(), fe1.getNoun(), fe2.getVerb(), fe2.getNoun()) || checkSimilarity(fe1, fe2)) {
                    correctFeatures++;
                }
            }
        }

        if (correctFeatures > 0) {
            return correctFeatures / (double) goldenList.size();
        }

        return 0;
    }

    public static void tagTest() {


        String tagged = FeatureParser.tagString("turn off");
        LogUtil.log(tagged);
        tagged = FeatureParser.tagString("pick on");
        LogUtil.log(tagged);
        tagged = FeatureParser.tagString("dial down");
        LogUtil.log(tagged);
        tagged = FeatureParser.tagString("turn on");
        LogUtil.log(tagged);
        tagged = FeatureParser.tagString("service for watching TV episodes");
        LogUtil.log(tagged);
        tagged = FeatureParser.tagString("rate tv");
        LogUtil.log(tagged);
        tagged = FeatureParser.tagString("Share updates, photos and videos");
        LogUtil.log(tagged);
//
//        String path = "data" + File.separator + "dict";
//        URL url;
//        // construct the dictionary object and open it
//        IDictionary dict = null;
//        try {
//            url = new URL("file", null, path);
//
//            dict = new Dictionary(url);
//
//            dict.open();
//
//            // get verb
//            IIndexWord idxWord = dict.getIndexWord("share", POS.VERB);
//            idxWord.getWordIDs().get(0);
//
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//
//        } finally {
//            if (dict != null) {
//                dict.close();
//            }
//        }

        tagged = FeatureParser.tagString("You won’t need to delete messages to save space.");
        LogUtil.log(tagged);

        tagged = FeatureParser.tagString("you don't need to pay for every message");
        LogUtil.log(tagged);
    }

    public static boolean checkSimilarity(AppFeatureDataPoint fe1, AppFeatureDataPoint fe2) {

        if (fe2.getVerb().contains(fe1.getVerb()) && fe2.getNoun().contains(fe1.getNoun())) {
            return true;
        }
        return false;

    }


}
