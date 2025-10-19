package yasminemassaoudi.grp3.fyourf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;
import yasminemassaoudi.grp3.fyourf.LocationDatabase;

public class HistoryFragment extends Fragment {

    private ListView historyList;
    private Button sortByDateBtn;
    private Button sortByPhoneBtn;
    private Button exportHistoryBtn;
    private LocationDatabase locationDatabase;
    private LocationHistoryAdapter adapter;
    private List<LocationDatabase.LocationEntry> currentLocations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyList = view.findViewById(R.id.historyList);
        sortByDateBtn = view.findViewById(R.id.sortByDateBtn);
        sortByPhoneBtn = view.findViewById(R.id.sortByPhoneBtn);
        exportHistoryBtn = view.findViewById(R.id.exportHistoryBtn);

        locationDatabase = new LocationDatabase(getContext());
        currentLocations = new ArrayList<>();

        loadLocationHistory();

        sortByDateBtn.setOnClickListener(v -> sortByDate());
        sortByPhoneBtn.setOnClickListener(v -> sortByPhone());
        exportHistoryBtn.setOnClickListener(v -> exportHistory());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLocationHistory();
    }

    private void loadLocationHistory() {
        currentLocations = new ArrayList<>(locationDatabase.getAllLocations());
        adapter = new LocationHistoryAdapter(getContext(), currentLocations);
        historyList.setAdapter(adapter);
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
}
