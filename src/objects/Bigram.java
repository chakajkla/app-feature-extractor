package objects;

public class Bigram {

	private String verb;
	private String noun;

	public Bigram(String v, String n) {
		this.verb = v;
		this.noun = n;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getNoun() {
		return noun;
	}

	public void setNoun(String noun) {
		this.noun = noun;
	}

	public String toString() {
		return this.verb + " " + this.noun;
	}
	
	public String toKey() {
		return this.verb + "_" + this.noun;
	}

}
