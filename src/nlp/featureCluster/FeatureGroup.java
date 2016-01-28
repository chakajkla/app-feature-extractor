package nlp.featureCluster;

import java.util.ArrayList;

import objects.AppFeatureDataPoint;



public class FeatureGroup {

	private ArrayList<AppFeatureDataPoint> groupMembers = new ArrayList<>();

	private AppFeatureDataPoint groupLeader = null;

	public ArrayList<AppFeatureDataPoint> getGroupMembers() {
		return groupMembers;
	}

	public void setGroupMembers(ArrayList<AppFeatureDataPoint> groupMembers) {
		this.groupMembers = groupMembers;
	}

	public void addGroupMember(AppFeatureDataPoint newMember) {

		if (this.groupLeader != null) {
			switch (this.groupLeader.compareTo(newMember)) {
			case -1:
				this.groupLeader = newMember;
				break;
			}

		} else {
			this.groupLeader = newMember;
		}

		this.groupMembers.add(newMember);

	}

	public AppFeatureDataPoint getGroupLeader() {
		return groupLeader;
	}

	public void setGroupLeader(AppFeatureDataPoint groupLeader) {
		this.groupLeader = groupLeader;
	}

	public boolean canAddMember(AppFeatureDataPoint other_fea) {
		return this.groupLeader.isEqual(other_fea);
	}

}
