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
    private Object mLock = new Object();

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
        synchronized (mLock) {
            List<Tracker> trackers = new ArrayList<>();
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.query(
                    AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME,
                    app_tracker_projection,
                    AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?",
                    new String[]{"" + app_uid},
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()) {
                Tracker tracker = TrackerListManager.getInstance(mContext)
                        .getTracker(cursor.getInt(cursor
                                .getColumnIndex(AppTrackerEntry.COLUMN_NAME_TRACKER)));
                if (tracker != null)
                    trackers.add(tracker);

            }
            return trackers;
        }
    }



    public void setWhiteListed(Tracker tracker, int app_uid, boolean addToWhitelist) {
        synchronized (mLock) {
            SQLiteDatabase db = getWritableDatabase();
            if (addToWhitelist) {
                ContentValues values = new ContentValues();
                values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, app_uid);
                values.put(AppTrackerEntry.COLUMN_NAME_TRACKER, tracker.getId());

                db.insert(AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME, null, values);

            } else {
                db.delete(AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME, AppTrackerEntry.COLUMN_NAME_APP_UID + " = ? AND " + AppTrackerEntry.COLUMN_NAME_TRACKER + " = ?", new String[]{app_uid + "", "" + tracker.getId()});
            }
        }
    }

    public void setWhiteListed(int app_uid, boolean addToWhitelist) {
        synchronized (mLock) {
            SQLiteDatabase db = getWritableDatabase();
            if (addToWhitelist) {
                ContentValues values = new ContentValues();
                values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, app_uid);
                db.insert(AppTrackerEntry.APP_WHITELIST_TABLE_NAME, null, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, app_uid);
                db.delete(AppTrackerEntry.APP_WHITELIST_TABLE_NAME, AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?", new String[]{app_uid + ""});
            }
        }
    }

    public boolean isAppWhitelisted(int appUid) {
        synchronized (mLock) {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.query(
                    AppTrackerEntry.APP_WHITELIST_TABLE_NAME,
                    app_projection,
                    AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?",
                    new String[]{"" + appUid},
                    null,
                    null,
                    null
            );

            return cursor.getCount() > 0;
        }
    }

    public List<Integer> getWhiteListedApps() {
        synchronized (mLock) {
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
            while (cursor.moveToNext()) {
                apps.add(cursor.getInt(cursor
                        .getColumnIndex(AppTrackerEntry.COLUMN_NAME_APP_UID)));

            }
            return apps;
        }
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
        synchronized (mLock) {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.query(
                    AppTrackerEntry.APP_TRACKER_WHITELIST_TABLE_NAME,
                    app_tracker_projection,
                    AppTrackerEntry.COLUMN_NAME_TRACKER + " = ? AND " + AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?",
                    new String[]{"" + trackerId, "" + uid},
                    null,
                    null,
                    null
            );

            return cursor.getCount() > 0;
        }
    }

    public static AppTrackerWhitelist getInstance(Context ct){
        if(sAppTrackerWhitelist == null){
            sAppTrackerWhitelist = new AppTrackerWhitelist(ct);
        }
        return sAppTrackerWhitelist;
    }


}
