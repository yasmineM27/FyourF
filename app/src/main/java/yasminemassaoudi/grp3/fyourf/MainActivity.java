package yasminemassaoudi.grp3.fyourf;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import yasminemassaoudi.grp3.fyourf.HistoryFragment;
import yasminemassaoudi.grp3.fyourf.NotificationsFragment;
import yasminemassaoudi.grp3.fyourf.SettingsFragment;
import yasminemassaoudi.grp3.fyourf.ui.dashboard.DashboardFragment;
import yasminemassaoudi.grp3.fyourf.ui.home.HomeFragment;
import yasminemassaoudi.grp3.fyourf.ui.geoquiz.GeoQuizFragment;
import yasminemassaoudi.grp3.fyourf.ui.geoquiz.BadgesFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabTracking;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Bouton FAB pour le tracking
        fabTracking = findViewById(R.id.fabTracking);
        if (fabTracking != null) {
            fabTracking.setOnClickListener(v -> openTrackingActivity());
        }

        requestPermissions();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            android.util.Log.d("MainActivity", "Menu item clicked: " + item.getItemId());

            if (item.getItemId() == R.id.nav_home) {
                android.util.Log.d("MainActivity", "Loading HomeFragment");
                fragment = new HomeFragment();
                showFab(); // Afficher le FAB uniquement sur Home
            } else if (item.getItemId() == R.id.nav_dashboard) {
                android.util.Log.d("MainActivity", "Loading DashboardFragment");
                fragment = new DashboardFragment();
                hideFab();
            } else if (item.getItemId() == R.id.nav_history) {
                android.util.Log.d("MainActivity", "Loading HistoryFragment");
                fragment = new HistoryFragment();
                hideFab();
            } else if (item.getItemId() == R.id.nav_notifications) {
                android.util.Log.d("MainActivity", "Loading NotificationsFragment");
                fragment = new NotificationsFragment();
                hideFab();
            } else if (item.getItemId() == R.id.nav_settings) {
                android.util.Log.d("MainActivity", "Loading SettingsFragment");
                fragment = new SettingsFragment();
                hideFab();
            } else if (item.getItemId() == R.id.nav_geoquiz) {
                android.util.Log.d("MainActivity", "✓ Loading GeoQuizFragment");
                fragment = new GeoQuizFragment();
                hideFab();
            }

            if (fragment != null) {
                android.util.Log.d("MainActivity", "Loading fragment: " + fragment.getClass().getSimpleName());
                loadFragment(fragment);
            }
            return true;
        });

        if (getIntent().getBooleanExtra("navigate_to_notifications", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
            loadFragment(new NotificationsFragment());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_tracking) {
            openTrackingActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openTrackingActivity() {
        Intent intent = new Intent(this, TrackingActivity.class);
        startActivity(intent);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        PERMISSION_REQUEST_CODE);
            }
        }

        // Request background location permission separately (required for Android 10+)
        requestBackgroundLocationPermission();

        // Request battery optimization exemption
        requestBatteryOptimizationExemption();
    }

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Show explanation dialog first
                new AlertDialog.Builder(this)
                        .setTitle("Background Location Permission")
                        .setMessage("This app needs background location access to send your location even when the app is closed or the phone is locked. Please select 'Allow all the time' in the next screen.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    PERMISSION_REQUEST_CODE + 1);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();

            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                new AlertDialog.Builder(this)
                        .setTitle("Battery Optimization")
                        .setMessage("To ensure location sharing works even when the phone is locked, please disable battery optimization for this app.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    public void navigateToFragment(Fragment fragment) {
        loadFragment(fragment);
    }

    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    // Méthodes pour gérer la visibilité du FAB
    private void showFab() {
        if (fabTracking != null) {
            fabTracking.show();
        }
    }

    private void hideFab() {
        if (fabTracking != null) {
            fabTracking.hide();
        }
    }
}
