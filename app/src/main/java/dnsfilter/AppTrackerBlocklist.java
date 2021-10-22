package dnsfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AppTrackerBlocklist extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AppTrackerBlocklist.db";

    public AppTrackerBlocklist(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }


    public static class AppTrackerEntry implements BaseColumns {
        public static final String TABLE_NAME = "app_tracker_blocklist";
        public static final String COLUMN_NAME_APP_UID = "app_uid";
        public static final String COLUMN_NAME_TRACKER = "tracker";
    }

    String[] projection = {
            AppTrackerEntry.COLUMN_NAME_APP_UID,
            AppTrackerEntry.COLUMN_NAME_TRACKER
    };

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + AppTrackerEntry.TABLE_NAME + " (" +
                    AppTrackerEntry._ID + " INTEGER PRIMARY KEY," +
                    AppTrackerEntry.COLUMN_NAME_APP_UID + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_TRACKER + " TEXT)";


    public void blockTrackerForApp(String tracker, int uid){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, uid);
        values.put(AppTrackerEntry.COLUMN_NAME_TRACKER, tracker);

        long newRowId = db.insert(AppTrackerEntry.TABLE_NAME, null, values);
        db.close();

    }

    public boolean isTrackerBlockedForApp(String tracker, int uid){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                AppTrackerEntry.TABLE_NAME,
                projection,
                AppTrackerEntry.COLUMN_NAME_TRACKER+" = ? AND "+AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?",
                new String[]{tracker, ""+uid},
                null,
                null,
                null
        );

        return cursor.getCount() > 0;
    }


}
