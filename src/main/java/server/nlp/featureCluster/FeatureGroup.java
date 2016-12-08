package server.nlp.featureCluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import server.objects.AppFeatureDataPoint;


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

        for (AppFeatureDataPoint fe : groupMembers) {
            if (!fe.isEqual(other_fea)) {
                return false;
            }
        }
        return true;

    }

    public ArrayList<AppFeatureDataPoint> getAdditionalMembers(int pickSize) {

        //sort
        Comparator<AppFeatureDataPoint> featureSortDesc = new Comparator<AppFeatureDataPoint>() {
            @Override
            public int compare(AppFeatureDataPoint o1, AppFeatureDataPoint o2) {
                if (o2.getScore() > o1.getScore()) {
                    return 1;
                } else if (o1.getScore() > o2.getScore()) {
                    return -1;
                }

                return 0;
            }
        };

        Collections.sort(groupMembers, featureSortDesc);


        ArrayList<AppFeatureDataPoint> members = new ArrayList<>();

        for (int i = 0; i < pickSize && i < groupMembers.size(); i++) {
            members.add(groupMembers.get(i));
        }

        return members;

    }

    public double getAverageScore() {

        double av = 0;

        for (AppFeatureDataPoint fe : groupMembers) {
            av += fe.getScore();
        }

        return av / (double) groupMembers.size();
    }

}
