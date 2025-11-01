package yasminemassaoudi.grp3.fyourf;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * AsyncTask pour télécharger les positions depuis le serveur MySQL
 * 
 * Usage:
 * new Download(new Download.DownloadCallback() {
 *     @Override
 *     public void onDownloadComplete(List<PositionData> positions) {
 *         // Traiter les positions
 *     }
 *     
 *     @Override
 *     public void onDownloadError(String error) {
 *         // Gérer l'erreur
 *     }
 * }).execute();
 */
public class Download extends AsyncTask<Void, Void, List<Download.PositionData>> {
    
    private static final String TAG = "Download";
    private DownloadCallback callback;
    private String errorMessage = null;
    
    /**
     * Constructeur
     * @param callback Callback pour recevoir les résultats
     */
    public Download(DownloadCallback callback) {
        this.callback = callback;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "Début du téléchargement des positions...");
    }
    
    @Override
    protected List<PositionData> doInBackground(Void... voids) {
        List<PositionData> positions = new ArrayList<>();
        
        try {
            // Utiliser JSONParser pour faire la requête
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(MySQLConfig.MYSQL_GET_ALL_URL);
            
            if (response == null) {
                errorMessage = "Aucune réponse du serveur";
                return null;
            }
            
            Log.d(TAG, "Réponse reçue: " + response.toString());
            
            // Vérifier le succès
            boolean success = response.optBoolean("success", false);
            
            if (!success) {
                errorMessage = response.optString("message", "Erreur inconnue");
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
            
            Log.d(TAG, "Total positions téléchargées: " + positions.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du téléchargement: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "Erreur: " + e.getMessage();
            return null;
        }
        
        return positions;
    }
    
    @Override
    protected void onPostExecute(List<PositionData> positions) {
        super.onPostExecute(positions);
        
        if (callback != null) {
            if (positions != null && errorMessage == null) {
                Log.d(TAG, "Téléchargement terminé avec succès: " + positions.size() + " positions");
                callback.onDownloadComplete(positions);
            } else {
                Log.e(TAG, "Téléchargement échoué: " + errorMessage);
                callback.onDownloadError(errorMessage != null ? errorMessage : "Erreur inconnue");
            }
        }
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
    public interface DownloadCallback {
        /**
         * Appelé quand le téléchargement est terminé avec succès
         * @param positions Liste des positions téléchargées
         */
        void onDownloadComplete(List<PositionData> positions);
        
        /**
         * Appelé en cas d'erreur
         * @param error Message d'erreur
         */
        void onDownloadError(String error);
    }
}

