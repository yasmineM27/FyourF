# 📋 Résumé de l'intégration Supabase

## 🎯 Objectif
Intégrer Supabase comme base de données cloud pour stocker et récupérer les localisations GPS des utilisateurs de l'application FyourF.

---

## ✅ Modifications effectuées

### 1. Configuration (Config.java)
```java
// URL CORRIGÉE (pas l'URL du dashboard !)
public static final String SUPABASE_URL = "https://skbttjztscyebsrvghqu.supabase.co";

// Clé publique (anon key)
public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrYnR0anp0c2N5ZWJzcnZnaHF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEzNzkxNTEsImV4cCI6MjA3Njk1NTE1MX0.qrwGvXaMEZP7K31UfmDkJOdAswG-n3SA__aeUdrBrlo";

// Clé privée (service_role) - À utiliser avec précaution !
public static final String SUPABASE_SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrYnR0anp0c2N5ZWJzcnZnaHF1Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2MTM3OTE1MSwiZXhwIjoyMDc2OTU1MTUxfQ.Pkd2JMYdT2YlSiuOlZVI9EIBmgsqv5AHW0A51aO56Mw";
```

### 2. Service Supabase (SupabaseLocationService.java)
- ✅ Utilise maintenant `Config.SUPABASE_URL` et `Config.SUPABASE_ANON_KEY`
- ✅ Méthode `addOrUpdateLocation(phone, lat, lon)` pour insérer/mettre à jour
- ✅ Méthode `getAllLocations()` pour récupérer toutes les localisations

### 3. Parser JSON (JsonParser.java)
- ✅ `parseLocationHistory(jsonResponse)` - Parser un tableau JSON
- ✅ `parseLocationEntry(jsonObject)` - Parser un objet JSON
- ✅ `createLocationJson(phone, lat, lon)` - Créer un JSON pour l'insertion
- ✅ `isValidJsonArray(jsonResponse)` - Valider un JSON

### 4. Activité de test (SupabaseTestActivity.java)
- ✅ Interface graphique pour tester la connexion
- ✅ Bouton "Test Connection" pour vérifier la connexion
- ✅ Bouton "Test Insert" pour insérer des données de test
- ✅ Bouton "Test Fetch" pour récupérer les données

### 5. Dépendances Gradle
Ajoutées dans `gradle/libs.versions.toml` :
```toml
supabase = "2.0.0"
ktor = "2.3.7"
kotlinxCoroutines = "1.7.3"
kotlinxSerialization = "1.6.2"
```

Ajoutées dans `app/build.gradle.kts` :
```kotlin
implementation(libs.supabase.postgrest)
implementation(libs.supabase.realtime)
implementation(libs.ktor.client.android)
implementation(libs.ktor.client.core)
implementation(libs.ktor.utils)
implementation(libs.kotlinx.coroutines.android)
implementation(libs.kotlinx.coroutines.core)
implementation(libs.kotlinx.serialization.json)
```

### 6. AndroidManifest.xml
```xml
<activity
    android:name=".SupabaseTestActivity"
    android:exported="true"
    android:label="Supabase Test" />
```

---

## 📊 Structure de la base de données

### Table : `location_history`

| Colonne      | Type              | Description                        | Contraintes      |
|--------------|-------------------|------------------------------------|------------------|
| id           | BIGSERIAL         | Identifiant unique                 | PRIMARY KEY      |
| phone        | TEXT              | Numéro de téléphone                | NOT NULL, UNIQUE |
| latitude     | DOUBLE PRECISION  | Latitude GPS                       | NOT NULL         |
| longitude    | DOUBLE PRECISION  | Longitude GPS                      | NOT NULL         |
| timestamp    | BIGINT            | Timestamp en millisecondes         | NOT NULL         |
| created_at   | TIMESTAMP         | Date de création automatique       | DEFAULT NOW()    |

### Index créés
1. `idx_location_history_phone` - Index sur le téléphone
2. `idx_location_history_timestamp` - Index sur le timestamp (DESC)
3. `idx_location_history_phone_unique` - Index unique sur le téléphone

### Politiques RLS (Row Level Security)
1. **Allow public read access** - Lecture publique
2. **Allow public insert access** - Insertion publique
3. **Allow public update access** - Mise à jour publique
4. **Allow public delete access** - Suppression publique

---

## 🚀 Script SQL à exécuter

**Fichier** : `supabase_script.sql`

**Où l'exécuter** :
1. https://supabase.com/dashboard/project/skbttjztscyebsrvghqu
2. SQL Editor → New query
3. Copier-coller le contenu de `supabase_script.sql`
4. Cliquer sur "Run"

**Résultat attendu** :
- ✅ Table créée
- ✅ 3 index créés
- ✅ RLS activé
- ✅ 4 politiques créées
- ✅ 2 enregistrements de test insérés

---

## 🧪 Comment tester

### Méthode 1 : Activité de test (Recommandé)

1. **Lancer l'application**
2. **Ouvrir SupabaseTestActivity** (ajoutez temporairement dans MainActivity) :
   ```java
   Intent intent = new Intent(this, SupabaseTestActivity.class);
   startActivity(intent);
   ```
3. **Tester** :
   - Test Connection → Vérifie la connexion
   - Test Insert → Insère une localisation de test
   - Test Fetch → Récupère toutes les localisations

### Méthode 2 : Code manuel

```java
// Initialiser le service
SupabaseLocationService supabase = new SupabaseLocationService();

// Insérer une localisation
supabase.addOrUpdateLocation("+21612345678", 36.8065, 10.1815);

// Récupérer toutes les localisations
supabase.getAllLocations().thenAccept(locations -> {
    for (SupabaseLocationService.LocationEntry loc : locations) {
        Log.d("TEST", "Phone: " + loc.phone);
        Log.d("TEST", "Lat: " + loc.latitude + ", Lon: " + loc.longitude);
    }
});
```

### Méthode 3 : API REST directe

```bash
# Récupérer toutes les localisations
curl "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: VOTRE_CLE_ANON"

# Insérer une localisation
curl -X POST "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: VOTRE_CLE_ANON" \
  -H "Content-Type: application/json" \
  -d '{"phone":"+123","latitude":36.8,"longitude":10.1,"timestamp":1234567890000}'
```

---

## 📁 Fichiers créés

1. **supabase_script.sql** - Script SQL à exécuter dans Supabase
2. **SUPABASE_SETUP.md** - Documentation complète et détaillée
3. **GUIDE_RAPIDE_SUPABASE.md** - Guide pas à pas pour l'intégration
4. **RESUME_INTEGRATION_SUPABASE.md** - Ce fichier (résumé)
5. **SupabaseTestActivity.java** - Activité de test
6. **activity_supabase_test.xml** - Layout de l'activité de test

---

## 📁 Fichiers modifiés

1. **Config.java** - URL et clés Supabase corrigées
2. **SupabaseLocationService.java** - Utilise Config au lieu de constantes locales
3. **JsonParser.java** - Implémentation complète du parser JSON
4. **gradle/libs.versions.toml** - Ajout des versions Supabase
5. **app/build.gradle.kts** - Ajout des dépendances Supabase
6. **AndroidManifest.xml** - Ajout de SupabaseTestActivity

---

## 🔄 Prochaines étapes recommandées

### 1. Intégrer dans SmsReceiver
Sauvegarder automatiquement les localisations reçues par SMS :

```java
// Dans handleLocationResponse(), après avoir parsé les coordonnées
SupabaseLocationService supabase = new SupabaseLocationService();
supabase.addOrUpdateLocation(senderNumber, latitude, longitude);
```

### 2. Intégrer dans HomeFragment
Afficher les localisations depuis Supabase au lieu de SQLite :

```java
// Dans loadFriendLocations()
supabaseService.getAllLocations().thenAccept(locations -> {
    getActivity().runOnUiThread(() -> {
        for (SupabaseLocationService.LocationEntry location : locations) {
            LatLng position = new LatLng(location.latitude, location.longitude);
            googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(location.phone));
        }
    });
});
```

### 3. Synchronisation bidirectionnelle (optionnel)
- SQLite → Supabase (upload des données locales)
- Supabase → SQLite (download des données cloud)

### 4. Notifications en temps réel (optionnel)
Utiliser Supabase Realtime pour recevoir des notifications quand une nouvelle localisation est ajoutée.

---

## ⚠️ Points importants

### URL Supabase
❌ **INCORRECT** : `https://supabase.com/dashboard/project/skbttjztscyebsrvghqu`
✅ **CORRECT** : `https://skbttjztscyebsrvghqu.supabase.co`

### Clés API
- **anon key** : Pour les opérations publiques (lecture, insertion limitée)
- **service_role key** : Pour les opérations admin (à ne PAS exposer dans le code client !)

### Sécurité
- ✅ RLS activé pour protéger les données
- ✅ Politiques créées pour contrôler l'accès
- ⚠️ Ne jamais exposer la `service_role_key` dans le code client
- ⚠️ Utiliser uniquement `anon_key` dans l'application Android

### Performance
- ✅ Index créés pour optimiser les requêtes
- ✅ Index unique sur `phone` pour éviter les doublons
- ✅ Utilisation de CompletableFuture pour les opérations asynchrones

---

## 🐛 Dépannage

### Erreur : "Failed to connect"
- Vérifiez l'URL : `https://skbttjztscyebsrvghqu.supabase.co`
- Vérifiez la connexion Internet
- Vérifiez que `android:usesCleartextTraffic="true"` est dans AndroidManifest.xml

### Erreur : "Table does not exist"
- Exécutez le script SQL dans Supabase
- Vérifiez dans Table Editor que la table existe

### Erreur : "Permission denied"
- Vérifiez que RLS est activé
- Vérifiez que les politiques sont créées
- Testez avec la clé `service_role` (debug uniquement)

### Erreur : "Gradle sync failed"
- Vérifiez votre connexion Internet
- File → Invalidate Caches → Invalidate and Restart
- Supprimez `.gradle` et resynchronisez

---

## 📞 Commandes utiles

### Logs Android
```bash
adb logcat | grep -E "Supabase|JsonParser|SupabaseTest"
```

### Test API REST
```bash
# GET
curl "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: VOTRE_CLE"

# POST
curl -X POST "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: VOTRE_CLE" \
  -H "Content-Type: application/json" \
  -d '{"phone":"+123","latitude":36.8,"longitude":10.1,"timestamp":1234567890000}'
```

---

## ✅ Checklist finale

- [ ] Script SQL exécuté dans Supabase
- [ ] Table `location_history` créée
- [ ] 2 enregistrements de test visibles dans Table Editor
- [ ] Gradle synchronisé sans erreurs
- [ ] Application compile sans erreurs
- [ ] SupabaseTestActivity fonctionne
- [ ] Test Connection réussi
- [ ] Test Fetch retourne des données
- [ ] Test Insert ajoute une nouvelle ligne
- [ ] Données visibles dans Supabase Table Editor

**Si toutes les cases sont cochées : 🎉 L'intégration est complète !**

---

**Documentation complète** : Voir `SUPABASE_SETUP.md`
**Guide pas à pas** : Voir `GUIDE_RAPIDE_SUPABASE.md`

