package server.objects;

public class Bigram {

    private String verb;
    private String noun;

    private String preposition;
    private String particle;

    public Bigram(String v, String n) {
        this.verb = v;
        this.noun = n;
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
        return this.verb + "-" + this.preposition != null ? this.preposition : this.particle + " " + this.noun;
    }

    public String toKey() {
        return this.verb + "_" + this.noun;
    }

    public String toVerbKey() {
        return this.verb + "_" + this.preposition != null ? this.preposition : this.particle;
    }

}
