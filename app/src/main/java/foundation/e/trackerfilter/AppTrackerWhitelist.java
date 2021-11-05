package foundation.e.trackerfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import foundation.e.privacymodules.trackers.IBlockTrackersPrivacyModule;
import foundation.e.privacymodules.trackers.Tracker;

public class AppTrackerWhitelist extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AppTrackerWhitelist.db";
    private static AppTrackerWhitelist sAppTrackerWhitelist;
    private final Context mContext;

    public AppTrackerWhitelist(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(APP_TRACKER_SQL_CREATE_TABLE);
        db.execSQL(APP_SQL_CREATE_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }





    public List<Tracker> getWhiteList(int app_uid) {
        List<Tracker> trackers = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME,
                app_tracker_projection,
                AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?",
                new String[]{""+app_uid},
                null,
                null,
                null
        );
        while(cursor.moveToNext()){
            Tracker tracker = TrackerListManager.getInstance(mContext)
                    .getTracker(cursor.getInt(cursor
                            .getColumnIndex(AppTrackerEntry.COLUMN_NAME_TRACKER)));
            if(tracker!=null)
                trackers.add(tracker);

        }
        return trackers;
    }



    public void setWhiteListed(Tracker tracker, int app_uid, boolean addToWhitelist) {
        SQLiteDatabase db = getWritableDatabase();
        if(addToWhitelist) {
            ContentValues values = new ContentValues();
            values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, app_uid);
            values.put(AppTrackerEntry.COLUMN_NAME_TRACKER, tracker.getId());

            db.insert(AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, app_uid);
            values.put(AppTrackerEntry.COLUMN_NAME_TRACKER, tracker.getId());

            db.delete(AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME, null, new String[]{app_uid+"", tracker.getHostname()});

        }
        db.close();
    }

    public void setWhiteListed(int app_uid, boolean addToWhitelist) {
        SQLiteDatabase db = getWritableDatabase();
        if(addToWhitelist) {
            ContentValues values = new ContentValues();
            values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, app_uid);
            db.insert(AppTrackerEntry.APP_WHITELIST_TABLE_NAME, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, app_uid);
            db.delete(AppTrackerEntry.APP_WHITELIST_TABLE_NAME, null, new String[]{app_uid+""});
        }
        db.close();
    }

    public boolean isAppWhitelisted(int appUid) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                AppTrackerEntry.APP_WHITELIST_TABLE_NAME,
                app_projection,
                AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?",
                new String[]{""+appUid},
                null,
                null,
                null
        );

        return cursor.getCount() > 0;
    }

    public List<Integer> getWhiteListedApps() {
        List<Integer> apps = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                AppTrackerEntry.APP_WHITELIST_TABLE_NAME,
                app_projection,
                null,
                null,
                null,
                null,
                null
        );
        while(cursor.moveToNext()){
            apps.add(cursor.getInt(cursor
                    .getColumnIndex(AppTrackerEntry.COLUMN_NAME_APP_UID)));

        }
        return apps;
    }


    public static class AppTrackerEntry implements BaseColumns {
        public static final String APP_TRACKER_WHITELIST_TABLE_NAME = "app_tracker_whitelist";
        public static final String APP_WHITELIST_TABLE_NAME = "app_whitelist";
        public static final String COLUMN_NAME_APP_UID = "app_uid";
        public static final String COLUMN_NAME_TRACKER = "tracker";
    }

    String[] app_tracker_projection = {
            AppTrackerEntry.COLUMN_NAME_APP_UID,
            AppTrackerEntry.COLUMN_NAME_TRACKER
    };
    String[] app_projection = {
            AppTrackerEntry.COLUMN_NAME_APP_UID
    };

    private static final String APP_TRACKER_SQL_CREATE_TABLE =
            "CREATE TABLE " + AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME + " (" +
                    AppTrackerEntry._ID + " INTEGER PRIMARY KEY," +
                    AppTrackerEntry.COLUMN_NAME_APP_UID + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_TRACKER + " INTEGER)";

    private static final String APP_SQL_CREATE_TABLE =
            "CREATE TABLE " + AppTrackerEntry.APP_WHITELIST_TABLE_NAME + " (" +
                    AppTrackerEntry.COLUMN_NAME_APP_UID + " INTEGER UNIQUE)";



    public boolean isTrackerWhitelistedForApp(int trackerId, int uid){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME,
                app_tracker_projection,
                AppTrackerEntry.COLUMN_NAME_TRACKER+" = ? AND "+AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?",
                new String[]{""+trackerId, ""+uid},
                null,
                null,
                null
        );

        return cursor.getCount() > 0;
    }

    public static AppTrackerWhitelist getInstance(Context ct){
        if(sAppTrackerWhitelist == null){
            sAppTrackerWhitelist = new AppTrackerWhitelist(ct);
        }
        return sAppTrackerWhitelist;
    }


}
