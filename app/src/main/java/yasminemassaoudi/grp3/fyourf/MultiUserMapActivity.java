package yasminemassaoudi.grp3.fyourf;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity pour afficher plusieurs utilisateurs sur la carte
 */
public class MultiUserMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private int user1Id = 1;
    private int user2Id = 2;
    private LatLng user1Location;
    private LatLng user2Location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_map);

        // Initialiser Volley
        requestQueue = Volley.newRequestQueue(this);

        // Obtenir le SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Charger les positions des utilisateurs
        loadUsersLocations();
    }

    /**
     * Charger les positions des utilisateurs depuis le serveur
     */
    private void loadUsersLocations() {
        String url = "http://192.168.56.1/servicephp/connections/get_distance.php?user1_id=" + user1Id + "&user2_id=" + user2Id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Récupérer les positions
                            double user1Lat = response.getDouble("user1_lat");
                            double user1Lon = response.getDouble("user1_lon");
                            double user2Lat = response.getDouble("user2_lat");
                            double user2Lon = response.getDouble("user2_lon");

                            user1Location = new LatLng(user1Lat, user1Lon);
                            user2Location = new LatLng(user2Lat, user2Lon);

                            // Afficher les marqueurs
                            displayMarkers();

                            // Afficher la ligne de distance
                            displayDistanceLine();

                            // Centrer la carte
                            centerMap();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MultiUserMapActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MultiUserMapActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Afficher les marqueurs des utilisateurs
     */
    private void displayMarkers() {
        if (mMap == null) return;

        // Marqueur pour l'utilisateur 1
        mMap.addMarker(new MarkerOptions()
                .position(user1Location)
                .title("User 1")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Marqueur pour l'utilisateur 2
        mMap.addMarker(new MarkerOptions()
                .position(user2Location)
                .title("User 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    /**
     * Afficher la ligne de distance entre les deux utilisateurs
     */
    private void displayDistanceLine() {
        if (mMap == null || user1Location == null || user2Location == null) return;

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(user1Location)
                .add(user2Location)
                .color(getResources().getColor(R.color.primary_blue))
                .width(5);

        mMap.addPolyline(polylineOptions);
    }

    /**
     * Centrer la carte sur les deux utilisateurs
     */
    private void centerMap() {
        if (mMap == null || user1Location == null || user2Location == null) return;

        // Calculer le centre
        double centerLat = (user1Location.latitude + user2Location.latitude) / 2;
        double centerLon = (user1Location.longitude + user2Location.longitude) / 2;
        LatLng center = new LatLng(centerLat, centerLon);

        // Calculer le zoom
        float zoomLevel = calculateZoomLevel();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel));
    }

    /**
     * Calculer le niveau de zoom basé sur la distance
     */
    private float calculateZoomLevel() {
        if (user1Location == null || user2Location == null) return 15;

        double distance = calculateDistance(user1Location, user2Location);

        if (distance < 1) return 18;
        if (distance < 5) return 16;
        if (distance < 10) return 15;
        if (distance < 50) return 13;
        return 12;
    }

    /**
     * Calculer la distance entre deux points (formule de Haversine)
     */
    private double calculateDistance(LatLng point1, LatLng point2) {
        double lat1 = Math.toRadians(point1.latitude);
        double lon1 = Math.toRadians(point1.longitude);
        double lat2 = Math.toRadians(point2.latitude);
        double lon2 = Math.toRadians(point2.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double radius = 6371; // Rayon de la Terre en km

        return radius * c;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}

