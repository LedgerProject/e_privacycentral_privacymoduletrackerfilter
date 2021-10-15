package dnsfilter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

public class DNSBlockerService extends Service {
    private static DNSBlockerRunnable sDNSBlocker;
    private static DNSFilterManager sDnsFilter;

    public DNSBlockerService() {
    }

    public static void stop(boolean appExit) {
        if(sDNSBlocker != null)
            sDNSBlocker.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sDnsFilter = DNSFilterManager.getInstance();
        try {
            try {
                sDnsFilter.init();
            } catch (IllegalStateException e){ //fired if already running

            }
            sDNSBlocker = new DNSBlockerRunnable(8888);
            new Thread(sDNSBlocker).start();
        } catch (IOException e) {
            sDnsFilter = null;
            e.printStackTrace();
        }


        return START_STICKY;

    }
}