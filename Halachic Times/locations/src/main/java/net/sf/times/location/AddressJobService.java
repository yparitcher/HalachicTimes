/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.times.location;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import net.sf.times.location.AddressProvider.OnFindAddressListener;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static net.sf.times.location.ZmanimLocationListener.ACTION_ADDRESS;
import static net.sf.times.location.ZmanimLocationListener.ACTION_ELEVATION;

/**
 * Service to find an address.
 *
 * @author Moshe Waisberg
 */
@TargetApi(LOLLIPOP_MR1)
public class AddressJobService extends JobService implements OnFindAddressListener {

    private static final String TAG = "AddressJobService";

    public static final String EXTRA_ACTION = "android.intent.ACTION";

    private static final String PARAMETER_LOCATION = ZmanimLocationListener.EXTRA_LOCATION;
    private static final String PARAMETER_ADDRESS = ZmanimLocationListener.EXTRA_ADDRESS;
    private static final String PARAMETER_PERSIST = ZmanimLocationListener.EXTRA_PERSIST;

    private static final boolean PERSIST = true;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.v(TAG, "start job");
        LocationApplication app = (LocationApplication) getApplication();
        final AddressProvider addressProvider = app.getAddresses();

        final PersistableBundle extras = jobParameters.getExtras();
        if (extras.isEmpty())
            return false;
        final Location location = LocationUtils.readParcelable(extras, PARAMETER_LOCATION);
        if (location == null)
            return false;

        final AddressProvider provider = addressProvider;
        if (provider == null)
            return false;
        String action = extras.getString(EXTRA_ACTION);
        if (ACTION_ADDRESS.equals(action)) {
            new Thread() {
                @Override
                public void run() {
                    if (extras.containsKey(PARAMETER_PERSIST)) {
                        Bundle locationExtras = location.getExtras();
                        if (locationExtras == null) {
                            locationExtras = new Bundle();
                        }
                        locationExtras.putBoolean(PARAMETER_PERSIST, extras.getBoolean(PARAMETER_PERSIST, PERSIST));
                        location.setExtras(locationExtras);
                    }
                    provider.findNearestAddress(location, AddressJobService.this);
                    jobFinished(jobParameters, false);
                }
            }.start();
            return true;
        }
        if (ACTION_ELEVATION.equals(action)) {
            new Thread() {
                @Override
                public void run() {
                    provider.findElevation(location, AddressJobService.this);
                    jobFinished(jobParameters, false);
                }
            }.start();
            return true;
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.v(TAG, "stop job");
        return false;
    }

    @Override
    public void onFindAddress(AddressProvider provider, Location location, Address address) {
        ZmanimAddress addr = null;
        if (address != null) {
            if (address instanceof ZmanimAddress) {
                addr = (ZmanimAddress) address;
            } else {
                addr = new ZmanimAddress(address);
                if (location.hasAltitude()) {
                    addr.setElevation(location.getAltitude());
                }
            }
            Bundle extras = location.getExtras();
            if ((extras == null) || extras.getBoolean(PARAMETER_PERSIST, PERSIST)) {
                provider.insertOrUpdateAddress(location, addr);
            }
        }

        Intent result = new Intent(ACTION_ADDRESS);
        result.setPackage(getPackageName());
        result.putExtra(PARAMETER_LOCATION, location);
        result.putExtra(PARAMETER_ADDRESS, addr);
        sendBroadcast(result);
    }

    @Override
    public void onFindElevation(AddressProvider provider, Location location, ZmanimLocation elevated) {
        if (elevated != null) {
            provider.insertOrUpdateElevation(elevated);

            Intent result = new Intent(ACTION_ELEVATION);
            result.setPackage(getPackageName());
            result.putExtra(PARAMETER_LOCATION, elevated);
            sendBroadcast(result);
        }
    }
}
