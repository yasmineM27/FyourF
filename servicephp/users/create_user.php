<?php
/**
 * 👤 create_user.php - Créer un nouvel utilisateur
 */

header('Content-Type: application/json; charset=utf-8');

require_once '../config.php';

try {
    // Récupérer les données
    $data = json_decode(file_get_contents("php://input"), true);
    
    if (!$data) {
        throw new Exception("Données invalides");
    }

    $numero = $data['numero'] ?? null;
    $pseudo = $data['pseudo'] ?? null;
    $email = $data['email'] ?? null;
    $phone = $data['phone'] ?? null;

    if (!$numero || !$pseudo) {
        throw new Exception("Numéro et pseudo requis");
    }

    // Vérifier si l'utilisateur existe déjà
    $checkStmt = $conn->prepare("SELECT id FROM users WHERE numero = ?");
    $checkStmt->bind_param("s", $numero);
    $checkStmt->execute();
    $result = $checkStmt->get_result();

    if ($result->num_rows > 0) {
        throw new Exception("Utilisateur déjà existant");
    }

    // Créer l'utilisateur
    $stmt = $conn->prepare("
        INSERT INTO users (numero, pseudo, email, phone, status)
        VALUES (?, ?, ?, ?, 'online')
    ");
    
    $stmt->bind_param("ssss", $numero, $pseudo, $email, $phone);
    
    if (!$stmt->execute()) {
        throw new Exception("Erreur création utilisateur: " . $stmt->error);
    }

    $userId = $conn->insert_id;

    echo json_encode([
        "success" => true,
        "message" => "Utilisateur créé avec succès",
        "user_id" => $userId,
        "user" => [
            "id" => $userId,
            "numero" => $numero,
            "pseudo" => $pseudo,
            "email" => $email,
            "phone" => $phone,
            "status" => "online"
        ]
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

