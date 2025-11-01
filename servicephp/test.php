<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test API FyourF</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            min-height: 100vh;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }
        h1 {
            color: #333;
            margin-bottom: 10px;
            text-align: center;
        }
        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }
        .section {
            margin-bottom: 30px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
            border-left: 4px solid #667eea;
        }
        .section h2 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.3em;
        }
        .test-btn {
            background: #667eea;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            margin: 5px;
            transition: all 0.3s;
        }
        .test-btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        .result {
            margin-top: 15px;
            padding: 15px;
            background: white;
            border-radius: 5px;
            border: 1px solid #ddd;
            font-family: 'Courier New', monospace;
            font-size: 14px;
            max-height: 400px;
            overflow-y: auto;
            white-space: pre-wrap;
            word-wrap: break-word;
        }
        .success {
            border-left: 4px solid #28a745;
            background: #d4edda;
        }
        .error {
            border-left: 4px solid #dc3545;
            background: #f8d7da;
        }
        .info {
            background: #d1ecf1;
            border-left: 4px solid #17a2b8;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
        }
        .form-group {
            margin-bottom: 15px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            color: #333;
            font-weight: 500;
        }
        .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }
        .status {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 3px;
            font-size: 12px;
            font-weight: bold;
        }
        .status.ok {
            background: #28a745;
            color: white;
        }
        .status.fail {
            background: #dc3545;
            color: white;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧪 Test API FyourF - MySQL</h1>
        <p class="subtitle">Interface de test pour les services PHP</p>

        <?php
        // Tester la connexion à la base de données
        $dbStatus = "fail";
        $dbMessage = "";
        try {
            require_once 'config.php';
            $dbStatus = "ok";
            $dbMessage = "Connexion réussie à la base de données";
        } catch (Exception $e) {
            $dbMessage = "Erreur: " . $e->getMessage();
        }
        ?>

        <div class="info">
            <strong>📊 Statut de la connexion:</strong> 
            <span class="status <?php echo $dbStatus; ?>">
                <?php echo $dbStatus === 'ok' ? '✓ CONNECTÉ' : '✗ ERREUR'; ?>
            </span>
            <br>
            <small><?php echo $dbMessage; ?></small>
        </div>

        <!-- Section 1: Test GET ALL -->
        <div class="section">
            <h2>📥 Test GET ALL - Récupérer toutes les positions</h2>
            <button class="test-btn" onclick="testGetAll()">🔄 Tester get_all.php</button>
            <button class="test-btn" onclick="testGetAll(10)">🔄 Tester avec limite (10)</button>
            <div id="result-getall" class="result" style="display:none;"></div>
        </div>

        <!-- Section 2: Test ADD POSITION -->
        <div class="section">
            <h2>📤 Test ADD POSITION - Ajouter une position</h2>
            <div class="form-group">
                <label>Longitude:</label>
                <input type="number" step="0.000001" id="longitude" value="10.1815" placeholder="10.1815">
            </div>
            <div class="form-group">
                <label>Latitude:</label>
                <input type="number" step="0.000001" id="latitude" value="36.8065" placeholder="36.8065">
            </div>
            <div class="form-group">
                <label>Numéro:</label>
                <input type="text" id="numero" value="+21612345678" placeholder="+21612345678">
            </div>
            <div class="form-group">
                <label>Pseudo (optionnel):</label>
                <input type="text" id="pseudo" value="TestUser" placeholder="TestUser">
            </div>
            <button class="test-btn" onclick="testAddPosition()">➕ Ajouter Position</button>
            <button class="test-btn" onclick="testAddRandom()">🎲 Ajouter Position Aléatoire</button>
            <div id="result-add" class="result" style="display:none;"></div>
        </div>

        <!-- Section 3: Informations -->
        <div class="section">
            <h2>ℹ️ Informations</h2>
            <p><strong>URL de base:</strong> <?php echo 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']); ?></p>
            <p><strong>get_all.php:</strong> <?php echo 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/get_all.php'; ?></p>
            <p><strong>add_position.php:</strong> <?php echo 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/add_position.php'; ?></p>
        </div>
    </div>

    <script>
        const baseUrl = window.location.href.replace('test.php', '');

        function showResult(elementId, data, isSuccess) {
            const element = document.getElementById(elementId);
            element.style.display = 'block';
            element.className = 'result ' + (isSuccess ? 'success' : 'error');
            element.textContent = JSON.stringify(data, null, 2);
        }

        async function testGetAll(limit = null) {
            try {
                const url = limit ? `${baseUrl}get_all.php?limit=${limit}` : `${baseUrl}get_all.php`;
                const response = await fetch(url);
                const data = await response.json();
                showResult('result-getall', data, data.success);
            } catch (error) {
                showResult('result-getall', { error: error.message }, false);
            }
        }

        async function testAddPosition() {
            try {
                const longitude = document.getElementById('longitude').value;
                const latitude = document.getElementById('latitude').value;
                const numero = document.getElementById('numero').value;
                const pseudo = document.getElementById('pseudo').value;
                
                const url = `${baseUrl}add_position.php?longitude=${longitude}&latitude=${latitude}&numero=${encodeURIComponent(numero)}&pseudo=${encodeURIComponent(pseudo)}`;
                const response = await fetch(url);
                const data = await response.json();
                showResult('result-add', data, data.success);
                
                // Rafraîchir la liste si succès
                if (data.success) {
                    setTimeout(() => testGetAll(), 500);
                }
            } catch (error) {
                showResult('result-add', { error: error.message }, false);
            }
        }

        async function testAddRandom() {
            // Générer des coordonnées aléatoires autour de Tunis
            const longitude = (10.1815 + (Math.random() - 0.5) * 0.1).toFixed(6);
            const latitude = (36.8065 + (Math.random() - 0.5) * 0.1).toFixed(6);
            const numero = '+216' + Math.floor(10000000 + Math.random() * 90000000);
            const pseudo = 'User' + Math.floor(Math.random() * 1000);
            
            document.getElementById('longitude').value = longitude;
            document.getElementById('latitude').value = latitude;
            document.getElementById('numero').value = numero;
            document.getElementById('pseudo').value = pseudo;
            
            testAddPosition();
        }

        // Charger les données au démarrage
        window.onload = function() {
            testGetAll();
        };
    </script>
</body>
</html>

