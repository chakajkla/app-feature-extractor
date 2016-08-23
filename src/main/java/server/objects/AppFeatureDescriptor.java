package server.objects;

import java.util.ArrayList;

public class AppFeatureDescriptor {

	private String description;
	private ArrayList<AppFeatureDataPoint> functionList = new ArrayList<>();
	private String name;
	private String tagged;

	private int featureCount = 0;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<AppFeatureDataPoint> getFunctionList() {
		return functionList;
	}

	public void addFunctionList(AppFeatureDataPoint feature) {

		feature.setUniqueName(featureCount);
		this.functionList.add(feature);
		featureCount++;
	}

	public void clearFunctionList(){
		this.functionList.clear();
		featureCount = 0;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;

	}

	@Override
	public String toString() {
		return "Functions: " + this.getFunctionList();

	}

	public void setTaggedDescription(String tagged) {
		this.tagged = tagged;
	}

	public String getTaggedDescription() {
		return tagged;
	}

}
