<?php
/**
 * 👥 get_all_users.php - Récupérer tous les utilisateurs
 */

header('Content-Type: application/json; charset=utf-8');

require_once '../config.php';

try {
    $limit = $_GET['limit'] ?? 100;
    $offset = $_GET['offset'] ?? 0;

    $stmt = $conn->prepare("
        SELECT id, numero, pseudo, email, phone, status, last_seen, created_at
        FROM users
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
    ");
    
    $stmt->bind_param("ii", $limit, $offset);
    $stmt->execute();
    $result = $stmt->get_result();

    $users = [];
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }

    // Compter le total
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM users");
    $countStmt->execute();
    $countResult = $countStmt->get_result();
    $countRow = $countResult->fetch_assoc();
    $total = $countRow['total'];

    echo json_encode([
        "success" => true,
        "users" => $users,
        "total" => $total,
        "count" => count($users)
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

