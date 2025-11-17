<?php
/**
 * save_trajectory.php
 * Sauvegarde un trajet complet avec ses statistiques
 * 
 * Méthode: POST
 * URL: http://votre_ip/servicephp/save_trajectory.php
 * 
 * Paramètres requis (JSON):
 * {
 *   "numero": "+21612345678",
 *   "pseudo": "User1",
 *   "positions": [
 *     {"latitude": 36.8065, "longitude": 10.1815, "timestamp": 1234567890000},
 *     {"latitude": 36.8070, "longitude": 10.1820, "timestamp": 1234567920000}
 *   ],
 *   "duration_ms": 30000,
 *   "total_distance_km": 0.5,
 *   "average_speed_kmh": 60.0,
 *   "start_time": 1234567890000,
 *   "end_time": 1234567920000
 * }
 */

require_once 'config.php';

try {
    // Récupérer les données JSON
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);
    
    if (!$data) {
        throw new Exception("Données JSON invalides");
    }
    
    // Valider les paramètres requis
    if (empty($data['numero'])) {
        throw new Exception("Le numéro est requis");
    }
    
    if (empty($data['positions']) || !is_array($data['positions'])) {
        throw new Exception("Les positions sont requises");
    }
    
    $numero = cleanInput($data['numero']);
    $pseudo = isset($data['pseudo']) ? cleanInput($data['pseudo']) : '';
    $positions = $data['positions'];
    $duration_ms = isset($data['duration_ms']) ? intval($data['duration_ms']) : 0;
    $total_distance_km = isset($data['total_distance_km']) ? floatval($data['total_distance_km']) : 0;
    $average_speed_kmh = isset($data['average_speed_kmh']) ? floatval($data['average_speed_kmh']) : 0;
    $start_time = isset($data['start_time']) ? intval($data['start_time']) : 0;
    $end_time = isset($data['end_time']) ? intval($data['end_time']) : 0;
    
    // Créer la table trajectories si elle n'existe pas
    $create_table_sql = "CREATE TABLE IF NOT EXISTS trajectories (
        id_trajectory INT AUTO_INCREMENT PRIMARY KEY,
        numero VARCHAR(20) NOT NULL,
        pseudo VARCHAR(100),
        start_time BIGINT NOT NULL,
        end_time BIGINT NOT NULL,
        duration_ms INT,
        total_distance_km DOUBLE,
        average_speed_kmh DOUBLE,
        point_count INT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_numero (numero),
        INDEX idx_start_time (start_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
    
    if (!$conn->query($create_table_sql)) {
        throw new Exception("Erreur création table trajectories: " . $conn->error);
    }
    
    // Insérer le trajet
    $sql_trajectory = "INSERT INTO trajectories 
        (numero, pseudo, start_time, end_time, duration_ms, total_distance_km, average_speed_kmh, point_count)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    $stmt = $conn->prepare($sql_trajectory);
    if (!$stmt) {
        throw new Exception("Erreur préparation: " . $conn->error);
    }
    
    $point_count = count($positions);
    $stmt->bind_param("ssiiiddi", $numero, $pseudo, $start_time, $end_time, 
                      $duration_ms, $total_distance_km, $average_speed_kmh, $point_count);
    
    if (!$stmt->execute()) {
        throw new Exception("Erreur insertion trajet: " . $stmt->error);
    }
    
    $trajectory_id = $conn->insert_id;
    $stmt->close();
    
    // Insérer les positions du trajet
    $sql_positions = "INSERT INTO positions 
        (longitude, latitude, numero, pseudo, timestamp)
        VALUES (?, ?, ?, ?, ?)";
    
    $stmt = $conn->prepare($sql_positions);
    if (!$stmt) {
        throw new Exception("Erreur préparation positions: " . $conn->error);
    }
    
    $inserted_count = 0;
    foreach ($positions as $pos) {
        if (!isset($pos['latitude']) || !isset($pos['longitude'])) {
            continue;
        }
        
        $latitude = floatval($pos['latitude']);
        $longitude = floatval($pos['longitude']);
        $timestamp = isset($pos['timestamp']) ? intval($pos['timestamp']) : time() * 1000;
        
        $stmt->bind_param("ddsss", $longitude, $latitude, $numero, $pseudo, $timestamp);
        
        if (!$stmt->execute()) {
            error_log("Erreur insertion position: " . $stmt->error);
            continue;
        }
        
        $inserted_count++;
    }
    
    $stmt->close();
    
    // Réponse de succès
    echo json_encode([
        "success" => true,
        "message" => "Trajet sauvegardé avec succès",
        "trajectory_id" => $trajectory_id,
        "positions_saved" => $inserted_count,
        "total_positions" => $point_count,
        "data" => [
            "numero" => $numero,
            "pseudo" => $pseudo,
            "duration_ms" => $duration_ms,
            "total_distance_km" => $total_distance_km,
            "average_speed_kmh" => $average_speed_kmh,
            "start_time" => $start_time,
            "end_time" => $end_time
        ],
        "timestamp" => time()
    ], JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    logError("Erreur dans save_trajectory.php", [
        "error" => $e->getMessage(),
        "file" => $e->getFile(),
        "line" => $e->getLine()
    ]);
    
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de la sauvegarde du trajet",
        "error" => $e->getMessage(),
        "timestamp" => time()
    ]);
}

if ($conn) {
    $conn->close();
}
?>

