# Configuration Supabase pour FyourF

## 📋 Table des matières
1. [Configuration de la base de données](#1-configuration-de-la-base-de-données)
2. [Script SQL à exécuter](#2-script-sql-à-exécuter)
3. [Configuration de l'application](#3-configuration-de-lapplication)
4. [Test de l'intégration](#4-test-de-lintégration)
5. [Dépannage](#5-dépannage)

---

## 1. Configuration de la base de données

### Étape 1 : Accéder à votre projet Supabase
1. Allez sur [https://supabase.com/dashboard](https://supabase.com/dashboard)
2. Sélectionnez votre projet : `skbttjztscyebsrvghqu`
3. Cliquez sur l'icône SQL Editor dans le menu de gauche

### Étape 2 : Vérifier les informations de connexion
- **URL du projet** : `https://skbttjztscyebsrvghqu.supabase.co`
- **Anon Key** : Déjà configurée dans `Config.java`
- **Service Role Key** : Déjà configurée dans `Config.java`

---

## 2. Script SQL à exécuter

Copiez et exécutez ce script dans l'éditeur SQL de Supabase :

```sql
-- ============================================
-- Script de création de la table location_history
-- ============================================

-- Supprimer la table si elle existe déjà (optionnel)
DROP TABLE IF EXISTS location_history;

-- Créer la table location_history
CREATE TABLE location_history (
    id BIGSERIAL PRIMARY KEY,
    phone TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Créer un index sur le numéro de téléphone pour des recherches rapides
CREATE INDEX idx_location_history_phone ON location_history(phone);

-- Créer un index sur le timestamp pour trier par date
CREATE INDEX idx_location_history_timestamp ON location_history(timestamp DESC);

-- Créer un index unique pour éviter les doublons de téléphone
CREATE UNIQUE INDEX idx_location_history_phone_unique ON location_history(phone);

-- Activer Row Level Security (RLS)
ALTER TABLE location_history ENABLE ROW LEVEL SECURITY;

-- Supprimer les anciennes politiques si elles existent
DROP POLICY IF EXISTS "Allow public read access" ON location_history;
DROP POLICY IF EXISTS "Allow public insert access" ON location_history;
DROP POLICY IF EXISTS "Allow public update access" ON location_history;
DROP POLICY IF EXISTS "Allow public delete access" ON location_history;

-- Créer une politique pour permettre la lecture avec la clé anon
CREATE POLICY "Allow public read access" ON location_history
    FOR SELECT
    USING (true);

-- Créer une politique pour permettre l'insertion avec la clé anon
CREATE POLICY "Allow public insert access" ON location_history
    FOR INSERT
    WITH CHECK (true);

-- Créer une politique pour permettre la mise à jour avec la clé anon
CREATE POLICY "Allow public update access" ON location_history
    FOR UPDATE
    USING (true);

-- Créer une politique pour permettre la suppression avec la clé anon
CREATE POLICY "Allow public delete access" ON location_history
    FOR DELETE
    USING (true);

-- Insérer des données de test
INSERT INTO location_history (phone, latitude, longitude, timestamp)
VALUES 
    ('+1234567890', 36.8065, 10.1815, EXTRACT(EPOCH FROM NOW()) * 1000),
    ('+0987654321', 36.8500, 10.2000, EXTRACT(EPOCH FROM NOW()) * 1000);

-- Vérifier que les données ont été insérées
SELECT * FROM location_history;
```

### Résultat attendu
Après l'exécution, vous devriez voir :
- ✅ Table `location_history` créée
- ✅ 3 index créés
- ✅ RLS activé avec 4 politiques
- ✅ 2 lignes de données de test insérées

---

## 3. Configuration de l'application

### Fichiers modifiés

#### ✅ `Config.java`
```java
public static final String SUPABASE_URL = "https://skbttjztscyebsrvghqu.supabase.co";
public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
public static final String SUPABASE_SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
```

#### ✅ `SupabaseLocationService.java`
- Utilise maintenant `Config.SUPABASE_URL` et `Config.SUPABASE_ANON_KEY`
- Méthodes disponibles :
  - `addOrUpdateLocation(phone, lat, lon)` - Ajouter/mettre à jour une localisation
  - `getAllLocations()` - Récupérer toutes les localisations

#### ✅ `JsonParser.java`
- Nouvelles méthodes pour parser les réponses JSON :
  - `parseLocationHistory(jsonResponse)` - Parser un tableau de localisations
  - `parseLocationEntry(jsonObject)` - Parser une seule localisation
  - `createLocationJson(phone, lat, lon)` - Créer un JSON pour l'insertion
  - `isValidJsonArray(jsonResponse)` - Valider un JSON

#### ✅ Dépendances Gradle
Ajoutées dans `gradle/libs.versions.toml` et `app/build.gradle.kts` :
- Supabase Postgrest
- Supabase Realtime
- Ktor Client
- Kotlinx Coroutines
- Kotlinx Serialization

---

## 4. Test de l'intégration

### Option 1 : Utiliser l'activité de test (Recommandé)

1. **Ajouter l'activité dans AndroidManifest.xml** :
```xml
<activity
    android:name=".SupabaseTestActivity"
    android:exported="true"
    android:label="Supabase Test">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

2. **Lancer l'application** et sélectionner "Supabase Test"

3. **Tester les fonctionnalités** :
   - Cliquez sur "Test Connection" pour vérifier la connexion
   - Cliquez sur "Test Insert" pour insérer des données de test
   - Cliquez sur "Test Fetch" pour récupérer les données

### Option 2 : Test manuel avec code

Dans n'importe quelle activité, ajoutez :

```java
// Initialiser le service
SupabaseLocationService supabaseService = new SupabaseLocationService();

// Insérer une localisation
supabaseService.addOrUpdateLocation("+1234567890", 36.8065, 10.1815);

// Récupérer toutes les localisations
supabaseService.getAllLocations().thenAccept(locations -> {
    for (SupabaseLocationService.LocationEntry location : locations) {
        Log.d("Supabase", "Phone: " + location.phone);
        Log.d("Supabase", "Lat: " + location.latitude + ", Lon: " + location.longitude);
    }
});
```

### Option 3 : Test avec l'API REST directement

Utilisez Postman ou curl :

```bash
# Récupérer toutes les localisations
curl -X GET "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Authorization: Bearer YOUR_ANON_KEY"

# Insérer une localisation
curl -X POST "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Authorization: Bearer YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+1234567890",
    "latitude": 36.8065,
    "longitude": 10.1815,
    "timestamp": 1234567890000
  }'
```

---

## 5. Dépannage

### Erreur : "Failed to connect to Supabase"
- ✅ Vérifiez que l'URL est correcte : `https://skbttjztscyebsrvghqu.supabase.co`
- ✅ Vérifiez que la clé API est correcte
- ✅ Vérifiez votre connexion Internet
- ✅ Vérifiez que `android:usesCleartextTraffic="true"` est dans AndroidManifest.xml

### Erreur : "Table does not exist"
- ✅ Exécutez le script SQL dans l'éditeur SQL de Supabase
- ✅ Vérifiez que la table `location_history` existe dans la section "Table Editor"

### Erreur : "Permission denied"
- ✅ Vérifiez que RLS est activé
- ✅ Vérifiez que les politiques sont créées correctement
- ✅ Essayez avec la clé `service_role` au lieu de `anon` (pour le debug uniquement)

### Erreur : "JSON parsing error"
- ✅ Vérifiez que la réponse de Supabase est bien un JSON valide
- ✅ Utilisez `JsonParser.isValidJsonArray()` pour valider
- ✅ Vérifiez les logs avec `Log.d("JsonParser", ...)`

### Les données ne s'affichent pas
- ✅ Vérifiez que les données existent dans Supabase (Table Editor)
- ✅ Vérifiez les logs Logcat pour voir les erreurs
- ✅ Testez avec l'activité de test `SupabaseTestActivity`

---

## 📊 Structure de la table

| Colonne      | Type                  | Description                          |
|--------------|-----------------------|--------------------------------------|
| id           | BIGSERIAL (PK)        | Identifiant unique auto-incrémenté   |
| phone        | TEXT (UNIQUE)         | Numéro de téléphone                  |
| latitude     | DOUBLE PRECISION      | Latitude GPS                         |
| longitude    | DOUBLE PRECISION      | Longitude GPS                        |
| timestamp    | BIGINT                | Timestamp en millisecondes           |
| created_at   | TIMESTAMP             | Date de création (auto)              |

---

## 🎯 Prochaines étapes

1. ✅ Exécuter le script SQL dans Supabase
2. ✅ Synchroniser Gradle pour télécharger les dépendances
3. ✅ Tester avec `SupabaseTestActivity`
4. ✅ Intégrer dans `SmsReceiver` pour sauvegarder les localisations reçues par SMS
5. ✅ Intégrer dans `HomeFragment` pour afficher les localisations depuis Supabase

---

## 📞 Support

Si vous rencontrez des problèmes :
1. Vérifiez les logs Logcat
2. Vérifiez la console Supabase pour les erreurs
3. Testez l'API REST directement avec curl/Postman
4. Vérifiez que toutes les dépendances sont bien téléchargées

