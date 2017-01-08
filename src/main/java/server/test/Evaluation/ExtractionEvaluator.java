package server.test.Evaluation;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import server.FeatureProcessor;
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

public class ExtractionEvaluator {

    public static void mainner(String[] args) {
        tagTest();
    }

//    public static void main(String[] args) {
//
//        File descDir = new File("data/evaluation/apps");
//        File goldenDir = new File("data/evaluation/apps/features");
//
//        double global_ct = 0;
//        double global_prec = 0;
//        double global_rec = 0;
//
//        for (File f : descDir.listFiles()) {
//
//            if (!f.isDirectory()) {
//
//                global_ct++;
//
//                String packageName = f.getName().replaceAll(".txt", "");
//                String description = readFileContent(f);
//
//                File goldenFile = new File(goldenDir + "/" + f.getName());
//                String goldenFeatures = readFileContent(goldenFile);
//                ArrayList<AppFeatureDataPoint> goldenList = new ArrayList<>();
//                for (String feature : goldenFeatures.split("\\n")) {
//                    String[] sp = feature.split("\\s");
//                    if (sp.length == 2) {
//                        AppFeatureDataPoint af = new AppFeatureDataPoint();
//                        System.out.println(feature);
//                        af.setVerb(sp[0]);
//                        af.setNoun(sp[1]);
//                        goldenList.add(af);
//                    } else {
//                        AppFeatureDataPoint af = new AppFeatureDataPoint();
//                        System.out.println(feature);
//                        af.setVerb(sp[0]);
//                        af.setNoun("thing");
//                        goldenList.add(af);
//                    }
//                }
//
//                AppFeatureDescriptor ap = FeatureProcessor.extractFeatures(packageName, description);
//                for (AppFeatureDataPoint fe : ap.getFunctionList()) {
//                    System.out.println(fe.toString() + " " + fe.getScore());
//                }
//
//                global_prec += calculatePrecision(ap.getFunctionList(), goldenList);
//                global_rec += calculateRecall(ap.getFunctionList(), goldenList);
//
//            }
//
//        }
//
//        System.out.println("Precision : " + global_prec / global_ct);
//        System.out.println("Recall : " + global_rec / global_ct);
//
//    }

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
                if (NLPUtil.checksynonymnDistance(fe1.getVerb(), fe1.getNoun(), fe2.getVerb(), fe2.getNoun())) {
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
                if (NLPUtil.checksynonymnDistance(fe1.getVerb(), fe1.getNoun(), fe2.getVerb(), fe2.getNoun())) {
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
        System.out.println(tagged);
        tagged = FeatureParser.tagString("pick on");
        System.out.println(tagged);
        tagged = FeatureParser.tagString("dial down");
        System.out.println(tagged);
        tagged = FeatureParser.tagString("turn on");
        System.out.println(tagged);
        tagged = FeatureParser.tagString("service for watching TV episodes");
        System.out.println(tagged);
        tagged = FeatureParser.tagString("rate tv");
        System.out.println(tagged);
        tagged = FeatureParser.tagString("watched over");
        System.out.println(tagged);
    }


}
