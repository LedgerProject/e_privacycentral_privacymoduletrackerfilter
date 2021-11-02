package foundation.e.trackerfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatsDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TrackerFilterStats.db";

    public StatsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }


    public static class AppTrackerEntry implements BaseColumns {
        public static final String TABLE_NAME = "tracker_filter_stats";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_TRACKER = "tracker";
        public static final String COLUMN_NAME_APP_UID = "app_uid";
        public static final String COLUMN_NAME_NUMBER_CONTACTED = "sum_contacted";
        public static final String COLUMN_NAME_NUMBER_BLOCKED = "sum_blocked";

    }

    String[] projection = {
            AppTrackerEntry.COLUMN_NAME_YEAR,
            AppTrackerEntry.COLUMN_NAME_MONTH,
            AppTrackerEntry.COLUMN_NAME_DAY,
            AppTrackerEntry.COLUMN_NAME_HOUR,
            AppTrackerEntry.COLUMN_NAME_APP_UID,
            AppTrackerEntry.COLUMN_NAME_TRACKER,
            AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED,
            AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED
    };

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + AppTrackerEntry.TABLE_NAME + " (" +
                    AppTrackerEntry._ID + " INTEGER PRIMARY KEY," +
                    AppTrackerEntry.COLUMN_NAME_YEAR + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_MONTH + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_DAY + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_HOUR + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_APP_UID + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_TRACKER + " INTEGER)";


    public void logAccess(int trackerId, int appUid){
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppTrackerEntry.COLUMN_NAME_HOUR, hour);
        values.put(AppTrackerEntry.COLUMN_NAME_DAY, day);
        values.put(AppTrackerEntry.COLUMN_NAME_MONTH, month);
        values.put(AppTrackerEntry.COLUMN_NAME_YEAR, year);
        values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, appUid);
        values.put(AppTrackerEntry.COLUMN_NAME_TRACKER, trackerId);

        long newRowId = db.insert(AppTrackerEntry.TABLE_NAME, null, values);
        db.close();

    }
    

    public List<StatEntry> getStatEntries(String tracker, int app_uid){
        String selection = null;
        String[] selectionArg = null;
        if(tracker != null && app_uid >=0){
            selection = AppTrackerEntry.COLUMN_NAME_TRACKER+" = ? AND "+ AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?";
            selectionArg = new String[]{tracker, ""+app_uid};
        } else if(tracker != null) {
            selection = AppTrackerEntry.COLUMN_NAME_TRACKER + " = ?" ;
            selectionArg = new String[]{tracker};
        } else if(app_uid >=0){
            selection = AppTrackerEntry.COLUMN_NAME_APP_UID+" = ?";
            selectionArg = new String[]{""+app_uid};
        }
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                AppTrackerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArg,
                null,
                null,
                null
        );
        List<StatEntry> entries = new ArrayList<>();
        while(cursor.moveToNext()){
            StatEntry entry = new StatEntry();
            entry.hour = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_HOUR));
            entry.day = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_DAY));
            entry.month = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_MONTH));
            entry.year = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_YEAR));
            entry.app_uid = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_APP_UID));
            entry.sum_blocked = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED));
            entry.sum_contacted = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED));
            entry.tracker = cursor.getString(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TRACKER));
            entries.add(entry);
        }
        cursor.close();
        return entries;
    }

    public List<String> getAllTrackersOfApp(int app_uid){
        String selection = null;
        String[] selectionArg = null;
        if(app_uid >=0){
            selection = AppTrackerEntry.COLUMN_NAME_APP_UID+" = ?";
            selectionArg = new String[]{""+app_uid};
        }
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                AppTrackerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArg,
                AppTrackerEntry.COLUMN_NAME_TRACKER,
                null,
                null
        );
        List<String> tracker = new ArrayList<>();
        while(cursor.moveToNext()){
            tracker.add(cursor.getString(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TRACKER)));
        }
        cursor.close();
        return tracker;
    }

    public List<String> getAllTrackers(){
        return getAllTrackersOfApp(-1);
    }



    public static class StatEntry {
        int year;
        int month;
        int day;
        int hour;
        int app_uid;
        int sum_contacted;
        int sum_blocked;
        String tracker;
    }
}
