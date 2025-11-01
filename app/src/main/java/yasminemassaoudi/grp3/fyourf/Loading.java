package yasminemassaoudi.grp3.fyourf;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread pour charger les positions depuis le serveur MySQL
 * Alternative moderne à AsyncTask
 * 
 * Usage:
 * new Loading(new Loading.LoadingCallback() {
 *     @Override
 *     public void onLoadingComplete(List<PositionData> positions) {
 *         // Traiter les positions
 *     }
 *     
 *     @Override
 *     public void onLoadingError(String error) {
 *         // Gérer l'erreur
 *     }
 * }).start();
 */
public class Loading extends Thread {
    
    private static final String TAG = "Loading";
    private LoadingCallback callback;
    private Handler mainHandler;
    
    /**
     * Constructeur
     * @param callback Callback pour recevoir les résultats
     */
    public Loading(LoadingCallback callback) {
        this.callback = callback;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public void run() {
        Log.d(TAG, "Début du chargement des positions...");
        
        try {
            // Charger les positions
            final List<PositionData> positions = loadPositions();
            
            // Retourner le résultat sur le thread principal
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        if (positions != null) {
                            Log.d(TAG, "Chargement terminé avec succès: " + positions.size() + " positions");
                            callback.onLoadingComplete(positions);
                        } else {
                            Log.e(TAG, "Chargement échoué: positions null");
                            callback.onLoadingError("Erreur lors du chargement");
                        }
                    }
                }
            });
            
        } catch (final Exception e) {
            Log.e(TAG, "Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
            
            // Retourner l'erreur sur le thread principal
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onLoadingError("Erreur: " + e.getMessage());
                    }
                }
            });
        }
    }
    
    /**
     * Charge les positions depuis le serveur
     * @return Liste des positions ou null en cas d'erreur
     */
    private List<PositionData> loadPositions() {
        List<PositionData> positions = new ArrayList<>();
        
        try {
            // Utiliser JSONParser pour faire la requête
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(MySQLConfig.MYSQL_GET_ALL_URL);
            
            if (response == null) {
                Log.e(TAG, "Aucune réponse du serveur");
                return null;
            }
            
            Log.d(TAG, "Réponse reçue: " + response.toString());
            
            // Vérifier le succès
            boolean success = response.optBoolean("success", false);
            
            if (!success) {
                String errorMsg = response.optString("message", "Erreur inconnue");
                Log.e(TAG, "Erreur serveur: " + errorMsg);
                return null;
            }
            
            // Récupérer le tableau de données
            JSONArray dataArray = response.optJSONArray("data");
            
            if (dataArray == null) {
                Log.w(TAG, "Aucune donnée trouvée");
                return positions; // Retourner liste vide
            }
            
            // Parser chaque position
            for (int i = 0; i < dataArray.length(); i++) {
                try {
                    JSONObject posObj = dataArray.getJSONObject(i);
                    
                    PositionData position = new PositionData();
                    position.idposition = posObj.optInt("idposition", 0);
                    position.longitude = posObj.optDouble("longitude", 0.0);
                    position.latitude = posObj.optDouble("latitude", 0.0);
                    position.numero = posObj.optString("numero", "");
                    position.pseudo = posObj.optString("pseudo", "");
                    position.timestamp = posObj.optLong("timestamp", 0);
                    
                    // Valider les données
                    if (position.isValid()) {
                        positions.add(position);
                        Log.d(TAG, "Position ajoutée: " + position.numero + " (" + position.latitude + ", " + position.longitude + ")");
                    } else {
                        Log.w(TAG, "Position invalide ignorée: " + position.numero);
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors du parsing de la position " + i + ": " + e.getMessage());
                }
            }
            
            Log.d(TAG, "Total positions chargées: " + positions.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        return positions;
    }
    
    /**
     * Classe pour représenter une position
     */
    public static class PositionData {
        public int idposition;
        public double longitude;
        public double latitude;
        public String numero;
        public String pseudo;
        public long timestamp;
        
        /**
         * Vérifie si la position est valide
         */
        public boolean isValid() {
            // Vérifier que les coordonnées sont dans les limites valides
            if (latitude < -90 || latitude > 90) return false;
            if (longitude < -180 || longitude > 180) return false;
            
            // Vérifier que ce n'est pas une position par défaut invalide
            if (latitude == 0.0 && longitude == 0.0) return false;
            if (latitude == 999.0 && longitude == 999.0) return false;
            
            // Vérifier que le numéro n'est pas vide
            if (numero == null || numero.trim().isEmpty()) return false;
            
            return true;
        }
        
        /**
         * Convertit en LocationDatabase.LocationEntry
         */
        public LocationDatabase.LocationEntry toLocationEntry() {
            return new LocationDatabase.LocationEntry(
                numero,
                latitude,
                longitude,
                timestamp
            );
        }
        
        @Override
        public String toString() {
            return "Position{" +
                    "id=" + idposition +
                    ", numero='" + numero + '\'' +
                    ", lat=" + latitude +
                    ", lon=" + longitude +
                    ", pseudo='" + pseudo + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
    
    /**
     * Interface de callback pour les résultats
     */
    public interface LoadingCallback {
        /**
         * Appelé quand le chargement est terminé avec succès
         * @param positions Liste des positions chargées
         */
        void onLoadingComplete(List<PositionData> positions);
        
        /**
         * Appelé en cas d'erreur
         * @param error Message d'erreur
         */
        void onLoadingError(String error);
    }
}

