<?php
/**
 * Configuration de la connexion MySQL
 * Application: FyourF - Location Tracking
 * 
 * INSTRUCTIONS:
 * 1. Copier ce dossier 'servicephp' dans:
 *    - XAMPP: C:\xampp\htdocs\servicephp\
 *    - WAMP: C:\wamp64\www\servicephp\
 *    - LAMP: /var/www/html/servicephp/
 * 
 * 2. Modifier les paramètres ci-dessous selon votre configuration
 */

// Headers pour permettre les requêtes depuis l'application Android
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Gérer les requêtes OPTIONS (preflight)
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// ============================================
// PARAMÈTRES DE CONNEXION MYSQL
// ============================================

$user = "root";                    // Utilisateur MySQL (par défaut: root)
$mp = "";                          // Mot de passe (vide par défaut pour XAMPP/WAMP)
$database = "fyourf_db";    // Nom de la base de données
$server = "localhost";             // Serveur (localhost pour local)
$port = "3306";                    // Port MySQL (3306 par défaut)

// ============================================
// CONNEXION À LA BASE DE DONNÉES
// ============================================

$conn = null;

try {
    // Créer la connexion
    $conn = new mysqli($server, $user, $mp, $database, $port);
    
    // Vérifier la connexion
    if ($conn->connect_error) {
        throw new Exception("Erreur de connexion: " . $conn->connect_error);
    }
    
    // Définir le charset UTF-8
    if (!$conn->set_charset("utf8mb4")) {
        throw new Exception("Erreur lors de la définition du charset: " . $conn->error);
    }
    
    // Log de succès (à désactiver en production)
    // error_log("Connexion MySQL réussie");
    
} catch (Exception $e) {
    // En cas d'erreur, retourner un JSON et arrêter l'exécution
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => "Database connection failed",
        "message" => $e->getMessage(),
        "timestamp" => time()
    ]);
    exit();
}

// ============================================
// FONCTIONS UTILITAIRES
// ============================================

/**
 * Fonction pour nettoyer les entrées utilisateur
 */
function cleanInput($data) {
    global $conn;
    $data = trim($data);
    $data = stripslashes($data);
    $data = htmlspecialchars($data);
    return $conn->real_escape_string($data);
}

/**
 * Fonction pour retourner une réponse JSON
 */
function sendJsonResponse($success, $data = [], $message = "", $httpCode = 200) {
    http_response_code($httpCode);
    echo json_encode([
        "success" => $success,
        "message" => $message,
        "data" => $data,
        "timestamp" => time()
    ]);
    exit();
}

/**
 * Fonction pour logger les erreurs
 */
function logError($message, $context = []) {
    $logMessage = date('Y-m-d H:i:s') . " - " . $message;
    if (!empty($context)) {
        $logMessage .= " - Context: " . json_encode($context);
    }
    error_log($logMessage);
}

// ============================================
// CONFIGURATION SUPPLÉMENTAIRE
// ============================================

// Timezone
date_default_timezone_set('Africa/Tunis');

// Affichage des erreurs (à désactiver en production)
ini_set('display_errors', 0);
ini_set('display_startup_errors', 0);
error_reporting(E_ALL);

// Log des erreurs dans un fichier
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/php_errors.log');

?>

