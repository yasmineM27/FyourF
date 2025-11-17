<?php
/**
 * verify_connection.php
 * Vérifie la connexion à la base de données MySQL
 * Utile pour tester la configuration
 * 
 * Méthode: GET
 * URL: http://votre_ip/servicephp/verify_connection.php
 * 
 * Réponse:
 * {
 *   "success": true,
 *   "message": "Connexion MySQL réussie",
 *   "database_info": {
 *     "server": "localhost",
 *     "database": "fyourf_db",
 *     "version": "5.7.30",
 *     "charset": "utf8mb4"
 *   },
 *   "tables": ["positions", "trajectories"],
 *   "timestamp": 1234567890
 * }
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

try {
    // Paramètres de connexion
    $server = "localhost";
    $user = "root";
    $password = "";
    $database = "fyourf_db";
    $port = "3306";
    
    // Créer la connexion
    $conn = new mysqli($server, $user, $password, $database, $port);
    
    // Vérifier la connexion
    if ($conn->connect_error) {
        throw new Exception("Erreur de connexion: " . $conn->connect_error);
    }
    
    // Définir le charset
    $conn->set_charset("utf8mb4");
    
    // Récupérer les informations du serveur
    $server_version = $conn->server_info;
    $client_version = $conn->client_info;
    
    // Récupérer la liste des tables
    $tables = [];
    $result = $conn->query("SHOW TABLES");
    
    if ($result) {
        while ($row = $result->fetch_row()) {
            $tables[] = $row[0];
        }
    }
    
    // Vérifier les tables requises
    $required_tables = ["positions"];
    $missing_tables = [];
    
    foreach ($required_tables as $table) {
        if (!in_array($table, $tables)) {
            $missing_tables[] = $table;
        }
    }
    
    // Récupérer les statistiques de la base de données
    $stats = [
        "positions_count" => 0,
        "trajectories_count" => 0,
        "last_position_time" => null
    ];
    
    // Compter les positions
    $result = $conn->query("SELECT COUNT(*) as count FROM positions");
    if ($result) {
        $row = $result->fetch_assoc();
        $stats["positions_count"] = intval($row["count"]);
    }
    
    // Compter les trajectoires (si la table existe)
    if (in_array("trajectories", $tables)) {
        $result = $conn->query("SELECT COUNT(*) as count FROM trajectories");
        if ($result) {
            $row = $result->fetch_assoc();
            $stats["trajectories_count"] = intval($row["count"]);
        }
    }
    
    // Récupérer la dernière position
    $result = $conn->query("SELECT MAX(timestamp) as last_time FROM positions");
    if ($result) {
        $row = $result->fetch_assoc();
        if ($row["last_time"]) {
            $stats["last_position_time"] = intval($row["last_time"]);
        }
    }
    
    // Fermer la connexion
    $conn->close();
    
    // Réponse de succès
    $response = [
        "success" => true,
        "message" => "Connexion MySQL réussie",
        "database_info" => [
            "server" => $server,
            "database" => $database,
            "port" => $port,
            "server_version" => $server_version,
            "client_version" => $client_version,
            "charset" => "utf8mb4"
        ],
        "tables" => $tables,
        "required_tables_status" => [
            "positions" => in_array("positions", $tables) ? "✓ OK" : "✗ MANQUANTE",
            "trajectories" => in_array("trajectories", $tables) ? "✓ OK" : "✗ MANQUANTE"
        ],
        "statistics" => $stats,
        "timestamp" => time()
    ];
    
    // Ajouter un avertissement si des tables manquent
    if (!empty($missing_tables)) {
        $response["warning"] = "Tables manquantes: " . implode(", ", $missing_tables);
    }
    
    echo json_encode($response, JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Erreur de connexion à la base de données",
        "error" => $e->getMessage(),
        "timestamp" => time()
    ], JSON_PRETTY_PRINT);
}
?>

