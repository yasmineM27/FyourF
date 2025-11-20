<?php
/**
 * 🔗 get_connections.php - Récupérer les connexions d'un utilisateur
 */

header('Content-Type: application/json; charset=utf-8');

require_once '../config.php';

try {
    $user_id = $_GET['user_id'] ?? null;
    $status = $_GET['status'] ?? 'connected';

    if (!$user_id) {
        throw new Exception("ID utilisateur requis");
    }

    // Récupérer les amis connectés
    $stmt = $conn->prepare("
        SELECT 
            u.id, u.numero, u.pseudo, u.email, u.phone, u.status, u.last_seen
        FROM user_connections uc
        JOIN users u ON (
            (uc.user1_id = ? AND uc.user2_id = u.id) OR
            (uc.user2_id = ? AND uc.user1_id = u.id)
        )
        WHERE uc.status = ?
        ORDER BY u.pseudo ASC
    ");
    
    $stmt->bind_param("iss", $user_id, $user_id, $status);
    $stmt->execute();
    $result = $stmt->get_result();

    $friends = [];
    while ($row = $result->fetch_assoc()) {
        $friends[] = $row;
    }

    echo json_encode([
        "success" => true,
        "friends" => $friends,
        "count" => count($friends)
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

