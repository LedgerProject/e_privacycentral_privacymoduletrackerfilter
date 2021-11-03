package foundation.e.trackerfilter.api;

import android.content.Context;

import java.util.List;

import foundation.e.privacymodules.trackers.ITrackTrackersPrivacyModule;
import foundation.e.privacymodules.trackers.Tracker;
import foundation.e.trackerfilter.StatsDatabase;

public class TrackTrackersPrivacyModule implements ITrackTrackersPrivacyModule {
    private static TrackTrackersPrivacyModule sTrackTrackersPrivacyModule;
    private final Context mContext;

    public TrackTrackersPrivacyModule(Context ct) {
        mContext = ct;
    }

    @Override
    public List<Integer> getPast24HoursTrackersCalls() {
        return StatsDatabase.getInstance(mContext).getPast24h();
    }

    @Override
    public List<Integer> getPastMonthTrackersCalls() {
        return null;
    }

    @Override
    public List<Integer> getPastYearTrackersCalls() {
        return null;
    }

    @Override
    public int getTrackersCount() {
        return 0;
    }

    @Override
    public List<Tracker> getTrackersForApp(int i) {
        return null;
    }
    public static TrackTrackersPrivacyModule getInstance(Context ct){
        if(sTrackTrackersPrivacyModule == null){
            sTrackTrackersPrivacyModule = new TrackTrackersPrivacyModule(ct);
        }
        return sTrackTrackersPrivacyModule;
    }
}
