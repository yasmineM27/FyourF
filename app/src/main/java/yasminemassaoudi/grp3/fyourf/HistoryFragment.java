package yasminemassaoudi.grp3.fyourf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;
import yasminemassaoudi.grp3.fyourf.LocationDatabase;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";

    private ListView historyList;
    private Button sortByDateBtn;
    private Button sortByPhoneBtn;
    private Button exportHistoryBtn;
    private Button refreshBtn;
    private LocationDatabase locationDatabase;
    private LocationHistoryAdapter adapter;
    private List<LocationDatabase.LocationEntry> currentLocations;
    private BroadcastReceiver locationUpdateReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyList = view.findViewById(R.id.historyList);
        sortByDateBtn = view.findViewById(R.id.sortByDateBtn);
        sortByPhoneBtn = view.findViewById(R.id.sortByPhoneBtn);
        exportHistoryBtn = view.findViewById(R.id.exportHistoryBtn);
        refreshBtn = view.findViewById(R.id.refreshBtn);

        locationDatabase = new LocationDatabase(getContext());
        currentLocations = new ArrayList<>();

        setupLocationUpdateReceiver();
        loadLocationHistory();

        sortByDateBtn.setOnClickListener(v -> sortByDate());
        sortByPhoneBtn.setOnClickListener(v -> sortByPhone());
        exportHistoryBtn.setOnClickListener(v -> exportHistory());
        if (refreshBtn != null) {
            refreshBtn.setOnClickListener(v -> refreshFromMySQL());
        }

        return view;
    }

    private void setupLocationUpdateReceiver() {
        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String phoneNumber = intent.getStringExtra("phoneNumber");
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);

                Log.d(TAG, "Location update received for " + phoneNumber + ": " + latitude + ", " + longitude);

                // Recharger depuis la base locale (les données sont déjà sauvegardées par SmsReceiver)
                loadFromLocalDatabase();

                // Synchroniser avec MySQL en arrière-plan si activé
                if (Config.USE_MYSQL) {
                    syncFromMySQL();
                }
            }
        };

        // Register receiver
        Context context = getContext();
        if (context != null) {
            ContextCompat.registerReceiver(
                    context,
                    locationUpdateReceiver,
                    new IntentFilter("LOCATION_UPDATED"),
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLocationHistory();
    }

    private void loadLocationHistory() {
        // Toujours charger depuis la base locale pour éviter les crashs
        // MySQL sera utilisé en arrière-plan pour synchroniser
        loadFromLocalDatabase();

        // Si MySQL est activé, essayer de synchroniser en arrière-plan
        if (Config.USE_MYSQL) {
            syncFromMySQL();
        }
    }

    /**
     * Synchronise depuis MySQL en arrière-plan (sans bloquer l'UI)
     */
    private void syncFromMySQL() {
        Log.d(TAG, "Synchronisation depuis MySQL en arrière-plan...");

        new Loading(new Loading.LoadingCallback() {
            @Override
            public void onLoadingComplete(List<Loading.PositionData> positions) {
                Log.d(TAG, "✓ Synchronisation MySQL réussie: " + positions.size() + " positions");

                // Sauvegarder dans la base locale
                for (Loading.PositionData pos : positions) {
                    if (pos.isValid()) {
                        locationDatabase.addLocation(pos.numero, pos.latitude, pos.longitude);
                    }
                }

                // Recharger depuis la base locale
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadFromLocalDatabase();
                        Toast.makeText(getContext(), "✓ Synchronisé avec MySQL", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onLoadingError(String error) {
                Log.w(TAG, "Synchronisation MySQL échouée: " + error);
                // Pas de toast d'erreur, on utilise déjà les données locales
            }
        }).start();
    }

    /**
     * Charge les positions depuis MySQL
     */
    private void loadFromMySQL() {
        Log.d(TAG, "Chargement depuis MySQL...");

        new Loading(new Loading.LoadingCallback() {
            @Override
            public void onLoadingComplete(List<Loading.PositionData> positions) {
                Log.d(TAG, "✓ Positions chargées depuis MySQL: " + positions.size());

                // Convertir en LocationEntry
                currentLocations.clear();
                for (Loading.PositionData pos : positions) {
                    if (pos.isValid()) {
                        currentLocations.add(pos.toLocationEntry());
                        // Sauvegarder dans la base locale pour cache
                        locationDatabase.addLocation(pos.numero, pos.latitude, pos.longitude);
                    }
                }

                // Mettre à jour l'adapter
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter = new LocationHistoryAdapter(getContext(), currentLocations);
                        historyList.setAdapter(adapter);
                        Toast.makeText(getContext(), "Chargé: " + currentLocations.size() + " positions",
                                      Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onLoadingError(String error) {
                Log.e(TAG, "✗ Erreur chargement MySQL: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur MySQL: " + error, Toast.LENGTH_SHORT).show();
                        // Fallback: charger depuis la base locale
                        loadFromLocalDatabase();
                    });
                }
            }
        }).start();
    }

    /**
     * Charge les positions depuis la base de données locale
     */
    private void loadFromLocalDatabase() {
        Log.d(TAG, "Chargement depuis la base locale...");
        currentLocations = new ArrayList<>(locationDatabase.getAllLocations());
        adapter = new LocationHistoryAdapter(getContext(), currentLocations);
        historyList.setAdapter(adapter);
    }

    /**
     * Rafraîchit les données depuis MySQL
     */
    private void refreshFromMySQL() {
        if (Config.USE_MYSQL) {
            Toast.makeText(getContext(), "Rafraîchissement...", Toast.LENGTH_SHORT).show();
            syncFromMySQL();
        } else {
            Toast.makeText(getContext(), "MySQL non activé", Toast.LENGTH_SHORT).show();
            loadFromLocalDatabase();
        }
    }

    private void sortByDate() {
        Collections.sort(currentLocations, (a, b) -> Math.toIntExact(b.timestamp));
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Sorted by date (newest first)", Toast.LENGTH_SHORT).show();
    }

    private void sortByPhone() {
        Collections.sort(currentLocations, (a, b) -> a.phoneNumber.compareTo(b.phoneNumber));
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Sorted by phone number", Toast.LENGTH_SHORT).show();
    }

    private void exportHistory() {
        StringBuilder export = new StringBuilder("FindMyFriend Location History\n");
        export.append("================================\n\n");

        for (LocationDatabase.LocationEntry entry : currentLocations) {
            export.append("Phone: ").append(entry.phoneNumber).append("\n");
            export.append("Latitude: ").append(entry.latitude).append("\n");
            export.append("Longitude: ").append(entry.longitude).append("\n");
            export.append("Time: ").append(entry.timestamp).append("\n");
            export.append("---\n");
        }

        Toast.makeText(getContext(), "Export data: " + export.toString().substring(0, Math.min(50, export.length())) + "...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister receiver to prevent memory leaks
        if (getContext() != null && locationUpdateReceiver != null) {
            try {
                getContext().unregisterReceiver(locationUpdateReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered: " + e.getMessage());
            }
        }
    }
}
