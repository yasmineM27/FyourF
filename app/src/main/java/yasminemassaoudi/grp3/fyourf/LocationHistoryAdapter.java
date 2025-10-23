package yasminemassaoudi.grp3.fyourf;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.LocationDatabase;

public class LocationHistoryAdapter extends ArrayAdapter<LocationDatabase.LocationEntry> {

    private Context context;
    private List<LocationDatabase.LocationEntry> locations;

    public LocationHistoryAdapter(Context context, List<LocationDatabase.LocationEntry> locations) {
        super(context, 0, locations);
        this.context = context;
        this.locations = locations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        LocationDatabase.LocationEntry entry = locations.get(position);

        TextView title = convertView.findViewById(android.R.id.text1);
        TextView subtitle = convertView.findViewById(android.R.id.text2);

        title.setText("📞 " + entry.phone);
        String coords = String.format("%.6f, %.6f", entry.latitude, entry.longitude);
        String time = LocationUtils.formatTimestamp(entry.timestamp);
        subtitle.setText("📍 " + coords + " 🕒 " + time);

        // Validate coordinates - if they're 0.0,0.0 or 999.0,999.0, show as "No location data"
        if ((entry.latitude == 0.0 && entry.longitude == 0.0) ||
            (entry.latitude == 999.0 && entry.longitude == 999.0)) {
            subtitle.setText("📍 Waiting for location response 🕒 " + time);
            convertView.setAlpha(0.6f); // Make it more transparent
            title.setText("⏳ " + entry.phone + " (pending)");
        } else {
            convertView.setAlpha(1.0f);
            title.setText("📞 " + entry.phone);
        }

        convertView.setOnClickListener(v -> {
            Intent mapIntent = new Intent(context, MapActivity.class);
            mapIntent.putExtra("latitude", entry.latitude);
            mapIntent.putExtra("longitude", entry.longitude);
            mapIntent.putExtra("friendName", entry.phone);
            context.startActivity(mapIntent);
        });

        return convertView;
    }
}
