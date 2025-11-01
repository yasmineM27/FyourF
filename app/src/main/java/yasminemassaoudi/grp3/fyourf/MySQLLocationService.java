package yasminemassaoudi.grp3.fyourf;

import android.util.Log;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service pour gérer les opérations MySQL de localisation
 * Permet d'ajouter, mettre à jour et récupérer des positions
 */
public class MySQLLocationService {
    
    private static final String TAG = "MySQLLocationService";
    private ExecutorService executorService;
    private JSONParser jsonParser;
    
    /**
     * Constructeur
     */
    public MySQLLocationService() {
        this.executorService = Executors.newFixedThreadPool(3);
        this.jsonParser = new JSONParser();
        
        // Vérifier la configuration
        if (!MySQLConfig.isConfigValid()) {
            Log.w(TAG, "⚠️ Configuration MySQL non valide. Veuillez vérifier MySQLConfig.java");
        }
    }
    
    /**
     * Ajoute ou met à jour une position dans MySQL
     * @param numero Numéro de téléphone
     * @param latitude Latitude GPS
     * @param longitude Longitude GPS
     * @param pseudo Pseudo (optionnel)
     * @param callback Callback pour le résultat
     */
    public void addOrUpdatePosition(final String numero, final double latitude, final double longitude, 
                                   final String pseudo, final OperationCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Ajout/MAJ position pour " + numero + " (" + latitude + ", " + longitude + ")");
                    
                    // Préparer les paramètres
                    HashMap<String, String> params = new HashMap<>();
                    params.put("numero", numero);
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longitude));
                    params.put("pseudo", pseudo != null ? pseudo : "");
                    params.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    
                    // Faire la requête
                    JSONObject response = jsonParser.makeHttpRequest(
                        MySQLConfig.MYSQL_ADD_POSITION_URL,
                        "POST",
                        params
                    );
                    
                    if (response == null) {
                        if (callback != null) {
                            callback.onError("Aucune réponse du serveur");
                        }
                        return;
                    }
                    
                    Log.d(TAG, "Réponse: " + response.toString());
                    
                    boolean success = response.optBoolean("success", false);
                    String message = response.optString("message", "");
                    
                    if (success) {
                        Log.d(TAG, "✓ Position sauvegardée: " + message);
                        if (callback != null) {
                            callback.onSuccess(message);
                        }
                    } else {
                        Log.e(TAG, "✗ Erreur: " + message);
                        if (callback != null) {
                            callback.onError(message);
                        }
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Exception lors de l'ajout: " + e.getMessage());
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Exception: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * Ajoute ou met à jour une position (sans pseudo)
     */
    public void addOrUpdatePosition(String numero, double latitude, double longitude, OperationCallback callback) {
        addOrUpdatePosition(numero, latitude, longitude, null, callback);
    }
    
    /**
     * Ajoute ou met à jour une position (sans callback)
     */
    public void addOrUpdatePosition(String numero, double latitude, double longitude) {
        addOrUpdatePosition(numero, latitude, longitude, null, null);
    }
    
    /**
     * Récupère toutes les positions depuis MySQL
     * @param callback Callback pour recevoir les positions
     */
    public void getAllPositions(final GetPositionsCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Récupération de toutes les positions...");
                    
                    JSONObject response = jsonParser.makeRequest(MySQLConfig.MYSQL_GET_ALL_URL);
                    
                    if (response == null) {
                        if (callback != null) {
                            callback.onError("Aucune réponse du serveur");
                        }
                        return;
                    }
                    
                    Log.d(TAG, "Réponse: " + response.toString());
                    
                    boolean success = response.optBoolean("success", false);
                    
                    if (success) {
                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    } else {
                        String message = response.optString("message", "Erreur inconnue");
                        if (callback != null) {
                            callback.onError(message);
                        }
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Exception lors de la récupération: " + e.getMessage());
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Exception: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * Teste la connexion au serveur MySQL
     * @param callback Callback pour le résultat
     */
    public void testConnection(final OperationCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Test de connexion au serveur...");
                    
                    JSONObject response = jsonParser.makeRequest(MySQLConfig.MYSQL_GET_ALL_URL);
                    
                    if (response != null) {
                        boolean success = response.optBoolean("success", false);
                        if (success) {
                            Log.d(TAG, "✓ Connexion réussie");
                            if (callback != null) {
                                callback.onSuccess("Connexion réussie au serveur MySQL");
                            }
                        } else {
                            Log.e(TAG, "✗ Connexion échouée");
                            if (callback != null) {
                                callback.onError("Le serveur a répondu mais avec une erreur");
                            }
                        }
                    } else {
                        Log.e(TAG, "✗ Pas de réponse du serveur");
                        if (callback != null) {
                            callback.onError("Pas de réponse du serveur");
                        }
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Exception lors du test: " + e.getMessage());
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Exception: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * Supprime une position par son ID
     * @param idposition ID de la position à supprimer
     * @param callback Callback pour le résultat
     */
    public void deletePosition(final int idposition, final OperationCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Suppression de la position ID: " + idposition);

                    // Préparer les paramètres
                    HashMap<String, String> params = new HashMap<>();
                    params.put("id", String.valueOf(idposition));

                    // Faire la requête
                    JSONObject response = jsonParser.makeHttpRequest(
                        MySQLConfig.MYSQL_DELETE_POSITION_URL,
                        "POST",
                        params
                    );

                    if (response == null) {
                        if (callback != null) {
                            callback.onError("Aucune réponse du serveur");
                        }
                        return;
                    }

                    Log.d(TAG, "Réponse: " + response.toString());

                    boolean success = response.optBoolean("success", false);
                    String message = response.optString("message", "");

                    if (success) {
                        Log.d(TAG, "✓ Position supprimée: " + message);
                        if (callback != null) {
                            callback.onSuccess(message);
                        }
                    } else {
                        Log.e(TAG, "✗ Erreur: " + message);
                        if (callback != null) {
                            callback.onError(message);
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Exception lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Exception: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Récupère les positions d'un trajet (par numéro et période)
     * @param numero Numéro de téléphone
     * @param startTime Timestamp de début (optionnel, 0 pour tout)
     * @param endTime Timestamp de fin (optionnel, 0 pour tout)
     * @param callback Callback pour recevoir les positions
     */
    public void getTrajectory(final String numero, final long startTime, final long endTime,
                             final GetPositionsCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Récupération du trajet pour " + numero);

                    // Construire l'URL avec paramètres
                    String url = MySQLConfig.MYSQL_GET_TRAJECTORY_URL + "?numero=" + numero;
                    if (startTime > 0) {
                        url += "&start=" + startTime;
                    }
                    if (endTime > 0) {
                        url += "&end=" + endTime;
                    }

                    JSONObject response = jsonParser.makeRequest(url);

                    if (response == null) {
                        if (callback != null) {
                            callback.onError("Aucune réponse du serveur");
                        }
                        return;
                    }

                    Log.d(TAG, "Réponse: " + response.toString());

                    boolean success = response.optBoolean("success", false);

                    if (success) {
                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    } else {
                        String message = response.optString("message", "Erreur inconnue");
                        if (callback != null) {
                            callback.onError(message);
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Exception lors de la récupération du trajet: " + e.getMessage());
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Exception: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Ferme le service et libère les ressources
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "Service arrêté");
        }
    }

    /**
     * Interface de callback pour les opérations simples
     */
    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Interface de callback pour récupérer les positions
     */
    public interface GetPositionsCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }
}

