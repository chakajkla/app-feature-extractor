package server.nlp;

import server.objects.Bigram;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            // LogUtil.log(Arrays.toString(tokens));

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
                String possibleAdverb = null;
                if (sameSize) {
                    //String rawSentence = tokenizedStringRaw.get(sentenceIndex);
                    String taggedRawSentence = FeatureParser.tagString(tokenizedStringRaw.get(sentenceIndex));
                    int index = findRawVerb(taggedRawSentence, verb);
                    rawVerb = index != -1 ? taggedRawSentence.split("\\s")[index] : null;
                    possibleAdverb = index != -1 ? findAdverb(index, taggedRawSentence) : null;
                } else {
                    String rs1 = tokenizedStringRaw.get(sentenceIndex);
                    String rs2 = tokenizedStringRaw.get(sentenceIndex - 1 > 0 ? sentenceIndex - 1 : sentenceIndex);
                    String rs3 = tokenizedStringRaw.get(sentenceIndex + 1 < tokenizedStringRaw.size() ? sentenceIndex + 1 : sentenceIndex);

                    rs1 = FeatureParser.tagString(rs1);
                    rs2 = FeatureParser.tagString(rs2);
                    rs3 = FeatureParser.tagString(rs3);

                    int index1 = findRawVerb(rs1, verb);
                    int index2 = findRawVerb(rs2, verb);
                    int index3 = findRawVerb(rs3, verb);
                    rawVerb = index1 != -1 ? rs1.split("\\s")[index1] : index2 != -1 ? rs2.split("\\s")[index2] :
                            index3 != -1 ? rs3.split("\\s")[index3] : null;
                    possibleAdverb = index1 != -1 ? findAdverb(index1, rs1) : index2 != -1 ? findAdverb(index2, rs2) :
                            index3 != -1 ? findAdverb(index3, rs3) : null;
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
                            bi.setAdverb(possibleAdverb);
                            bigrams.add(bi);
                            checkList.add(key);
                        }
                    }
                }

            }

        }

        return bigrams;
    }

    private static int adverbCt = 5;

    private static String findAdverb(int index, String taggedRawString) {
        String[] sp = taggedRawString.split("\\s");
        int ct = 0;
        for (int i = index; i < sp.length && i >= 0 && ct < adverbCt; i--) {
            if (sp[i].contains("_RB")) {
                return sp[i];
            }
        }
        return null;
    }


    public static int findRawVerb(String rawSentence, String verb) {

        String[] sp = rawSentence.split("\\s+");

        for (int i = 0; i < sp.length; i++) {
            String possibleVerb = sp[i];
            possibleVerb = possibleVerb.toLowerCase().replaceAll("_\\s+", "");
            if (possibleVerb.contains(verb) || possibleVerb.equals(verb)) {
                return i;
            }
        }
        return -1;
    }


}
