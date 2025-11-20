<?php
/**
 * 👤 get_user.php - Récupérer un utilisateur
 */

header('Content-Type: application/json; charset=utf-8');

require_once '../config.php';

try {
    $numero = $_GET['numero'] ?? null;
    $id = $_GET['id'] ?? null;

    if (!$numero && !$id) {
        throw new Exception("Numéro ou ID requis");
    }

    if ($numero) {
        $stmt = $conn->prepare("
            SELECT id, numero, pseudo, email, phone, status, last_seen, created_at
            FROM users
            WHERE numero = ?
        ");
        $stmt->bind_param("s", $numero);
    } else {
        $stmt = $conn->prepare("
            SELECT id, numero, pseudo, email, phone, status, last_seen, created_at
            FROM users
            WHERE id = ?
        ");
        $stmt->bind_param("i", $id);
    }

    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("Utilisateur non trouvé");
    }

    $user = $result->fetch_assoc();

    echo json_encode([
        "success" => true,
        "user" => $user
    ]);

} catch (Exception $e) {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ]);
}

$conn->close();
?>

