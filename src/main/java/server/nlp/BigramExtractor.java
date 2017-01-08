package server.nlp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import server.objects.Bigram;

public class BigramExtractor {

    public static int dist = 5; //window length

    public static List<Bigram> extractBigram(List<String> tokenizedString, String raw_description) {

        List<String> tokenizedStringRaw = NLPUtil.tokenizeString(raw_description);
        boolean sameSize = false;
        if (tokenizedStringRaw.size() == tokenizedString.size()) {
            sameSize = true;
        }

        List<Bigram> bigrams = new ArrayList<>();

        Set<String> checkList = new HashSet<>();

        for (int sentenceIndex = 0; sentenceIndex < tokenizedString.size(); sentenceIndex++) {

            String sentence = tokenizedString.get(sentenceIndex);

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

                //find raw verb form
                String rawVerb = null;
                if (sameSize) {
                    String rawSentence = tokenizedStringRaw.get(sentenceIndex);
                    rawVerb = findRawVerb(rawSentence, verb);
                } else {
                    String rs1 = tokenizedStringRaw.get(sentenceIndex);
                    String rs2 = tokenizedStringRaw.get(sentenceIndex - 1 > 0 ? sentenceIndex - 1 : sentenceIndex);
                    String rs3 = tokenizedStringRaw.get(sentenceIndex + 1 < tokenizedStringRaw.size() ? sentenceIndex + 1 : sentenceIndex);
                    rawVerb = findRawVerb(rs1, verb) != null ? findRawVerb(rs1, verb) : findRawVerb(rs2, verb) != null ? findRawVerb(rs2, verb) :
                            findRawVerb(rs3, verb) != null ? findRawVerb(rs3, verb) : null;
                }

                // find nextInt
                int nextInt = i + dist;

                if (nextInt >= tokens.length) {
                    nextInt = tokens.length - 1;
                }

                int index = i;
                if (preposition != null || particle != null) {
                    index = i + 1; //skip the preposition/particle
                }

                for (int j = index + 1; j <= nextInt; j++) {
                    String noun = tokens[j];

                    if (verb != null && noun != null) {
                        String key = verb + "#" + noun;

                        if (!checkList.contains(key) && !verb.equals(noun) && !NLPUtil.checkNegativeFeature(key)) {
                            Bigram bi = new Bigram(verb, noun);
                            bi.setParticle(particle);
                            bi.setPreposition(preposition);
                            bi.setRawVerb(rawVerb);
                            bigrams.add(bi);
                            checkList.add(key);
                        }
                    }
                }

            }

        }

        return bigrams;
    }


    public static String findRawVerb(String rawSentence, String verb) {

        String[] sp = rawSentence.split("\\s+");

        for (String possibleVerb : sp) {
            possibleVerb = possibleVerb.toLowerCase();
            if (possibleVerb.contains(verb) || possibleVerb.equals(verb)) {
                return possibleVerb;
            }
        }
        return null;
    }


}
