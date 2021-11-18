package foundation.e.trackerfilter;

/*
 PersonalDNSFilter 1.5
 Copyright (C) 2017 Ingo Zenz
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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import dnsfilter.DNSResponsePatcher;
import dnsfilter.android.DNSFilterService;
import foundation.e.privacymodules.trackers.Tracker;
import foundation.e.trackerfilter.api.BlockTrackersPrivacyModule;
import foundation.e.trackerfilter.api.TrackTrackersPrivacyModule;
import util.ExecutionEnvironment;
import util.Logger;

public class DNSBlockerRunnable implements Runnable {

	private static List<Integer> sSystemApps;
	private final Context mContext;
	ServerSocket resolverReceiver;
	boolean stopped = false;
	int port = 8888;
	private final String TAG = DNSBlockerRunnable.class.getName();

	public DNSBlockerRunnable(Context ct, int port) {
		this.mContext = ct;
		this.port = port;
	}

	public static void init(Context context){
		if(sSystemApps == null) {
			sSystemApps = new ArrayList<>();
			List<ApplicationInfo> apps = AppTrackerWhitelist.getInstance(context).getWhitelistableApps(true);
			for (ApplicationInfo app : apps) {
						sSystemApps.add(app.uid);
			}
		}
	}

	@Override
	public void run() {
		init(mContext);
		try {
			resolverReceiver
					= new ServerSocket(8888);

	//		ExecutionEnvironment.getEnvironment().protectSocket(resolverReceiver, 1);

		} catch (IOException eio) {
			Logger.getLogger().logLine("Exception:Cannot open DNS port " + port + "!" + eio.getMessage());
			return;
		}
		Logger.getLogger().logLine("DNSFilterProxy running on port " + port + "!");

		while (!stopped) {
			try {
				Log.d(TAG,"Wating for connection");
				Socket socket = resolverReceiver.accept();
				Log.d(TAG,"Connection accepted");

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				Log.d(TAG,"Reading buffer");
				String line = reader.readLine();
				Log.d(TAG,"Contexnt: "+line);
				String [] params = line.split(",");
				OutputStream output = socket.getOutputStream();
				PrintWriter writer = new PrintWriter(output, true);
				AppTrackerWhitelist dbHelper = AppTrackerWhitelist.getInstance(mContext);
				String domainName = params[0];
				int appUid = Integer.parseInt(params[1]);
				boolean shouldBlock = false;
				String [] packages = mContext.getPackageManager().getPackagesForUid(appUid);
				boolean isBrowser = false;
				if(packages != null) {
					for (String packageName : packages)
						if (packageName.equals("foundation.e.browser"))
							isBrowser = true;
					if (domainName.equals("chrome.cloudflare-dns.com") && isBrowser)
						shouldBlock = true;
					if (DNSResponsePatcher.filter(domainName, false)) {

						if (BlockTrackersPrivacyModule.getInstance(mContext).isBlockingEnabled() && !sSystemApps.contains(appUid)) {
							Tracker tracker = TrackerListManager.getInstance(mContext).getTrackerByDomainName(domainName);

							// tracker can be null if not in exodus list and was never encountered. if app isn't whitelisted, we block null trackers
							if (!dbHelper.isAppWhitelisted(appUid) && (tracker == null || !dbHelper.isTrackerWhitelistedForApp(tracker.getId(), appUid))) {
								writer.println("block");
								shouldBlock = true;
								if (tracker != null)
									Log.d(TAG, "tracker " + tracker.getLabel());
								for (String packageName : packages)
									Log.d(TAG, "blocking " + domainName + " for " + packageName);

							}
						}
						StatsIntentService.startActionLog(mContext, domainName, appUid, shouldBlock);
					}
				}
				if(!shouldBlock) {
					writer.println("pass");
					if(packages != null)
					for(String packageName: packages)
						Log.d(TAG, "not blocking " + domainName + " for " + packageName);

				}
				socket.close();
				// Printing bufferedreader data
			} catch (IOException e) {
				Log.d(TAG,"exception "+ e.getMessage());

			}
		}
		Log.d(TAG,"Stopped");
		try {
			resolverReceiver.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public synchronized void stop() {
		//stopped = true;

	}


}
