<?php
/**
 * get_statistics.php
 * Récupère les statistiques d'un trajet ou de tous les trajets
 * 
 * Méthode: GET
 * URL: http://votre_ip/servicephp/get_statistics.php
 * 
 * Paramètres optionnels:
 * - numero: numéro de téléphone (pour un trajet spécifique)
 * - limit: nombre maximum de trajets (défaut: 10)
 * - start_date: date de début (format: YYYY-MM-DD)
 * - end_date: date de fin (format: YYYY-MM-DD)
 * 
 * Exemples:
 * http://192.168.1.100/servicephp/get_statistics.php
 * http://192.168.1.100/servicephp/get_statistics.php?numero=+21612345678
 * http://192.168.1.100/servicephp/get_statistics.php?numero=+21612345678&limit=5
 */

require_once 'config.php';

try {
    // Récupérer les paramètres
    $numero = isset($_GET['numero']) ? cleanInput($_GET['numero']) : null;
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : 10;
    $start_date = isset($_GET['start_date']) ? cleanInput($_GET['start_date']) : null;
    $end_date = isset($_GET['end_date']) ? cleanInput($_GET['end_date']) : null;
    
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
    
    $conn->query($create_table_sql);
    
    // Construire la requête SQL
    $sql = "SELECT 
                id_trajectory,
                numero,
                pseudo,
                start_time,
                end_time,
                duration_ms,
                total_distance_km,
                average_speed_kmh,
                point_count,
                created_at,
                updated_at
            FROM trajectories
            WHERE 1=1";
    
    $params = [];
    $types = "";
    
    // Ajouter le filtre par numéro si spécifié
    if ($numero !== null) {
        $sql .= " AND numero = ?";
        $params[] = $numero;
        $types .= "s";
    }
    
    // Ajouter les filtres de date si spécifiés
    if ($start_date !== null) {
        $start_timestamp = strtotime($start_date) * 1000;
        $sql .= " AND start_time >= ?";
        $params[] = $start_timestamp;
        $types .= "i";
    }
    
    if ($end_date !== null) {
        $end_timestamp = (strtotime($end_date) + 86400) * 1000; // Fin du jour
        $sql .= " AND end_time <= ?";
        $params[] = $end_timestamp;
        $types .= "i";
    }
    
    // Trier par date décroissante
    $sql .= " ORDER BY start_time DESC";
    
    // Ajouter la limite
    if ($limit > 0) {
        $sql .= " LIMIT ?";
        $params[] = $limit;
        $types .= "i";
    }
    
    // Préparer et exécuter la requête
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        throw new Exception("Erreur de préparation: " . $conn->error);
    }
    
    if (!empty($params)) {
        $stmt->bind_param($types, ...$params);
    }
    
    if (!$stmt->execute()) {
        throw new Exception("Erreur d'exécution: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    $trajectories = [];
    
    while ($row = $result->fetch_assoc()) {
        $trajectories[] = [
            "id_trajectory" => intval($row["id_trajectory"]),
            "numero" => $row["numero"],
            "pseudo" => $row["pseudo"],
            "start_time" => intval($row["start_time"]),
            "end_time" => intval($row["end_time"]),
            "duration_ms" => intval($row["duration_ms"]),
            "duration_formatted" => formatDuration(intval($row["duration_ms"])),
            "total_distance_km" => floatval($row["total_distance_km"]),
            "average_speed_kmh" => floatval($row["average_speed_kmh"]),
            "point_count" => intval($row["point_count"]),
            "created_at" => $row["created_at"],
            "updated_at" => $row["updated_at"]
        ];
    }
    
    $stmt->close();
    
    // Calculer les statistiques globales
    $global_stats = [
        "total_trajectories" => count($trajectories),
        "total_distance_km" => 0,
        "total_duration_ms" => 0,
        "average_speed_kmh" => 0,
        "total_points" => 0
    ];
    
    foreach ($trajectories as $traj) {
        $global_stats["total_distance_km"] += $traj["total_distance_km"];
        $global_stats["total_duration_ms"] += $traj["duration_ms"];
        $global_stats["total_points"] += $traj["point_count"];
    }
    
    if ($global_stats["total_duration_ms"] > 0) {
        $global_stats["average_speed_kmh"] = round(
            ($global_stats["total_distance_km"] / 1000) / 
            ($global_stats["total_duration_ms"] / (1000 * 3600)), 
            2
        );
    }
    
    $global_stats["total_distance_km"] = round($global_stats["total_distance_km"], 2);
    $global_stats["total_duration_formatted"] = formatDuration($global_stats["total_duration_ms"]);
    
    // Réponse de succès
    echo json_encode([
        "success" => true,
        "message" => count($trajectories) > 0 ? "Statistiques récupérées" : "Aucun trajet trouvé",
        "global_stats" => $global_stats,
        "trajectories" => $trajectories,
        "count" => count($trajectories),
        "timestamp" => time()
    ], JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    logError("Erreur dans get_statistics.php", [
        "error" => $e->getMessage(),
        "file" => $e->getFile(),
        "line" => $e->getLine()
    ]);
    
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de la récupération des statistiques",
        "error" => $e->getMessage(),
        "timestamp" => time()
    ]);
}

/**
 * Formate la durée en millisecondes au format HH:MM:SS
 */
function formatDuration($ms) {
    $seconds = intval($ms / 1000);
    $hours = intval($seconds / 3600);
    $minutes = intval(($seconds % 3600) / 60);
    $secs = $seconds % 60;
    
    return sprintf("%02d:%02d:%02d", $hours, $minutes, $secs);
}

if ($conn) {
    $conn->close();
}
?>

