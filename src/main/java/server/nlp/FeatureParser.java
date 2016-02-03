package server.nlp;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	public static void clusterFeatureMap(
			Map<String, AppFeatureDescriptor> appFeatureMap) throws Exception {

		ArrayList<AppFeatureDataPoint> allFeatures = new ArrayList<>();

		for (String key : appFeatureMap.keySet()) {
			allFeatures.addAll(appFeatureMap.get(key).getFunctionList());
		}

		System.out.println(allFeatures.size());

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

		}

		// process FG
		// Unique_Key -> AppFeatureDataPoint
		Map<String, AppFeatureDataPoint> featureReducedMap = new HashMap<String, AppFeatureDataPoint>();
		for (FeatureGroup fg : clusterList) {
			for (AppFeatureDataPoint dp : fg.getGroupMembers()) {
				featureReducedMap.put(dp.getUniqueName(), fg.getGroupLeader());
			}
		}

		for (String key : appFeatureMap.keySet()) {
			for (AppFeatureDataPoint dp : appFeatureMap.get(key)
					.getFunctionList()) {
				if (featureReducedMap.get(dp.getUniqueName()) == null) {
					throw new Exception("Clustering fault");
				}

				// if (!dp.toString().equals(
				// featureReducedMap.get(dp.getUniqueName()).toString()))
				// System.out.println(dp.toString()
				// + " __ "
				// + featureReducedMap.get(dp.getUniqueName())
				// .toString() + " __ " + dp.getUniqueName());
			}
		}

	}

	private static MaxentTagger tagger = null;
	private static DependencyParser parser = null;

	public static AppFeatureDescriptor preprocessAppFeature(
			List<Bigram> bigrams, String description, String name) {

		AppFeatureDescriptor ap = new AppFeatureDescriptor();
		ap.setDescription(description);
		ap.setName(name);

		if (tagger == null) {
			tagger = new MaxentTagger(
					"data/postagger/english-bidirectional-distsim.tagger");
		}
		if (parser == null) {
			parser = DependencyParser
					.loadFromModelFile(DependencyParser.DEFAULT_MODEL);
		}

		HashSet<String> negVerbDict = findNegativeVerbs(tagger, parser,
				description);

		for (Bigram bg : bigrams) {

			double NgramScore = getBigramScore(bg);

			String tagged = tagger.tagString(bg.toString());

			boolean status = checkFeature(bg.getVerb(), bg.getNoun(), tagged,
					negVerbDict);

			/**
			 * Chi-Square Pearson check at 0.005
			 */
			if (NgramScore < 7.88)
				status = false;

			if (status) {

				// search using only noun
				double indexScore = 0;
				try {
					indexScore = IndexBuilder.getIndexScore(bg.getVerb() + " "
							+ bg.getNoun(), name, TYPE.android);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// System.out.println(status + "__" + tagged
				// + "  __ NGramScore: " + NgramScore
				// + " __ IndexScore: " + indexScore);

				AppFeatureDataPoint app_feature = new AppFeatureDataPoint();
				app_feature.setName(name);
				app_feature.setVerb(bg.getVerb());
				app_feature.setNoun(bg.getNoun());
				app_feature.setNgramScore(NgramScore);
				app_feature.setTfScore(indexScore);

				ap.addFunctionList(app_feature);

			}

		}

		return ap;

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

	private static boolean checkFeature(String verb, String noun,
			String tagged, HashSet<String> negVerbDict) {

		if (noun.length() <= 1 || verb.length() <= 1) {
			return false;
		}

		if (negVerbDict.contains(verb)) {
			return false;
		}

		String path = "data" + File.separator + "dict";
		URL url;
		try {
			url = new URL("file", null, path);

			// construct the dictionary object and open it
			IDictionary dict = new Dictionary(url);

			dict.open();

			// get verb
			IIndexWord idxWord = dict.getIndexWord(verb, POS.VERB);
			idxWord.getWordIDs().get(0);

			// get noun
			idxWord = dict.getIndexWord(noun, POS.NOUN);
			idxWord.getWordIDs().get(0);

			// check if second component is NN
			String[] sp = tagged.split("\\s");

			if (sp[1].contains("NN")) {
				return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			return false;
		}

		return false;
	}

	public static AppFeatureDescriptor applyScoreFilter(
			AppFeatureDescriptor featurelist) {

		double[] tfscore = new double[featurelist.getFunctionList().size()];
		double[] ngramscore = new double[featurelist.getFunctionList().size()];

		for (int i = 0; i < featurelist.getFunctionList().size(); i++) {

			AppFeatureDataPoint dp = featurelist.getFunctionList().get(i);
			tfscore[i] = dp.getTfScore();
			ngramscore[i] = dp.getNgramScore();

			// System.out.println(dp.toString() + "\n\n");

		}

		// softmax
		tfscore = softmax(tfscore);
		ngramscore = softmax(ngramscore);

		for (int i = 0; i < featurelist.getFunctionList().size(); i++) {

			AppFeatureDataPoint dp = featurelist.getFunctionList().get(i);
			dp.setTfScore(tfscore[i]);
			dp.setNgramScore(ngramscore[i]);

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
