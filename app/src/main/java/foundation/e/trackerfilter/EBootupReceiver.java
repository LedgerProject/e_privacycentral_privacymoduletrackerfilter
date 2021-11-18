/*
 Copyright (C) 2021 ECORP

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

 */

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