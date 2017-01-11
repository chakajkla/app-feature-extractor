package server.objects;

public class Bigram {

    private String verb;
    private String noun;

    private String preposition;
    private String particle;
    private String rawVerb;
    private String adverb;

    public Bigram(String v, String n) {
        this.verb = v;
        this.noun = n;
    }

    public String getAdverb() {
        return adverb;
    }

    public void setAdverb(String adverb) {
        this.adverb = adverb;
    }

    public String getRawVerb() {
        return rawVerb;
    }

    public void setRawVerb(String rawVerb) {
        this.rawVerb = rawVerb;
    }

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

    public String toVerbString() {
        if (isSingleVerb()) {
            return this.verb;
        }
        String secondPart = this.preposition != null ? this.preposition : this.particle;
        return this.verb + " " + secondPart;
    }

    public String toRawVerbString() {
        if (this.rawVerb != null) {
            return this.rawVerb + " " + this.noun;
        }
        return null;
    }

    //key used or searchig coloc score
    public String toKey() {
        return this.verb + "_" + this.noun;
    }

    public String toVerbKey() {
        if (isSingleVerb()) {
            return null;
        }
        String secondPart = this.preposition != null ? this.preposition : this.particle;
        return this.verb + "_" + secondPart;
    }

    public boolean isSingleVerb() {
        return this.preposition == null && this.particle == null;
    }

}
