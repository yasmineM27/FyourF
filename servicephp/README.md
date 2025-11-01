# 📂 ServicePHP - FyourF Location Tracking

## 📋 Description
Ce dossier contient les scripts PHP pour gérer la base de données MySQL de l'application FyourF.

## 📁 Structure des fichiers

```
servicephp/
├── config.php          - Configuration de la connexion MySQL
├── get_all.php         - Récupère toutes les positions
├── add_position.php    - Ajoute/met à jour une position
├── test.php            - Interface de test web
└── README.md           - Ce fichier
```

## 🚀 Installation

### Étape 1: Copier le dossier

Copier ce dossier `servicephp` dans le répertoire web de votre serveur:

- **XAMPP (Windows):** `C:\xampp\htdocs\servicephp\`
- **WAMP (Windows):** `C:\wamp64\www\servicephp\`
- **LAMP (Linux):** `/var/www/html/servicephp/`
- **MAMP (Mac):** `/Applications/MAMP/htdocs/servicephp/`

### Étape 2: Configurer la base de données

1. Ouvrir phpMyAdmin: `http://localhost/phpmyadmin`
2. Créer la base de données `locationdatabase`
3. Exécuter le script SQL fourni dans `database_setup.sql`

### Étape 3: Configurer config.php

Ouvrir `config.php` et modifier si nécessaire:

```php
$user = "root";                    // Utilisateur MySQL
$mp = "";                          // Mot de passe (vide par défaut)
$database = "locationdatabase";    // Nom de la base
$server = "localhost";             // Serveur
$port = "3306";                    // Port
```

### Étape 4: Tester l'installation

Ouvrir dans le navigateur:
```
http://localhost/servicephp/test.php
```

Vous devriez voir une interface de test avec le statut de connexion.

## 🧪 Tests

### Test local (depuis votre PC)

1. **Test get_all.php:**
   ```
   http://localhost/servicephp/get_all.php
   ```
   
   Réponse attendue:
   ```json
   {
     "success": true,
     "count": 0,
     "data": [],
     "message": "Aucune position trouvée",
     "timestamp": 1234567890
   }
   ```

2. **Test add_position.php:**
   ```
   http://localhost/servicephp/add_position.php?longitude=10.1815&latitude=36.8065&numero=+21612345678&pseudo=Test
   ```
   
   Réponse attendue:
   ```json
   {
     "success": true,
     "message": "Position ajoutée avec succès",
     "action": "insert",
     "data": {
       "idposition": 1,
       "longitude": 10.1815,
       "latitude": 36.8065,
       "numero": "+21612345678",
       "pseudo": "Test",
       "timestamp": "1234567890000"
     },
     "timestamp": 1234567890
   }
   ```

### Test depuis le smartphone

1. **Trouver votre IPv4:**
   - Windows: Ouvrir CMD et taper `ipconfig`
   - Chercher "Adresse IPv4" (ex: 192.168.1.100)

2. **Configurer le pare-feu:**
   - Autoriser le port 80 dans le pare-feu Windows
   - Voir le guide complet dans `MYSQL_MIGRATION_GUIDE.md`

3. **Tester depuis le smartphone:**
   - Connecter le smartphone au même WiFi que votre PC
   - Ouvrir le navigateur du smartphone
   - Tester: `http://192.168.1.100/servicephp/test.php`

## 📡 API Documentation

### GET /get_all.php

Récupère toutes les positions de la base de données.

**Paramètres (optionnels):**
- `limit` (int): Nombre maximum de résultats
- `numero` (string): Filtrer par numéro de téléphone

**Exemples:**
```
GET /servicephp/get_all.php
GET /servicephp/get_all.php?limit=10
GET /servicephp/get_all.php?numero=+21612345678
```

**Réponse:**
```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "idposition": 1,
      "longitude": 10.1815,
      "latitude": 36.8065,
      "numero": "+21612345678",
      "pseudo": "User1",
      "timestamp": "1234567890000",
      "created_at": "2025-10-31 10:30:00",
      "updated_at": "2025-10-31 10:30:00"
    }
  ],
  "message": "Positions récupérées avec succès",
  "timestamp": 1234567890
}
```

### POST /add_position.php

Ajoute ou met à jour une position.

**Paramètres (requis):**
- `longitude` (double): Longitude GPS
- `latitude` (double): Latitude GPS
- `numero` (string): Numéro de téléphone

**Paramètres (optionnels):**
- `pseudo` (string): Pseudo de l'utilisateur
- `timestamp` (bigint): Timestamp en millisecondes

**Exemples:**
```
POST /servicephp/add_position.php
Body: longitude=10.1815&latitude=36.8065&numero=+21612345678&pseudo=User1

GET /servicephp/add_position.php?longitude=10.1815&latitude=36.8065&numero=+21612345678
```

**Réponse (insert):**
```json
{
  "success": true,
  "message": "Position ajoutée avec succès",
  "action": "insert",
  "data": {
    "idposition": 1,
    "longitude": 10.1815,
    "latitude": 36.8065,
    "numero": "+21612345678",
    "pseudo": "User1",
    "timestamp": "1234567890000"
  },
  "timestamp": 1234567890
}
```

**Réponse (update):**
```json
{
  "success": true,
  "message": "Position mise à jour avec succès",
  "action": "update",
  "data": { ... },
  "timestamp": 1234567890
}
```

## 🔧 Dépannage

### Erreur "Connection refused"
- Vérifier que Apache est démarré dans XAMPP/WAMP
- Vérifier le pare-feu Windows
- Vérifier que le smartphone est sur le même réseau WiFi

### Erreur "Access denied for user"
- Vérifier les identifiants dans `config.php`
- Vérifier que MySQL est démarré

### Erreur "Table doesn't exist"
- Vérifier que la base de données `locationdatabase` existe
- Exécuter le script `database_setup.sql`

### Pas de données retournées
- Vérifier que des données existent: `SELECT * FROM Position;`
- Vérifier les logs PHP: `C:\xampp\apache\logs\error.log`

### Erreur CORS (depuis l'application)
- Les headers CORS sont déjà configurés dans `config.php`
- Vérifier que `Access-Control-Allow-Origin: *` est présent

## 📝 Logs

Les erreurs PHP sont enregistrées dans:
- **XAMPP:** `C:\xampp\apache\logs\error.log`
- **WAMP:** `C:\wamp64\logs\apache_error.log`
- **Fichier local:** `servicephp/php_errors.log`

## 🔒 Sécurité

**ATTENTION:** Cette configuration est pour le développement uniquement!

Pour la production:
1. Créer un utilisateur MySQL dédié (pas root)
2. Utiliser un mot de passe fort
3. Limiter les CORS à votre domaine
4. Utiliser HTTPS
5. Valider et nettoyer toutes les entrées
6. Implémenter l'authentification

## 📞 Support

Pour toute question, consulter:
- `MYSQL_MIGRATION_GUIDE.md` - Guide complet de migration
- `database_setup.sql` - Script de création de la base
- Logs PHP pour les erreurs détaillées

## ✅ Checklist

- [ ] Dossier copié dans htdocs/www
- [ ] Base de données créée
- [ ] Table Position créée
- [ ] config.php configuré
- [ ] Test local réussi (localhost)
- [ ] IPv4 identifiée
- [ ] Pare-feu configuré
- [ ] Test smartphone réussi
- [ ] Application Android configurée

---

**Version:** 1.0  
**Date:** 2025-10-31  
**Application:** FyourF Location Tracking

