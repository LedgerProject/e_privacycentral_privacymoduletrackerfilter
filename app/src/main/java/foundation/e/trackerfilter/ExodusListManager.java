package foundation.e.trackerfilter;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import foundation.e.privacymodules.trackers.Tracker;

public class ExodusListManager {
    private static ExodusListManager sExodusListManager;
    private Context mContext;
    private HashMap<String, String> domainToTracker = new HashMap<>();
    private List<Tracker> trackers= new ArrayList<>();

    public ExodusListManager(Context context){
        mContext = context;
        parseExodusList();
    }

    private void parseExodusList(){
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(mContext.getAssets().open("exodus.json"), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append((line));
            }
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        String exodus = sb.toString();
        if(!exodus.isEmpty()){
            try {
                JSONObject object = new JSONObject(exodus);
                JSONObject trackersJson = object.getJSONObject("trackers");
                Iterator<String> keys = trackersJson.keys();

                while(keys.hasNext()) {
                    String key = keys.next();
                    JSONObject tracker = trackersJson.getJSONObject(key);
                    String networkSignature = tracker.getString("network_signature");
                    Tracker trackerDetailed = new Tracker(-1, tracker.getString("name"),
                            "",  tracker.getInt("id"), tracker.getString("description"), networkSignature);

                    if(!networkSignature.isEmpty()){
                        domainToTracker.put(networkSignature, tracker.getString("name"));
                    }
                    trackers.add(trackerDetailed);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public List<Tracker> getTrackers(){
        return trackers;
    }

    public static ExodusListManager getInstance(Context context){
        if(sExodusListManager == null)
            sExodusListManager = new ExodusListManager(context);
        return sExodusListManager;
    }

    public String getTrackForDomain(String domain){
        for(Map.Entry<String, String> entry : domainToTracker.entrySet()){
            //if(entry.getKey().contains("google"))
             //   Log.d("DNSBlockerRunnable","is google "+entry.getKey());
            String net = entry.getKey();
            String [] regexs = net.split("\\|");
            for(String regex : regexs){
                regex = ".*"+regex;
                if(domain.matches(regex))
                    return entry.getValue();
            }

        }
        return null;
    }
}
