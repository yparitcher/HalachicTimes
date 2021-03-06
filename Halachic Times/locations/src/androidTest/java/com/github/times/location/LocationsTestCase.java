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
package com.github.times.location;

import android.content.Context;
import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.TimeZone;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getContext;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LocationsTestCase {

    protected LocationApplication getApplication() {
        final Context context = getTargetContext();
        assertNotNull(context);
        Context applicationContext = context.getApplicationContext();
        assertNotNull(applicationContext);
        assertTrue(applicationContext instanceof LocationApplication);
        return (LocationApplication) applicationContext;
    }

    /**
     * Test application.
     */
    @Test
    public void testApp() {
        final Context context = getContext();
        assertNotNull(context);
        assertEquals("com.github.times.location.test", context.getPackageName());

        LocationApplication app = getApplication();
        assertNotNull(app);
        LocationsProvider locations = app.getLocations();
        assertNotNull(locations);
    }

    /**
     * Test time zones.
     * <p/>
     * Loop through all TZs and check that their longitude and latitude are
     * valid.
     */
    @Test
    public void testTZ() {
        LocationApplication app = getApplication();
        assertNotNull(app);
        LocationsProvider locations = app.getLocations();
        assertNotNull(locations);

        String[] ids = TimeZone.getAvailableIDs();
        assertNotNull(ids);

        TimeZone tz;
        Location loc;
        double latitude;
        double longitude;

        for (String id : ids) {
            assertNotNull(id);
            tz = TimeZone.getTimeZone(id);
            assertNotNull(tz);

            loc = locations.getLocationTZ(tz);
            assertNotNull(loc);
            latitude = loc.getLatitude();
            assertTrue(id + " " + latitude, latitude >= ZmanimLocation.LATITUDE_MIN);
            assertTrue(id + " " + latitude, latitude <= ZmanimLocation.LATITUDE_MAX);
            longitude = loc.getLongitude();
            assertTrue(id + " " + longitude, longitude >= ZmanimLocation.LONGITUDE_MIN);
            assertTrue(id + " " + longitude, longitude <= ZmanimLocation.LONGITUDE_MAX);
        }
    }

    /**
     * Test Rhumb Line angles.
     */
    @Test
    public void testAngle() {
        ZmanimLocation temple = new ZmanimLocation("temple");
        temple.setLatitude(toDegrees(31, 46, 40, Hemisphere.NORTH));
        temple.setLongitude(toDegrees(35, 14, 04, Hemisphere.EAST));
        assertLocation(temple);
        assertEquals(31.77777777f, temple.getLatitude(), 0.000001);

        ZmanimLocation loc = new ZmanimLocation("city");
        final float delta = 1f;

        // Anchorage
        loc.setLatitude(toDegrees(61, 01, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(150, 00, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(256, loc.angleTo(temple), delta);

        // San Francisco
        loc.setLatitude(toDegrees(37, 45, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(122, 27, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(93, loc.angleTo(temple), delta);

        // Los Angeles
        loc.setLatitude(toDegrees(34, 03, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(118, 15, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(91, loc.angleTo(temple), delta);

        // Chicago
        loc.setLatitude(toDegrees(41, 50, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(87, 37, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(96, loc.angleTo(temple), delta);

        // Miami
        loc.setLatitude(toDegrees(25, 45, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(80, 15, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(87, loc.angleTo(temple), delta);

        // Toronto
        loc.setLatitude(toDegrees(43, 42, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(79, 25, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(97, loc.angleTo(temple), delta);

        // Washington
        loc.setLatitude(toDegrees(38, 55, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(77, 00, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(94, loc.angleTo(temple), delta);

        // Philadelphia
        loc.setLatitude(toDegrees(40, 00, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(75, 10, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(95, loc.angleTo(temple), delta);

        // New York
        loc.setLatitude(toDegrees(40, 43, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(74, 00, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(96, loc.angleTo(temple), delta);

        // Boston
        loc.setLatitude(toDegrees(42, 21, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(71, 04, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(97, loc.angleTo(temple), delta);

        // Bueons Aires
        loc.setLatitude(toDegrees(34, 40, Hemisphere.SOUTH));
        loc.setLongitude(toDegrees(58, 30, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(53, loc.angleTo(temple), delta);

        // London
        loc.setLatitude(toDegrees(51, 30, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(0, 10, Hemisphere.WEST));
        assertLocation(loc);
        assertEquals(127, loc.angleTo(temple), delta);

        // Paris
        loc.setLatitude(toDegrees(48, 52, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(2, 20, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(125, loc.angleTo(temple), delta);

        // Budapest
        loc.setLatitude(toDegrees(47, 30, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(19, 03, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(142, loc.angleTo(temple), delta);

        // Johannesburg
        loc.setLatitude(toDegrees(26, 10, Hemisphere.SOUTH));
        loc.setLongitude(toDegrees(28, 02, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(7, loc.angleTo(temple), delta);

        // Kiev
        loc.setLatitude(toDegrees(50, 25, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(30, 30, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(169, loc.angleTo(temple), delta);

        // Tel Aviv
        loc.setLatitude(toDegrees(32, 05, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(34, 46, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(128, loc.angleTo(temple), delta);

        // Haifa
        loc.setLatitude(toDegrees(32, 49, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(34, 59, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(168, loc.angleTo(temple), delta);

        // Moscow
        loc.setLatitude(toDegrees(55, 45, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(37, 37, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(184, loc.angleTo(temple), delta);

        // Tokyo
        loc.setLatitude(toDegrees(35, 40, Hemisphere.NORTH));
        loc.setLongitude(toDegrees(139, 45, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(267, loc.angleTo(temple), delta);

        // Melbourne
        loc.setLatitude(toDegrees(37, 50, Hemisphere.SOUTH));
        loc.setLongitude(toDegrees(144, 59, Hemisphere.EAST));
        assertLocation(loc);
        assertEquals(304, loc.angleTo(temple), delta);
    }

    private void assertLocation(Location location) {
        final double latitude = location.getLatitude();
        if ((latitude < ZmanimLocation.LATITUDE_MIN) || (latitude > ZmanimLocation.LATITUDE_MAX))
            fail("Invalid latitude: " + latitude);
        final double longitude = location.getLongitude();
        if ((longitude < ZmanimLocation.LONGITUDE_MIN) || (longitude > ZmanimLocation.LONGITUDE_MAX))
            fail("Invalid longitude: " + longitude);
    }

    private enum Hemisphere {
        NORTH(+1),
        EAST(+1),
        SOUTH(-1),
        WEST(-1);

        private final int sign;

        Hemisphere(int sign) {
            this.sign = sign;
        }

        public int sign() {
            return sign;
        }
    }

    private double toDegrees(int d, int m, Hemisphere hemisphere) {
        return toDegrees(d, m, 0, hemisphere);
    }

    private double toDegrees(int d, int m, int s, Hemisphere hemisphere) {
        assertTrue(d >= 0);
        assertTrue(d <= 180);
        assertTrue(m >= 0);
        assertTrue(m < 60);
        assertTrue(s >= 0);
        assertTrue(s < 60);
        return hemisphere.sign() * (d + ((m + (s / 60.0)) / 60.0));
    }
}
