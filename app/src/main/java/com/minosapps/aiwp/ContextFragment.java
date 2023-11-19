package com.minosapps.aiwp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.minosapps.aiwp.databinding.TabContextBinding;

public class ContextFragment extends Fragment {

    private TabContextBinding binding;

    private Settings settings;


    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final String[] PERMISSIONS_REQUIRED = {
            Manifest.permission.SET_WALLPAPER,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
    };

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        settings = ((MainActivity)getContext()).settings;

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            updateLocationState();
        });

        binding = TabContextBinding.inflate(getLayoutInflater());

        UITools.setupSwitch(binding.switchTime, isChecked -> {
            settings.setUseTime(isChecked);
        });
        binding.switchTime.setChecked(settings.getUseTime());
        UITools.setupSwitch(binding.switchLocation, isChecked -> {
            settings.setUseLocation(isChecked);
            if(isChecked){
                requestPermissionsIfNeeded();
            }
        });
        binding.switchLocation.setChecked(settings.getUseLocation());
        UITools.setupSwitch(binding.switchWeather, isChecked -> {
            settings.setUseWeather(isChecked);
            if(isChecked){
                requestPermissionsIfNeeded();
            }
        });
        binding.switchWeather.setChecked(settings.getUseWeather());

        binding.backgroundPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestBackgroundPermissionsIfNeeded();
            }
        });

        updateLocationState();

        return binding.getRoot();
    }

    private void requestPermissionsIfNeeded() {
        if (!hasBackgroundPermission()){
            requestPermissionLauncher.launch(PERMISSIONS_REQUIRED);
        }
    }

    private void requestBackgroundPermissionsIfNeeded(){
        if (!hasBackgroundPermission()) {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
        }
    }

    private boolean hasBasePermissions(){
        return hasPermissions(getContext(), PERMISSIONS_REQUIRED) || hasPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean hasBackgroundPermission(){
        return hasPermissions(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    private void updateLocationState(){
        if(!hasBasePermissions() && !hasBackgroundPermission()){
            binding.switchLocation.setChecked(false);
            settings.setUseLocation(false);
            binding.switchWeather.setChecked(false);
            settings.setUseWeather(false);
            binding.backgroundPermissionSection.setVisibility(View.GONE);
            binding.locationPermissionStatus.setChecked(false);
            binding.locationPermissionStatus.setText(R.string.permission_status_unchecked);
        }
        else if(!hasBackgroundPermission()){
            binding.switchLocation.setChecked(false);
            settings.setUseLocation(false);
            binding.switchWeather.setChecked(false);
            settings.setUseWeather(false);
            binding.backgroundPermissionSection.setVisibility(View.VISIBLE);
            binding.locationPermissionStatus.setChecked(false);
            binding.locationPermissionStatus.setText(R.string.permission_status_unchecked);
        }
        else{
            binding.backgroundPermissionSection.setVisibility(View.GONE);
            binding.locationPermissionStatus.setChecked(true);
            binding.locationPermissionStatus.setText(R.string.permission_status_checked);
        }
    }

    static boolean hasPermissions(Context context, String... permissions) {
        if (context != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public void onResume() {
        super.onResume();
        updateLocationState();
    }
}
