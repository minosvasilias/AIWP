package com.minosapps.aiwp;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.IOException;

public class AIWPWallpaperManager {

    private Context context;

    private StorageManager storageManager;

    public AIWPWallpaperManager(Context _context) {
        this.context = _context;
        this.storageManager = new StorageManager(_context);
    }

    public void setWallpaper(Bitmap bitmap) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

        try {
            storageManager.saveWallpaper(bitmap);
            Bitmap wallpaperBitmap = resizeAndCropForWallpaper(bitmap);
            wallpaperManager.setBitmap(wallpaperBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception, maybe notify the user that the wallpaper couldn't be set
        }
    }

    private Bitmap resizeAndCropForWallpaper(Bitmap originalBitmap) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int desiredMinWidth = metrics.widthPixels;
        int desiredMinHeight = metrics.heightPixels;

        // Calculate the scale factor
        float scale = Math.max((float) desiredMinWidth / originalBitmap.getWidth(),
                (float) desiredMinHeight / originalBitmap.getHeight());

        // New dimensions for the scaled bitmap
        int scaledWidth = Math.round(originalBitmap.getWidth() * scale);
        int scaledHeight = Math.round(originalBitmap.getHeight() * scale);

        // Scale the original bitmap
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);

        // Calculate crop dimensions to center the image
        int cropX = (scaledWidth - desiredMinWidth) / 2;
        int cropY = (scaledHeight - desiredMinHeight) / 2;

        // Crop and create the final bitmap
        Bitmap finalBitmap = Bitmap.createBitmap(scaledBitmap, cropX, cropY, desiredMinWidth, desiredMinHeight);

        // Check if scaledBitmap and originalBitmap are different and recycle
        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle();
        }

        return finalBitmap;
    }
}
