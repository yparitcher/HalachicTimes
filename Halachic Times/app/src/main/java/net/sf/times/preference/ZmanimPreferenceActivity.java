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
package net.sf.times.preference;

import android.os.Bundle;

import net.sf.preference.PreferenceActivity;
import net.sf.times.R;

import java.util.List;

import static net.sf.preference.ThemedPreferences.KEY_THEME;
import static net.sf.times.compass.preference.CompassPreferences.KEY_THEME_COMPASS;

/**
 * Application preferences that populate the settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimPreferenceActivity extends PreferenceActivity {

    /**
     * Constructs a new preferences.
     */
    public ZmanimPreferenceActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Zmanim_Settings);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean shouldRestartParentActivityForUi(String key) {
        return KEY_THEME.equals(key) || KEY_THEME_COMPASS.equals(key);
    }
}
