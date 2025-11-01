<?php
/**
 * get_all.php
 * Récupère toutes les positions de la base de données
 * 
 * Méthode: GET
 * URL: http://votre_ip/servicephp/get_all.php
 * 
 * Paramètres optionnels:
 * - limit: nombre maximum de résultats (défaut: tous)
 * - numero: filtrer par numéro de téléphone
 * 
 * Exemples:
 * http://192.168.1.100/servicephp/get_all.php
 * http://192.168.1.100/servicephp/get_all.php?limit=10
 * http://192.168.1.100/servicephp/get_all.php?numero=+21612345678
 */

require_once 'config.php';

try {
    // Récupérer les paramètres optionnels
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : 0;
    $numero = isset($_GET['numero']) ? cleanInput($_GET['numero']) : null;
    
    // Construire la requête SQL
    $sql = "SELECT
                idposition,
                longitude,
                latitude,
                numero,
                pseudo,
                timestamp,
                created_at,
                updated_at
            FROM positions";
    
    // Ajouter le filtre par numéro si spécifié
    if ($numero !== null) {
        $sql .= " WHERE numero = ?";
    }
    
    // Trier par timestamp décroissant (plus récent en premier)
    $sql .= " ORDER BY timestamp DESC";
    
    // Ajouter la limite si spécifiée
    if ($limit > 0) {
        $sql .= " LIMIT ?";
    }
    
    // Préparer la requête
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        throw new Exception("Erreur de préparation: " . $conn->error);
    }
    
    // Lier les paramètres
    if ($numero !== null && $limit > 0) {
        $stmt->bind_param("si", $numero, $limit);
    } elseif ($numero !== null) {
        $stmt->bind_param("s", $numero);
    } elseif ($limit > 0) {
        $stmt->bind_param("i", $limit);
    }
    
    // Exécuter la requête
    if (!$stmt->execute()) {
        throw new Exception("Erreur d'exécution: " . $stmt->error);
    }
    
    // Récupérer les résultats
    $result = $stmt->get_result();
    
    $positions = array();
    
    if ($result->num_rows > 0) {
        while($row = $result->fetch_assoc()) {
            $positions[] = array(
                "idposition" => intval($row["idposition"]),
                "longitude" => floatval($row["longitude"]),
                "latitude" => floatval($row["latitude"]),
                "numero" => $row["numero"],
                "pseudo" => $row["pseudo"],
                "timestamp" => $row["timestamp"] ? strval($row["timestamp"]) : null,
                "created_at" => $row["created_at"],
                "updated_at" => $row["updated_at"]
            );
        }
    }
    
    // Fermer le statement
    $stmt->close();
    
    // Retourner la réponse JSON
    echo json_encode([
        "success" => true,
        "count" => count($positions),
        "data" => $positions,
        "message" => count($positions) > 0 ? "Positions récupérées avec succès" : "Aucune position trouvée",
        "timestamp" => time()
    ], JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    // Log l'erreur
    logError("Erreur dans get_all.php", [
        "error" => $e->getMessage(),
        "file" => $e->getFile(),
        "line" => $e->getLine()
    ]);
    
    // Retourner l'erreur en JSON
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de la récupération des positions",
        "error" => $e->getMessage(),
        "timestamp" => time()
    ]);
}

// Fermer la connexion
if ($conn) {
    $conn->close();
}
?>

