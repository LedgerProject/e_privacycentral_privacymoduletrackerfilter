package foundation.e.trackerfilter.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import dnsfilter.android.DNSFilterService;
import dnsfilter.android.R;
import foundation.e.privacymodules.trackers.IBlockTrackersPrivacyModule;
import foundation.e.privacymodules.trackers.Tracker;
import foundation.e.trackerfilter.AppTrackerWhitelist;

public class BlockTrackersPrivacyModule implements IBlockTrackersPrivacyModule {

    private final Context mContext;
    private List<Listener> mListeners = new ArrayList<>();
    private static BlockTrackersPrivacyModule sBlockTrackersPrivacyModule;

    public BlockTrackersPrivacyModule(Context ct) {
        mContext = ct;
    }

    @Override
    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    @Override
    public void clearListeners() {
        mListeners.clear();
    }

    @Override
    public void disableBlocking() {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("enable_global_blocking", false).commit();
        for(Listener listener:mListeners){
            listener.onBlockingToggle(false);
        }
    }

    @Override
    public void enableBlocking() {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("enable_global_blocking", true).commit();
        for(Listener listener:mListeners){
            listener.onBlockingToggle(true);
        }
    }

    @Override
    public List<Tracker> getWhiteList(int i) {
        return AppTrackerWhitelist.getInstance(mContext).getWhiteList(i);
    }

    @Override
    public List<Integer> getWhiteListedApp() {
        return AppTrackerWhitelist.getInstance(mContext).getWhiteListedApps();
    }

    @Override
    public boolean isBlockingEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("enable_global_blocking", false);
    }

    @Override
    public boolean isWhiteListEmpty() {
        return AppTrackerWhitelist.getInstance(mContext).getWhiteListedApps().isEmpty();
    }

    @Override
    public boolean isWhitelisted(int appUid) {
        return AppTrackerWhitelist.getInstance(mContext).isAppWhitelisted(appUid);
    }

    @Override
    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void setWhiteListed(Tracker tracker, int i, boolean b) {
        AppTrackerWhitelist.getInstance(mContext).setWhiteListed(tracker,i, b);
    }

    @Override
    public void setWhiteListed(int i, boolean b) {
        AppTrackerWhitelist.getInstance(mContext).setWhiteListed(i, b);
    }

    public static BlockTrackersPrivacyModule getInstance(Context ct){
        if(sBlockTrackersPrivacyModule == null){
            sBlockTrackersPrivacyModule = new BlockTrackersPrivacyModule(ct);
        }
        return sBlockTrackersPrivacyModule;
    }
}
