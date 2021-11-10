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
import java.util.HashMap;
import java.util.List;

import foundation.e.privacymodules.trackers.Tracker;


public class TrackerListManager extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TrackerListManager.db";

    private static TrackerListManager sTrackerListManager;
    private final Context mContext;
    public HashMap<Integer, Tracker> mTrackersIdMap;
    private Object lock = new Object();



    public static class TrackerEntry implements BaseColumns {
        public static final String TRACKERS_TABLE_NAME = "tracker_table";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_LABEL = "label";
        public static final String COLUMN_EXODE_ID = "exode_id";
        public static final String COLUMN_HOSTNAME = "hostname";
        public static final String COLUMN_NETWORK_SIGNATURE = "network_signature";
        public static final String COLUMN_DESCRIPTION = "description";
    }
    private static final String TRACKERS_CREATE_TABLE =
            "CREATE TABLE " + TrackerEntry.TRACKERS_TABLE_NAME + " (" +
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
            mTrackersIdMap = new HashMap<>();

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(
                    TrackerEntry.TRACKERS_TABLE_NAME,
                    new String[]{TrackerEntry.COLUMN_ID,
                            TrackerEntry.COLUMN_LABEL,
                            TrackerEntry.COLUMN_HOSTNAME,
                            TrackerEntry.COLUMN_DESCRIPTION,
                            TrackerEntry.COLUMN_EXODE_ID,
                            TrackerEntry.COLUMN_NETWORK_SIGNATURE},
                    null,
                    null,
                    null,
                    null,
                    null
            );
            while(cursor.moveToNext()){
                Tracker tracker = new Tracker(cursor.getInt(cursor.getColumnIndex(TrackerEntry.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(TrackerEntry.COLUMN_LABEL)),
                        cursor.getString(cursor.getColumnIndex(TrackerEntry.COLUMN_HOSTNAME)),
                        cursor.getInt(cursor.getColumnIndex(TrackerEntry.COLUMN_EXODE_ID)),
                        cursor.getString(cursor.getColumnIndex(TrackerEntry.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(TrackerEntry.COLUMN_NETWORK_SIGNATURE)));
                mTrackersIdMap.put(tracker.getId(), tracker);
            }
            cursor.close();
            db.close();
        }
    }


    public List<Tracker> getTrackers(){
        return new ArrayList<>(mTrackersIdMap.values());
    }

    public void syncWithExodusList(){

        List<Tracker> exodusTrackers =  ExodusListManager.getInstance(mContext).getTrackers();
        List<Tracker> trackers = new ArrayList<>(new ArrayList<>(mTrackersIdMap.values()));
        for(Tracker exodusTracker : exodusTrackers){
            boolean isIn = false;
            for(Tracker tracker:trackers){
                if(tracker.getExodusId() == exodusTracker.getExodusId()){
                    isIn = true;
                    break;
                }
            }
            if(!isIn){
                addTracker(exodusTracker);
            }
        }

    }

    public Tracker addTracker(Tracker tracker) {
        synchronized (lock){
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TrackerEntry.COLUMN_EXODE_ID, ""+tracker.getExodusId());
            values.put(TrackerEntry.COLUMN_HOSTNAME, tracker.getHostname());
            values.put(TrackerEntry.COLUMN_NETWORK_SIGNATURE, tracker.getNetworkSignature());
            values.put(TrackerEntry.COLUMN_DESCRIPTION, tracker.getDescription());
            values.put(TrackerEntry.COLUMN_LABEL, tracker.getLabel());

            long id = db.insert(TrackerEntry.TRACKERS_TABLE_NAME, null, values);
            tracker = new Tracker((int) id, tracker.getLabel(), tracker.getHostname(), tracker.getExodusId(), tracker.getDescription(), tracker.getNetworkSignature());
            mTrackersIdMap.put(tracker.getId(), tracker);
            return tracker;
        }
    }


    public Tracker getTrackerByDomainName(String domain){
        for(Tracker trackerDetailed : new ArrayList<>(mTrackersIdMap.values())){
            //if(entry.getKey().contains("google"))
            //   Log.d("DNSBlockerRunnable","is google "+entry.getKey());
            if(trackerDetailed.getExodusId() >=0) {
                String net = trackerDetailed.getNetworkSignature();
                if(net.isEmpty())
                    continue;
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

    public Tracker getTracker(int trackerId) {
        return mTrackersIdMap.get(trackerId);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRACKERS_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
