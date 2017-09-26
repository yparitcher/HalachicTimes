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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import net.sf.times.ClockWidget;
import net.sf.times.ZmanimListWidget;
import net.sf.times.ZmanimWidget;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS;

/**
 * This fragment shows the preferences for a header.
 */
public abstract class AbstractPreferenceFragment extends net.sf.preference.AbstractPreferenceFragment {

    @Override
    protected void notifyPreferenceChanged() {
        super.notifyPreferenceChanged();
        notifyAppWidgets();
    }

    protected void notifyAppWidgets() {
        final Context context = getActivity();
        notifyAppWidgetsUpdate(context, ZmanimWidget.class);
        notifyAppWidgetsUpdate(context, ZmanimListWidget.class);
        notifyAppWidgetsUpdate(context, ClockWidget.class);
    }

    protected void notifyAppWidgetsUpdate(Context context, Class<?> clazz) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, clazz);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
        if ((appWidgetIds == null) || (appWidgetIds.length == 0))
            return;

        Intent intent = new Intent(context, clazz);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        intent.putExtra(EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
}
