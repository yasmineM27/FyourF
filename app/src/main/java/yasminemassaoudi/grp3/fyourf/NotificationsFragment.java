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
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";

    private ListView notificationsList;
    private Button clearNotificationsBtn;
    private Button markAsReadBtn;
    private TextView emptyNotificationsText;
    private NotificationDatabase notificationDatabase;
    private NotificationAdapter adapter;
    private BroadcastReceiver locationUpdateReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationsList = view.findViewById(R.id.notificationsList);
        clearNotificationsBtn = view.findViewById(R.id.clearNotificationsBtn);
        markAsReadBtn = view.findViewById(R.id.markAsReadBtn);
        emptyNotificationsText = view.findViewById(R.id.emptyNotificationsText);

        notificationDatabase = new NotificationDatabase(getContext());

        setupLocationUpdateReceiver();
        loadNotifications();

        clearNotificationsBtn.setOnClickListener(v -> clearNotifications());
        markAsReadBtn.setOnClickListener(v -> markAsRead());

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

                // Refresh the notifications list
                loadNotifications();
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
        loadNotifications();
    }

    private void loadNotifications() {
        List<NotificationDatabase.NotificationEntry> notifications = notificationDatabase.getAllNotifications();

        if (notifications.isEmpty()) {
            notificationsList.setVisibility(View.GONE);
            emptyNotificationsText.setVisibility(View.VISIBLE);
        } else {
            notificationsList.setVisibility(View.VISIBLE);
            emptyNotificationsText.setVisibility(View.GONE);
            adapter = new NotificationAdapter(getContext(), notifications);
            notificationsList.setAdapter(adapter);
        }
    }

    private void clearNotifications() {
        notificationDatabase.deleteAllNotifications();
        loadNotifications();
        Toast.makeText(getContext(), "All notifications cleared", Toast.LENGTH_SHORT).show();
    }

    private void markAsRead() {
        notificationDatabase.markAllAsRead();
        loadNotifications();
        Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
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
