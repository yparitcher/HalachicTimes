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
package com.github.times.preference;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;

import com.github.preference.RingtonePreference;
import com.github.times.R;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;
import static android.text.TextUtils.isEmpty;
import static com.github.times.compass.preference.CompassPreferences.KEY_COMPASS_BEARING;
import static com.github.times.location.LocationPreferences.KEY_COORDS_FORMAT;
import static com.github.times.preference.ZmanimPreferences.KEY_REMINDER_RINGTONE;
import static com.github.times.preference.ZmanimPreferences.KEY_REMINDER_SETTINGS;
import static com.github.times.preference.ZmanimPreferences.KEY_REMINDER_STREAM;

/**
 * This fragment shows the preferences for the General header.
 */
public class GeneralPreferenceFragment extends AbstractPreferenceFragment {

    private RingtonePreference reminderRingtonePreference;

    @Override
    protected int getPreferencesXml() {
        return R.xml.general_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            Preference pref = findPreference(KEY_REMINDER_SETTINGS);
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final Context context = preference.getContext();
                        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                        startActivity(intent);
                        return true;
                    }
                });
            }
        }
        reminderRingtonePreference = initRingtone(KEY_REMINDER_RINGTONE);
        initList(KEY_REMINDER_STREAM);

        initList(KEY_COORDS_FORMAT);
        initList(KEY_COMPASS_BEARING);
    }

    @Override
    protected boolean onListPreferenceChange(ListPreference preference, Object newValue) {
        boolean result = super.onListPreferenceChange(preference, newValue);

        String key = preference.getKey();
        if (KEY_REMINDER_STREAM.equals(key) && (reminderRingtonePreference != null)) {
            String value = newValue.toString();
            int audioStreamType = isEmpty(value) ? AudioManager.STREAM_ALARM : Integer.parseInt(value);
            int ringType;
            if (audioStreamType == AudioManager.STREAM_NOTIFICATION) {
                ringType = RingtoneManager.TYPE_NOTIFICATION;
            } else {
                ringType = RingtoneManager.TYPE_ALARM;
            }
            reminderRingtonePreference.setRingtoneType(ringType);
        }
        return result;
    }
}
