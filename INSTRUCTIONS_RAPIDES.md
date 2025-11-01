# 🚀 Instructions Rapides - Supabase Integration

## ✅ ÉTAPE 1 : Exécuter le script SQL (5 minutes)

1. **Allez sur Supabase** : https://supabase.com/dashboard/project/skbttjztscyebsrvghqu
2. **Cliquez sur "SQL Editor"** dans le menu de gauche
3. **Cliquez sur "New query"**
4. **Copiez-collez le contenu du fichier `supabase_script.sql`**
5. **Cliquez sur "Run"** (ou Ctrl+Enter)
6. **Vérifiez** : Vous devriez voir "Success" et 2 lignes de données de test

---

## ✅ ÉTAPE 2 : Synchroniser Gradle (2 minutes)

1. **Ouvrez Android Studio**
2. **Cliquez sur "Sync Now"** (bannière en haut)
3. **Attendez** que la synchronisation se termine
4. **Vérifiez** qu'il n'y a pas d'erreurs

---

## ✅ ÉTAPE 3 : Lancer l'application (2 minutes)

### Option A : Tester avec SupabaseTestActivity

1. **Ouvrez `MainActivity.java`**
2. **Ajoutez ce code dans `onCreate()` APRÈS `setContentView()`** :

```java
// TEST SUPABASE - À SUPPRIMER APRÈS
Intent intent = new Intent(this, SupabaseTestActivity.class);
startActivity(intent);
```

3. **Lancez l'application** (▶️ Run)
4. **L'activité de test s'ouvrira automatiquement**
5. **Testez** :
   - Cliquez sur "Test Connection"
   - Cliquez sur "Test Fetch" → Devrait afficher 2 localisations
   - Cliquez sur "Test Insert" → Ajoute une nouvelle localisation
   - Cliquez à nouveau sur "Test Fetch" → Devrait afficher 3 localisations

### Option B : Tester manuellement

Ajoutez ce code dans n'importe quelle activité :

```java
SupabaseLocationService supabase = new SupabaseLocationService();

// Insérer une localisation
supabase.addOrUpdateLocation("+21612345678", 36.8065, 10.1815);

// Récupérer toutes les localisations
supabase.getAllLocations().thenAccept(locations -> {
    runOnUiThread(() -> {
        for (SupabaseLocationService.LocationEntry loc : locations) {
            Log.d("TEST", "Phone: " + loc.phone + " - Lat: " + loc.latitude);
        }
    });
});
```

---

## ✅ ÉTAPE 4 : Vérifier dans Supabase

1. **Retournez sur Supabase**
2. **Cliquez sur "Table Editor"**
3. **Sélectionnez "location_history"**
4. **Vous devriez voir** les localisations ajoutées par l'application

---

## 🔧 En cas d'erreur

### Erreur : "Failed to connect"
- Vérifiez votre connexion Internet
- Vérifiez que le script SQL a été exécuté
- Vérifiez que l'URL est correcte dans `Config.java`

### Erreur : "Table does not exist"
- Exécutez le script SQL dans Supabase (Étape 1)

### Erreur : "Permission denied"
- Vérifiez que les politiques RLS sont créées (dans le script SQL)

### Erreur de compilation
- Synchronisez Gradle (File → Sync Project with Gradle Files)
- Nettoyez le projet (Build → Clean Project)
- Rebuild (Build → Rebuild Project)

---

## 📊 Résumé des fichiers modifiés

### Fichiers créés :
- ✅ `SupabaseLocationService.java` - Service HTTP pour Supabase
- ✅ `SupabaseTestActivity.java` - Activité de test
- ✅ `JsonParser.java` - Parser JSON
- ✅ `activity_supabase_test.xml` - Layout de test
- ✅ `supabase_script.sql` - Script SQL
- ✅ Documentation (SUPABASE_SETUP.md, GUIDE_RAPIDE_SUPABASE.md, etc.)

### Fichiers modifiés :
- ✅ `Config.java` - URL et clés Supabase corrigées
- ✅ `AndroidManifest.xml` - Activité de test ajoutée
- ✅ `app/build.gradle.kts` - Dépendances simplifiées

---

## 🎯 Prochaines étapes

### 1. Intégrer dans SmsReceiver

Ouvrez `SmsReceiver.java` et ajoutez dans `handleLocationResponse()` :

```java
// Après avoir parsé les coordonnées
SupabaseLocationService supabase = new SupabaseLocationService();
supabase.addOrUpdateLocation(senderNumber, latitude, longitude);
Log.d(TAG, "Localisation sauvegardée dans Supabase");
```

### 2. Intégrer dans HomeFragment

Ouvrez `HomeFragment.java` et modifiez `loadFriendLocations()` :

```java
private void loadFriendLocations() {
    if (googleMap == null) return;
    
    googleMap.clear();
    
    SupabaseLocationService supabase = new SupabaseLocationService();
    supabase.getAllLocations().thenAccept(locations -> {
        getActivity().runOnUiThread(() -> {
            friendCountText.setText("Friends: " + locations.size());
            
            for (SupabaseLocationService.LocationEntry location : locations) {
                LatLng position = new LatLng(location.latitude, location.longitude);
                googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(location.phone));
            }
        });
    });
}
```

---

## ✅ Checklist finale

- [ ] Script SQL exécuté dans Supabase
- [ ] Table `location_history` visible dans Table Editor
- [ ] Gradle synchronisé sans erreurs
- [ ] Application compile sans erreurs
- [ ] SupabaseTestActivity fonctionne
- [ ] Test Fetch retourne des données
- [ ] Test Insert ajoute une nouvelle ligne
- [ ] Données visibles dans Supabase

**Si toutes les cases sont cochées : 🎉 L'intégration fonctionne !**

---

## 📞 Commandes utiles

### Voir les logs
```bash
adb logcat | grep -E "SupabaseService|SupabaseTest"
```

### Tester l'API REST directement
```bash
curl "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrYnR0anp0c2N5ZWJzcnZnaHF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEzNzkxNTEsImV4cCI6MjA3Njk1NTE1MX0.qrwGvXaMEZP7K31UfmDkJOdAswG-n3SA__aeUdrBrlo"
```

---

**Bon courage ! 🚀**

