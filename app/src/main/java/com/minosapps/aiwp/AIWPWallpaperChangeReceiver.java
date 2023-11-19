package com.minosapps.aiwp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class AIWPWallpaperChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AIWPWallpaperWorker.class).build();
        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
