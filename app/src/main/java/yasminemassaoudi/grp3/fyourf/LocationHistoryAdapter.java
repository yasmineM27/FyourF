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

        title.setText("From: " + entry.phone);
        subtitle.setText(LocationUtils.formatTimestamp(entry.timestamp) + " - " +
                String.format("%.4f, %.4f", entry.latitude, entry.longitude));

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
