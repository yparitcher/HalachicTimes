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
package net.sf.times;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Reminders. Receive alarm events, or date-time events, to update reminders.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminder extends BroadcastReceiver {

    private static final String TAG = "ZmanimReminder";

    private SimpleDateFormat dateFormat;

    /** No-argument constructor for broadcast receiver. */
    public ZmanimReminder() {
    }

    @Override
    @SuppressWarnings("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        String nowFormatted = formatDateTime(System.currentTimeMillis());
        Log.i(TAG, "onReceive " + intent + " [" + nowFormatted + "]");

        // Delegate actions to the service.
        Intent service = new Intent(intent);
        service.setClass(context, ZmanimReminderService.class);
        context.startService(service);
    }

    /**
     * Format the date and time with seconds.<br>
     * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS}"
     *
     * @param time
     *         the time to format.
     * @return the formatted time.
     */
    private String formatDateTime(Date time) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        }
        return dateFormat.format(time);
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time
     *         the time to format.
     * @return the formatted time.
     * @see #formatDateTime(Date)
     */
    private String formatDateTime(long time) {
        return formatDateTime(new Date(time));
    }
}
