package foundation.e.trackerfilter;

import foundation.e.privacymodules.trackers.Tracker;

public class TrackerDetailed extends Tracker {
    String networkSignature;
    int id;
    int exodusId;
    String description;


    public TrackerDetailed(String label, String hostname) {
        super(label, hostname);
    }
}
