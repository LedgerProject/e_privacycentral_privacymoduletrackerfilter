package foundation.e.trackerfilter;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import foundation.e.privacymodules.trackers.Tracker;


public class StatsIntentService extends IntentService {

    private static final String ACTION_LOG = "dnsfilter.action.log";

    // TODO: Rename parameters
    private static final String EXTRA_DOMAIN_NAME = "domain_name";
    private static final String EXTRA_APP_UID = "app_uid";
    private static final String EXTRA_BLOCKED = "blocked";
    private static final String TAG = StatsIntentService.class.getName();

    public StatsIntentService() {
        super("StatIntentService");
    }

    /**
     * Start the intent service to log tracker access to database
     * @param context
     * @param domainName
     * @param appUid
     */
    public static void startActionLog(Context context, String domainName, int appUid, boolean wasBlocked) {
        Intent intent = new Intent(context, StatsIntentService.class);
        intent.setAction(ACTION_LOG);
        intent.putExtra(EXTRA_DOMAIN_NAME, domainName);
        intent.putExtra(EXTRA_APP_UID, appUid);
        intent.putExtra(EXTRA_BLOCKED, wasBlocked);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOG.equals(action)) {
                final String domaineName = intent.getStringExtra(EXTRA_DOMAIN_NAME);
                final int appUid = intent.getIntExtra(EXTRA_APP_UID,-1);
                handleActionLog(domaineName, appUid, intent.getBooleanExtra(EXTRA_BLOCKED, false));
            }
        }
    }


    private void handleActionLog(String domainName, int appId, boolean wasBlocked) {
        Tracker tracker = TrackerListManager.getInstance(this).getTrackerByDomainName(domainName);
        if(tracker == null){
            tracker = new Tracker(-1, "", domainName,  -1, null, null);
            tracker = TrackerListManager.getInstance(this).addTracker(tracker);
        }
        StatsDatabase database = StatsDatabase.getInstance(this);
        database.logAccess(tracker.getId(),appId, wasBlocked);
    }


}