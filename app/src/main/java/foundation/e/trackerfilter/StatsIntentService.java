package foundation.e.trackerfilter;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import foundation.e.privacymodules.trackers.Tracker;


public class StatsIntentService extends IntentService {

    private static final String ACTION_LOG = "dnsfilter.action.log";
    private static final String ACTION_BAZ = "dnsfilter.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_DOMAIN_NAME = "dnsfilter.extra.PARAM1";
    private static final String EXTRA_APP_UID = "dnsfilter.extra.PARAM2";
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
    public static void startActionLog(Context context, String domainName, int appUid) {
        Intent intent = new Intent(context, StatsIntentService.class);
        intent.setAction(ACTION_LOG);
        intent.putExtra(EXTRA_DOMAIN_NAME, domainName);
        intent.putExtra(EXTRA_APP_UID, appUid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOG.equals(action)) {
                final String domaineName = intent.getStringExtra(EXTRA_DOMAIN_NAME);
                final int appUid = intent.getIntExtra(EXTRA_APP_UID,-1);
                handleActionLog(domaineName, appUid);
            }
        }
    }


    private void handleActionLog(String domainName, int appId) {
        Tracker trackerDetailed = TrackerListManager.getInstance(this).getTrackerByDomainName(domainName);
        if(trackerDetailed == null){
            trackerDetailed = new TrackerDetailed(null, domainName);
            trackerDetailed = TrackerListManager.getInstance(this).addTracker(trackerDetailed);
        }
        StatsDatabase database = new StatsDatabase(this);
        database.logAccess(trackerDetailed.getId(),appId);

    }


}