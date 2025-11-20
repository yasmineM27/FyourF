<?php
/**
 * 📍 get_distance.php - Calculer la distance entre 2 utilisateurs
 */

header('Content-Type: application/json; charset=utf-8');

require_once '../config.php';

try {
    $user1_id = $_GET['user1_id'] ?? null;
    $user2_id = $_GET['user2_id'] ?? null;

    if (!$user1_id || !$user2_id) {
        throw new Exception("IDs utilisateurs requis");
    }

    // Récupérer les dernières positions des deux utilisateurs
    $stmt = $conn->prepare("
        SELECT 
            u.id, u.pseudo,
            p.latitude, p.longitude, p.timestamp
        FROM users u
        LEFT JOIN (
            SELECT numero, latitude, longitude, timestamp
            FROM positions
            WHERE numero = (SELECT numero FROM users WHERE id = ?)
            ORDER BY timestamp DESC
            LIMIT 1
        ) p ON 1=1
        WHERE u.id = ?
        
        UNION ALL
        
        SELECT 
            u.id, u.pseudo,
            p.latitude, p.longitude, p.timestamp
        FROM users u
        LEFT JOIN (
            SELECT numero, latitude, longitude, timestamp
            FROM positions
            WHERE numero = (SELECT numero FROM users WHERE id = ?)
            ORDER BY timestamp DESC
            LIMIT 1
        ) p ON 1=1
        WHERE u.id = ?
    ");
    
    $stmt->bind_param("iiii", $user1_id, $user1_id, $user2_id, $user2_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $positions = [];
    while ($row = $result->fetch_assoc()) {
        $positions[$row['id']] = $row;
    }

    if (count($positions) !== 2) {
        throw new Exception("Positions manquantes pour un ou plusieurs utilisateurs");
    }

    $user1 = $positions[$user1_id];
    $user2 = $positions[$user2_id];

    if (!$user1['latitude'] || !$user2['latitude']) {
        throw new Exception("Positions invalides");
    }

    // Calculer la distance (formule Haversine)
    $lat1 = deg2rad($user1['latitude']);
    $lon1 = deg2rad($user1['longitude']);
    $lat2 = deg2rad($user2['latitude']);
    $lon2 = deg2rad($user2['longitude']);

    $dlat = $lat2 - $lat1;
    $dlon = $lon2 - $lon1;

    $a = sin($dlat / 2) * sin($dlat / 2) +
         cos($lat1) * cos($lat2) * sin($dlon / 2) * sin($dlon / 2);
    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
    $distance_meters = 6371000 * $c; // Rayon terrestre en mètres

    // Calculer l'azimut
    $y = sin($dlon) * cos($lat2);
    $x = cos($lat1) * sin($lat2) - sin($lat1) * cos($lat2) * cos($dlon);
    $bearing = (atan2($y, $x) * 180 / M_PI + 360) % 360;

    // Calculer le temps estimé (vitesse moyenne: 50 km/h)
    $distance_km = $distance_meters / 1000;
    $average_speed = 50; // km/h
    $time_seconds = ($distance_km / $average_speed) * 3600;

    echo json_encode([
        "success" => true,
        "user1" => [
            "id" => $user1['id'],
            "pseudo" => $user1['pseudo'],
            "latitude" => $user1['latitude'],
            "longitude" => $user1['longitude'],
            "timestamp" => $user1['timestamp']
        ],
        "user2" => [
            "id" => $user2['id'],
            "pseudo" => $user2['pseudo'],
            "latitude" => $user2['latitude'],
            "longitude" => $user2['longitude'],
            "timestamp" => $user2['timestamp']
        ],
        "distance" => [
            "meters" => round($distance_meters, 2),
            "kilometers" => round($distance_km, 2),
            "miles" => round($distance_km * 0.621371, 2)
        ],
        "direction" => [
            "bearing" => round($bearing, 2),
            "cardinal" => getCardinalDirection($bearing)
        ],
        "time_remaining" => [
            "seconds" => round($time_seconds),
            "formatted" => formatTime($time_seconds)
        ]
    ]);

} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ]);
}

/**
 * Obtenir la direction cardinale
 */
function getCardinalDirection($bearing) {
    if ($bearing >= 337.5 || $bearing < 22.5) return "Nord";
    if ($bearing >= 22.5 && $bearing < 67.5) return "Nord-Est";
    if ($bearing >= 67.5 && $bearing < 112.5) return "Est";
    if ($bearing >= 112.5 && $bearing < 157.5) return "Sud-Est";
    if ($bearing >= 157.5 && $bearing < 202.5) return "Sud";
    if ($bearing >= 202.5 && $bearing < 247.5) return "Sud-Ouest";
    if ($bearing >= 247.5 && $bearing < 292.5) return "Ouest";
    return "Nord-Ouest";
}

/**
 * Formater le temps
 */
function formatTime($seconds) {
    $hours = floor($seconds / 3600);
    $minutes = floor(($seconds % 3600) / 60);
    $secs = floor($seconds % 60);

    if ($hours > 0) {
        return sprintf("%dh %dm", $hours, $minutes);
    } elseif ($minutes > 0) {
        return sprintf("%dm %ds", $minutes, $secs);
    } else {
        return sprintf("%ds", $secs);
    }
}

$conn->close();
?>

