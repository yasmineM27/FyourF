package yasminemassaoudi.grp3.fyourf;


import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CircleOptions;
import android.graphics.Color;

import yasminemassaoudi.grp3.fyourf.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private String friendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        friendName = getIntent().getStringExtra("friendName");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng friendLocation = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions()
                .position(friendLocation)
                .title(friendName != null ? friendName : "Friend's Location")
                .snippet("Lat: " + String.format("%.4f", latitude) +
                        ", Lon: " + String.format("%.4f", longitude)));

        mMap.addCircle(new CircleOptions()
                .center(friendLocation)
                .radius(100) // 100 meters
                .fillColor(0x220095F6)
                .strokeColor(0xFF0095F6)
                .strokeWidth(2));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLocation, 15));

        String address = LocationUtils.getAddressFromCoordinates(this, latitude, longitude);
        Toast.makeText(this, address, Toast.LENGTH_LONG).show();
    }
}
