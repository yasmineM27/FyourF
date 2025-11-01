# 🧪 Test des Endpoints PHP

## 📋 Prérequis

- Apache et MySQL démarrés
- Base de données `fyourf_db` créée
- Fichiers PHP dans htdocs/www/servicephp/
- Remplacer `192.168.1.100` par votre IP

---

## 1️⃣ Test GET ALL (Récupérer toutes les positions)

### Navigateur
```
http://192.168.1.100/servicephp/get_all.php
```

### cURL
```bash
curl http://192.168.1.100/servicephp/get_all.php
```

### Réponse Attendue
```json
{
  "success": true,
  "positions": [
    {
      "idposition": 1,
      "longitude": 10.1815,
      "latitude": 36.8065,
      "numero": "+21612345678",
      "pseudo": "TestUser",
      "timestamp": 1234567890000,
      "created_at": "2024-01-01 12:00:00",
      "updated_at": "2024-01-01 12:00:00"
    }
  ]
}
```

---

## 2️⃣ Test ADD POSITION (Ajouter une position)

### cURL (POST)
```bash
curl -X POST http://192.168.1.100/servicephp/add_position.php \
  -d "longitude=10.1815" \
  -d "latitude=36.8065" \
  -d "numero=+21698765432" \
  -d "pseudo=MonPseudo" \
  -d "timestamp=1704110400000"
```

### Postman
```
Method: POST
URL: http://192.168.1.100/servicephp/add_position.php
Body (x-www-form-urlencoded):
  longitude: 10.1815
  latitude: 36.8065
  numero: +21698765432
  pseudo: MonPseudo
  timestamp: 1704110400000
```

### Réponse Attendue
```json
{
  "success": true,
  "message": "Position ajoutée avec succès",
  "idposition": 4
}
```

---

## 3️⃣ Test DELETE POSITION (Supprimer une position)

### cURL (POST)
```bash
curl -X POST http://192.168.1.100/servicephp/delete_position.php \
  -d "id=4"
```

### Postman
```
Method: POST
URL: http://192.168.1.100/servicephp/delete_position.php
Body (x-www-form-urlencoded):
  id: 4
```

### Réponse Attendue
```json
{
  "success": true,
  "message": "Position supprimée avec succès"
}
```

---

## 4️⃣ Test GET TRAJECTORY (Récupérer un trajet)

### Navigateur (Toutes les positions d'un numéro)
```
http://192.168.1.100/servicephp/get_trajectory.php?numero=+21612345678
```

### Navigateur (Avec période)
```
http://192.168.1.100/servicephp/get_trajectory.php?numero=+21612345678&start=1704110400000&end=1704196800000
```

### cURL
```bash
# Toutes les positions
curl "http://192.168.1.100/servicephp/get_trajectory.php?numero=+21612345678"

# Avec période
curl "http://192.168.1.100/servicephp/get_trajectory.php?numero=+21612345678&start=1704110400000&end=1704196800000"
```

### Réponse Attendue
```json
{
  "success": true,
  "message": "Trajet récupéré avec succès",
  "positions": [
    {
      "idposition": 1,
      "longitude": 10.1815,
      "latitude": 36.8065,
      "numero": "+21612345678",
      "pseudo": "TestUser",
      "timestamp": 1704110400000,
      "created_at": "2024-01-01 12:00:00",
      "updated_at": "2024-01-01 12:00:00"
    },
    {
      "idposition": 2,
      "longitude": 10.1820,
      "latitude": 36.8070,
      "numero": "+21612345678",
      "pseudo": "TestUser",
      "timestamp": 1704110430000,
      "created_at": "2024-01-01 12:00:30",
      "updated_at": "2024-01-01 12:00:30"
    }
  ],
  "stats": {
    "total_positions": 2,
    "numero": "+21612345678",
    "start_time": 0,
    "end_time": 0,
    "first_position": {...},
    "last_position": {...},
    "duration_ms": 30000,
    "total_distance_km": 0.65
  }
}
```

---

## 🔍 Vérification dans MySQL

### Voir toutes les positions
```sql
USE fyourf_db;
SELECT * FROM positions ORDER BY timestamp DESC;
```

### Compter les positions
```sql
SELECT COUNT(*) as total FROM positions;
```

### Positions par numéro
```sql
SELECT numero, COUNT(*) as count 
FROM positions 
GROUP BY numero;
```

### Dernières positions
```sql
SELECT * FROM positions 
ORDER BY created_at DESC 
LIMIT 10;
```

---

## ❌ Erreurs Courantes

### Erreur: "Connection failed"
```
✅ Vérifier que MySQL est démarré
✅ Vérifier config.php (DB_HOST, DB_USER, DB_PASS, DB_NAME)
✅ Tester: mysql -u root -p
```

### Erreur: "Table 'positions' doesn't exist"
```
✅ Exécuter database_simple.sql
✅ Vérifier: USE fyourf_db; SHOW TABLES;
```

### Erreur: "Access denied for user"
```
✅ Vérifier le mot de passe dans config.php
✅ Vérifier les permissions MySQL
```

### Erreur 404: "Not Found"
```
✅ Vérifier que les fichiers sont dans htdocs/servicephp/
✅ Vérifier l'URL (IP correcte)
✅ Vérifier que Apache est démarré
```

---

## 📊 Script de Test Complet

### Bash Script (Linux/Mac)
```bash
#!/bin/bash

IP="192.168.1.100"
BASE_URL="http://$IP/servicephp"

echo "🧪 Test des Endpoints FyourF"
echo "=============================="

echo ""
echo "1️⃣ Test GET ALL"
curl -s "$BASE_URL/get_all.php" | python -m json.tool

echo ""
echo "2️⃣ Test ADD POSITION"
curl -s -X POST "$BASE_URL/add_position.php" \
  -d "longitude=10.1815" \
  -d "latitude=36.8065" \
  -d "numero=+21698765432" \
  -d "pseudo=TestScript" \
  -d "timestamp=$(date +%s)000" | python -m json.tool

echo ""
echo "3️⃣ Test GET TRAJECTORY"
curl -s "$BASE_URL/get_trajectory.php?numero=+21698765432" | python -m json.tool

echo ""
echo "✅ Tests terminés!"
```

### PowerShell Script (Windows)
```powershell
$IP = "192.168.1.100"
$BASE_URL = "http://$IP/servicephp"

Write-Host "🧪 Test des Endpoints FyourF" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green

Write-Host "`n1️⃣ Test GET ALL" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/get_all.php" | ConvertTo-Json

Write-Host "`n2️⃣ Test ADD POSITION" -ForegroundColor Yellow
$body = @{
    longitude = 10.1815
    latitude = 36.8065
    numero = "+21698765432"
    pseudo = "TestScript"
    timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
}
Invoke-RestMethod -Uri "$BASE_URL/add_position.php" -Method POST -Body $body | ConvertTo-Json

Write-Host "`n3️⃣ Test GET TRAJECTORY" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/get_trajectory.php?numero=+21698765432" | ConvertTo-Json

Write-Host "`n✅ Tests terminés!" -ForegroundColor Green
```

---

## 📱 Test depuis Android

### Logcat Filter
```
TrackingService|MySQLLocationService|Loading
```

### Vérifier les logs
```
D/MySQLLocationService: ✓ Position ajoutée: ID=5
D/TrackingService: Position sauvegardée: 36.8065, 10.1815
D/Loading: ✓ Positions chargées: 10
```

---

## ✅ Checklist de Test

- [ ] GET ALL fonctionne
- [ ] ADD POSITION fonctionne
- [ ] DELETE POSITION fonctionne
- [ ] GET TRAJECTORY fonctionne
- [ ] Les données apparaissent dans MySQL
- [ ] L'app Android peut se connecter
- [ ] Le tracking enregistre les positions
- [ ] L'historique affiche les positions

---

**Tous les tests passent ? Vous êtes prêt ! 🎉**

