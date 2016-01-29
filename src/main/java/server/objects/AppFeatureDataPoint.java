package server.objects;

import server.nlp.NLPUtil;

public class AppFeatureDataPoint implements Comparable<AppFeatureDataPoint> {

	private String name;
	private String uniqueName;
	private String verb;
	private String noun;

	private double ngramScore;
	private double tfScore;

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

	public void setUniqueName(int featureCount) {
		this.uniqueName = this.name + "_" + featureCount;

	}

	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public int compareTo(AppFeatureDataPoint other) {

		double thisScore = this.ngramScore * this.tfScore;
		double otherScore = other.ngramScore * other.tfScore;

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
				+ this.tfScore;
	}
	
	public String getFeature(){
	   return this.verb + " " + this.noun;	
	}

	public double getScore() {
		return this.ngramScore * this.tfScore;
	}

}
