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
import android.location.Address;
import android.text.TextUtils;

import com.github.io.StreamUtils;
import com.github.net.HTTPReader;
import com.github.util.LocaleUtils;
import com.github.util.LogUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * A class for handling geocoding and reverse geocoding.
 *
 * @author Moshe Waisberg
 */
public abstract class GeocoderBase {
    private static final String TAG = "GeocoderBase";

    /**
     * The "user pick a city" location provider.
     */
    public static final String USER_PROVIDER = "user";

    /**
     * Maximum radius to consider two locations in the same vicinity.
     */
    protected static final float SAME_LOCATION = 250f;// 250 metres.
    /**
     * Maximum radius to consider a location near the same city.
     * <p/>
     * New York city, USA, is <tt>8,683 km<sup>2</sup></tt>, thus radius is
     * about <tt>37.175 km</tt>.<br>
     * Johannesburg/East Rand, ZA, is <tt>2,396 km<sup>2</sup></tt>, thus radius
     * is about <tt>19.527 km</tt>..<br>
     * Cape Town, ZA, is <tt>686 km<sup>2</sup></tt>, thus radius is about
     * <tt>10.449 km</tt>.
     */
    protected static final float SAME_CITY = 15000f;// 15 kilometres.
    /**
     * Maximum radius to consider a location near the same plateau with similar terrain.
     */
    protected static final float SAME_PLATEAU = 100000f;// 100 kilometres.
    /**
     * Maximum radius to consider a location near the same planet.
     */
    protected static final float SAME_PLANET = 6000000f;// 6000 kilometres.
    /**
     * Lowest possible natural elevation on the surface of the earth.
     */
    protected static final double ELEVATION_LOWEST_SURFACE = -500;

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;
    protected static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    protected static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    protected final Locale locale;
    private static SAXParserFactory parserFactory;
    private static SAXParser parser;

    /**
     * Creates a new geocoder.
     *
     * @param locale the locale.
     */
    public GeocoderBase(Locale locale) {
//        this.context = context;
        this.locale = locale;
    }

    /**
     * Creates a new geocoder.
     *
     * @param context the context.
     */
    public GeocoderBase(Context context) {
        this(LocaleUtils.getDefaultLocale(context));
    }

    protected SAXParserFactory getParserFactory() {
        if (parserFactory == null)
            parserFactory = SAXParserFactory.newInstance();
        return parserFactory;
    }

    protected SAXParser getParser() throws ParserConfigurationException, SAXException {
        if (parser == null)
            parser = getParserFactory().newSAXParser();
        return parser;
    }

    /**
     * Returns an array of Addresses that are known to describe the area
     * immediately surrounding the given latitude and longitude.
     *
     * @param latitude   the latitude a point for the search.
     * @param longitude  the longitude a point for the search.
     * @param maxResults maximum number of addresses to return. Smaller numbers (1 to
     *                   5) are recommended.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    public abstract List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException;

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     * <p/>
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName a user-supplied description of a location.
     * @param maxResults   max number of addresses to return. Smaller numbers (1 to 5)
     *                     are recommended.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        return null;
    }

    /**
     * Returns an array of Addresses that are known to describe the named
     * location, which may be a place name such as "Dalvik, Iceland", an address
     * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
     * such as "SFO", etc.. The returned addresses will be localized for the
     * locale provided to this class's constructor.
     * <p/>
     * You may specify a bounding box for the search results by including the
     * Latitude and Longitude of the Lower Left point and Upper Right point of
     * the box.
     * <p/>
     * The query will block and returned values will be obtained by means of a
     * network lookup. The results are a best guess and are not guaranteed to be
     * meaningful or correct. It may be useful to call this method from a thread
     * separate from your primary UI thread.
     *
     * @param locationName        a user-supplied description of a location.
     * @param maxResults          max number of addresses to return. Smaller numbers (1 to 5)
     *                            are recommended.
     * @param lowerLeftLatitude   the latitude of the lower left corner of the bounding box.
     * @param lowerLeftLongitude  the longitude of the lower left corner of the bounding box.
     * @param upperRightLatitude  the latitude of the upper right corner of the bounding box.
     * @param upperRightLongitude the longitude of the upper right corner of the bounding box.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    public List<Address> getFromLocationName(String locationName, int maxResults, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
                                             double upperRightLongitude) throws IOException {
        if (locationName == null)
            throw new IllegalArgumentException("locationName == null");
        if (lowerLeftLatitude < LATITUDE_MIN || lowerLeftLatitude > LATITUDE_MAX)
            throw new IllegalArgumentException("lowerLeftLatitude == " + lowerLeftLatitude);
        if (lowerLeftLongitude < LONGITUDE_MIN || lowerLeftLongitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("lowerLeftLongitude == " + lowerLeftLongitude);
        if (upperRightLatitude < LATITUDE_MIN || upperRightLatitude > LATITUDE_MAX)
            throw new IllegalArgumentException("upperRightLatitude == " + upperRightLatitude);
        if (upperRightLongitude < LONGITUDE_MIN || upperRightLongitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("upperRightLongitude == " + upperRightLongitude);
        return null;
    }

    /**
     * Get the address by parsing the XML results.
     *
     * @param queryUrl   the URL.
     * @param maxResults the maximum number of results.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    protected List<Address> getAddressXMLFromURL(String queryUrl, int maxResults) throws IOException {
        URL url = new URL(queryUrl);
        InputStream data = HTTPReader.read(url, HTTPReader.CONTENT_XML);
        try {
            return parseLocations(data, maxResults);
        } catch (ParserConfigurationException pce) {
            throw new IOException(pce);
        } catch (SAXException se) {
            throw new IOException(se);
        }
    }

    /**
     * Parse the XML response for addresses.
     *
     * @param data       the XML data.
     * @param maxResults the maximum number of results.
     * @return a list of addresses. Returns {@code null} or empty list if no
     * matches were found or there is no backend service available.
     * @throws ParserConfigurationException if an XML error occurs.
     * @throws SAXException                 if an XML error occurs.
     * @throws IOException                  if an I/O error occurs.
     */
    protected List<Address> parseLocations(InputStream data, int maxResults) throws ParserConfigurationException, SAXException, IOException {
        // Minimum length for "<X/>"
        if ((data == null) || (data.available() <= 4)) {
            return null;
        }

        List<Address> results = new ArrayList<Address>(maxResults);
        SAXParser parser = getParser();
        DefaultHandler handler = createAddressResponseHandler(results, maxResults, locale);
        parser.parse(data, handler);

        return results;
    }

    /**
     * Create an SAX XML handler for addresses.
     *
     * @param results    the list of results to populate.
     * @param maxResults the maximum number of results.
     * @param locale     the locale.
     * @return the XML handler.
     */
    protected abstract DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale);

    /**
     * Get the ISO 639 language code.
     *
     * @return the language code.
     */
    protected String getLanguage() {
        String language = locale.getLanguage();
        if ("in".equals(language))
            return "id";
        if ("iw".equals(language))
            return "he";
        if ("ji".equals(language))
            return "yi";
        return language;
    }

    /**
     * Get the location with elevation.
     *
     * @param latitude  the latitude a point for the search.
     * @param longitude the longitude a point for the search.
     * @return the location - {@code null} otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    public abstract ZmanimLocation getElevation(double latitude, double longitude) throws IOException;

    /**
     * Get the elevation by parsing the XML results.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - {@code null} otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    protected ZmanimLocation getElevationXMLFromURL(double latitude, double longitude, String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        InputStream data = HTTPReader.read(url, HTTPReader.CONTENT_XML);
        try {
            return parseElevationXML(latitude, longitude, data);
        } catch (ParserConfigurationException pce) {
            throw new IOException(pce);
        } catch (SAXException se) {
            throw new IOException(se);
        }
    }

    /**
     * Parse the XML response for an elevation.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param data      the XML data.
     * @return the location - {@code null} otherwise.
     * @throws ParserConfigurationException if an XML error occurs.
     * @throws SAXException                 if an XML error occurs.
     * @throws @throws                      IOException if an I/O error occurs.
     */
    protected ZmanimLocation parseElevationXML(double latitude, double longitude, InputStream data) throws ParserConfigurationException, SAXException, IOException {
        // Minimum length for "<X/>"
        if ((data == null) || (data.available() <= 4)) {
            return null;
        }

        List<ZmanimLocation> results = new ArrayList<ZmanimLocation>(1);
        SAXParser parser = getParser();
        DefaultHandler handler = createElevationResponseHandler(latitude, longitude, results);
        parser.parse(data, handler);

        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     * Get the elevation by parsing the plain text result.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param queryUrl  the URL.
     * @return the location - {@code null} otherwise.
     * @throws IOException if the network is unavailable or any other I/O problem occurs.
     */
    protected ZmanimLocation getElevationTextFromURL(double latitude, double longitude, String queryUrl) throws IOException {
        URL url = new URL(queryUrl);
        InputStream data = HTTPReader.read(url);
        return parseElevationText(latitude, longitude, data);
    }

    /**
     * Parse the plain text response for an elevation.
     *
     * @param latitude  the latitude.
     * @param longitude the longitude.
     * @param data      the textual data.
     * @return the location - {@code null} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    protected ZmanimLocation parseElevationText(double latitude, double longitude, InputStream data) throws IOException {
        // Minimum length for "0"
        if ((data == null) || (data.available() <= 0)) {
            return null;
        }
        String text = StreamUtils.toString(data);
        char first = text.charAt(0);
        if (!Character.isDigit(first) && (first != '-')) {
            return null;
        }
        double elevation;
        ZmanimLocation elevated;
        try {
            elevation = Double.parseDouble(text);
            if (elevation <= ELEVATION_LOWEST_SURFACE) {
                return null;
            }
            elevated = new ZmanimLocation(USER_PROVIDER);
            elevated.setTime(System.currentTimeMillis());
            elevated.setLatitude(latitude);
            elevated.setLongitude(longitude);
            elevated.setAltitude(elevation);
            return elevated;
        } catch (NumberFormatException nfe) {
            LogUtils.e(TAG, "Bad elevation: [" + text + "] at " + latitude + "," + longitude, nfe);
        }
        return null;
    }

    /**
     * Create an SAX XML handler for elevations.
     *
     * @param latitude
     * @param longitude
     * @param results   the list of results to populate.
     * @return the XML handler.
     */
    protected abstract DefaultHandler createElevationResponseHandler(double latitude, double longitude, List<ZmanimLocation> results);

    /**
     * Handler for parsing the XML response.
     *
     * @author Moshe
     */
    protected abstract static class DefaultAddressResponseHandler extends DefaultHandler2 {

        private final StringBuffer text = new StringBuffer();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (TextUtils.isEmpty(localName))
                localName = qName;

            text.delete(0, text.length());

            startElement(uri, localName, attributes);
        }

        protected void startElement(String uri, String localName, Attributes attributes) throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (length == 0)
                return;
            text.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (TextUtils.isEmpty(localName))
                localName = qName;

            endElement(uri, localName, qName, text.toString().trim());
        }

        protected void endElement(String uri, String localName, String qName, String text) throws SAXException {
        }
    }
}
