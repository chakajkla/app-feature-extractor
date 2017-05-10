package server.objects.response;

import java.util.HashSet;
import java.util.Set;

public class Response {

    private final String packageID;
    private  Set<Feature> features = null;
	
    public Response(String packageID) {
        this.packageID = packageID;
    }

    public String getId() {
        return packageID;
    }
	
	public void setFeatures(Set<Feature> ft){
	    this.features = ft;
	}

    public Set<Feature> getFeatures() {

        //Set<Feature> featureSet = new HashSet<>();

        //featureSet.add(new Feature("send sms"));
        //featureSet.add(new Feature("save photo"));
		
		if(features == null){
		   return new HashSet<Feature>();
		}

        return features;
    }
}
