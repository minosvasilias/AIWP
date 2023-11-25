package com.minosapps.aiwp;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.core.util.Consumer;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.Arrays;

public class UITools {

    public static void populateSpinner(Context context, Spinner spinner, Enum<?>[] values) {
        // Convert enum values to formatted strings
        String[] formattedValues = Arrays.stream(values)
                .map(EnumUtils::toFormattedString)
                .toArray(String[]::new);

        // Use the formatted strings for the ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, formattedValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void populateSlider(Slider slider, Enum<?>[] values) {
        // Set the slider's value range
        slider.setValueFrom(0);
        slider.setValueTo(values.length - 1);

        // Set the step size to 1 to snap to each enum value
        slider.setStepSize(1);

        // Set label formatter to display the formatted string of the enum
        slider.setLabelFormatter(value -> {
            // Convert float value to int to use as an index
            int index = (int) value;

            // Ensure index is within bounds
            if (index < 0 || index >= values.length) {
                return "";
            }

            // Return the formatted string for the enum value
            String formattedValue = EnumUtils.toFormattedString(values[index]);
            return "  " + formattedValue + "  ";
        });
    }

    static void setupSwitch(MaterialSwitch switchMaterial, Consumer<Boolean> onCheckedChangedAction) {
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onCheckedChangedAction.accept(isChecked);
        });
    }

    static void setupSpinner(Spinner spinner, Consumer<Integer> onItemSelectedAction) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemSelectedAction.accept(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle the case where nothing is selected
            }
        });
    }

    static void setupEditText(EditText editText, Consumer<String> afterTextChangedAction) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used here
            }

            @Override
            public void afterTextChanged(Editable s) {
                afterTextChangedAction.accept(s.toString());
            }
        });
    }

    public static void setupSlider(Slider slider, Consumer<Float> onValueChangedAction) {
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                onValueChangedAction.accept(value);
            }
        });
    }
}
