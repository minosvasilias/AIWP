package com.minosapps.aiwp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StorageManager {

    private Context context;
    private static final String WALLPAPER_DIRECTORY_NAME = "wallpapers";
    private static final String SHARED_PREFS_NAME = "com.minosapps.aiwp.sharedprefs";
    private static final String LAST_SAVED_IMAGE_URI_KEY = "lastSavedImageUri";

    public StorageManager(Context context) {
        this.context = context;
    }
    public File saveWallpaper(Bitmap wallpaper) throws IOException {
        // Format the current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "wallpaper_" + formatter.format(new Date()) + ".png";

        // Rest of the method remains the same
        return saveWallpaperToPublicGallery(wallpaper, fileName);
    }
    public File saveWallpaperToPublicGallery(Bitmap wallpaper, String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Wallpapers");

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        saveUriToSharedPreferences(uri.toString());

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            wallpaper.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        // No need to notify media scanner for MediaStore API
        return new File(uri.getPath());
    }

    private void saveUriToSharedPreferences(String uriString) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_SAVED_IMAGE_URI_KEY, uriString);
        editor.apply();
    }

    private String getUriStringFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LAST_SAVED_IMAGE_URI_KEY, null);
    }

    public Bitmap getLastStoredImage() throws IOException {
        String uriString = getUriStringFromSharedPreferences();
        if (uriString == null) {
            return null;
        }
        Uri uri = Uri.parse(uriString);
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            Log.e("ImageLoader", "File not found for URI: " + uriString, e);
            return null;
        } catch (IOException e) {
            Log.e("ImageLoader", "IOException when trying to open URI: " + uriString, e);
            return null;
        } catch (SecurityException e) {
            Log.e("ImageLoader", "Security Exception: Insufficient permissions for URI: " + uriString, e);
            return null;
        }
    }

}
