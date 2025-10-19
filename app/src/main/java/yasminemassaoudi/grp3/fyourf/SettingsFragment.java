package yasminemassaoudi.grp3.fyourf;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import yasminemassaoudi.grp3.fyourf.R;

public class SettingsFragment extends Fragment {

    private CheckBox enableNotificationsCheckbox;
    private CheckBox enableSoundCheckbox;
    private CheckBox enableVibrationCheckbox;
    private CheckBox autoShareLocationCheckbox;
    private CheckBox highAccuracyCheckbox;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        enableNotificationsCheckbox = view.findViewById(R.id.enableNotificationsCheckbox);
        enableSoundCheckbox = view.findViewById(R.id.enableSoundCheckbox);
        enableVibrationCheckbox = view.findViewById(R.id.enableVibrationCheckbox);
        autoShareLocationCheckbox = view.findViewById(R.id.autoShareLocationCheckbox);
        highAccuracyCheckbox = view.findViewById(R.id.highAccuracyCheckbox);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        loadSettings();

        enableNotificationsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(getContext(), "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        enableSoundCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("sound_enabled", isChecked).apply();
            Toast.makeText(getContext(), "Sound " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        enableVibrationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("vibration_enabled", isChecked).apply();
            Toast.makeText(getContext(), "Vibration " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        autoShareLocationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("auto_share_location", isChecked).apply();
            Toast.makeText(getContext(), "Auto-share " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        highAccuracyCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("high_accuracy", isChecked).apply();
            Toast.makeText(getContext(), "High accuracy " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void loadSettings() {
        enableNotificationsCheckbox.setChecked(sharedPreferences.getBoolean("notifications_enabled", true));
        enableSoundCheckbox.setChecked(sharedPreferences.getBoolean("sound_enabled", true));
        enableVibrationCheckbox.setChecked(sharedPreferences.getBoolean("vibration_enabled", true));
        autoShareLocationCheckbox.setChecked(sharedPreferences.getBoolean("auto_share_location", true));
        highAccuracyCheckbox.setChecked(sharedPreferences.getBoolean("high_accuracy", false));
    }
}
