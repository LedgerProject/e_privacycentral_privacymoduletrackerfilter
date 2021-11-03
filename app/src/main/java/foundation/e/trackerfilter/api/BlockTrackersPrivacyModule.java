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

    private List<Listener> mListeners = new ArrayList<>();
    private static BlockTrackersPrivacyModule sBlockTrackersPrivacyModule;

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
        PreferenceManager.getDefaultSharedPreferences(DNSFilterService.ct).edit().putBoolean("enable_global_blocking", false).commit();
        for(Listener listener:mListeners){
            listener.onBlockingToggle(false);
        }
    }

    @Override
    public void enableBlocking() {
        PreferenceManager.getDefaultSharedPreferences(DNSFilterService.ct).edit().putBoolean("enable_global_blocking", true).commit();
        for(Listener listener:mListeners){
            listener.onBlockingToggle(true);
        }
    }

    @Override
    public List<Tracker> getWhiteList(int i) {
        return AppTrackerWhitelist.getInstance(DNSFilterService.ct).getWhiteList(i);
    }

    @Override
    public List<Integer> getWhiteListedApp() {
        return AppTrackerWhitelist.getInstance(DNSFilterService.ct).getWhiteListedApps();
    }

    @Override
    public boolean isBlockingEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(DNSFilterService.ct).getBoolean("enable_global_blocking", false);
    }

    @Override
    public boolean isWhiteListEmpty() {
        return AppTrackerWhitelist.getInstance(DNSFilterService.ct).getWhiteListedApps().isEmpty();
    }

    @Override
    public boolean isWhitelisted(int appUid) {
        return AppTrackerWhitelist.getInstance(DNSFilterService.ct).isAppWhitelisted(appUid);
    }

    @Override
    public void removeListener(Listener listener) {

    }

    @Override
    public void setWhiteListed(Tracker tracker, int i, boolean b) {
        AppTrackerWhitelist.getInstance(DNSFilterService.ct).setWhiteListed(tracker,i, b);
    }

    @Override
    public void setWhiteListed(int i, boolean b) {
        AppTrackerWhitelist.getInstance(DNSFilterService.ct).setWhiteListed(i, b);
    }

    public static BlockTrackersPrivacyModule getInstance(Context ct){
        if(sBlockTrackersPrivacyModule == null){
            sBlockTrackersPrivacyModule = new BlockTrackersPrivacyModule();
        }
        return sBlockTrackersPrivacyModule;
    }
}
