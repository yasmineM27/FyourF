package yasminemassaoudi.grp3.fyourf;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;

public class NotificationAdapter extends ArrayAdapter<NotificationDatabase.NotificationEntry> {

    private Context context;
    private List<NotificationDatabase.NotificationEntry> notifications;

    public NotificationAdapter(Context context, List<NotificationDatabase.NotificationEntry> notifications) {
        super(context, 0, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        }

        NotificationDatabase.NotificationEntry notification = notifications.get(position);

        TextView phoneText = convertView.findViewById(R.id.notificationPhone);
        TextView coordsText = convertView.findViewById(R.id.notificationCoords);
        TextView timeText = convertView.findViewById(R.id.notificationTime);
        Button viewMapBtn = convertView.findViewById(R.id.viewMapBtn);

        phoneText.setText("From: " + notification.phoneNumber);
        coordsText.setText(String.format("%.4f, %.4f", notification.latitude, notification.longitude));
        timeText.setText(notification.timestamp);

        if (notification.isRead) {
            convertView.setAlpha(0.6f);
        } else {
            convertView.setAlpha(1.0f);
        }

        viewMapBtn.setOnClickListener(v -> {
            Intent mapIntent = new Intent(context, MapActivity.class);
            mapIntent.putExtra("latitude", notification.latitude);
            mapIntent.putExtra("longitude", notification.longitude);
            mapIntent.putExtra("friendName", notification.phoneNumber);
            context.startActivity(mapIntent);
        });

        return convertView;
    }
}
