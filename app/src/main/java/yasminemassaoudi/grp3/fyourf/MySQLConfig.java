package yasminemassaoudi.grp3.fyourf;

/**
 * Configuration MySQL pour l'application FyourF
 * 
 * IMPORTANT: Modifier MYSQL_SERVER_IP avec votre adresse IPv4
 * 
 * Pour trouver votre IPv4:
 * - Windows: ouvrir CMD et taper "ipconfig"
 * - Linux/Mac: ouvrir Terminal et taper "ifconfig"
 * 
 * Exemple: 192.168.1.100
 */
public class MySQLConfig {
    
    // ============================================
    // CONFIGURATION SERVEUR - À MODIFIER
    // ============================================
    
    /**
     * Adresse IP de votre serveur PHP
     * IMPORTANT: Remplacer par votre IPv4
     */
    public static final String MYSQL_SERVER_IP = "192.168.1.17";
    
    /**
     * Port du serveur (80 par défaut pour Apache)
     */
    public static final int MYSQL_SERVER_PORT = 80;
    
    /**
     * Nom du dossier contenant les scripts PHP
     */
    public static final String MYSQL_SERVICE_FOLDER = "servicephp";
    
    // ============================================
    // URLS GÉNÉRÉES AUTOMATIQUEMENT
    // ============================================
    
    /**
     * URL de base du serveur
     */
    public static final String MYSQL_BASE_URL = "http://" + MYSQL_SERVER_IP + ":" + MYSQL_SERVER_PORT + "/" + MYSQL_SERVICE_FOLDER;
    
    /**
     * URL pour récupérer toutes les positions
     */
    public static final String MYSQL_GET_ALL_URL = MYSQL_BASE_URL + "/get_all.php";
    
    /**
     * URL pour ajouter/mettre à jour une position
     */
    public static final String MYSQL_ADD_POSITION_URL = MYSQL_BASE_URL + "/add_position.php";

    /**
     * URL pour supprimer une position
     */
    public static final String MYSQL_DELETE_POSITION_URL = MYSQL_BASE_URL + "/delete_position.php";

    /**
     * URL pour récupérer un trajet (positions d'un numéro sur une période)
     */
    public static final String MYSQL_GET_TRAJECTORY_URL = MYSQL_BASE_URL + "/get_trajectory.php";

    // ============================================
    // PARAMÈTRES DE CONNEXION
    // ============================================
    
    /**
     * Timeout de connexion en millisecondes
     */
    public static final int CONNECTION_TIMEOUT = 15000; // 15 secondes
    
    /**
     * Timeout de lecture en millisecondes
     */
    public static final int READ_TIMEOUT = 10000; // 10 secondes
    
    /**
     * Nombre de tentatives en cas d'échec
     */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * Délai entre les tentatives en millisecondes
     */
    public static final int RETRY_DELAY = 2000; // 2 secondes
    
    // ============================================
    // MÉTHODES UTILITAIRES
    // ============================================
    
    /**
     * Vérifie si la configuration est valide
     * @return true si la configuration semble valide
     */
    public static boolean isConfigValid() {
        // Vérifier que l'IP n'est pas l'IP par défaut
        if (MYSQL_SERVER_IP.equals("192.168.1.100")) {
            android.util.Log.w("MySQLConfig", "⚠️ ATTENTION: Vous utilisez l'IP par défaut. Veuillez la modifier!");
            return false;
        }
        
        // Vérifier que l'IP a un format valide
        if (!MYSQL_SERVER_IP.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            android.util.Log.e("MySQLConfig", " Format d'IP invalide: " + MYSQL_SERVER_IP);
            return false;
        }
        
        return true;
    }
    
    /**
     * Construit une URL avec des paramètres GET
     * @param baseUrl URL de base
     * @param params Paramètres à ajouter
     * @return URL complète avec paramètres
     */
    public static String buildUrlWithParams(String baseUrl, java.util.Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");
        
        boolean first = true;
        for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            try {
                urlBuilder.append(entry.getKey())
                          .append("=")
                          .append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (java.io.UnsupportedEncodingException e) {
                android.util.Log.e("MySQLConfig", "Erreur d'encodage: " + e.getMessage());
            }
            first = false;
        }
        
        return urlBuilder.toString();
    }
    
    /**
     * Affiche la configuration actuelle dans les logs
     */
    public static void logConfiguration() {
        android.util.Log.i("MySQLConfig", "=== Configuration MySQL ===");
        android.util.Log.i("MySQLConfig", "Serveur IP: " + MYSQL_SERVER_IP);
        android.util.Log.i("MySQLConfig", "Port: " + MYSQL_SERVER_PORT);
        android.util.Log.i("MySQLConfig", "Dossier: " + MYSQL_SERVICE_FOLDER);
        android.util.Log.i("MySQLConfig", "URL Base: " + MYSQL_BASE_URL);
        android.util.Log.i("MySQLConfig", "GET ALL: " + MYSQL_GET_ALL_URL);
        android.util.Log.i("MySQLConfig", "ADD POS: " + MYSQL_ADD_POSITION_URL);
        android.util.Log.i("MySQLConfig", "DELETE POS: " + MYSQL_DELETE_POSITION_URL);
        android.util.Log.i("MySQLConfig", "GET TRAJECTORY: " + MYSQL_GET_TRAJECTORY_URL);
        android.util.Log.i("MySQLConfig", "Config valide: " + isConfigValid());
        android.util.Log.i("MySQLConfig", "==========================");
    }
    
    /**
     * Teste la connectivité au serveur
     * @param callback Callback pour le résultat
     */
    public static void testConnection(final ConnectionTestCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    java.net.URL url = new java.net.URL(MYSQL_GET_ALL_URL);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(CONNECTION_TIMEOUT);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setRequestMethod("GET");
                    
                    int responseCode = connection.getResponseCode();
                    
                    if (responseCode == 200) {
                        callback.onSuccess("Connexion réussie au serveur MySQL");
                    } else {
                        callback.onError("Code de réponse: " + responseCode);
                    }
                    
                    connection.disconnect();
                    
                } catch (Exception e) {
                    callback.onError("Erreur de connexion: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Interface pour le callback de test de connexion
     */
    public interface ConnectionTestCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}

