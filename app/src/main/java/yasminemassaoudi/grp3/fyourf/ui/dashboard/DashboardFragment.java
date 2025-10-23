package yasminemassaoudi.grp3.fyourf.ui.dashboard;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;
import yasminemassaoudi.grp3.fyourf.LocationDatabase;
import yasminemassaoudi.grp3.fyourf.LocationHistoryAdapter;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";
    private static final String SMS_SENT = "SMS_SENT";
    private static final String SMS_DELIVERED = "SMS_DELIVERED";
    
    private EditText phoneNumberInput;
    private Button sendRequestBtn;
    private Button requestAllBtn;
    private Button clearHistoryBtn;
    private ListView recentContactsList;
    private LocationDatabase locationDatabase;
    private LocationHistoryAdapter adapter;
    
    private BroadcastReceiver sentReceiver;
    private BroadcastReceiver deliveredReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        phoneNumberInput = view.findViewById(R.id.phoneNumberInput);
        sendRequestBtn = view.findViewById(R.id.sendRequestBtn);
        requestAllBtn = view.findViewById(R.id.requestAllBtn);
        clearHistoryBtn = view.findViewById(R.id.clearHistoryBtn);
        recentContactsList = view.findViewById(R.id.recentContactsList);

        locationDatabase = new LocationDatabase(getContext());
        
        setupSmsReceivers();
        loadRecentContacts();

        sendRequestBtn.setOnClickListener(v -> sendLocationRequest());
        requestAllBtn.setOnClickListener(v -> requestAllLocations());
        clearHistoryBtn.setOnClickListener(v -> clearHistory());

        return view;
    }
    
    private void setupSmsReceivers() {
        // SMS sent receiver
        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case android.app.Activity.RESULT_OK:
                        Log.d(TAG, "SMS sent successfully");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "SMS failed to send", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No SMS service available", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "SMS error", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio is off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        
        // SMS delivered receiver
        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case android.app.Activity.RESULT_OK:
                        Toast.makeText(context, "Location request delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case android.app.Activity.RESULT_CANCELED:
                        Toast.makeText(context, "Location request not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        
        // Register receivers
        Context context = getContext();
        if (context != null) {
            ContextCompat.registerReceiver(
                    context,
                    sentReceiver,
                    new IntentFilter(SMS_SENT),
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );
            ContextCompat.registerReceiver(
                    context,
                    deliveredReceiver,
                    new IntentFilter(SMS_DELIVERED),
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );
        }
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
        
        // Validate phone number format
        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(getContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check SMS permission
        if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "SMS permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        sendSmsWithConfirmation(phoneNumber, "find friends");
        phoneNumberInput.setText("");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Basic validation - should contain only digits, +, -, spaces, parentheses
        return phoneNumber.matches("^[+\\-\\s()\\d]+$") && phoneNumber.replaceAll("[^\\d]", "").length() >= 8;
    }

    private void sendSmsWithConfirmation(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            Intent sentIntent = new Intent(SMS_SENT);
            PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Intent deliveredIntent = new Intent(SMS_DELIVERED);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(getContext(), 0, deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
            Toast.makeText(getContext(), "Location request sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SMS sent to: " + phoneNumber + " with message: " + message);

            // Add the phone number to recent contacts for testing
            if (locationDatabase != null) {
                // Add a dummy location entry to show in recent contacts
                // Use a placeholder that indicates "waiting for location"
                locationDatabase.addLocation(phoneNumber, 999.0, 999.0);
                Log.d(TAG, "Added " + phoneNumber + " to recent contacts (waiting for location)");
                loadRecentContacts(); // Refresh the list
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestAllLocations() {
        List<LocationDatabase.LocationEntry> locations = locationDatabase.getAllLocations();
        if (locations.isEmpty()) {
            Toast.makeText(getContext(), "No contacts in history", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check SMS permission
        if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "SMS permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "find friends";
        int successCount = 0;
        int failCount = 0;

        for (LocationDatabase.LocationEntry entry : locations) {
            try {
                sendSmsWithConfirmation(entry.phoneNumber, message);
                successCount++;
                // Add small delay between SMS sends
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SMS to " + entry.phoneNumber + ": " + e.getMessage());
                failCount++;
            }
        }

        String resultMessage = "Requests sent to " + successCount + " contacts";
        if (failCount > 0) {
            resultMessage += " (" + failCount + " failed)";
        }
        Toast.makeText(getContext(), resultMessage, Toast.LENGTH_LONG).show();
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
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister receivers to prevent memory leaks
        if (getContext() != null && sentReceiver != null) {
            try {
                getContext().unregisterReceiver(sentReceiver);
                getContext().unregisterReceiver(deliveredReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered: " + e.getMessage());
            }
        }
    }
}
