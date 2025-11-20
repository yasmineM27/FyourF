<?php
/**
 * 🔗 add_connection.php - Ajouter une connexion entre deux utilisateurs
 */

header('Content-Type: application/json; charset=utf-8');

require_once '../config.php';

try {
    $data = json_decode(file_get_contents("php://input"), true);
    
    if (!$data) {
        throw new Exception("Données invalides");
    }

    $user1_id = $data['user1_id'] ?? null;
    $user2_id = $data['user2_id'] ?? null;
    $status = $data['status'] ?? 'pending';

    if (!$user1_id || !$user2_id) {
        throw new Exception("IDs utilisateurs requis");
    }

    if ($user1_id === $user2_id) {
        throw new Exception("Impossible de se connecter à soi-même");
    }

    // Vérifier que les deux utilisateurs existent
    $checkStmt = $conn->prepare("SELECT id FROM users WHERE id IN (?, ?)");
    $checkStmt->bind_param("ii", $user1_id, $user2_id);
    $checkStmt->execute();
    $result = $checkStmt->get_result();

    if ($result->num_rows !== 2) {
        throw new Exception("Un ou plusieurs utilisateurs n'existent pas");
    }

    // Vérifier si la connexion existe déjà
    $existStmt = $conn->prepare("
        SELECT id FROM user_connections 
        WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)
    ");
    $existStmt->bind_param("iiii", $user1_id, $user2_id, $user2_id, $user1_id);
    $existStmt->execute();
    $existResult = $existStmt->get_result();

    if ($existResult->num_rows > 0) {
        throw new Exception("Connexion déjà existante");
    }

    // Créer la connexion
    $stmt = $conn->prepare("
        INSERT INTO user_connections (user1_id, user2_id, status)
        VALUES (?, ?, ?)
    ");
    
    $stmt->bind_param("iis", $user1_id, $user2_id, $status);
    
    if (!$stmt->execute()) {
        throw new Exception("Erreur création connexion: " . $stmt->error);
    }

    $connectionId = $conn->insert_id;

    echo json_encode([
        "success" => true,
        "message" => "Connexion créée avec succès",
        "connection_id" => $connectionId,
        "status" => $status
    ]);

} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ]);
}

$conn->close();
?>

