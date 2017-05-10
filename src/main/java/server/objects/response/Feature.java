package server.objects.response;

/**
 * Created by USER on 20/1/2559.
 */
public class Feature {

    private String featureName;

    private double score = 0;

    public Feature(String name, double score) {
        this.featureName = name;
		this.score = score;
    }

    public String getName() {
        return featureName;
    }

    public double getScore() {
        return score;
    }
	

}
