package server.objects;

import server.nlp.NLPUtil;

public class AppFeatureDataPoint implements Comparable<AppFeatureDataPoint> {

    private String name;
    private String uniqueName;
    private String verb;
    private String noun;

    private String preposition;
    private String particle;

    private double ngramScore;
    private double tfScore;
    private double nnFreqScore;
    private double vbFreqScore;

    public String getPreposition() {
        return preposition;
    }

    public void setPreposition(String preposition) {
        this.preposition = preposition;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = process(verb);
    }

    public String getNoun() {
        return noun;
    }

    public void setNoun(String noun) {
        this.noun = process(noun);
    }

    private String process(String w) {
        return w.toLowerCase();
    }

    public double getNgramScore() {
        return ngramScore;
    }

    public void setNgramScore(double ngramScore) {
        this.ngramScore = ngramScore;
    }

    public double getTfScore() {
        return tfScore;
    }

    public void setTfScore(double tfScore) {
        this.tfScore = tfScore;
    }

    public double getNnFreqScoreScore() {
        return nnFreqScore;
    }

    public void setNnFreqScore(double nnScore) {
        this.nnFreqScore = nnScore;
    }

    public double getVbFreqScoreScore() {
        return vbFreqScore;
    }

    public void setVbFreqScore(double vbScore) {
        this.vbFreqScore = vbScore;
    }

    public void setUniqueName(int featureCount) {
        this.uniqueName = this.name + "_" + featureCount;

    }

    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public int compareTo(AppFeatureDataPoint other) {

        double thisScore = this.getScore(); //this.ngramScore * this.tfScore;
        double otherScore = other.getScore(); //other.ngramScore * other.tfScore;

        if (thisScore > otherScore) {
            return 1;
        } else if (thisScore == otherScore) {
            return 0;
        }

        return -1;
    }

    public boolean isEqual(AppFeatureDataPoint other) {
        return NLPUtil.checkFeatureEquality(this, other);
    }

    public String toString() {
        return this.verb + "_" + this.noun + "_" + this.ngramScore + "_"
                + this.tfScore + "_" + this.nnFreqScore;
    }

    public double getScore() {
        return (this.ngramScore * 0.4) + (this.tfScore * 0.1) + (this.nnFreqScore * 0.4) /*+ (this.vbFreqScore * 0.1)*/;
    }

    public String getFeature() {
        if (this.preposition != null & this.particle != null) {
            String secondPart = this.preposition != null ? this.preposition : this.particle;
            return this.verb + "-" + secondPart + " " + this.noun;
        }
        return this.verb + " " + this.noun;
    }

}
