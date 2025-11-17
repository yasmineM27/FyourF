package yasminemassaoudi.grp3.fyourf;

import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TrackingSyncManager
 * Gère la synchronisation des données de tracking avec MySQL
 */
public class TrackingSyncManager {
    private static final String TAG = "TrackingSyncManager";
    private Context context;
    private RequestQueue requestQueue;
    private SyncCallback callback;
    
    public interface SyncCallback {
        void onSyncSuccess(String message);
        void onSyncError(String error);
        void onSyncProgress(int current, int total);
    }
    
    public TrackingSyncManager(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }
    
    /**
     * Sauvegarde un trajet complet avec ses statistiques
     */
    public void saveTrajectory(String numero, String pseudo, ArrayList<com.google.android.gms.maps.model.LatLng> positions,
                               long durationMs, double totalDistanceKm, double averageSpeedKmh,
                               long startTime, long endTime, SyncCallback callback) {
        this.callback = callback;
        
        try {
            // Construire le JSON du trajet
            JSONObject trajectoryData = new JSONObject();
            trajectoryData.put("numero", numero);
            trajectoryData.put("pseudo", pseudo);
            trajectoryData.put("duration_ms", durationMs);
            trajectoryData.put("total_distance_km", totalDistanceKm);
            trajectoryData.put("average_speed_kmh", averageSpeedKmh);
            trajectoryData.put("start_time", startTime);
            trajectoryData.put("end_time", endTime);
            
            // Ajouter les positions
            JSONArray positionsArray = new JSONArray();
            for (com.google.android.gms.maps.model.LatLng pos : positions) {
                JSONObject posObj = new JSONObject();
                posObj.put("latitude", pos.latitude);
                posObj.put("longitude", pos.longitude);
                posObj.put("timestamp", startTime + (long)(positions.indexOf(pos) * (durationMs / positions.size())));
                positionsArray.put(posObj);
            }
            trajectoryData.put("positions", positionsArray);
            
            // Envoyer la requête
            String url = Config.MYSQL_BASE_URL + "/save_trajectory.php";
            
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    trajectoryData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("success")) {
                                    Log.d(TAG, "Trajet sauvegardé avec succès");
                                    if (callback != null) {
                                        callback.onSyncSuccess("Trajet sauvegardé: " + 
                                            response.getInt("positions_saved") + " positions");
                                    }
                                } else {
                                    String error = response.optString("message", "Erreur inconnue");
                                    if (callback != null) {
                                        callback.onSyncError(error);
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Erreur parsing réponse", e);
                                if (callback != null) {
                                    callback.onSyncError("Erreur: " + e.getMessage());
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Erreur réseau", error);
                            if (callback != null) {
                                callback.onSyncError("Erreur réseau: " + error.getMessage());
                            }
                        }
                    }
            );
            
            requestQueue.add(request);
            
        } catch (JSONException e) {
            Log.e(TAG, "Erreur construction JSON", e);
            if (callback != null) {
                callback.onSyncError("Erreur: " + e.getMessage());
            }
        }
    }
    
    /**
     * Récupère les statistiques des trajets
     */
    public void getStatistics(String numero, SyncCallback callback) {
        this.callback = callback;
        
        String url = Config.MYSQL_BASE_URL + "/get_statistics.php";
        if (numero != null && !numero.isEmpty()) {
            url += "?numero=" + numero;
        }
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                Log.d(TAG, "Statistiques récupérées");
                                if (callback != null) {
                                    callback.onSyncSuccess("Statistiques: " + 
                                        response.getInt("count") + " trajets");
                                }
                            } else {
                                if (callback != null) {
                                    callback.onSyncError(response.optString("message", "Erreur"));
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Erreur parsing", e);
                            if (callback != null) {
                                callback.onSyncError("Erreur: " + e.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur réseau", error);
                        if (callback != null) {
                            callback.onSyncError("Erreur réseau");
                        }
                    }
                }
        );
        
        requestQueue.add(request);
    }
    
    /**
     * Vérifie la connexion à MySQL
     */
    public void verifyConnection(SyncCallback callback) {
        this.callback = callback;
        
        String url = Config.MYSQL_BASE_URL + "/verify_connection.php";
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                Log.d(TAG, "Connexion MySQL OK");
                                if (callback != null) {
                                    callback.onSyncSuccess("Connexion MySQL réussie");
                                }
                            } else {
                                if (callback != null) {
                                    callback.onSyncError("Connexion échouée");
                                }
                            }
                        } catch (JSONException e) {
                            if (callback != null) {
                                callback.onSyncError("Erreur: " + e.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur connexion", error);
                        if (callback != null) {
                            callback.onSyncError("Impossible de se connecter au serveur");
                        }
                    }
                }
        );
        
        requestQueue.add(request);
    }
}

