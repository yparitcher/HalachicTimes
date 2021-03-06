package com.github.times.remind;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.text.style.TypefaceSpan;
import com.github.times.R;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.github.util.LocaleUtils;
import com.github.util.LogUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.github.times.ZmanimItem.NEVER;
import static com.github.util.TimeUtils.roundUp;

/**
 * Shows a reminder alarm for a (<em>zman</em>).
 *
 * @author Moshe Waisberg
 */
public class AlarmActivity<P extends ZmanimPreferences> extends Activity implements
        ThemeCallbacks<P>, View.OnClickListener {

    private static final String TAG = "AlarmActivity";

    /**
     * Extras name for the reminder.
     */
    public static final String EXTRA_REMINDER = "reminder";
    /**
     * Extras name for the reminder id.
     */
    public static final String EXTRA_REMINDER_ID = "reminder_id";
    /**
     * Extras name for the reminder title.
     */
    public static final String EXTRA_REMINDER_TITLE = "reminder_title";
    /**
     * Extras name for the reminder time.
     */
    public static final String EXTRA_REMINDER_TIME = "reminder_time";

    private LocaleCallbacks<P> localeCallbacks;
    private ThemeCallbacks<P> themeCallbacks;
    /**
     * The preferences.
     */
    private P preferences;
    private SimpleDateFormat dateFormat;
    private Format timeFormat;
    private long timeFormatGranularity;
    private MediaPlayer ringtone;

    private TextView timeView;
    private TextView titleView;
    private View dismissView;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate();

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Window win = getWindow();
        // Turn on the screen.
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.alarm_activity);

        timeView = findViewById(R.id.time);
        titleView = findViewById(android.R.id.title);
        dismissView = findViewById(R.id.reminder_dismiss);
        dismissView.setOnClickListener(this);

        final Context context = this;
        final Locale locale = LocaleUtils.getDefaultLocale(context);
        final P prefs = getZmanimPreferences();
        boolean time24 = DateFormat.is24HourFormat(context);

        if (prefs.isSeconds()) {
            String pattern = context.getString(time24 ? R.string.twenty_four_hour_time_format : R.string.twelve_hour_time_format);
            this.timeFormat = new SimpleDateFormat(pattern, locale);
            this.timeFormatGranularity = SECOND_IN_MILLIS;
        } else {
            this.timeFormat = DateFormat.getTimeFormat(context);
            this.timeFormatGranularity = MINUTE_IN_MILLIS;
        }

        handleIntent(getIntent());
    }

    @Override
    public void onCreate() {
        localeCallbacks.onCreate(this);
        getThemeCallbacks().onCreate();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public P getThemePreferences() {
        return getThemeCallbacks().getThemePreferences();
    }

    protected ThemeCallbacks<P> getThemeCallbacks() {
        if (themeCallbacks == null) {
            themeCallbacks = createThemeCallbacks(this);
        }
        return themeCallbacks;
    }

    protected ThemeCallbacks<P> createThemeCallbacks(ContextWrapper context) {
        return new SimpleThemeCallbacks<>(context, getZmanimPreferences());
    }

    public P getZmanimPreferences() {
        if (preferences == null) {
            preferences = (P) new SimpleZmanimPreferences(this);
        }
        return preferences;
    }

    protected void handleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            ZmanimReminderItem item;

            if (extras.containsKey(EXTRA_REMINDER)) {
                item = extras.getParcelable(EXTRA_REMINDER);
            } else {
                int id = extras.getInt(EXTRA_REMINDER_ID);
                CharSequence contentTitle = extras.getCharSequence(EXTRA_REMINDER_TITLE);
                if ((id != 0) && (contentTitle == null)) {
                    contentTitle = getString(id);
                }
                long when = extras.getLong(EXTRA_REMINDER_TIME, NEVER);
                item = new ZmanimReminderItem(id, contentTitle, null, when);
            }

            if ((item != null) && (item.time > 0L)) {
                notifyNow(item);
            } else {
                close();
            }
        }
    }

    /**
     * Notify now.
     *
     * @param item the reminder item.
     */
    public void notifyNow(ZmanimReminderItem item) {
        LogUtils.i(TAG, "remind now [" + item.title + "] for [" + formatDateTime(item.time) + "]");
        if (item.isEmpty()) {
            close();
            return;
        }

        CharSequence timeLabel = timeFormat.format(roundUp(item.time, timeFormatGranularity));
        SpannableStringBuilder spans = SpannableStringBuilder.valueOf(timeLabel);
        int indexMinutes = TextUtils.indexOf(timeLabel, ':');
        if (indexMinutes >= 0) {
            // Regular "sans-serif" is like bold for "sans-serif-thin".
            spans.setSpan(new TypefaceSpan(Typeface.SANS_SERIF), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            int indexSeconds = TextUtils.indexOf(timeLabel, ':', indexMinutes + 1);
            if (indexSeconds > indexMinutes) {
                spans.setSpan(new RelativeSizeSpan(0.5f), indexSeconds, timeLabel.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

        timeView.setText(spans);
        titleView.setText(item.title);

        startNoise();
    }

    /**
     * Format the date and time with seconds.<br>
     * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS}"
     *
     * @param time the time to format.
     * @return the formatted time.
     */
    private String formatDateTime(Date time) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        }
        return dateFormat.format(time);
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time the time to format.
     * @return the formatted time.
     * @see #formatDateTime(Date)
     */
    private String formatDateTime(long time) {
        if (time == NEVER) {
            return "NEVER";
        }
        return formatDateTime(new Date(time));
    }

    @Override
    public void onClick(View view) {
        if (view == dismissView) {
            dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // User must explicitly cancel the reminder.
    }

    /**
     * Dismiss the reminder.
     */
    public void dismiss() {
        stopNoise();
        MediaPlayer ringtone = this.ringtone;
        if (ringtone != null) {
            ringtone.release();
        }
        setResult(RESULT_CANCELED);
        close();
    }

    private void close() {
        finish();
    }

    private void startNoise() {
        Log.v(TAG, "start noise");
        Context context = this;
        playSound(context);
        vibrate(context, true);
    }

    private void stopNoise() {
        Log.v(TAG, "stop noise");
        Context context = this;
        stopSound();
        vibrate(context, false);
    }

    private void playSound(Context context) {
        MediaPlayer ringtone = getRingtone(context);
        Log.v(TAG, "play sound");
        if ((ringtone != null) && !ringtone.isPlaying()) {
            ringtone.start();
        }
    }

    private void stopSound() {
        Log.v(TAG, "stop sound");
        MediaPlayer ringtone = this.ringtone;
        if ((ringtone != null) && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private MediaPlayer getRingtone(Context context) {
        if (ringtone == null) {
            final P prefs = getZmanimPreferences();
            Uri prefRingtone = prefs.getReminderRingtone();
            if (prefRingtone != null) {
                MediaPlayer ringtone = MediaPlayer.create(context, prefRingtone);
                if (ringtone != null) {
                    int audioStreamType = prefs.getReminderStream();

                    ringtone.setLooping(audioStreamType == AudioManager.STREAM_ALARM);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setLegacyStreamType(audioStreamType)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build();
                        ringtone.setAudioAttributes(audioAttributes);
                    } else {
                        ringtone.setAudioStreamType(audioStreamType);
                    }
                }
                this.ringtone = ringtone;
            }
        }
        return ringtone;
    }

    /**
     * Vibrate the device.
     *
     * @param context the context.
     * @param vibrate {@code true} to start vibrating - {@code false} to stop.
     */
    private void vibrate(Context context, boolean vibrate) {
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        if ((vibrator == null) || !vibrator.hasVibrator()) {
            return;
        }
        if (vibrate) {
            vibrator.vibrate(DateUtils.SECOND_IN_MILLIS);
        } else {
            vibrator.cancel();
        }
    }
}
