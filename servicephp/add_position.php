<?php
/**
 * add_position.php
 * Ajoute ou met à jour une position dans la base de données
 * 
 * Méthode: GET ou POST
 * URL: http://votre_ip/servicephp/add_position.php
 * 
 * Paramètres requis:
 * - longitude: longitude GPS (double)
 * - latitude: latitude GPS (double)
 * - numero: numéro de téléphone (string)
 * 
 * Paramètres optionnels:
 * - pseudo: pseudo de l'utilisateur (string)
 * - timestamp: timestamp en millisecondes (bigint)
 * 
 * Exemples:
 * GET:  http://192.168.1.100/servicephp/add_position.php?longitude=10.1815&latitude=36.8065&numero=+21612345678&pseudo=User1
 * POST: Envoyer les paramètres dans le body
 */

require_once 'config.php';

try {
    // Récupérer les paramètres (GET ou POST)
    $longitude = null;
    $latitude = null;
    $numero = null;
    $pseudo = '';
    $timestamp = null;
    
    // Vérifier POST d'abord, puis GET
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $longitude = isset($_POST['longitude']) ? $_POST['longitude'] : null;
        $latitude = isset($_POST['latitude']) ? $_POST['latitude'] : null;
        $numero = isset($_POST['numero']) ? $_POST['numero'] : null;
        $pseudo = isset($_POST['pseudo']) ? $_POST['pseudo'] : '';
        $timestamp = isset($_POST['timestamp']) ? $_POST['timestamp'] : null;
    } else {
        $longitude = isset($_GET['longitude']) ? $_GET['longitude'] : null;
        $latitude = isset($_GET['latitude']) ? $_GET['latitude'] : null;
        $numero = isset($_GET['numero']) ? $_GET['numero'] : null;
        $pseudo = isset($_GET['pseudo']) ? $_GET['pseudo'] : '';
        $timestamp = isset($_GET['timestamp']) ? $_GET['timestamp'] : null;
    }
    
    // Validation des paramètres requis
    if ($longitude === null || $latitude === null || $numero === null) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "Paramètres manquants",
            "required" => ["longitude", "latitude", "numero"],
            "received" => [
                "longitude" => $longitude !== null,
                "latitude" => $latitude !== null,
                "numero" => $numero !== null
            ],
            "timestamp" => time()
        ]);
        exit();
    }
    
    // Nettoyer et valider les données
    $longitude = floatval($longitude);
    $latitude = floatval($latitude);
    $numero = cleanInput($numero);
    $pseudo = cleanInput($pseudo);
    
    // Si timestamp n'est pas fourni, utiliser le timestamp actuel en millisecondes
    if ($timestamp === null || $timestamp === '') {
        $timestamp = round(microtime(true) * 1000);
    } else {
        $timestamp = strval($timestamp);
    }
    
    // Validation des coordonnées GPS
    if ($longitude < -180 || $longitude > 180) {
        throw new Exception("Longitude invalide: doit être entre -180 et 180");
    }
    if ($latitude < -90 || $latitude > 90) {
        throw new Exception("Latitude invalide: doit être entre -90 et 90");
    }
    
    // Vérifier si une position existe déjà pour ce numéro
    $check_sql = "SELECT idposition FROM positions WHERE numero = ?";
    $stmt_check = $conn->prepare($check_sql);
    
    if (!$stmt_check) {
        throw new Exception("Erreur de préparation (check): " . $conn->error);
    }
    
    $stmt_check->bind_param("s", $numero);
    $stmt_check->execute();
    $result = $stmt_check->get_result();
    
    $action = "";
    $id = null;
    
    if ($result->num_rows > 0) {
        // UPDATE - La position existe déjà
        $row = $result->fetch_assoc();
        $id = $row['idposition'];
        
        $sql = "UPDATE positions
                SET longitude = ?,
                    latitude = ?,
                    pseudo = ?,
                    timestamp = ?
                WHERE numero = ?";
        
        $stmt = $conn->prepare($sql);
        
        if (!$stmt) {
            throw new Exception("Erreur de préparation (update): " . $conn->error);
        }
        
        $stmt->bind_param("ddsss", $longitude, $latitude, $pseudo, $timestamp, $numero);
        
        if (!$stmt->execute()) {
            throw new Exception("Erreur lors de la mise à jour: " . $stmt->error);
        }
        
        $action = "update";
        $message = "Position mise à jour avec succès";
        
    } else {
        // INSERT - Nouvelle position
        $sql = "INSERT INTO positions (longitude, latitude, numero, pseudo, timestamp)
                VALUES (?, ?, ?, ?, ?)";

        $stmt = $conn->prepare($sql);
        
        if (!$stmt) {
            throw new Exception("Erreur de préparation (insert): " . $conn->error);
        }
        
        $stmt->bind_param("ddsss", $longitude, $latitude, $numero, $pseudo, $timestamp);
        
        if (!$stmt->execute()) {
            throw new Exception("Erreur lors de l'insertion: " . $stmt->error);
        }
        
        $id = $conn->insert_id;
        $action = "insert";
        $message = "Position ajoutée avec succès";
    }
    
    // Fermer les statements
    $stmt->close();
    $stmt_check->close();
    
    // Retourner la réponse de succès
    echo json_encode([
        "success" => true,
        "message" => $message,
        "action" => $action,
        "data" => [
            "idposition" => $id,
            "longitude" => $longitude,
            "latitude" => $latitude,
            "numero" => $numero,
            "pseudo" => $pseudo,
            "timestamp" => $timestamp
        ],
        "timestamp" => time()
    ], JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    // Log l'erreur
    logError("Erreur dans add_position.php", [
        "error" => $e->getMessage(),
        "file" => $e->getFile(),
        "line" => $e->getLine(),
        "params" => [
            "longitude" => $longitude ?? 'null',
            "latitude" => $latitude ?? 'null',
            "numero" => $numero ?? 'null'
        ]
    ]);
    
    // Retourner l'erreur en JSON
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de l'ajout/mise à jour de la position",
        "error" => $e->getMessage(),
        "timestamp" => time()
    ]);
}

// Fermer la connexion
if ($conn) {
    $conn->close();
}
?>

