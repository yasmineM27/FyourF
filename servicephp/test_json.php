<?php
/**
 * Script de test simple pour vérifier la connexion MySQL
 * et afficher les données en JSON
 * 
 * URL: http://192.168.1.17/servicephp/test_json.php
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

// Informations de connexion
$server = "localhost";
$user = "root";
$password = "";
$database = "fyourf_db";
$port = 3306;

$response = [
    "test_info" => [
        "script" => "test_json.php",
        "timestamp" => date('Y-m-d H:i:s'),
        "server_ip" => $_SERVER['SERVER_ADDR'] ?? 'Unknown',
        "client_ip" => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        "user_agent" => $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown'
    ],
    "database_config" => [
        "host" => $server,
        "database" => $database,
        "port" => $port
    ],
    "tests" => []
];

// Test 1: Connexion MySQL
try {
    $conn = new mysqli($server, $user, $password, $database, $port);
    
    if ($conn->connect_error) {
        throw new Exception("Connexion échouée: " . $conn->connect_error);
    }
    
    $response["tests"][] = [
        "name" => "MySQL Connection",
        "status" => "✅ SUCCESS",
        "message" => "Connexion réussie à la base de données"
    ];
    
    // Test 2: Vérifier la table positions
    $tableCheck = $conn->query("SHOW TABLES LIKE 'positions'");
    if ($tableCheck && $tableCheck->num_rows > 0) {
        $response["tests"][] = [
            "name" => "Table 'positions' exists",
            "status" => "✅ SUCCESS",
            "message" => "La table 'positions' existe"
        ];
        
        // Test 3: Compter les positions
        $countResult = $conn->query("SELECT COUNT(*) as total FROM positions");
        if ($countResult) {
            $count = $countResult->fetch_assoc()['total'];
            $response["tests"][] = [
                "name" => "Count positions",
                "status" => "✅ SUCCESS",
                "message" => "Nombre de positions: " . $count,
                "count" => (int)$count
            ];
            
            // Test 4: Récupérer toutes les positions
            $sql = "SELECT * FROM positions ORDER BY timestamp DESC";
            $result = $conn->query($sql);
            
            if ($result) {
                $positions = [];
                while ($row = $result->fetch_assoc()) {
                    $positions[] = [
                        "idposition" => (int)$row['idposition'],
                        "numero" => $row['numero'],
                        "pseudo" => $row['pseudo'],
                        "latitude" => (float)$row['latitude'],
                        "longitude" => (float)$row['longitude'],
                        "timestamp" => (int)$row['timestamp'],
                        "date_formatted" => date('Y-m-d H:i:s', $row['timestamp'])
                    ];
                }
                
                $response["tests"][] = [
                    "name" => "Fetch all positions",
                    "status" => "✅ SUCCESS",
                    "message" => "Positions récupérées avec succès"
                ];
                
                $response["data"] = [
                    "success" => true,
                    "count" => count($positions),
                    "positions" => $positions
                ];
            } else {
                $response["tests"][] = [
                    "name" => "Fetch all positions",
                    "status" => " ERROR",
                    "message" => "Erreur SQL: " . $conn->error
                ];
            }
        }
    } else {
        $response["tests"][] = [
            "name" => "Table 'positions' exists",
            "status" => " ERROR",
            "message" => "La table 'positions' n'existe pas"
        ];
    }
    
    // Test 5: Informations sur la base de données
    $dbInfo = $conn->query("SELECT DATABASE() as db_name, VERSION() as mysql_version");
    if ($dbInfo) {
        $info = $dbInfo->fetch_assoc();
        $response["database_info"] = [
            "current_database" => $info['db_name'],
            "mysql_version" => $info['mysql_version']
        ];
    }
    
    $conn->close();
    
} catch (Exception $e) {
    $response["tests"][] = [
        "name" => "MySQL Connection",
        "status" => "❌ ERROR",
        "message" => $e->getMessage()
    ];
    $response["error"] = true;
}

// Test 6: Configuration PHP
$response["php_info"] = [
    "version" => phpversion(),
    "extensions" => [
        "mysqli" => extension_loaded('mysqli') ? "✅ Loaded" : " Not loaded",
        "json" => extension_loaded('json') ? "✅ Loaded" : " Not loaded",
        "pdo_mysql" => extension_loaded('pdo_mysql') ? "✅ Loaded" : " Not loaded"
    ]
];

// Résumé
$successCount = 0;
$errorCount = 0;
foreach ($response["tests"] as $test) {
    if (strpos($test["status"], "SUCCESS") !== false) {
        $successCount++;
    } else {
        $errorCount++;
    }
}

$response["summary"] = [
    "total_tests" => count($response["tests"]),
    "success" => $successCount,
    "errors" => $errorCount,
    "overall_status" => $errorCount === 0 ? " ALL TESTS PASSED" : "⚠ SOME TESTS FAILED"
];

// Retourner le JSON
echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
?>

