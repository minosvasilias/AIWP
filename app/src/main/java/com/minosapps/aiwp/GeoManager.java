package com.minosapps.aiwp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class GeoManager {

    private FusedLocationProviderClient locationClient;
    private Context context;

    public GeoManager(Context context) {
        this.context = context;
        this.locationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getLocation(OnSuccessListener<Location> listener) {
        // Ensure proper permissions are handled before calling this
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(listener);
    }

    public Location getLocationSynchronously() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Location> future = executor.submit(new Callable<Location>() {
            @Override
            public Location call() throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                final Location[] result = new Location[1];

                getLocation(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        result[0] = location;
                        boolean isNull = location == null;
                        latch.countDown();
                    }
                });

                latch.await(); // Wait for the latch to be decremented
                return result[0];
            }
        });

        try {
            return future.get();
        } finally {
            executor.shutdown();
        }
    }

    public String getCityString() {
        Location location = null;
        try {
            location = getLocationSynchronously();
            if(location == null){
                return "";
            }
        } catch (ExecutionException e) {
            return "";
        } catch (InterruptedException e) {
            return "";
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String country = address.getCountryName();
                return city + ", " + country;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }


    public String getWeather() {
        String meteoUrl = context.getString(R.string.meteo_url);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Set read timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout
                .build();
        Location location = null;
        try {
            location = getLocationSynchronously();
            if(location == null){
                return "";
            }
        } catch (ExecutionException e) {
            return "";
        } catch (InterruptedException e) {
            return "";
        }
        String url = String.format(meteoUrl, location.getLatitude(), location.getLongitude());

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONObject currentData = jsonObject.getJSONObject("current");
                JSONObject currentUnits = jsonObject.getJSONObject("current_units");
                String daytimeString = currentData.getInt("is_day") == 1 ? "Daytime." : "Nighttime.";

                Double temperature = currentData.getDouble("temperature_2m");
                Double rainAmount = currentData.getDouble("rain");
                Double snowAmount = currentData.getDouble("snowfall");
                Double cloudCover = currentData.getDouble("cloud_cover");

                String cloudString =  getCloudString(cloudCover);
                String temperatureString = getTemperatureString(temperature);
                String rainString = getRainString(rainAmount);

                String weatherString = daytimeString + temperatureString + " " + cloudString + " " + rainString;
                if (currentData.getDouble("snowfall") > 0) {
                    String snowString = getSnowString(snowAmount);
                    weatherString += " " + snowString;
                }
                return weatherString;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getCloudString(Double cloudCover){
        if(cloudCover < 10){
            return "Clear skies.";
        }
        if(cloudCover >= 10){
            return "Clear skies with a few individual clouds.";
        }
        if(cloudCover >= 30){
            return "Largely clear skies with intermittent clouds.";
        }
        if(cloudCover >= 50){
            return "Cloudy with some spots of clear sky.";
        }
        if(cloudCover >= 70){
            return "Fairly cloudy.";
        }
        else{
            return "Overcast.";
        }
    }

    private String getTemperatureString(Double temperature){
        if(temperature < 2){
            return "It is freezing cold.";
        }
        else if(temperature < 5){
            return "It is very cold.";
        }
        else if(temperature < 10){
            return "It is chilly.";
        }
        else if(temperature < 15){
            return "It is somewhat cool outside.";
        }
        else if(temperature < 20){
            return "It is relatively warm.";
        }
        else if(temperature < 25){
            return "It is warm.";
        }
        else if(temperature < 30){
            return "It is hot outside.";
        }
        else{
            return "It is incredibly hot outside.";
        }
    }
    private String getRainString(Double rainAmount){
        if (rainAmount == null) {
            return "No rain data available.";
        } else if (rainAmount <= 0) {
            return "It's not raining.";
        } else if (rainAmount <= 0.5) {
            return "There is very light rain.";
        } else if (rainAmount <= 2.5) {
            return "There is light rain.";
        } else if (rainAmount <= 10) {
            return "There is moderate rain.";
        } else if (rainAmount <= 50) {
            return "There is heavy rainfall.";
        } else if (rainAmount <= 100) {
            return "There is very heavy rainfall.";
        } else {
            return "There is extreme rainfall.";
        }
    }

    private String getSnowString(Double snowAmount){
        if (snowAmount == null) {
            return "No snow data available.";
        } else if (snowAmount <= 0) {
            return "It's not snowing.";
        } else if (snowAmount <= 1) {
            return "There is very light snowfall.";
        } else if (snowAmount <= 5) {
            return "There is light snowfall.";
        } else if (snowAmount <= 15) {
            return "There is moderate snowfall.";
        } else if (snowAmount <= 30) {
            return "There is heavy snowfall.";
        } else if (snowAmount <= 60) {
            return "There is very heavy snowfall.";
        } else {
            return "There is extreme snowfall.";
        }
    }

}

