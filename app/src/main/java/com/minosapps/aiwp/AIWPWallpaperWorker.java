package com.minosapps.aiwp;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.InputStream;

public class AIWPWallpaperWorker extends Worker {

    private static final String PREFS_NAME = "AIWPWallpaperWorkerPrefs";
    private static final String LAST_RUN_TIME_KEY = "lastRunTime";
    private static final long TEN_MINUTES_MILLIS = 10 * 60 * 1000;

    public AIWPWallpaperWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        if (!shouldExecuteWork()) {
            return Result.success();
        }

        try {
            OpenAIManager openAIManager = new OpenAIManager(getApplicationContext());
            try {
                InputStream imageStream = openAIManager.fetchImage();
                if (imageStream != null) {
                    AIWPWallpaperManager wallpaperManager = new AIWPWallpaperManager(getApplicationContext());
                    Bitmap wallpaperBitmap = BitmapFactory.decodeStream(imageStream);
                    wallpaperManager.setWallpaper(wallpaperBitmap);

                    saveLastRunTime();
                    return Result.success();
                } else {
                    return Result.failure();
                }
            }
            catch (Exception e){
                return Result.failure();
            }
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private boolean shouldExecuteWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastRunTime = prefs.getLong(LAST_RUN_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastRunTime) > TEN_MINUTES_MILLIS;
    }

    private void saveLastRunTime() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(LAST_RUN_TIME_KEY, System.currentTimeMillis());
        editor.apply();
    }
}
