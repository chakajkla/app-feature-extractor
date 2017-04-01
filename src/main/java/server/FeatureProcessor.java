package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import server.nlp.BigramExtractor;
import server.nlp.FeatureParser;
import server.nlp.IndexBuilder;
import server.nlp.IndexBuilder.TYPE;
import server.nlp.NLPUtil;
import server.objects.AndroidApp;
import server.objects.AppFeatureDataPoint;
import server.objects.AppFeatureDescriptor;
import server.objects.Bigram;
import server.crawler.PlayStoreAppPageCrawler;
import server.database.DataAccess;

public class FeatureProcessor {

    public static AppFeatureDescriptor getAppFeatures(String packageID) {

        AppFeatureDescriptor featurelist = null;

        boolean containOffline = containPackageIDOffline(packageID);
        boolean containDB = containPackageID(packageID);

        /*if (!containDB && !containOffline) {

            System.out.println("Features for " + packageID + " not found...building new features");

            // if not crawl it
            AndroidApp app = PlayStoreAppPageCrawler.crawlAppPages(packageID);
            if (app != null) {


                String name = app.getPackageName();
                String description = app.getDescription();

                System.out.println(name + " __ " + description);

                featurelist = extractFeatures(name, description);

                *//***
         * Update only features are extracted correctly
         *//*
                // update databse for packageID + description etc.
                updateDatabase(app);

                // store for future access
                storeFeatures(featurelist);

            } else {
                // this app could not be crawled and no info in database
                return null;
            }

        } else */
        if (containOffline) {

            featurelist = buildOfflineFeatureList(packageID);

        } /*else {
            System.out.println("Found features for " + packageID);
            featurelist = DataAccess.getFeatures(packageID);


        }*/

        if (!containOffline) {
            appendPackageID(packageID);
        }

        return featurelist;
    }

    private static final String missingPackageFilePath = "/home/vmadmin/data_storage/packages";

    private static void appendPackageID(String packageID) {

        Path FILE_PATH = Paths.get(missingPackageFilePath, "missing_packages.txt");
        List<String> packageIDs = new ArrayList<>();
        try (Stream<String> stream = Files.lines(FILE_PATH)) {

            packageIDs = stream.collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        HashSet<String> uniqueIDs = new HashSet<>();
        uniqueIDs.addAll(packageIDs);

        if (!uniqueIDs.contains(packageID)) {
            
            String newPackageID = packageID + "\n";
            //Writing to the file temp.txt
            try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                writer.write(newPackageID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static AppFeatureDescriptor buildOfflineFeatureList(String packageID) {

        AppFeatureDescriptor featurelist = new AppFeatureDescriptor();
        featurelist.setName(packageID);

        ArrayList<String[]> features = getOfflineFeatures(packageID);

        for (String[] feature : features) {
            AppFeatureDataPoint app_feature = new AppFeatureDataPoint();
            app_feature.setName(packageID);
            app_feature.setVerb(feature[0]);
            app_feature.setNoun(feature[1]);
            app_feature.setNgramScore(Double.parseDouble(feature[2]));

            featurelist.addFunctionList(app_feature);
        }

        return featurelist;

    }

    private static ArrayList<String[]> getOfflineFeatures(String packageID) {

        ArrayList<String[]> featureLists = new ArrayList<>();


        File dir = new File(apps_features_directory);
        if (!dir.exists()) {
            return featureLists;
        }
        for (File f : dir.listFiles()) {
            if (f.getName().contains(packageID)) {

                //read file
                try (Stream<String> stream = Files.lines(Paths.get(f.getAbsolutePath()))) {

                    stream.forEach(s -> {

                        String[] sp = s.split("\\s");

                        if (sp.length >= 2) {

                            String[] featurePts = new String[3];
                            Arrays.fill(featurePts, "0");


                            featurePts[0] = sp[0];
                            featurePts[1] = sp[1];

                            if (sp.length == 3) {
                                try {
                                    Double.parseDouble(sp[2]);
                                    featurePts[2] = sp[2];
                                } catch (NumberFormatException e) {
                                    //we ignore this since a feature maybe of 3 words
                                }

                            }

                            featureLists.add(featurePts);
                        }

                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }


        return featureLists;

    }

    public static AppFeatureDescriptor extractFeatures(String name, String description) {

        List<String> tokenizedString = NLPUtil
                .preprocessString(description);
        String processed_desc = NLPUtil.assembleString(tokenizedString);

        System.out.println("Processed desc : " + processed_desc);

        // update Lucene index
        IndexBuilder.addIndex(processed_desc, name, TYPE.android);

        List<Bigram> bigrams = BigramExtractor
                .extractBigram(tokenizedString, description);
        AppFeatureDescriptor featurelist = FeatureParser.preprocessAppFeature(bigrams,
                description, name);

        return featurelist;
    }


    private static void storeFeatures(AppFeatureDescriptor featurelist) {
        DataAccess.storeFeatures(featurelist);
    }

    private static void updateDatabase(AndroidApp app) {
        DataAccess.updateData(app);
    }

    private static boolean containPackageID(String packageID) {
        return DataAccess.checkPackageID(packageID);
    }

    private static final String apps_features_directory = "/home/vmadmin/server/data/evaluation/apps/features";

    private static boolean containPackageIDOffline(String packageID) {
        File dir = new File(apps_features_directory);
        if (!dir.exists()) {
            return false;
        }
        for (File f : dir.listFiles()) {
            if (f.getName().contains(packageID)) {
                return true;
            }
        }
        return false;
    }

}
