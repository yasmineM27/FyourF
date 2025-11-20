<?php
/**
 * 👤 update_user.php - Mettre à jour un utilisateur
 */

header('Content-Type: application/json; charset=utf-8');

require_once '../config.php';

try {
    $data = json_decode(file_get_contents("php://input"), true);
    
    if (!$data) {
        throw new Exception("Données invalides");
    }

    $id = $data['id'] ?? null;
    if (!$id) {
        throw new Exception("ID utilisateur requis");
    }

    // Construire la requête dynamiquement
    $updates = [];
    $params = [];
    $types = "";

    if (isset($data['pseudo'])) {
        $updates[] = "pseudo = ?";
        $params[] = $data['pseudo'];
        $types .= "s";
    }

    if (isset($data['email'])) {
        $updates[] = "email = ?";
        $params[] = $data['email'];
        $types .= "s";
    }

    if (isset($data['phone'])) {
        $updates[] = "phone = ?";
        $params[] = $data['phone'];
        $types .= "s";
    }

    if (isset($data['status'])) {
        $updates[] = "status = ?";
        $params[] = $data['status'];
        $types .= "s";
    }

    if (empty($updates)) {
        throw new Exception("Aucune donnée à mettre à jour");
    }

    $params[] = $id;
    $types .= "i";

    $query = "UPDATE users SET " . implode(", ", $updates) . " WHERE id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param($types, ...$params);

    if (!$stmt->execute()) {
        throw new Exception("Erreur mise à jour: " . $stmt->error);
    }

    echo json_encode([
        "success" => true,
        "message" => "Utilisateur mis à jour avec succès",
        "affected_rows" => $stmt->affected_rows
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

