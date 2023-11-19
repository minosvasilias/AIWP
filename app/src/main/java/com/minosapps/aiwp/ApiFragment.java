package com.minosapps.aiwp;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.minosapps.aiwp.databinding.TabApiBinding;

public class ApiFragment extends Fragment {

    private @NonNull TabApiBinding binding;
    private Settings settings;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        settings = ((MainActivity)getContext()).settings;
        binding = TabApiBinding.inflate(getLayoutInflater());


        binding.edittextApiKey.setText(settings.getApiKey());
        UITools.setupEditText(binding.edittextApiKey, text -> {
            settings.setApiKey(text);
            updateApiState();
        });

        updateApiState();

        binding.apiKeySubInfo.setMovementMethod(LinkMovementMethod.getInstance());



        return binding.getRoot();
    }

    private void updateApiState(){
        binding.apiCheckbox.setChecked(isApiKeyValid(settings));
    }

    static boolean isApiKeyValid(Settings settings){
        String apiKey = settings.getApiKey();
        boolean isValid = apiKey.length() > 40 && apiKey.substring(0,2).equals("sk");
        return isValid;
    }
}
