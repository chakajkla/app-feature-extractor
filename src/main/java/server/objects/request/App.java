package server.objects.request;

import java.util.List;

public class App {

    private String packageID;
    private List<Float> featureVector = null;

    public App(String packageID) {
        this.packageID = packageID;
    }

    public App(){

    }

    public void setId(String packageID){
        this.packageID = packageID;
    }

    public String getId() {
        return packageID;
    }

    public void setFeatureVector(List<Float> ft) {
        this.featureVector = ft;
    }

    public List<Float> getFeatureVector() {
        return this.featureVector;
    }
}
