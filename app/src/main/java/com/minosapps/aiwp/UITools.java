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

import com.google.android.material.switchmaterial.SwitchMaterial;

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
    static void setupSwitch(SwitchMaterial switchMaterial, Consumer<Boolean> onCheckedChangedAction) {
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
}
