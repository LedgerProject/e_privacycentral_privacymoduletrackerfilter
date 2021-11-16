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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

import dnsfilter.DNSFilterManager;

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
            sDNSBlocker = new DNSBlockerRunnable(this,8888);
            new Thread(sDNSBlocker).start();
        } catch (IOException e) {
            sDnsFilter = null;
            e.printStackTrace();
        }
        ForegroundStarter.startForeground(this);


        return START_STICKY;

    }
    
}