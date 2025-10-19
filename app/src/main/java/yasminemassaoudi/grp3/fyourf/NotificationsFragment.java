package yasminemassaoudi.grp3.fyourf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;

public class NotificationsFragment extends Fragment {

    private ListView notificationsList;
    private Button clearNotificationsBtn;
    private Button markAsReadBtn;
    private TextView emptyNotificationsText;
    private NotificationDatabase notificationDatabase;
    private NotificationAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationsList = view.findViewById(R.id.notificationsList);
        clearNotificationsBtn = view.findViewById(R.id.clearNotificationsBtn);
        markAsReadBtn = view.findViewById(R.id.markAsReadBtn);
        emptyNotificationsText = view.findViewById(R.id.emptyNotificationsText);

        notificationDatabase = new NotificationDatabase(getContext());

        loadNotifications();

        clearNotificationsBtn.setOnClickListener(v -> clearNotifications());
        markAsReadBtn.setOnClickListener(v -> markAsRead());

        return view;
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
}
