package foundation.e.trackerfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;

import java.util.Properties;

import dnsfilter.android.AndroidEnvironment;
import dnsfilter.android.DNSFilterService;
import dnsfilter.android.DNSProxyActivity;

public class EBootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidEnvironment.initEnvironment(context);
        if (Build.VERSION.SDK_INT >= 28) {
            Intent i = new Intent(context, DNSBlockerService.class);
            context.startForegroundService(i);
            i = new Intent(context, StatsIntentService.class);
            context.startService(i);
        }
    }
}