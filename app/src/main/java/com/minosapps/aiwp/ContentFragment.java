package com.minosapps.aiwp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.minosapps.aiwp.Settings.Frequency;
import com.minosapps.aiwp.Settings.Instructions;
import com.minosapps.aiwp.Settings.Style;
import com.minosapps.aiwp.databinding.TabContentBinding;

public class ContentFragment extends Fragment {

    private TabContentBinding binding;
    private Settings settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        settings = ((MainActivity)getContext()).settings;

        binding = TabContentBinding.inflate(getLayoutInflater());

        UITools.populateSpinner(getContext(), binding.spinnerInstructions, Instructions.values());
        UITools.populateSpinner(getContext(), binding.spinnerStyle, Style.values());
        UITools.populateSpinner(getContext(), binding.spinnerFrequency, Frequency.values());

        UITools.setupSpinner(binding.spinnerInstructions, position -> {
            Instructions selectedInstruction = Instructions.values()[position];
            settings.setInstructions(selectedInstruction);
            binding.edittextInstructions.setVisibility(selectedInstruction == Instructions.CUSTOM ? View.VISIBLE : View.GONE);
        });
        binding.spinnerInstructions.setSelection(settings.getInstructions().ordinal());
        UITools.setupSpinner(binding.spinnerStyle, position -> {
            Style selectedStyle = Style.values()[position];
            settings.setStyle(selectedStyle);
            binding.edittextStyle.setVisibility(selectedStyle == Style.CUSTOM ? View.VISIBLE : View.GONE);
        });
        binding.spinnerStyle.setSelection(settings.getStyle().ordinal());
        UITools.setupSpinner(binding.spinnerFrequency, position -> {
            Frequency selectedFrequency = Frequency.values()[position];
            settings.setFrequency(selectedFrequency);
        });
        binding.spinnerFrequency.setSelection(settings.getFrequency().ordinal());

        binding.edittextInstructions.setText(settings.getCustomInstructions());
        UITools.setupEditText(binding.edittextInstructions, text -> settings.setCustomInstructions(text));

        binding.edittextStyle.setText(settings.getCustomStyle());
        UITools.setupEditText(binding.edittextStyle, text -> settings.setCustomStyle(text));

        return binding.getRoot();
    }
}
