package dnsfilter;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExodusListManager {
    private static ExodusListManager sExodusListManager;
    private Context mContext;
    private HashMap<String, String> domainToTracker = new HashMap<>();

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
                JSONObject trackers = object.getJSONObject("trackers");
                Iterator<String> keys = trackers.keys();

                while(keys.hasNext()) {
                    String key = keys.next();
                    JSONObject tracker = trackers.getJSONObject(key);
                    String networkSignature = tracker.getString("network_signature");
                    if(!networkSignature.isEmpty()){
                        domainToTracker.put(networkSignature, tracker.getString("name"));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
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
