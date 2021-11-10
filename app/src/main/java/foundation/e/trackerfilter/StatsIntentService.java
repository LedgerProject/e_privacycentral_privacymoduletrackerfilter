/*
 * Copyright (C) 2021 E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.trackerfilter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import foundation.e.privacymodules.trackers.Tracker;


public class StatsIntentService extends Service {

    private static final String ACTION_LOG = "dnsfilter.action.log";

    // TODO: Rename parameters
    private static final String EXTRA_DOMAIN_NAME = "domain_name";
    private static final String EXTRA_APP_UID = "app_uid";
    private static final String EXTRA_BLOCKED = "blocked";
    private static final String TAG = StatsIntentService.class.getName();



    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    public int onStartCommand(Intent intent, int flags, int startId) {
        int start = super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOG.equals(action)) {
                final String domaineName = intent.getStringExtra(EXTRA_DOMAIN_NAME);
                final int appUid = intent.getIntExtra(EXTRA_APP_UID,-1);
                handleActionLog(domaineName, appUid, intent.getBooleanExtra(EXTRA_BLOCKED, false));
            }
        }
        return start;
    }


    private void handleActionLog(String domainName, int appId, boolean wasBlocked) {
        Tracker tracker = TrackerListManager.getInstance(this).getTrackerByDomainName(domainName);
        if(tracker == null){
            tracker = new Tracker(-1, domainName, domainName,  -1, null, null);
            tracker = TrackerListManager.getInstance(this).addTracker(tracker);
        }
        StatsDatabase database = StatsDatabase.getInstance(this);
        database.logAccess(tracker.getId(),appId, wasBlocked);
    }


}