package com.minosapps.aiwp;

import android.content.Context;
import android.util.Log;

import com.minosapps.aiwp.Settings.Instructions;
import com.minosapps.aiwp.Settings.Style;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIManager {

    private Settings settings;
    private Context context;

    private GeoManager geoManager;

    private int retries = 0;

    public OpenAIManager(Context _context) {
        this.context = _context;
        this.settings = new Settings(context);
        this.geoManager = new GeoManager(context);
    }

    public String assemblePrompt() {
        Instructions instructions = settings.getInstructions();
        Style style = settings.getStyle();


        String sourcePrompt = "";
        if(settings.getUseTime()){
            String time_source = context.getString(R.string.time_source);
            sourcePrompt += String.format(time_source, getTime() + " in " + getMonth());
        }
        if(settings.getUseLocation()){
            String location_source = context.getString(R.string.location_source);
            sourcePrompt += String.format(location_source, geoManager.getCityString());
        }
        if(settings.getUseWeather()){
            String weather_source = context.getString(R.string.weather_source);
            sourcePrompt += String.format(weather_source, geoManager.getWeather());
        }
        sourcePrompt = sourcePrompt.replace(".", ". ");


        String instructionPrompt = "";

        switch (instructions) {
            case CITY:
                instructionPrompt = context.getString(R.string.city_instructions);
                break;
            case NATURE:
                instructionPrompt = context.getString(R.string.nature_instructions);
                break;
            case LANDMARKS:
                instructionPrompt = context.getString(R.string.landmarks_instructions);
                break;
            case PEOPLE:
                instructionPrompt = context.getString(R.string.people_instructions);
                break;
            case ANIMALS:
                instructionPrompt = context.getString(R.string.animals_instructions);
                break;
            case CUSTOM:
                instructionPrompt = settings.getCustomInstructions();
                break;
        }

        String stylePrompt = "";

        switch (style) {
            case WATERCOLOR:
                stylePrompt = context.getString(R.string.watercolor_style);
                break;
            case POINTILLIST:
                stylePrompt = context.getString(R.string.pointilism_style);
                break;
            case ABSTRACT:
                stylePrompt = context.getString(R.string.abstract_style);
                break;
            case PHOTOREALISTIC:
                stylePrompt = context.getString(R.string.photorealistic_style);
                break;
            case IMPRESSIONIST:
                stylePrompt = context.getString(R.string.impressionist_style);
                break;
            case CUBIST:
                stylePrompt = context.getString(R.string.cubist_style);
                break;
            case CUSTOM:
                stylePrompt = settings.getCustomStyle();
                break;
        }

        String promptTemplate = context.getString(R.string.prompt_template);
        String prompt = String.format(promptTemplate, sourcePrompt, instructionPrompt, stylePrompt);
        return prompt;
    }
    public InputStream fetchImage() throws Exception {
        String prompt = assemblePrompt();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS) // Set connection timeout
                .readTimeout(120, TimeUnit.SECONDS)    // Set read timeout
                .writeTimeout(120, TimeUnit.SECONDS)   // Set write timeout
                .build();
        String json = "{"
                + "\"model\": \"dall-e-3\","
                + "\"prompt\":\"" + prompt + "\","
                + "\"n\": 1,"
                + "\"size\": \"1024x1792\","
                + "\"quality\":\"hd\""
                + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(context.getString(R.string.openai_url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.getApiKey())
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                String imageUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
                return fetchImageFromUrl(imageUrl);
            }
            else{
                retries += 1;
                if (retries > 5){
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONObject errObject = jsonObject.getJSONObject("error");
                    String message = errObject.getString("message");
                    throw new Exception(message);
                }
                return fetchImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            retries += 1;
            if (retries > 5){
                throw e;
            }
            return fetchImage();
        }
    }

    private InputStream fetchImageFromUrl(String imageUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(imageUrl).build();
        try {
            Response response = client.newCall(request).execute();
            return response.isSuccessful() ? response.body().byteStream() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTime() {
        Calendar now = Calendar.getInstance();
        if(now.get(Calendar.AM_PM) == Calendar.AM){
            return now.get(Calendar.HOUR) < 8 ? "early morning" : "late morning";
        }else{
            if(now.get(Calendar.HOUR) < 3){
                return "early afternoon";
            }
            if(now.get(Calendar.HOUR) < 6){
                return "late afternoon";
            }
            if(now.get(Calendar.HOUR) < 9){
                return "early evening";
            }
            else{
                return "late evening";
            }
        }
    }

    private String getMonth(){
        Calendar now = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        return month_date.format(now.getTime());
    }
}
