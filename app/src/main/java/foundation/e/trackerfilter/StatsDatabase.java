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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.e.privacymodules.trackers.Tracker;
import foundation.e.trackerfilter.api.TrackTrackersPrivacyModule;

public class StatsDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TrackerFilterStats.db";
    private static StatsDatabase sStatsDatabase;
    private final Context mContext;
    private final Object lock = new Object();

    public StatsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static StatsDatabase getInstance(Context ct){
        if(sStatsDatabase == null){
            sStatsDatabase = new StatsDatabase(ct);
        }
        return sStatsDatabase;
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
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
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
            AppTrackerEntry.COLUMN_NAME_TIMESTAMP,
            AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED
    };

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + AppTrackerEntry.TABLE_NAME + " (" +
                    AppTrackerEntry._ID + " INTEGER PRIMARY KEY," +
                    AppTrackerEntry.COLUMN_NAME_YEAR + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_MONTH + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_DAY + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_HOUR + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_TIMESTAMP + " INTEGER,"+
                    AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_APP_UID + " INTEGER," +
                    AppTrackerEntry.COLUMN_NAME_TRACKER + " INTEGER)";



    public List<Integer> getPast24h(){
        synchronized (lock) {
            long current = System.currentTimeMillis();
            long min = current - 24 * 60 * 60 * 1000;
            SQLiteDatabase db = getWritableDatabase();

            String selection = AppTrackerEntry.COLUMN_NAME_TIMESTAMP + " >= ?";

            String[] selectionArg = new String[]{"" + min};
            String projection =
                    AppTrackerEntry.COLUMN_NAME_YEAR + ", " +
                            AppTrackerEntry.COLUMN_NAME_MONTH + ", " +
                            AppTrackerEntry.COLUMN_NAME_DAY + ", " +
                            AppTrackerEntry.COLUMN_NAME_HOUR + ", " +
                            AppTrackerEntry.COLUMN_NAME_TIMESTAMP + ", " +
                            "SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + ")" + "," +
                            "SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED + ")";
            Cursor cursor = db.rawQuery("SELECT " + projection + " FROM " + AppTrackerEntry.TABLE_NAME
                    + " WHERE " + selection +
                    " GROUP BY " + AppTrackerEntry.COLUMN_NAME_HOUR +
                    " ORDER BY " + AppTrackerEntry.COLUMN_NAME_TIMESTAMP + " DESC" +
                    " LIMIT 24", selectionArg);

            List<Integer> entries = new ArrayList<>();
            HashMap<Long, Integer> timedEntries = new HashMap<>();
            while (cursor.moveToNext()) {
                timedEntries.put(cursor.getLong(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TIMESTAMP)), cursor.getInt(cursor.getColumnIndex("SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + ")")));
            }
            for (int i = 1; i <= 24; i++) {
                min = current - i * 60 * 60 * 1000;
                boolean found = false;
                for (Map.Entry<Long, Integer> timedEntry : timedEntries.entrySet()) {
                    if (timedEntry.getKey() >= min && timedEntry.getKey() < min + 60 * 60 * 1000) {
                        entries.add(timedEntry.getValue());
                        found = true;
                    }
                }
                if (!found) {
                    entries.add(0);
                }
            }
            cursor.close();
            db.close();
            return entries;
        }

    }

    public List<Integer> getPastYear(){
        synchronized (lock) {
            long current = System.currentTimeMillis();
            long substract = 12L * 30L * 24L * 60L * 60L * 1000L;
            long min = current - substract;
            SQLiteDatabase db = getWritableDatabase();
            String selection = AppTrackerEntry.COLUMN_NAME_TIMESTAMP + " >= ?";

            String[] selectionArg = new String[]{"" + min};
            String projection =
                    AppTrackerEntry.COLUMN_NAME_YEAR + ", " +
                            AppTrackerEntry.COLUMN_NAME_MONTH + ", " +
                            AppTrackerEntry.COLUMN_NAME_TIMESTAMP + ", " +
                            "SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + ")" + "," +
                            "SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED + ")";
            Cursor cursor = db.rawQuery("SELECT " + projection + " FROM " + AppTrackerEntry.TABLE_NAME
                    + " WHERE " + selection +
                    " GROUP BY " + AppTrackerEntry.COLUMN_NAME_MONTH +
                    " ORDER BY " + AppTrackerEntry.COLUMN_NAME_TIMESTAMP + " DESC" +
                    " LIMIT 12", selectionArg);
            List<Integer> entries = new ArrayList<>();
            HashMap<Long, Integer> timedEntries = new HashMap<>();
            while (cursor.moveToNext()) {
                timedEntries.put(cursor.getLong(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TIMESTAMP)), cursor.getInt(cursor.getColumnIndex("SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + ")")));
            }
            long month = 30L * 24L * 60L * 60L * 1000L;

            for (long i = 1; i <= 12; i++) {
                min = current - i * month;
                boolean found = false;
                for (Map.Entry<Long, Integer> timedEntry : timedEntries.entrySet()) {
                    if (timedEntry.getKey() >= min && timedEntry.getKey() < min + month) {
                        entries.add(timedEntry.getValue());
                        found = true;
                    }
                }
                if (!found) {
                    entries.add(0);
                }
            }
            cursor.close();
            db.close();
            return entries;
        }
    }

    public List<Integer> getPastMonth(){
        synchronized (lock) {
            long current = System.currentTimeMillis();
            long substract = 30L * 24L * 60L * 60L * 1000L;
            long min = current - substract;
            SQLiteDatabase db = getWritableDatabase();
            String selection = AppTrackerEntry.COLUMN_NAME_TIMESTAMP + " >= ?";

            String[] selectionArg = new String[]{"" + min};
            String projection =
                    AppTrackerEntry.COLUMN_NAME_YEAR + ", " +
                            AppTrackerEntry.COLUMN_NAME_MONTH + ", " +
                            AppTrackerEntry.COLUMN_NAME_DAY + ", " +
                            AppTrackerEntry.COLUMN_NAME_TIMESTAMP + ", " +
                            "SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + ")" + "," +
                            "SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED + ")";
            Cursor cursor = db.rawQuery("SELECT " + projection + " FROM " + AppTrackerEntry.TABLE_NAME
                    + " WHERE " + selection +
                    " GROUP BY " + AppTrackerEntry.COLUMN_NAME_DAY +
                    " ORDER BY " + AppTrackerEntry.COLUMN_NAME_TIMESTAMP + " DESC" +
                    " LIMIT 30", selectionArg);
            List<Integer> entries = new ArrayList<>();
            HashMap<Long, Integer> timedEntries = new HashMap<>();
            while (cursor.moveToNext()) {
                timedEntries.put(cursor.getLong(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TIMESTAMP)), cursor.getInt(cursor.getColumnIndex("SUM(" + AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED + ")")));
            }
            for (int i = 1; i <= 30; i++) {
                min = current - i * 24 * 60 * 60 * 1000;
                boolean found = false;
                for (Map.Entry<Long, Integer> timedEntry : timedEntries.entrySet()) {
                    if (timedEntry.getKey() >= min && timedEntry.getKey() < min + 24 * 60 * 60 * 1000) {
                        entries.add(timedEntry.getValue());
                        found = true;
                    }
                }
                if (!found) {
                    entries.add(0);
                }
            }
            cursor.close();
            db.close();
            return entries;
        }
    }

    public void logAccess(int trackerId, int appUid, boolean blocked){
        synchronized (lock) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(AppTrackerEntry.COLUMN_NAME_HOUR, hour);
            values.put(AppTrackerEntry.COLUMN_NAME_DAY, day);
            values.put(AppTrackerEntry.COLUMN_NAME_MONTH, month);
            values.put(AppTrackerEntry.COLUMN_NAME_YEAR, year);
            values.put(AppTrackerEntry.COLUMN_NAME_APP_UID, appUid);
            values.put(AppTrackerEntry.COLUMN_NAME_TRACKER, trackerId);
            values.put(AppTrackerEntry.COLUMN_NAME_TIMESTAMP, cal.getTimeInMillis());

        /*String query = "UPDATE product SET "+AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED+" = "+AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED+" + 1 ";
        if(blocked)
            query+=AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED+" = "+AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED+" + 1 ";
*/
            String selection = AppTrackerEntry.COLUMN_NAME_HOUR + " = ? AND " + AppTrackerEntry.COLUMN_NAME_DAY + " = ? AND " + AppTrackerEntry.COLUMN_NAME_MONTH + " = ? AND " +
                    AppTrackerEntry.COLUMN_NAME_YEAR + " = ? AND " + AppTrackerEntry.COLUMN_NAME_APP_UID + " = ? AND " +
                    AppTrackerEntry.COLUMN_NAME_TRACKER + " = ? ";

            String[] selectionArg = new String[]{"" + hour, "" + day, "" + month, "" + year, "" + appUid, "" + trackerId};

            Cursor cursor = db.query(
                    AppTrackerEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArg,
                    null,
                    null,
                    null
            );
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                StatEntry entry = cursorToEntry(cursor);
                if (blocked)
                    values.put(AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED, entry.sum_blocked + 1);
                else
                    values.put(AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED, entry.sum_blocked);
                values.put(AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED, entry.sum_contacted + 1);
                db.update(AppTrackerEntry.TABLE_NAME, values, selection, selectionArg);
                // db.execSQL(query, new String[]{""+hour, ""+day, ""+month, ""+year, ""+appUid, ""+trackerId});
            } else {

                if (blocked)
                    values.put(AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED, 1);
                else
                    values.put(AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED, 0);
                values.put(AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED, 1);


                long newRowId = db.insert(AppTrackerEntry.TABLE_NAME, null, values);
            }
            db.close();
        }

    }
    

    public List<StatEntry> getStatEntries(String tracker, int app_uid) {
        synchronized (lock) {
            String selection = null;
            String[] selectionArg = null;
            if (tracker != null && app_uid >= 0) {
                selection = AppTrackerEntry.COLUMN_NAME_TRACKER + " = ? AND " + AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?";
                selectionArg = new String[]{tracker, "" + app_uid};
            } else if (tracker != null) {
                selection = AppTrackerEntry.COLUMN_NAME_TRACKER + " = ?";
                selectionArg = new String[]{tracker};
            } else if (app_uid >= 0) {
                selection = AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?";
                selectionArg = new String[]{"" + app_uid};
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
            while (cursor.moveToNext()) {
                StatEntry entry = cursorToEntry(cursor);
                entries.add(entry);
            }
            cursor.close();
            return entries;
        }
    }

    private StatEntry cursorToEntry(Cursor cursor){
        StatEntry entry = new StatEntry();
        entry.hour = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_HOUR));
        entry.day = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_DAY));
        entry.month = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_MONTH));
        entry.year = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_YEAR));
        entry.timestamp = cursor.getLong(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TIMESTAMP));
        entry.app_uid = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_APP_UID));
        entry.sum_blocked = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_NUMBER_BLOCKED));
        entry.sum_contacted = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_NUMBER_CONTACTED));
        entry.tracker = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TRACKER));
        return entry;
    }

    public List<Tracker> getAllTrackersOfApp(int app_uid){
        synchronized (lock) {
            String[] projection = {
                    "DISTINCT("+AppTrackerEntry.COLUMN_NAME_TRACKER+")",
            };
            String selection = null;
            String[] selectionArg = null;
            if (app_uid >= 0) {
                selection = AppTrackerEntry.COLUMN_NAME_APP_UID + " = ?";
                selectionArg = new String[]{"" + app_uid};
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
            List<Tracker> trackers = new ArrayList<>();
            while (cursor.moveToNext()) {
                int trackerId = cursor.getInt(cursor.getColumnIndex(AppTrackerEntry.COLUMN_NAME_TRACKER));
                Log.d("trackerdebug","trackerId "+trackerId);
                Tracker tracker = TrackerListManager.getInstance(mContext).getTracker(trackerId);
                if (tracker != null)
                    trackers.add(tracker);
            }
            cursor.close();
            return trackers;
        }
    }

    public List<Tracker> getAllTrackers(){
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
        long timestamp;
        int tracker;
    }
}
