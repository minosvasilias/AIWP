package com.minosapps.aiwp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private Context context;
    private SharedPreferences sharedPreferences;
    private List<AIWPCallback> callbacks = new ArrayList<>();

    public Settings(Context _context) {
        context = _context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE);
    }

    public enum Instructions {
        CITY, NATURE, LANDMARKS, PEOPLE, ANIMALS, CUSTOM, RANDOM;
    }

    public enum Style {
        POINTILLIST, WATERCOLOR, ABSTRACT, PHOTOREALISTIC, IMPRESSIONIST, CUBIST, CUSTOM, RANDOM;
    }

    public enum Frequency {
        DAILY, TWICE_DAILY, THREE_TIMES_DAILY, FOUR_TIMES_DAILY, FIVE_TIMES_DAILY;
    }

    public void setUseTime(boolean useTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("USE_TIME", useTime);
        editor.apply();
    }

    public boolean getUseTime() {
        return sharedPreferences.getBoolean("USE_TIME", false);
    }

    public void setUseLocation(boolean useLocation) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("USE_LOCATION", useLocation);
        editor.apply();
    }

    public boolean getUseLocation() {
        return sharedPreferences.getBoolean("USE_LOCATION", false);
    }

    public void setUseWeather(boolean useWeather) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("USE_WEATHER", useWeather);
        editor.apply();
    }

    public boolean getUseWeather() {
        return sharedPreferences.getBoolean("USE_WEATHER", false);
    }


    public void setUseExactTimer(boolean useExact) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (getUseExactTimer() != useExact) {
            editor.putBoolean("USE_EXACT", useExact);
            editor.apply();
            rescheduleWallpaperChange();
        }
    }

    public boolean getUseExactTimer() {
        return sharedPreferences.getBoolean("USE_EXACT", true);
    }

    public void setInstructions(Instructions instructions) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("INSTRUCTIONS", instructions.name());
        editor.apply();
    }

    public Instructions getInstructions() {
        String value = sharedPreferences.getString("INSTRUCTIONS", Instructions.CUSTOM.name());
        return Instructions.valueOf(value);
    }

    public void setStyle(Style style) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("STYLE", style.name());
        editor.apply();
    }

    public Style getStyle() {
        String value = sharedPreferences.getString("STYLE", Style.IMPRESSIONIST.name());
        return Style.valueOf(value);
    }

    public void setFrequency(Frequency frequency) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (getFrequency() != frequency) {
            editor.putString("FREQUENCY", frequency.name());
            editor.apply();
            rescheduleWallpaperChange();
        }
    }

    public Frequency getFrequency() {
        String value = sharedPreferences.getString("FREQUENCY", Frequency.THREE_TIMES_DAILY.name());
        return Frequency.valueOf(value);
    }


    public void setActive(boolean useTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_ACTIVE", useTime);
        editor.apply();
        rescheduleWallpaperChange();
    }

    public boolean getActive() {
        return sharedPreferences.getBoolean("IS_ACTIVE", false);
    }

    public void setApiKey(String apiKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("API_KEY", apiKey);
        editor.apply();
        triggerUpdates();
    }

    public String getApiKey() {
        return sharedPreferences.getString("API_KEY", ""); // Default is empty string
    }


    public void setCustomInstructions(String customInstructions) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CUSTOM_INSTRUCTIONS", customInstructions);
        editor.apply();
    }

    public String getCustomInstructions() {
        return sharedPreferences.getString("CUSTOM_INSTRUCTIONS", context.getString(R.string.default_custom_instructions));
    }

    public void setCustomStyle(String customStyle) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CUSTOM_STYLE", customStyle);
        editor.apply();
    }

    public String getCustomStyle() {
        return sharedPreferences.getString("CUSTOM_STYLE", context.getString(R.string.default_custom_style));
    }

    public void rescheduleWallpaperChange() {
        BackgroundTaskManager taskManager = new BackgroundTaskManager(context);
        taskManager.scheduleWallpaperChange();
    }


    public void addCallback(AIWPCallback callback) {
        callbacks.add(callback);
    }

    public void triggerUpdates() {
        for (AIWPCallback callback : callbacks) {
            callback.onCallback();
        }
    }

}