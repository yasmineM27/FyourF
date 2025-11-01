# 🚀 Guide Rapide - Intégration Supabase

## ✅ Ce qui a été fait

### 1. Configuration des fichiers
- ✅ **Config.java** : URL et clés Supabase corrigées
- ✅ **SupabaseLocationService.java** : Service pour interagir avec Supabase
- ✅ **JsonParser.java** : Classe pour parser les réponses JSON
- ✅ **SupabaseTestActivity.java** : Activité de test créée
- ✅ **Dépendances Gradle** : Toutes les bibliothèques Supabase ajoutées
- ✅ **AndroidManifest.xml** : Activité de test ajoutée

### 2. Fichiers créés
- 📄 `supabase_script.sql` - Script SQL à exécuter dans Supabase
- 📄 `SUPABASE_SETUP.md` - Documentation complète
- 📄 `GUIDE_RAPIDE_SUPABASE.md` - Ce guide

---

## 🎯 Étapes à suivre MAINTENANT

### Étape 1 : Exécuter le script SQL dans Supabase (5 minutes)

1. **Ouvrir Supabase Dashboard**
   - Allez sur : https://supabase.com/dashboard/project/skbttjztscyebsrvghqu
   - Connectez-vous si nécessaire

2. **Ouvrir l'éditeur SQL**
   - Cliquez sur **"SQL Editor"** dans le menu de gauche
   - Cliquez sur **"New query"**

3. **Copier-coller le script**
   - Ouvrez le fichier `supabase_script.sql`
   - Copiez TOUT le contenu
   - Collez dans l'éditeur SQL de Supabase

4. **Exécuter le script**
   - Cliquez sur **"Run"** (ou Ctrl+Enter)
   - Attendez quelques secondes

5. **Vérifier le résultat**
   - Vous devriez voir : "Success. No rows returned"
   - Puis un tableau avec 2 lignes de données de test
   - ✅ Si vous voyez ça, c'est bon !

6. **Vérifier la table**
   - Cliquez sur **"Table Editor"** dans le menu de gauche
   - Vous devriez voir la table **"location_history"**
   - Cliquez dessus pour voir les 2 enregistrements de test

---

### Étape 2 : Synchroniser Gradle (2 minutes)

1. **Ouvrir Android Studio**
   - Ouvrez votre projet FyourF

2. **Synchroniser Gradle**
   - Cliquez sur **"Sync Now"** (bannière en haut)
   - OU : File → Sync Project with Gradle Files
   - Attendez que la synchronisation se termine

3. **Vérifier qu'il n'y a pas d'erreurs**
   - Regardez la fenêtre "Build" en bas
   - ✅ Si "BUILD SUCCESSFUL", c'est bon !
   - ❌ Si erreurs, vérifiez votre connexion Internet

---

### Étape 3 : Tester l'application (5 minutes)

#### Option A : Avec l'activité de test (Recommandé)

1. **Modifier MainActivity temporairement**
   
   Ouvrez `MainActivity.java` et ajoutez ce code dans `onCreate()` :
   
   ```java
   // TEST SUPABASE - À SUPPRIMER APRÈS
   Intent intent = new Intent(this, SupabaseTestActivity.class);
   startActivity(intent);
   ```

2. **Lancer l'application**
   - Connectez votre téléphone ou lancez l'émulateur
   - Cliquez sur "Run" (▶️)
   - L'activité de test devrait s'ouvrir automatiquement

3. **Tester les fonctionnalités**
   - Cliquez sur **"Test Connection"** → Devrait afficher "Fetch successful"
   - Cliquez sur **"Test Fetch"** → Devrait afficher les 2 localisations de test
   - Cliquez sur **"Test Insert"** → Devrait insérer une nouvelle localisation
   - Cliquez à nouveau sur **"Test Fetch"** → Devrait afficher 3 localisations

4. **Vérifier dans Supabase**
   - Retournez sur Supabase → Table Editor → location_history
   - Vous devriez voir la nouvelle localisation ajoutée
   - ✅ Si vous voyez ça, TOUT FONCTIONNE !

#### Option B : Test manuel dans le code

Ajoutez ce code dans n'importe quelle activité :

```java
// Test Supabase
SupabaseLocationService supabase = new SupabaseLocationService();

// Test 1 : Insérer une localisation
supabase.addOrUpdateLocation("+21612345678", 36.8065, 10.1815);
Log.d("TEST", "Localisation insérée");

// Test 2 : Récupérer toutes les localisations
supabase.getAllLocations().thenAccept(locations -> {
    Log.d("TEST", "Nombre de localisations : " + locations.size());
    for (SupabaseLocationService.LocationEntry loc : locations) {
        Log.d("TEST", "Phone: " + loc.phone + " - Lat: " + loc.latitude + ", Lon: " + loc.longitude);
    }
});
```

Puis regardez les logs Logcat (filtre : "TEST")

---

### Étape 4 : Intégrer dans SmsReceiver (10 minutes)

Pour sauvegarder automatiquement les localisations reçues par SMS dans Supabase :

1. **Ouvrir SmsReceiver.java**

2. **Ajouter en haut de la classe** :
   ```java
   private SupabaseLocationService supabaseService;
   ```

3. **Dans la méthode `onReceive()`, après l'initialisation** :
   ```java
   supabaseService = new SupabaseLocationService();
   ```

4. **Dans la méthode `handleLocationResponse()`, après avoir parsé les coordonnées** :
   ```java
   // Sauvegarder dans Supabase
   supabaseService.addOrUpdateLocation(senderNumber, latitude, longitude);
   Log.d(TAG, "Localisation sauvegardée dans Supabase");
   ```

5. **Tester** :
   - Envoyez un SMS avec "WHERE" à votre application
   - Vérifiez dans Supabase que la localisation a été sauvegardée

---

### Étape 5 : Afficher les données dans HomeFragment (15 minutes)

Pour afficher les localisations depuis Supabase au lieu de SQLite :

1. **Ouvrir HomeFragment.java**

2. **Ajouter en haut de la classe** :
   ```java
   private SupabaseLocationService supabaseService;
   ```

3. **Dans `onCreateView()`, après l'initialisation** :
   ```java
   supabaseService = new SupabaseLocationService();
   ```

4. **Modifier la méthode `loadFriendLocations()`** :
   ```java
   private void loadFriendLocations() {
       if (googleMap == null) return;
       
       googleMap.clear();
       
       // Charger depuis Supabase au lieu de SQLite
       supabaseService.getAllLocations().thenAccept(locations -> {
           getActivity().runOnUiThread(() -> {
               friendCountText.setText("Friends: " + locations.size());
               
               for (SupabaseLocationService.LocationEntry location : locations) {
                   LatLng position = new LatLng(location.latitude, location.longitude);
                   googleMap.addMarker(new MarkerOptions()
                       .position(position)
                       .title(location.phone)
                       .snippet("Last seen: " + new Date(location.timestamp)));
               }
               
               if (!locations.isEmpty()) {
                   LatLng firstLocation = new LatLng(
                       locations.get(0).latitude, 
                       locations.get(0).longitude
                   );
                   googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12));
               }
           });
       }).exceptionally(throwable -> {
           Log.e("HomeFragment", "Error loading locations", throwable);
           return null;
       });
   }
   ```

5. **Tester** :
   - Lancez l'application
   - Allez dans l'onglet "Home"
   - Vous devriez voir les marqueurs sur la carte

---

## 🔧 Dépannage rapide

### Problème : "Failed to connect to Supabase"
**Solution** :
- Vérifiez votre connexion Internet
- Vérifiez que l'URL est : `https://skbttjztscyebsrvghqu.supabase.co`
- Vérifiez que la clé API est correcte dans `Config.java`

### Problème : "Table does not exist"
**Solution** :
- Exécutez le script SQL dans Supabase (Étape 1)
- Vérifiez dans Table Editor que la table existe

### Problème : "Permission denied"
**Solution** :
- Vérifiez que les politiques RLS sont créées (dans le script SQL)
- Essayez de désactiver RLS temporairement pour tester :
  ```sql
  ALTER TABLE location_history DISABLE ROW LEVEL SECURITY;
  ```

### Problème : "Gradle sync failed"
**Solution** :
- Vérifiez votre connexion Internet
- File → Invalidate Caches → Invalidate and Restart
- Supprimez le dossier `.gradle` et resynchronisez

### Problème : "No data returned"
**Solution** :
- Vérifiez dans Supabase Table Editor que les données existent
- Testez l'API REST directement avec curl :
  ```bash
  curl "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
    -H "apikey: VOTRE_CLE_ANON"
  ```

---

## 📊 Vérification finale

Cochez chaque étape :

- [ ] Script SQL exécuté dans Supabase
- [ ] Table `location_history` visible dans Table Editor
- [ ] 2 enregistrements de test visibles
- [ ] Gradle synchronisé sans erreurs
- [ ] Application lancée sans crash
- [ ] Test Connection réussi
- [ ] Test Fetch retourne des données
- [ ] Test Insert ajoute une nouvelle ligne
- [ ] Nouvelle ligne visible dans Supabase
- [ ] SmsReceiver sauvegarde dans Supabase
- [ ] HomeFragment affiche les données depuis Supabase

Si toutes les cases sont cochées : **🎉 FÉLICITATIONS ! L'intégration est complète !**

---

## 📞 Commandes utiles

### Voir les logs en temps réel
```bash
adb logcat | grep -E "Supabase|JsonParser|SupabaseTest"
```

### Tester l'API REST directement
```bash
# GET - Récupérer toutes les localisations
curl "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrYnR0anp0c2N5ZWJzcnZnaHF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEzNzkxNTEsImV4cCI6MjA3Njk1NTE1MX0.qrwGvXaMEZP7K31UfmDkJOdAswG-n3SA__aeUdrBrlo"

# POST - Insérer une localisation
curl -X POST "https://skbttjztscyebsrvghqu.supabase.co/rest/v1/location_history" \
  -H "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrYnR0anp0c2N5ZWJzcnZnaHF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEzNzkxNTEsImV4cCI6MjA3Njk1NTE1MX0.qrwGvXaMEZP7K31UfmDkJOdAswG-n3SA__aeUdrBrlo" \
  -H "Content-Type: application/json" \
  -d '{"phone":"+21612345678","latitude":36.8065,"longitude":10.1815,"timestamp":1234567890000}'
```

---

## 🎯 Prochaines étapes (optionnel)

1. **Synchronisation bidirectionnelle** : SQLite ↔ Supabase
2. **Notifications en temps réel** avec Supabase Realtime
3. **Authentification** avec Supabase Auth
4. **Stockage de fichiers** avec Supabase Storage
5. **Dashboard web** pour visualiser les localisations

---

**Bon courage ! 🚀**

