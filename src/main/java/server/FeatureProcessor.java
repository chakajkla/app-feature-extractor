package server;

import java.util.List;

import server.nlp.BigramExtractor;
import server.nlp.FeatureParser;
import server.nlp.IndexBuilder;
import server.nlp.IndexBuilder.TYPE;
import server.nlp.NLPUtil;
import server.objects.AndroidApp;
import server.objects.AppFeatureDescriptor;
import server.objects.Bigram;
import server.crawler.PlayStoreAppPageCrawler;
import server.database.DataAccess;

public class FeatureProcessor {

	public static AppFeatureDescriptor getAppFeatures(String packageID) {

		AppFeatureDescriptor featurelist = null;

		// check if app is present in local database
		if (!containPackageID(packageID)) {
			// if not crawl it
			AndroidApp app = PlayStoreAppPageCrawler.crawlAppPages(packageID);
			if (app != null) {
				// update databse for packageID + description etc.
				updateDatabase(app);

				String name = app.getPackageName();
				String description = app.getDescription();

				System.out.println(name + " __ " + description);

				List<String> tokenizedString = NLPUtil
						.preprocessString(description);
				String processed_desc = NLPUtil.assembleString(tokenizedString);

				System.out.println("Processed desc : " + processed_desc);

				// update Lucene index
				IndexBuilder.addIndex(processed_desc, name, TYPE.android);

				List<Bigram> bigrams = BigramExtractor
						.extractBigram(tokenizedString);
				featurelist = FeatureParser.preprocessAppFeature(bigrams,
						description, name);

				// store for future access
				storeFeatures(featurelist);

			} else {
				// this app could not be crawled and no info in database
				return null;
			}

		} else {
			featurelist = DataAccess.getFeatures(packageID);
		}

		// Apply softmax to feature scores
		featurelist = FeatureParser.applyScoreFilter(featurelist);

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

}