package foundation.e.trackerfilter.api;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

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
    public List<Integer> getPastDayTrackersCalls() {
        return StatsDatabase.getInstance(mContext).getPast24h();
    }

    @Override
    public List<Integer> getPastMonthTrackersCalls() {
        return StatsDatabase.getInstance(mContext).getPastMonth();
    }

    @Override
    public List<Integer> getPastYearTrackersCalls() {
        return StatsDatabase.getInstance(mContext).getPastYear();
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

    private int sumOfList(List<Integer> list){
        int total = 0;
        for(int call: list){
            total+=call;
        }
        return total;
    }

    @Override
    public int getPastDayTrackersCount() {
        /* TODO optimise */
        return sumOfList(getPastDayTrackersCalls());
    }

    @Override
    public int getPastMonthTrackersCount() {
        /* TODO optimise */
        return sumOfList(getPastMonthTrackersCalls());
    }

    @Override
    public int getPastYearTrackersCount() {
        /* TODO optimise */
        return sumOfList(getPastYearTrackersCalls());
    }
}
