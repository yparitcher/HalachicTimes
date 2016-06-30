/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.times.location;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.TimeZone;

/**
 * Activity that needs locations.
 *
 * @author Moshe Waisberg
 */
public abstract class LocatedActivity extends Activity implements ZmanimLocationListener {

    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

    /** Activity id for searching locations. */
    protected static final int ACTIVITY_LOCATIONS = 1;

    /** Provider for locations. */
    private LocationsProvider locations;
//    /** The address location. */
//    private Location addressLocation;
//    /** The address. */
//    private ZmanimAddress address;
//    /** Populate the header in UI thread. */
//    private Runnable populateHeader;
//    /** Update the location in UI thread. */
//    private Runnable updateLocation;

    /**
     * Get the locations provider.
     *
     * @return hte provider.
     */
    public LocationsProvider getLocations() {
        return locations;
    }

    /**
     * Get the location.
     *
     * @return the location.
     */
    protected Location getLocation() {
        return getLocations().getLocation();
    }

    /**
     * Get the time zone.
     *
     * @return the time zone.
     */
    protected TimeZone getTimeZone() {
        return getLocations().getTimeZone();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();

        Intent intent = getIntent();
        Location location = intent.getParcelableExtra(EXTRA_LOCATION);
        if (location != null)
            locations.setLocation(location);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locations.start(this);
    }

    @Override
    protected void onStop() {
        locations.stop(this);
        super.onStop();
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
    }

    @Override
    public void onElevationChanged(Location location) {
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    protected void startLocations() {
        Location loc = locations.getLocation();
        // Have we been destroyed?
        if (loc == null)
            return;

        Activity activity = this;
        Intent intent = new Intent(activity, getLocationActivityClass());
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
        activity.startActivityForResult(intent, ACTIVITY_LOCATIONS);
    }

    protected abstract Class<? extends Activity> getLocationActivityClass();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_LOCATIONS) {
            if (resultCode == RESULT_OK) {
                Location loc = data.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
                if (loc == null) {
                    locations.setLocation(null);
                    loc = locations.getLocation();
                }
                locations.setLocation(loc);
            }
        }
    }
}
