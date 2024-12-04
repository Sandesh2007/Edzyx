package com.sandesh.note_app;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatDelegate;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;

public class theme_switcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the saved theme before setting the content view
        ThemeUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_theme_switcher);

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.theme_switcher), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get references to the RadioGroup and RadioButtons
        RadioGroup themeRadioGroup = findViewById(R.id.theme_radio_group);
        RadioButton radioLight = findViewById(R.id.radio_light);
        RadioButton radioDark = findViewById(R.id.radio_dark);
        RadioButton radioSystem = findViewById(R.id.radio_system);
        Button colorPickerButton = findViewById(R.id.color_picker_button);

        colorPickerButton.setOnClickListener(v -> showColorPickerDialog());

        // Retrieve saved theme preference
        SharedPreferences sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        int savedTheme = sharedPreferences.getInt("selected_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Set the appropriate radio button based on the saved theme
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            radioLight.setChecked(true);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            radioDark.setChecked(true);
        } else {
            radioSystem.setChecked(true);
        }

        // Listen for changes in the radio button selection
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            // Change the theme based on the selected radio button
            if (checkedId == R.id.radio_light) {
                selectedTheme = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radio_dark) {
                selectedTheme = AppCompatDelegate.MODE_NIGHT_YES;
            }

            // Save the selected theme in SharedPreferences and apply it
            ThemeUtils.saveThemePreference(theme_switcher.this, selectedTheme);
            AppCompatDelegate.setDefaultNightMode(selectedTheme);
        });
    }

    private void showColorPickerDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_color_picker, null);

        ColorPickerView colorPickerView = dialogView.findViewById(R.id.colorPickerView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Color for all buttons [Beta feature]");
        builder.setView(dialogView);

        builder.setPositiveButton("Select", (dialog, which) -> {
            // Select button
            ColorEnvelope envelope = colorPickerView.getColorEnvelope();
            int color = envelope.getColor();
            findViewById(R.id.color_picker_button).setBackgroundColor(color);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // dismiss the dialog
            dialog.dismiss();
        });

        builder.setNeutralButton("Reset", (dialog, which) -> {
            // set to the default color
            findViewById(R.id.color_picker_button).setBackgroundColor(getResources().getColor(R.color.button_clr));
        });

        // show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
