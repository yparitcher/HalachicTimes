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

import java.util.Locale;

import android.location.Address;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Address that is stored in the local database.
 * 
 * @author Moshe Waisberg
 */
public class ZmanimAddress extends Address implements Comparable<ZmanimAddress> {

	/**
	 * Key to store the formatted address, instead of formatting it ourselves
	 * elsewhere.
	 */
	public static final String KEY_FORMATTED = "formatted_address";

	/** ISO 639 country code for Israel. */
	public static final String ISO639_ISRAEL = "IL";
	/** ISO 639 country code for Palestine. */
	public static final String ISO639_PALESTINE = "PS";

	/** Address field separator. */
	private static final String ADDRESS_SEPARATOR = ", ";
	/** Double subtraction error. */
	private static final double EPSILON = 1e-6;

	private long mId;
	private double mElevation;
	private boolean mHasElevation;
	private String mFormatted;
	private boolean mFavorite;

	/**
	 * Constructs a new address.
	 * 
	 * @param locale
	 *            the locale.
	 */
	public ZmanimAddress(Locale locale) {
		super(locale);
	}

	/**
	 * Constructs a new address.
	 * 
	 * @param address
	 *            the source address.
	 */
	public ZmanimAddress(Address address) {
		super(address.getLocale());

		int index = 0;
		String line = address.getAddressLine(index);
		while (line != null) {
			setAddressLine(index, line);
			index++;
			line = address.getAddressLine(index);
		}
		setAdminArea(address.getAdminArea());
		setCountryCode(address.getCountryCode());
		setCountryName(address.getCountryName());
		setExtras(address.getExtras());
		setFeatureName(address.getFeatureName());
		setLatitude(address.getLatitude());
		setLocality(address.getLocality());
		setLongitude(address.getLongitude());
		setPhone(address.getPhone());
		setPostalCode(address.getPostalCode());
		setPremises(address.getPremises());
		setSubAdminArea(address.getSubAdminArea());
		setSubLocality(address.getSubLocality());
		setSubThoroughfare(address.getSubThoroughfare());
		setThoroughfare(address.getThoroughfare());
		setUrl(address.getUrl());
	}

	/**
	 * Get the id.
	 * 
	 * @return the id
	 */
	public long getId() {
		return mId;
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 *            the id.
	 */
	public void setId(long id) {
		this.mId = id;
	}

	/**
	 * Get the formatted address.
	 * 
	 * @return the address
	 */
	public String getFormatted() {
		if (mFormatted == null)
			mFormatted = format();
		return mFormatted;
	}

	/**
	 * Set the formatted address.
	 * 
	 * @param formatted
	 *            the address.
	 */
	public void setFormatted(String formatted) {
		mFormatted = formatted;
	}

	/**
	 * Is favourite address?
	 * 
	 * @return {@code true} if favourite.
	 */
	public boolean isFavorite() {
		return mFavorite;
	}

	/**
	 * Mark the address as a favourite.
	 * 
	 * @param favorite
	 *            is favourite?
	 */
	public void setFavorite(boolean favorite) {
		mFavorite = favorite;
	}

	/**
	 * Set the elevation.
	 * 
	 * @param elevation
	 *            the elevation in metres.
	 */
	public void setElevation(double elevation) {
		mElevation = elevation;
		mHasElevation = true;
	}

	/**
	 * Get the elevation.
	 * 
	 * @return the elevation in metres.
	 */
	public double getElevation() {
		if (mHasElevation) {
			return mElevation;
		}
		throw new IllegalStateException();
	}

	/**
	 * Returns true if an elevation has been assigned to this Address, false
	 * otherwise.
	 */
	public boolean hasElevation() {
		return mHasElevation;
	}

	/**
	 * Format the address.
	 * 
	 * @return the formatted address.
	 */
	protected String format() {
		Bundle extras = getExtras();
		String formatted = (extras == null) ? null : extras.getString(KEY_FORMATTED);
		if (formatted != null)
			return formatted;

		StringBuilder buf = new StringBuilder();
		String feature = getFeatureName();
		String street = getThoroughfare();
		String subloc = getSubLocality();
		String locality = getLocality();
		String subadmin = getSubAdminArea();
		String admin = getAdminArea();
		String country = getCountryName();

		if (!TextUtils.isEmpty(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(feature);
		}
		if (!TextUtils.isEmpty(street) && !street.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(street);
		}
		if (!TextUtils.isEmpty(subloc) && !subloc.equals(street) && !subloc.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(subloc);
		}
		if (!TextUtils.isEmpty(locality) && !locality.equals(subloc) && !locality.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(locality);
		}
		if (!TextUtils.isEmpty(subadmin) && !subadmin.equals(locality) && !subadmin.equals(subloc) && !subadmin.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(subadmin);
		}
		if (!TextUtils.isEmpty(admin) && !admin.equals(subadmin) && !admin.equals(locality) && !admin.equals(subloc) && !admin.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(admin);
		}
		if (!TextUtils.isEmpty(country) && !country.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(country);
		}

		if (buf.length() == 0)
			return getLocale().getDisplayCountry();

		return buf.toString();
	}

	@Override
	public int compareTo(ZmanimAddress that) {
		double lat1 = this.getLatitude();
		double lat2 = that.getLatitude();
		double latD = lat1 - lat2;
		if (latD >= EPSILON)
			return 1;
		if (latD <= -EPSILON)
			return -1;

		double lng1 = this.getLongitude();
		double lng2 = that.getLongitude();
		double lngD = lng1 - lng2;
		if (lngD >= EPSILON)
			return 1;
		if (lngD <= -EPSILON)
			return -1;

		// Don't need to compare elevation.

		String format1 = this.getFormatted();
		String format2 = that.getFormatted();
		int c = format1.compareToIgnoreCase(format2);
		if (c != 0)
			return c;

		long id1 = this.getId();
		long id2 = that.getId();
		return (id1 < id2 ? -1 : (id1 == id2 ? 0 : 1));
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		super.writeToParcel(parcel, flags);
		parcel.writeLong(mId);
		parcel.writeDouble(mElevation);
		parcel.writeString(mFormatted);
		parcel.writeInt(mFavorite ? 1 : 0);
	}

	public static final Parcelable.Creator<ZmanimAddress> CREATOR = new Parcelable.Creator<ZmanimAddress>() {
		@Override
		public ZmanimAddress createFromParcel(Parcel source) {
			Address a = Address.CREATOR.createFromParcel(source);
			ZmanimAddress za = new ZmanimAddress(a);
			za.mId = source.readLong();
			za.mElevation = source.readDouble();
			za.mFormatted = source.readString();
			za.mFavorite = source.readInt() == 0 ? false : true;
			return za;
		}

		@Override
		public ZmanimAddress[] newArray(int size) {
			return new ZmanimAddress[size];
		}
	};

	public void setCountryCode(String countryCode) {
		if (ISO639_PALESTINE.equals(countryCode)) {
			countryCode = ISO639_ISRAEL;
			super.setCountryCode(countryCode);
			Locale locale = new Locale("", countryCode);
			setCountryName(locale.getDisplayCountry());
		} else {
			super.setCountryCode(countryCode);
		}
	}

	@Override
	public void setCountryName(String countryName) {
		if (getCountryName() != null)
			return;
		super.setCountryName(countryName);
	}
}