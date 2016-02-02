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
package net.sf.times.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;

import net.sf.media.RingtoneManager;
import net.sf.preference.TimePreference;
import net.sf.times.compass.R;

import java.util.Calendar;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimSettings {

    /** Preference name for the latitude. */
    private static final String KEY_LATITUDE = "latitude";
    /** Preference name for the longitude. */
    private static final String KEY_LONGITUDE = "longitude";
    /** Preference key for the elevation / altitude. */
    private static final String KEY_ELEVATION = "altitude";
    /** Preference name for the location provider. */
    private static final String KEY_PROVIDER = "provider";
    /** Preference name for the location time. */
    private static final String KEY_TIME = "time";
    /** Preference name for the co-ordinates visibility. */
    public static final String KEY_COORDS = "coords.visible";
    /** Preference name for the co-ordinates format. */
    public static final String KEY_COORDS_FORMAT = "coords.format";
    /** Preference name for showing seconds. */
    public static final String KEY_SECONDS = "seconds.visible";
    /** Preference name for showing summaries. */
    public static final String KEY_SUMMARIES = "summaries.visible";
    /** Preference name for enabling past times. */
    public static final String KEY_PAST = "past";
    /**
     * Preference name for the background gradient.
     *
     * @deprecated use #KEY_THEME
     */
    @Deprecated
    public static final String KEY_BG_GRADIENT = "gradient";
    /** Preference name for the theme. */
    public static final String KEY_THEME = "theme";
    /** Preference name for the last reminder. */
    private static final String KEY_REMINDER_LATEST = "reminder";
    /** Preference name for the reminder audio stream type. */
    public static final String KEY_REMINDER_STREAM = "reminder.stream";
    /** Preference name for the reminder ringtone. */
    public static final String KEY_REMINDER_RINGTONE = "reminder.ringtone";
    /** Preference name for the temporal hour visibility. */
    public static final String KEY_HOUR = "hour.visible";

    /** Preference name for temporal hour type. */
    public static final String KEY_OPINION_HOUR = "hour";
    /** Preference name for Alos type. */
    public static final String KEY_OPINION_DAWN = "dawn";
    /** Preference name for earliest tallis type. */
    public static final String KEY_OPINION_TALLIS = "tallis";
    /** Preference name for sunrise type. */
    public static final String KEY_OPINION_SUNRISE = "sunrise";
    /** Preference name for Last Shema type. */
    public static final String KEY_OPINION_SHEMA = "shema";
    /** Preference name for Last Morning Tfila type. */
    public static final String KEY_OPINION_TFILA = "prayers";
    /** Preference name for Last Biur Chametz type. */
    public static final String KEY_OPINION_BURN = "burn_chametz";
    /** Preference name for midday / noon type. */
    public static final String KEY_OPINION_NOON = "midday";
    /** Preference name for Earliest Mincha type. */
    public static final String KEY_OPINION_EARLIEST_MINCHA = "earliest_mincha";
    /** Preference name for Mincha Ketana type. */
    public static final String KEY_OPINION_MINCHA = "mincha";
    /** Preference name for Plug HaMincha type. */
    public static final String KEY_OPINION_PLUG_MINCHA = "plug_hamincha";
    /** Preference name for candle lighting minutes offset. */
    public static final String KEY_OPINION_CANDLES = "candles";
    /** Preference name for Chanukka candle lighting. */
    public static final String KEY_OPINION_CANDLES_CHANUKKA = "candles_chanukka";
    /** Preference name for sunset type. */
    public static final String KEY_OPINION_SUNSET = "sunset";
    /** Preference name for twilight type. */
    public static final String KEY_OPINION_TWILIGHT = "twilight";
    /** Preference name for nightfall type. */
    public static final String KEY_OPINION_NIGHTFALL = "nightfall";
    /** Preference name for Shabbath ends after nightfall. */
    public static final String KEY_OPINION_SHABBATH_ENDS = "shabbath_ends";
    public static final String KEY_OPINION_SHABBATH_ENDS_MINUTES = KEY_OPINION_SHABBATH_ENDS + ".minutes";
    /** Preference name for midnight type. */
    public static final String KEY_OPINION_MIDNIGHT = "midnight";
    /** Preference name for earliest kiddush levana type. */
    public static final String KEY_OPINION_EARLIEST_LEVANA = "levana_earliest";
    /** Preference name for latest kiddush levana type. */
    public static final String KEY_OPINION_LATEST_LEVANA = "levana_latest";
    /** Preference name for omer count suffix. */
    public static final String KEY_OPINION_OMER = "omer";

    static final String REMINDER_SUFFIX = ".reminder";
    static final String REMINDER_SUNDAY_SUFFIX = ".day." + Calendar.SUNDAY;
    static final String REMINDER_MONDAY_SUFFIX = ".day." + Calendar.MONDAY;
    static final String REMINDER_TUESDAY_SUFFIX = ".day." + Calendar.TUESDAY;
    static final String REMINDER_WEDNESDAY_SUFFIX = ".day." + Calendar.WEDNESDAY;
    static final String REMINDER_THURSDAY_SUFFIX = ".day." + Calendar.THURSDAY;
    static final String REMINDER_FRIDAY_SUFFIX = ".day." + Calendar.FRIDAY;
    static final String REMINDER_SATURDAY_SUFFIX = ".day." + Calendar.SATURDAY;

    private static final String EMPHASIS_SUFFIX = ".emphasis";
    private static final String ANIM_SUFFIX = ".anim";

    /** Preference name for Alos reminder. */
    public static final String KEY_REMINDER_DAWN = KEY_OPINION_DAWN + REMINDER_SUFFIX;
    /** Preference name for earliest tallis reminder. */
    public static final String KEY_REMINDER_TALLIS = KEY_OPINION_TALLIS + REMINDER_SUFFIX;
    /** Preference name for sunrise reminder. */
    public static final String KEY_REMINDER_SUNRISE = KEY_OPINION_SUNRISE + REMINDER_SUFFIX;
    /** Preference name for Last Shema reminder. */
    public static final String KEY_REMINDER_SHEMA = KEY_OPINION_SHEMA + REMINDER_SUFFIX;
    /** Preference name for Last Morning Tfila reminder. */
    public static final String KEY_REMINDER_TFILA = KEY_OPINION_TFILA + REMINDER_SUFFIX;
    /** Preference name for midday / noon reminder. */
    public static final String KEY_REMINDER_NOON = KEY_OPINION_NOON + REMINDER_SUFFIX;
    /** Preference name for Earliest Mincha reminder. */
    public static final String KEY_REMINDER_EARLIEST_MINCHA = KEY_OPINION_EARLIEST_MINCHA + REMINDER_SUFFIX;
    /** Preference name for Mincha Ketana reminder. */
    public static final String KEY_REMINDER_MINCHA = KEY_OPINION_MINCHA + REMINDER_SUFFIX;
    /** Preference name for Plug HaMincha reminder. */
    public static final String KEY_REMINDER_PLUG_MINCHA = KEY_OPINION_PLUG_MINCHA + REMINDER_SUFFIX;
    /** Preference name for candle lighting reminder. */
    public static final String KEY_REMINDER_CANDLES = KEY_OPINION_CANDLES + REMINDER_SUFFIX;
    /** Preference name for sunset reminder. */
    public static final String KEY_REMINDER_SUNSET = KEY_OPINION_SUNSET + REMINDER_SUFFIX;
    /** Preference name for twilight reminder. */
    public static final String KEY_REMINDER_TWILIGHT = KEY_OPINION_TWILIGHT + REMINDER_SUFFIX;
    /** Preference name for nightfall reminder. */
    public static final String KEY_REMINDER_NIGHTFALL = KEY_OPINION_NIGHTFALL + REMINDER_SUFFIX;
    /** Preference name for midnight reminder. */
    public static final String KEY_REMINDER_MIDNIGHT = KEY_OPINION_MIDNIGHT + REMINDER_SUFFIX;
    /** Preference name for earliest kiddush levana reminder. */
    public static final String KEY_REMINDER_EARLIEST_LEVANA = KEY_OPINION_EARLIEST_LEVANA + REMINDER_SUFFIX;
    /** Preference name for latest kiddush levana reminder. */
    public static final String KEY_REMINDER_LATEST_LEVANA = KEY_OPINION_LATEST_LEVANA + REMINDER_SUFFIX;

    /** Format the coordinates in decimal notation. */
    public static String FORMAT_DECIMAL;
    /** Format the coordinates in sexagesimal notation. */
    public static String FORMAT_SEXIGESIMAL;

    /** Show zmanim list without background. */
    public static String LIST_THEME_NONE;
    /** Show zmanim list with dark gradient background. */
    public static String LIST_THEME_DARK;
    /** Show zmanim list with light gradient background. */
    public static String LIST_THEME_LIGHT;

    /** No omer count. */
    public static String OMER_NONE;
    /** Omer count has "BaOmer" suffix. */
    public static String OMER_B;
    /** Omer count has "LaOmer" suffix. */
    public static String OMER_L;

    /** Unknown date. */
    public static final long NEVER = Long.MIN_VALUE;

    private Context context;
    private final SharedPreferences preferences;

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public ZmanimSettings(Context context) {
        Context app = context.getApplicationContext();
        if (app != null)
            context = app;
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get the data.
     *
     * @return the shared preferences.
     */
    public SharedPreferences getData() {
        return preferences;
    }

    /**
     * Get the editor to modify the preferences data.
     *
     * @return the editor.
     */
    public Editor edit() {
        return preferences.edit();
    }

    /**
     * Get the location.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocation() {
        if (!preferences.contains(KEY_LATITUDE))
            return null;
        if (!preferences.contains(KEY_LONGITUDE))
            return null;
        double latitude;
        double longitude;
        double elevation;
        try {
            latitude = Double.parseDouble(preferences.getString(KEY_LATITUDE, "0"));
            longitude = Double.parseDouble(preferences.getString(KEY_LONGITUDE, "0"));
            elevation = Double.parseDouble(preferences.getString(KEY_ELEVATION, "0"));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return null;
        }
        String provider = preferences.getString(KEY_PROVIDER, "");
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(elevation);
        location.setTime(preferences.getLong(KEY_TIME, 0L));
        return location;
    }

    /**
     * Set the location.
     *
     * @return the location.
     */
    public void putLocation(Location location) {
        Editor editor = preferences.edit();
        editor.putString(KEY_PROVIDER, location.getProvider());
        editor.putString(KEY_LATITUDE, Double.toString(location.getLatitude()));
        editor.putString(KEY_LONGITUDE, Double.toString(location.getLongitude()));
        editor.putString(KEY_ELEVATION, Double.toString(location.hasAltitude() ? location.getAltitude() : 0));
        editor.putLong(KEY_TIME, location.getTime());
        editor.commit();
    }

    /**
     * Are coordinates visible?
     *
     * @return {@code true} to show coordinates.
     */
    public boolean isCoordinates() {
        return preferences.getBoolean(KEY_COORDS, context.getResources().getBoolean(R.bool.coords_visible_defaultValue));
    }

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    public String getCoordinatesFormat() {
        return preferences.getString(KEY_COORDS_FORMAT, context.getString(R.string.coords_format_defaultValue));
    }

    /**
     * Are summaries visible?
     *
     * @return {@code true} to show summaries.
     */
    public boolean isSummaries() {
        return preferences.getBoolean(KEY_SUMMARIES, context.getResources().getBoolean(R.bool.summaries_visible_defaultValue));
    }

    /**
     * Get the application theme.
     *
     * @return the theme resource id.
     */
    public int getTheme() {
        String value = preferences.getString(KEY_THEME, context.getString(R.string.theme_defaultValue));
        if (TextUtils.isEmpty(value) || LIST_THEME_NONE.equals(value) || !preferences.getBoolean(KEY_BG_GRADIENT, true)) {
            return R.style.Theme_Zmanim_NoGradient;
        }
        if (LIST_THEME_LIGHT.equals(value)) {
            return R.style.Theme_Zmanim_Light;
        }
        return R.style.Theme_Zmanim_Dark;
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        FORMAT_DECIMAL = context.getString(R.string.coords_format_value_decimal);
        FORMAT_SEXIGESIMAL = context.getString(R.string.coords_format_value_sexagesimal);

        LIST_THEME_NONE = context.getString(R.string.theme_value_none);
        LIST_THEME_DARK = context.getString(R.string.theme_value_dark);
        LIST_THEME_LIGHT = context.getString(R.string.theme_value_light);
    }
}
