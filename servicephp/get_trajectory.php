<?php
/**
 * get_trajectory.php
 * Récupère les positions d'un trajet pour un numéro donné
 * Paramètres:
 *   - numero: numéro de téléphone (requis)
 *   - start: timestamp de début (optionnel)
 *   - end: timestamp de fin (optionnel)
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'config.php';

// Fonction pour envoyer une réponse JSON
function sendResponse($success, $message, $data = null) {
    $response = array(
        'success' => $success,
        'message' => $message
    );
    
    if ($data !== null) {
        $response['data'] = $data;
        $response['count'] = is_array($data) ? count($data) : 0;
    }
    
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
}

// Récupérer les paramètres
$numero = isset($_GET['numero']) ? trim($_GET['numero']) : (isset($_POST['numero']) ? trim($_POST['numero']) : '');
$start_time = isset($_GET['start']) ? intval($_GET['start']) : (isset($_POST['start']) ? intval($_POST['start']) : 0);
$end_time = isset($_GET['end']) ? intval($_GET['end']) : (isset($_POST['end']) ? intval($_POST['end']) : 0);

// Validation
if (empty($numero)) {
    sendResponse(false, 'Le numéro est requis');
}

try {
    // Connexion à la base de données
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    
    // Vérifier la connexion
    if ($conn->connect_error) {
        sendResponse(false, 'Erreur de connexion à la base de données: ' . $conn->connect_error);
    }
    
    // Définir le charset
    $conn->set_charset("utf8mb4");
    
    // Construire la requête SQL
    $sql = "SELECT idposition, longitude, latitude, numero, pseudo, timestamp, created_at, updated_at 
            FROM positions 
            WHERE numero = ?";
    
    $params = array($numero);
    $types = "s";
    
    // Ajouter les filtres de temps si fournis
    if ($start_time > 0 && $end_time > 0) {
        $sql .= " AND timestamp BETWEEN ? AND ?";
        $params[] = $start_time;
        $params[] = $end_time;
        $types .= "ii";
    } elseif ($start_time > 0) {
        $sql .= " AND timestamp >= ?";
        $params[] = $start_time;
        $types .= "i";
    } elseif ($end_time > 0) {
        $sql .= " AND timestamp <= ?";
        $params[] = $end_time;
        $types .= "i";
    }
    
    $sql .= " ORDER BY timestamp ASC";
    
    // Préparer la requête
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        sendResponse(false, 'Erreur de préparation de la requête: ' . $conn->error);
    }
    
    // Lier les paramètres
    $stmt->bind_param($types, ...$params);
    
    // Exécuter la requête
    if (!$stmt->execute()) {
        sendResponse(false, 'Erreur d\'exécution de la requête: ' . $stmt->error);
    }
    
    // Récupérer les résultats
    $result = $stmt->get_result();
    $positions = array();
    
    while ($row = $result->fetch_assoc()) {
        $positions[] = array(
            'idposition' => intval($row['idposition']),
            'longitude' => floatval($row['longitude']),
            'latitude' => floatval($row['latitude']),
            'numero' => $row['numero'],
            'pseudo' => $row['pseudo'],
            'timestamp' => intval($row['timestamp']),
            'created_at' => $row['created_at'],
            'updated_at' => $row['updated_at']
        );
    }
    
    // Calculer les statistiques du trajet
    $stats = array(
        'total_positions' => count($positions),
        'numero' => $numero,
        'start_time' => $start_time,
        'end_time' => $end_time
    );
    
    if (count($positions) > 0) {
        $stats['first_position'] = $positions[0];
        $stats['last_position'] = $positions[count($positions) - 1];
        $stats['duration_ms'] = $positions[count($positions) - 1]['timestamp'] - $positions[0]['timestamp'];
        
        // Calculer la distance approximative (formule de Haversine simplifiée)
        $total_distance = 0;
        for ($i = 0; $i < count($positions) - 1; $i++) {
            $lat1 = deg2rad($positions[$i]['latitude']);
            $lon1 = deg2rad($positions[$i]['longitude']);
            $lat2 = deg2rad($positions[$i + 1]['latitude']);
            $lon2 = deg2rad($positions[$i + 1]['longitude']);
            
            $dlat = $lat2 - $lat1;
            $dlon = $lon2 - $lon1;
            
            $a = sin($dlat / 2) * sin($dlat / 2) +
                 cos($lat1) * cos($lat2) *
                 sin($dlon / 2) * sin($dlon / 2);
            
            $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
            $distance = 6371 * $c; // Rayon de la Terre en km
            
            $total_distance += $distance;
        }
        
        $stats['total_distance_km'] = round($total_distance, 2);
    }
    
    // Fermer la connexion
    $stmt->close();
    $conn->close();
    
    // Envoyer la réponse
    $response = array(
        'success' => true,
        'message' => 'Trajet récupéré avec succès',
        'positions' => $positions,
        'stats' => $stats
    );
    
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    
} catch (Exception $e) {
    sendResponse(false, 'Erreur: ' . $e->getMessage());
}
?>

