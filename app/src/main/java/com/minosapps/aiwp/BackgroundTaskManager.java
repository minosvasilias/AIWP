package com.minosapps.aiwp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class BackgroundTaskManager {

    private static final String SHARED_PREFS_NAME = "AlarmManagerPrefs";
    private static final String ACTIVE_REQUEST_CODES_KEY = "activeRequestCodes";

    private Context context;
    private Settings settings;

    private SharedPreferences sharedPreferences;

    public BackgroundTaskManager(Context _context) {
        this.context = _context;
        this.settings = new Settings(context);
        this.sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE);
    }

    private void setAlarmForTime(int hourOfDay, int minute, int requestCode) {
        Log.d("AIWP", "Setting wallpaper-changing alarm for: " + hourOfDay + ":" + minute);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AIWPWallpaperChangeReceiver.class);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to start at a specific time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Repeat the alarm every day at the specified time
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
                alarmIntent);
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        //        alarmIntent);
        addActiveRequestCode(requestCode);
    }

    public void scheduleWallpaperChange() {
        Settings.Frequency currentFrequency = settings.getFrequency();
        // Check if the current schedule already matches the desired settings
        if (isWorkScheduled(currentFrequency)) {
            return; // Skip scheduling as it's already set with the desired frequency
        }

        clearAlarms();

        if (!settings.getActive()){
            clearCurrentSettings();
            return;
        }

        storeCurrentSettings(currentFrequency);
        switch (currentFrequency) {
            case TWICE_DAILY:
                setAlarmForTime(6, 0, 0);
                setAlarmForTime(18, 0, 1);
                return;
            case THREE_TIMES_DAILY:
                setAlarmForTime(6, 0, 0);
                setAlarmForTime(12, 0, 1);
                setAlarmForTime(18, 0, 2);
                return;
            case FOUR_TIMES_DAILY:
                setAlarmForTime(6, 0, 0);
                setAlarmForTime(11, 0, 1);
                setAlarmForTime(16, 0, 2);
                setAlarmForTime(21, 0, 3);
                return;
            case FIVE_TIMES_DAILY:
                setAlarmForTime(6, 0, 0);
                setAlarmForTime(11, 0, 1);
                setAlarmForTime(15, 0, 2);
                setAlarmForTime(19, 0, 3);
                setAlarmForTime(22, 0, 4);
                return;
            default:
                // Default to daily
                setAlarmForTime(12, 00, 0);
                return;
        }
    }


    private boolean isWorkScheduled(Settings.Frequency frequency) {
        String storedFrequency = sharedPreferences.getString("SCHEDULED_FREQUENCY", null);

        // Compare stored frequency with current frequency
        return frequency.name().equals(storedFrequency);
    }

    private void storeCurrentSettings(Settings.Frequency frequency) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SCHEDULED_FREQUENCY", frequency.name());
        editor.apply();
    }

    private void clearCurrentSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("SCHEDULED_FREQUENCY");
        editor.apply();
    }

    private void addActiveRequestCode(int requestCode) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> activeRequestCodes = prefs.getStringSet(ACTIVE_REQUEST_CODES_KEY, new HashSet<>());
        activeRequestCodes.add(String.valueOf(requestCode));
        prefs.edit().putStringSet(ACTIVE_REQUEST_CODES_KEY, activeRequestCodes).apply();
    }

    private void clearActiveRequestCodes() {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(ACTIVE_REQUEST_CODES_KEY).apply();
    }

    public void clearAlarms() {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> activeRequestCodes = prefs.getStringSet(ACTIVE_REQUEST_CODES_KEY, null);

        if (activeRequestCodes != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AIWPWallpaperChangeReceiver.class);

            for (String requestCodeStr : activeRequestCodes) {
                int requestCode = Integer.parseInt(requestCodeStr);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                if (alarmIntent != null) {
                    alarmManager.cancel(alarmIntent);
                    alarmIntent.cancel();
                }
            }

            clearActiveRequestCodes(); // Clear the stored request codes
        }
    }

}
