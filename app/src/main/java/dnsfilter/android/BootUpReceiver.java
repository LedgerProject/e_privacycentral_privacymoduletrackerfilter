/* 
 PersonalDNSFilter 1.5
 Copyright (C) 2017 Ingo Zenz
 Copyright (C) 2021 E FOUNDATION

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
package dnsfilter.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;

import foundation.e.trackerfilter.DNSBlockerService;
import foundation.e.trackerfilter.StatsIntentService;
import util.ExecutionEnvironment;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AndroidEnvironment.initEnvironment(context);
		Properties config;
		if ((config = getConfig()) != null && Boolean.parseBoolean(config.getProperty("AUTOSTART", "false"))) {

			if (Build.VERSION.SDK_INT >= 28) {
				Intent i = new Intent(context, DNSFilterService.class);
				VpnService.prepare(context);
				context.startForegroundService(i);
				i = new Intent(context, DNSBlockerService.class);
				context.startForegroundService(i);
				i = new Intent(context, StatsIntentService.class);
				context.startForegroundService(i);
			} else {
				DNSProxyActivity.BOOT_START = true;
				Intent i = new Intent(context, DNSProxyActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			}
		}
	}

	public Properties getConfig() {

		File propsFile = new File(ExecutionEnvironment.getEnvironment().getWorkDir()+"/dnsfilter.conf");

		try {
			InputStream in = new FileInputStream(propsFile);
			Properties config = new Properties();
			config.load(in);
			in.close();
			return config;
		} catch (Exception e) {
			return null;
		}
	}
}