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

package foundation.e.trackerfilter.api;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import foundation.e.privacymodules.trackers.ITrackTrackersPrivacyModule;
import foundation.e.privacymodules.trackers.Tracker;
import foundation.e.trackerfilter.StatsDatabase;

public class TrackTrackersPrivacyModule implements ITrackTrackersPrivacyModule {
    private static TrackTrackersPrivacyModule sTrackTrackersPrivacyModule;
    private final Context mContext;

    public TrackTrackersPrivacyModule(Context ct) {
        mContext = ct;
    }

    @Override
    public List<Integer> getPastDayTrackersCalls() {
        return StatsDatabase.getInstance(mContext).getPast24h();
    }

    @Override
    public List<Integer> getPastMonthTrackersCalls() {
        return StatsDatabase.getInstance(mContext).getPastMonth();
    }

    @Override
    public List<Integer> getPastYearTrackersCalls() {
        return StatsDatabase.getInstance(mContext).getPastYear();
    }

    @Override
    public int getTrackersCount() {
        return 0;
    }

    @Override
    public List<Tracker> getTrackersForApp(int i) {
        return StatsDatabase.getInstance(mContext).getAllTrackersOfApp(i);
    }
    public static TrackTrackersPrivacyModule getInstance(Context ct){
        if(sTrackTrackersPrivacyModule == null){
            sTrackTrackersPrivacyModule = new TrackTrackersPrivacyModule(ct);
        }
        return sTrackTrackersPrivacyModule;
    }

    private int sumOfList(List<Integer> list){
        int total = 0;
        for(int call: list){
            total+=call;
        }
        return total;
    }

    @Override
    public int getPastDayTrackersCount() {
        /* TODO optimise */
        return sumOfList(getPastDayTrackersCalls());
    }

    @Override
    public int getPastMonthTrackersCount() {
        /* TODO optimise */
        return sumOfList(getPastMonthTrackersCalls());
    }

    @Override
    public int getPastYearTrackersCount() {
        /* TODO optimise */
        return sumOfList(getPastYearTrackersCalls());
    }
}
