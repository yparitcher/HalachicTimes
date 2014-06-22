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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Address provider.<br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 * 
 * @author Moshe Waisberg
 */
public class AddressProvider {
	private static final String TAG = "AddressProvider";

	public static interface OnFindAddressListener {

		/**
		 * Called when an address is found.
		 * 
		 * @param provider
		 *            the address provider.
		 * @param location
		 *            the requested location.
		 * @param address
		 *            the found address.
		 */
		public void onFindAddress(AddressProvider provider, Location location, Address address);

	}

	private static final String[] COLUMNS = { BaseColumns._ID, AddressColumns.LOCATION_LATITUDE, AddressColumns.LOCATION_LONGITUDE, AddressColumns.LATITUDE,
			AddressColumns.LONGITUDE, AddressColumns.ELEVATION, AddressColumns.ADDRESS, AddressColumns.LANGUAGE, AddressColumns.FAVORITE };
	private static final int INDEX_ID = 0;
	private static final int INDEX_LOCATION_LATITUDE = 1;
	private static final int INDEX_LOCATION_LONGITUDE = 2;
	private static final int INDEX_LATITUDE = 3;
	private static final int INDEX_LONGITUDE = 4;
	private static final int INDEX_ELEVATION = 5;
	private static final int INDEX_ADDRESS = 6;
	private static final int INDEX_LANGUAGE = 7;
	private static final int INDEX_FAVORITE = 8;

	private static final String WHERE_ID = BaseColumns._ID + "=?";

	/** Maximum radius to consider two locations in the same vicinity. */
	private static final float SAME_LOCATION = 250f;// 250 metres.
	/** Maximum radius to consider a location near the same city. */
	private static final float SAME_CITY = 10000f;// 10 kilometres.
	/**
	 * Maximum radius to consider a location near the same plateau with similar
	 * terrain.
	 */
	private static final float SAME_PLATEAU = 50000f;// 50 kilometres.

	private final Context mContext;
	private final Locale mLocale;
	private SQLiteOpenHelper mOpenHelper;
	/** The list of countries. */
	private CountriesGeocoder mCountries;

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 */
	public AddressProvider(Context context) {
		this(context, Locale.getDefault());
	}

	/**
	 * Constructs a new provider.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public AddressProvider(Context context, Locale locale) {
		super();
		mContext = context;
		mLocale = locale;
		mCountries = new CountriesGeocoder(context, locale);
	}

	/**
	 * Find the nearest address of the location.
	 * 
	 * @param location
	 *            the location.
	 * @param listener
	 *            the listener.
	 * @return the address.
	 */
	public Address findNearestAddress(Location location, OnFindAddressListener listener) {
		if (location == null)
			return null;
		final double latitude = location.getLatitude();
		if ((latitude > 90) || (latitude < -90))
			return null;
		final double longitude = location.getLongitude();
		if ((longitude > 180) || (longitude < -180))
			return null;

		List<Address> addresses;
		Address best = null;
		Address bestCountry;
		Address bestCity;

		if (listener != null)
			listener.onFindAddress(this, location, best);

		addresses = findNearestCountry(location);
		best = findBestAddress(location, addresses);
		if ((best != null) && (listener != null))
			listener.onFindAddress(this, location, best);
		bestCountry = best;

		addresses = findNearestCity(location);
		best = findBestAddress(location, addresses);
		if ((best != null) && (listener != null))
			listener.onFindAddress(this, location, best);
		bestCity = best;

		addresses = findNearestAddressDatabase(location);
		best = findBestAddress(location, addresses);
		if ((best != null) && (listener != null))
			listener.onFindAddress(this, location, best);

		if (best == null) {
			addresses = findNearestAddressGeocoder(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			addresses = findNearestAddressGoogle(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			addresses = findNearestAddressBing(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			addresses = findNearestAddressGeoNames(location);
			best = findBestAddress(location, addresses);
			if ((best != null) && (listener != null))
				listener.onFindAddress(this, location, best);
		}
		if (best == null) {
			best = bestCity;
		}
		if (best == null) {
			best = bestCountry;
		}

		return best;
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the built-in Android {@link Geocoder} API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressGeocoder(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses = null;
		Geocoder geocoder = new Geocoder(mContext);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			Log.e(TAG, "Geocoder: " + e.getLocalizedMessage(), e);
		}
		return addresses;
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the Google Maps API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressGoogle(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses = null;
		GeocoderBase geocoder = new GoogleGeocoder(mContext, mLocale);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			Log.e(TAG, "GoogleGeocoder: " + e.getLocalizedMessage(), e);
		}
		return addresses;
	}

	/**
	 * Finds the nearest street and address for a given lat/lng pair.
	 * <p>
	 * Uses the GeoNames API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressGeoNames(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses = null;
		GeocoderBase geocoder = new GeoNamesGeocoder(mContext, mLocale);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 10);
		} catch (IOException e) {
			Log.e(TAG, "GeoNames: " + e.getLocalizedMessage(), e);
		}
		return addresses;
	}

	/**
	 * Finds the nearest street and address for a given lat/lng pair.
	 * <p>
	 * Uses the Bing API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressBing(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses = null;
		GeocoderBase geocoder = new BingGeocoder(mContext, mLocale);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			Log.e(TAG, "Bing: " + e.getLocalizedMessage(), e);
		}
		return addresses;
	}

	/**
	 * Find the best address by checking relevant fields.
	 * 
	 * @param location
	 *            the location.
	 * @param addresses
	 *            the list of addresses.
	 * @return the best address.
	 */
	private Address findBestAddress(Location location, List<Address> addresses) {
		if ((addresses == null) || addresses.isEmpty())
			return null;
		if (addresses.size() == 1)
			return addresses.get(0);

		// First, find the closest location.
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		float distanceMin = Float.MAX_VALUE;
		Address addrMin = null;
		float[] distances = new float[1];
		for (Address a : addresses) {
			if (!a.hasLatitude())
				continue;
			if (!a.hasLongitude())
				continue;
			Location.distanceBetween(latitude, longitude, a.getLatitude(), a.getLongitude(), distances);
			if (distances[0] <= distanceMin) {
				distanceMin = distances[0];
				addrMin = a;
			}
		}
		if (addrMin != null)
			return addrMin;

		// Next, find the best address part.
		for (Address a : addresses) {
			if (a.getFeatureName() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getLocality() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getSubLocality() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getAdminArea() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getSubAdminArea() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getCountryName() != null)
				return a;
		}
		return addresses.get(0);
	}

	/**
	 * Format the address.
	 * 
	 * @param a
	 *            the address.
	 * @return the formatted address name.
	 */
	public static String formatAddress(ZmanimAddress a) {
		return a.getFormatted();
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the local database.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	private List<Address> findNearestAddressDatabase(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		final String language = mLocale.getLanguage();
		final String country = mLocale.getCountry();

		List<Address> addresses = new ArrayList<Address>();
		SQLiteDatabase db = getReadableDatabase();
		if (db == null)
			return addresses;
		Cursor cursor = db.query(AddressOpenHelper.TABLE_ADDRESSES, COLUMNS, null, null, null, null, null);
		if ((cursor == null) || cursor.isClosed()) {
			db.close();
			return addresses;
		}

		try {
			if (cursor.moveToFirst()) {
				long id;
				double locationLatitude;
				double locationLongitude;
				double addressLatitude;
				double addressLongitude;
				double addressElevation;
				String formatted;
				String locationLanguage;
				Locale locale;
				ZmanimAddress address;
				float[] distanceLocation = new float[1];
				float[] distanceAddress = new float[1];

				do {
					locationLanguage = cursor.getString(INDEX_LANGUAGE);
					if ((locationLanguage == null) || locationLanguage.equals(language)) {
						locationLatitude = cursor.getDouble(INDEX_LOCATION_LATITUDE);
						locationLongitude = cursor.getDouble(INDEX_LOCATION_LONGITUDE);
						addressLatitude = cursor.getDouble(INDEX_LATITUDE);
						addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
						Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distanceLocation);
						Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distanceAddress);

						if ((distanceLocation[0] <= SAME_LOCATION) || (distanceAddress[0] <= SAME_LOCATION)) {
							id = cursor.getLong(INDEX_ID);
							addressElevation = cursor.getDouble(INDEX_ELEVATION);
							formatted = cursor.getString(INDEX_ADDRESS);
							if (locationLanguage == null)
								locale = mLocale;
							else
								locale = new Locale(locationLanguage, country);

							address = new ZmanimAddress(locale);
							address.setFormatted(formatted);
							address.setId(id);
							address.setLatitude(addressLatitude);
							address.setLongitude(addressLongitude);
							address.setElevation(addressElevation);
							addresses.add(address);
						}
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException se) {
			Log.e(TAG, "Find nearest db address: " + se.getLocalizedMessage(), se);
		} finally {
			cursor.close();
			db.close();
		}

		return addresses;
	}

	/**
	 * Get the readable addresses database.
	 * 
	 * @return the database - {@code null} otherwise.
	 */
	private SQLiteDatabase getReadableDatabase() {
		if (mOpenHelper == null)
			mOpenHelper = new AddressOpenHelper(mContext);
		try {
			return mOpenHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			Log.e(TAG, "no readable db", e);
		}
		return null;
	}

	/**
	 * Get the writable addresses database.
	 * 
	 * @return the database - {@code null} otherwise.
	 */
	private SQLiteDatabase getWritableDatabase() {
		if (mOpenHelper == null)
			mOpenHelper = new AddressOpenHelper(mContext);
		try {
			return mOpenHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			Log.e(TAG, "no writable db", e);
		}
		return null;
	}

	/**
	 * Add the address to the local database. The local database is supposed to
	 * reduce redundant network requests.
	 * 
	 * @param location
	 *            the location.
	 * @param address
	 *            the address.
	 */
	public void insertOrUpdate(Location location, ZmanimAddress address) {
		if (address == null)
			return;
		long id = address.getId();
		if (id < 0L)
			return;

		ContentValues values = new ContentValues();
		if (location == null) {
			values.put(AddressColumns.LOCATION_LATITUDE, address.getLatitude());
			values.put(AddressColumns.LOCATION_LONGITUDE, address.getLongitude());
		} else {
			values.put(AddressColumns.LOCATION_LATITUDE, location.getLatitude());
			values.put(AddressColumns.LOCATION_LONGITUDE, location.getLongitude());
		}
		values.put(AddressColumns.ADDRESS, formatAddress(address));
		values.put(AddressColumns.LANGUAGE, address.getLocale().getLanguage());
		values.put(AddressColumns.LATITUDE, address.getLatitude());
		values.put(AddressColumns.LONGITUDE, address.getLongitude());
		values.put(AddressColumns.ELEVATION, address.getElevation());
		values.put(AddressColumns.TIMESTAMP, System.currentTimeMillis());
		values.put(AddressColumns.FAVORITE, address.isFavorite());

		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (db == null)
				return;
			if (id == 0L) {
				id = db.insert(AddressOpenHelper.TABLE_ADDRESSES, null, values);
				address.setId(id);
			} else {
				String[] whereArgs = { Long.toString(id) };
				db.update(AddressOpenHelper.TABLE_ADDRESSES, values, WHERE_ID, whereArgs);
			}
		} finally {
			if (db != null)
				db.close();
		}
	}

	/** Close database resources. */
	public void close() {
		if (mOpenHelper != null)
			mOpenHelper.close();
	}

	/**
	 * Find the nearest country to the latitude and longitude.
	 * <p>
	 * Uses the pre-compiled array of countries from GeoNames.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses with at most 1 entry.
	 */
	private List<Address> findNearestCountry(Location location) {
		List<Address> countries = null;
		Address country = mCountries.findCountry(location);
		if (country != null) {
			countries = new ArrayList<Address>();
			countries.add(country);
		}
		return countries;
	}

	/**
	 * Find the nearest city to the latitude and longitude.
	 * <p>
	 * Uses the pre-compiled array of cities from GeoNames.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses with at most 1 entry.
	 */
	private List<Address> findNearestCity(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses = null;
		GeocoderBase geocoder = mCountries;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 10);
		} catch (IOException e) {
			Log.e(TAG, "City: " + e.getLocalizedMessage(), e);
		}
		return addresses;
	}

	/**
	 * Fetch addresses from the database.
	 * 
	 * @return the list of addresses.
	 */
	public List<ZmanimAddress> query() {
		final String language = mLocale.getLanguage();
		final String country = mLocale.getCountry();

		List<ZmanimAddress> addresses = new ArrayList<ZmanimAddress>();
		SQLiteDatabase db = getReadableDatabase();
		if (db == null)
			return addresses;
		Cursor cursor = db.query(AddressOpenHelper.TABLE_ADDRESSES, COLUMNS, null, null, null, null, null);
		if ((cursor == null) || cursor.isClosed()) {
			db.close();
			return addresses;
		}

		try {
			if (cursor.moveToFirst()) {
				long id;
				double addressLatitude;
				double addressLongitude;
				double addressElevation;
				String formatted;
				String locationLanguage;
				Locale locale;
				ZmanimAddress address;
				boolean favorite;

				do {
					locationLanguage = cursor.getString(INDEX_LANGUAGE);
					if ((locationLanguage == null) || locationLanguage.equals(language)) {
						addressLatitude = cursor.getDouble(INDEX_LATITUDE);
						addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
						addressElevation = cursor.getDouble(INDEX_ELEVATION);
						id = cursor.getLong(INDEX_ID);
						formatted = cursor.getString(INDEX_ADDRESS);
						favorite = cursor.getShort(INDEX_FAVORITE) != 0;
						if (locationLanguage == null)
							locale = mLocale;
						else
							locale = new Locale(locationLanguage, country);

						address = new ZmanimAddress(locale);
						address.setFormatted(formatted);
						address.setId(id);
						address.setLatitude(addressLatitude);
						address.setLongitude(addressLongitude);
						address.setElevation(addressElevation);
						address.setFavorite(favorite);
						addresses.add(address);
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException se) {
			Log.e(TAG, "Query addresses: " + se.getLocalizedMessage(), se);
		} finally {
			cursor.close();
			db.close();
		}

		return addresses;
	}

	/**
	 * Find the elevation (altitude).
	 * 
	 * @param location
	 *            the location.
	 * @return the elevation.
	 */
	public double findElevation(Location location) {
		if (location.hasAltitude())
			return location.getAltitude();
		double elevation;
		elevation = findElevationCity(location);
		if (elevation != Double.NaN)
			return elevation;
		elevation = findElevationCities(location);
		if (elevation != Double.NaN)
			return elevation;
		elevation = findElevationGoogle(location);
		if (elevation != Double.NaN)
			return elevation;
		elevation = findElevationGeoNames(location);
		if (elevation != Double.NaN)
			return elevation;
		return 0;
	}

	/**
	 * Find elevation of the nearest city. Cities within a 10km radius are
	 * assumed have the same elevation.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevation - {@code Double#NaN} otherwise.
	 */
	private double findElevationCity(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();

		List<ZmanimAddress> cities = mCountries.getCities();
		ZmanimAddress cityNearest = null;

		float distance;
		float distanceMin = Float.MAX_VALUE;
		float[] distances = new float[1];

		for (ZmanimAddress city : cities) {
			Location.distanceBetween(latitude, longitude, city.getLatitude(), city.getLongitude(), distances);
			distance = distances[0];
			if ((distance <= distanceMin) && (distance <= SAME_CITY)) {
				distanceMin = distances[0];
				cityNearest = city;
			}
		}

		if (cityNearest != null) {
			return cityNearest.getElevation();
		}

		return Double.NaN;
	}

	/**
	 * Find elevation of nearest cities. Calculates the average elevation of
	 * neighbouring cities.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevation - {@code Double#NaN} otherwise.
	 */
	private double findElevationCities(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();

		List<ZmanimAddress> cities = mCountries.getCities();
		int citiesCount = cities.size();

		float distance;
		float[] distanceCity = new float[1];
		double d;
		double distancesSum = 0;
		int n = 0;
		double[] distances = new double[citiesCount];
		double[] elevations = new double[citiesCount];

		for (ZmanimAddress city : cities) {
			Location.distanceBetween(latitude, longitude, city.getLatitude(), city.getLongitude(), distanceCity);
			distance = distanceCity[0];
			if (distance <= SAME_PLATEAU) {
				elevations[n] = city.getElevation();
				d = distance * distance;
				distances[n] = d;
				distancesSum += d;
				n++;
			}
		}

		if (n <= 1)
			return Double.NaN;

		double weight;
		double weightSum = 0;
		for (int i = 0; i < n; i++) {
			weight = (1 - (distances[i] / distancesSum)) * elevations[i];
			weightSum += weight;
		}

		return weightSum / (n - 1);
	}

	/**
	 * Find elevation according to Google Maps.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevation - {@code Double#NaN} otherwise.
	 */
	private double findElevationGoogle(Location location) {
		// TODO implement me!
		return Double.NaN;
	}

	/**
	 * Find elevation according to GeoNames.
	 * 
	 * @param location
	 *            the location.
	 * @return the elevation - {@code Double#NaN} otherwise.
	 */
	private double findElevationGeoNames(Location location) {
		// TODO implement me!
		return Double.NaN;
	}

}
