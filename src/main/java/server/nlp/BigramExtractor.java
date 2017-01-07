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

            String taggedString = FeatureParser.tagString(sentence);

            String[] taggedTokens = taggedString.split("\\s+");
            String[] tokens = sentence.split("\\s+");

            // System.out.println(Arrays.toString(tokens));

            for (int i = 0; i < tokens.length; i++) {

                String verb = tokens[i];
                String preposition = null;
                String particle = null;
                if (i < tokens.length - 1) {
                    preposition = taggedTokens[i + 1].contains("IN") ? tokens[i + 1] : null;
                    particle = taggedTokens[i + 1].contains("RP") ? tokens[i + 1] : null;
                }

                // find nextInt
                int nextInt = i + dist;

                if (nextInt >= tokens.length) {
                    nextInt = tokens.length - 1;
                }

                int index = i;
                if(preposition != null || particle != null){
                    index = i + 1; //skip the preposition/particle
                }

                for (int j = index + 1; j <= nextInt; j++) {
                    String noun = tokens[j];

                    String key = verb + "#" + noun;

                    if (!checkList.contains(key) && !verb.equals(noun) && !NLPUtil.checkNegativeFeature(key)) {
                        Bigram bi = new Bigram(verb, noun);
                        bi.setParticle(particle);
                        bi.setPreposition(preposition);
                        bigrams.add(bi);
                        checkList.add(key);
                    }
                }

            }

        }

        return bigrams;
    }

}
