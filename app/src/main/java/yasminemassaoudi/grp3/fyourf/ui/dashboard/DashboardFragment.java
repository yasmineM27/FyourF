package yasminemassaoudi.grp3.fyourf.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;
import yasminemassaoudi.grp3.fyourf.LocationDatabase;
import yasminemassaoudi.grp3.fyourf.LocationHistoryAdapter;

public class DashboardFragment extends Fragment {

    private EditText phoneNumberInput;
    private Button sendRequestBtn;
    private Button requestAllBtn;
    private Button clearHistoryBtn;
    private ListView recentContactsList;
    private LocationDatabase locationDatabase;
    private LocationHistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        phoneNumberInput = view.findViewById(R.id.phoneNumberInput);
        sendRequestBtn = view.findViewById(R.id.sendRequestBtn);
        requestAllBtn = view.findViewById(R.id.requestAllBtn);
        clearHistoryBtn = view.findViewById(R.id.clearHistoryBtn);
        recentContactsList = view.findViewById(R.id.recentContactsList);

        locationDatabase = new LocationDatabase(getContext());

        loadRecentContacts();

        sendRequestBtn.setOnClickListener(v -> sendLocationRequest());
        requestAllBtn.setOnClickListener(v -> requestAllLocations());
        clearHistoryBtn.setOnClickListener(v -> clearHistory());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentContacts();
    }

    private void sendLocationRequest() {
        String phoneNumber = phoneNumberInput.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
        String message = "find friends";
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);

        Toast.makeText(getContext(), "Location request sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        phoneNumberInput.setText("");
    }

    private void requestAllLocations() {
        List<LocationDatabase.LocationEntry> locations = locationDatabase.getAllLocations();
        if (locations.isEmpty()) {
            Toast.makeText(getContext(), "No contacts in history", Toast.LENGTH_SHORT).show();
            return;
        }

        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
        String message = "find friends";

        for (LocationDatabase.LocationEntry entry : locations) {
            smsManager.sendTextMessage(entry.phoneNumber, null, message, null, null);
        }

        Toast.makeText(getContext(), "Location requests sent to " + locations.size() + " contacts", Toast.LENGTH_SHORT).show();
    }

    private void clearHistory() {
        locationDatabase.deleteAllLocations();
        loadRecentContacts();
        Toast.makeText(getContext(), "History cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadRecentContacts() {
        List<LocationDatabase.LocationEntry> locations = locationDatabase.getAllLocations();
        adapter = new LocationHistoryAdapter(getContext(), locations);
        recentContactsList.setAdapter(adapter);
    }
}
