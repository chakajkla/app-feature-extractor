package server.nlp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import server.objects.Bigram;

public class BigramExtractor {

	public static int dist = 5; //window length

	public static List<Bigram> extractBigram(List<String> tokenizedString) {

		List<Bigram> bigrams = new ArrayList<>();

		Set<String> checkList = new HashSet<>();

		for (String sentence : tokenizedString) {

			String[] tokens = sentence.split("\\s+");

			// System.out.println(Arrays.toString(tokens));

			for (int i = 0; i < tokens.length; i++) {

				String verb = tokens[i];

				// find nextInt
				int nextInt = i + dist;

				if (nextInt >= tokens.length) {
					nextInt = tokens.length - 1;
				}

				for (int j = i + 1; j <= nextInt; j++) {
					String noun = tokens[j];

					String key = verb + "#" + noun;

					if (!checkList.contains(key) && !verb.equals(noun) && !NLPUtil.checkNegativeFeature(key)) {
						bigrams.add(new Bigram(verb, noun));
						checkList.add(key);
					}
				}

			}

		}

		return bigrams;
	}

}
