package foundation.e.trackerfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;


public class TrackerListManager extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TrackerListManager.db";

    private static TrackerListManager sTrackerListManager;
    private final Context mContext;
    public List<TrackerDetailed> mTrackers;
    private Object lock = new Object();
    public static class TrackerEntry implements BaseColumns {
        public static final String TRACKERS_TABLE_NAME = "app_tracker_blocklist";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_LABEL = "label";
        public static final String COLUMN_EXODE_ID = "exode_id";
        public static final String COLUMN_HOSTNAME = "hostname";
        public static final String COLUMN_NETWORK_SIGNATURE = "network_signature";
        public static final String COLUMN_DESCRIPTION = "description";
    }
    private static final String TRACKERS_CREATE_TABLE =
            "CREATE TABLE " + AppTrackerWhitelist.AppTrackerEntry.APP_WHITELIST_TABLE_NAME + " (" +
                    TrackerEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TrackerEntry.COLUMN_LABEL + " TEXT," +
                    TrackerEntry.COLUMN_EXODE_ID + " INTEGER," +
                    TrackerEntry.COLUMN_HOSTNAME + " STRING," +
                    TrackerEntry.COLUMN_NETWORK_SIGNATURE + " TEXT," +
                    TrackerEntry.COLUMN_DESCRIPTION + " TEXT" +
                    ")";


    public TrackerListManager(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        init();
        syncWithExodusList();
    }

    public static TrackerListManager getInstance(Context context) {
        if(sTrackerListManager == null){
            sTrackerListManager = new TrackerListManager(context);
        }
        return sTrackerListManager;
    }

    public void init(){
        synchronized (lock){
            mTrackers = new ArrayList<>();
        }
    }


    public List<TrackerDetailed> getTrackers(){
        return mTrackers;
    }

    public void syncWithExodusList(){

        List<TrackerDetailed> exodusTrackers =  ExodusListManager.getInstance(mContext).getTrackers();
        List<TrackerDetailed> trackers = new ArrayList<>(mTrackers);
        for(TrackerDetailed exodusTracker : exodusTrackers){
            boolean isIn = false;
            for(TrackerDetailed tracker:trackers){
                if(tracker.exodusId == exodusTracker.exodusId){
                    isIn = true;
                    break;
                }
            }
            if(!isIn){
                addTracker(exodusTracker);
            }
        }

    }

    public TrackerDetailed addTracker(TrackerDetailed tracker) {
        synchronized (lock){
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TrackerEntry.COLUMN_EXODE_ID, ""+tracker.exodusId);
            values.put(TrackerEntry.COLUMN_HOSTNAME, tracker.getHostname());
            values.put(TrackerEntry.COLUMN_NETWORK_SIGNATURE, tracker.networkSignature);
            values.put(TrackerEntry.COLUMN_DESCRIPTION, tracker.description);
            values.put(TrackerEntry.COLUMN_LABEL, tracker.getLabel());

            long id = db.insert(AppTrackerWhitelist.AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME, null, values);
            tracker.id = (int) id;
            return tracker;
        }
    }


    public TrackerDetailed getTrackerByDomainName(String domain){
        for(TrackerDetailed trackerDetailed : mTrackers){
            //if(entry.getKey().contains("google"))
            //   Log.d("DNSBlockerRunnable","is google "+entry.getKey());
            if(trackerDetailed.exodusId >=0) {
                String net = trackerDetailed.networkSignature;
                String[] regexs = net.split("\\|");
                for (String regex : regexs) {
                    regex = ".*" + regex;
                    if (domain.matches(regex))
                        return trackerDetailed;
                }
            }
            else if (trackerDetailed.getHostname().equals(domain)){
                return trackerDetailed;
            }
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRACKERS_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
