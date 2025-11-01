<?php
/**
 * delete_position.php
 * Supprime une position de la base de données
 * 
 * Méthode: GET ou POST
 * URL: http://votre_ip/servicephp/delete_position.php
 * 
 * Paramètres requis:
 * - id: ID de la position à supprimer (int)
 * 
 * Exemples:
 * GET:  http://192.168.1.100/servicephp/delete_position.php?id=5
 * POST: Envoyer id dans le body
 */

require_once 'config.php';

try {
    // Récupérer l'ID (GET ou POST)
    $id = null;
    
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $id = isset($_POST['id']) ? $_POST['id'] : null;
    } else {
        $id = isset($_GET['id']) ? $_GET['id'] : null;
    }
    
    // Validation du paramètre requis
    if ($id === null || $id === '') {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "Paramètre 'id' manquant",
            "required" => ["id"],
            "timestamp" => time()
        ]);
        exit();
    }
    
    // Convertir en entier
    $id = intval($id);
    
    if ($id <= 0) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "ID invalide: doit être un entier positif",
            "timestamp" => time()
        ]);
        exit();
    }
    
    // Vérifier si la position existe
    $check_sql = "SELECT idposition, numero, latitude, longitude FROM positions WHERE idposition = ?";
    $stmt_check = $conn->prepare($check_sql);
    
    if (!$stmt_check) {
        throw new Exception("Erreur de préparation (check): " . $conn->error);
    }
    
    $stmt_check->bind_param("i", $id);
    $stmt_check->execute();
    $result = $stmt_check->get_result();
    
    if ($result->num_rows === 0) {
        // Position non trouvée
        $stmt_check->close();
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "message" => "Position non trouvée",
            "id" => $id,
            "timestamp" => time()
        ]);
        exit();
    }
    
    // Récupérer les données avant suppression (pour le log)
    $row = $result->fetch_assoc();
    $deleted_data = [
        "idposition" => $row["idposition"],
        "numero" => $row["numero"],
        "latitude" => floatval($row["latitude"]),
        "longitude" => floatval($row["longitude"])
    ];
    
    $stmt_check->close();
    
    // Supprimer la position
    $delete_sql = "DELETE FROM positions WHERE idposition = ?";
    $stmt_delete = $conn->prepare($delete_sql);
    
    if (!$stmt_delete) {
        throw new Exception("Erreur de préparation (delete): " . $conn->error);
    }
    
    $stmt_delete->bind_param("i", $id);
    
    if (!$stmt_delete->execute()) {
        throw new Exception("Erreur lors de la suppression: " . $stmt_delete->error);
    }
    
    $affected_rows = $stmt_delete->affected_rows;
    $stmt_delete->close();
    
    // Log de la suppression
    logError("Position supprimée", [
        "id" => $id,
        "data" => $deleted_data,
        "affected_rows" => $affected_rows
    ]);
    
    // Retourner la réponse de succès
    echo json_encode([
        "success" => true,
        "message" => "Position supprimée avec succès",
        "deleted" => $deleted_data,
        "affected_rows" => $affected_rows,
        "timestamp" => time()
    ], JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    // Log l'erreur
    logError("Erreur dans delete_position.php", [
        "error" => $e->getMessage(),
        "file" => $e->getFile(),
        "line" => $e->getLine(),
        "id" => $id ?? 'null'
    ]);
    
    // Retourner l'erreur en JSON
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de la suppression de la position",
        "error" => $e->getMessage(),
        "timestamp" => time()
    ]);
}

// Fermer la connexion
if ($conn) {
    $conn->close();
}
?>

