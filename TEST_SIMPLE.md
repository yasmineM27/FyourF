# 🧪 Test Simple - FyourF

## 📱 Test 1 : MySQL depuis le Smartphone

### Ouvrez Chrome sur votre smartphone et testez cette URL :

```
http://192.168.1.18/servicephp/get_all.php
```

### ✅ Résultat Attendu (SUCCÈS) :
```json
{
  "success": true,
  "count": 7,
  "data": [
    {
      "id": "1",
      "pseudo": "+1234567890",
      "numero": "+1234567890",
      "latitude": "36.8065",
      "longitude": "10.1815",
      "timestamp": "2025-11-01 12:00:00"
    }
  ],
  "message": "Positions récupérées avec succès"
}
```

### ❌ Si vous voyez une erreur :

#### Erreur : "Ce site est inaccessible"
**Cause :** Smartphone et PC pas sur le même WiFi
**Solution :**
1. Vérifiez que les deux sont sur le même réseau WiFi
2. Sur le PC, double-cliquez sur `check_ip.bat` pour voir l'IP
3. Utilisez cette IP dans l'URL

---

#### Erreur : "404 Not Found"
**Cause :** Fichiers PHP pas déployés
**Solution :**
```
Double-cliquez sur : fix_and_deploy.bat
```

---

#### Erreur : "Connection refused"
**Cause :** Apache pas démarré
**Solution :**
1. Ouvrez XAMPP Control Panel
2. Cliquez "Start" pour Apache
3. Attendez que le bouton devienne vert

---

#### Erreur JSON : `{"success":false,"error":"La table 'fyourf_db.positions' n'existe pas"}`
**Cause :** Base de données pas créée
**Solution :**
```
Double-cliquez sur : setup_complet.bat
```

---

## 🔥 Test 2 : Pare-feu Windows

### Si le navigateur du smartphone ne peut pas se connecter :

**Créez une règle de pare-feu :**

1. Appuyez sur **Windows + R**
2. Tapez : `wf.msc` et appuyez sur Entrée
3. Cliquez sur **"Règles de trafic entrant"** (à gauche)
4. Cliquez sur **"Nouvelle règle..."** (à droite)
5. Sélectionnez **"Port"** → Suivant
6. Sélectionnez **"TCP"** et tapez **"80"** → Suivant
7. Sélectionnez **"Autoriser la connexion"** → Suivant
8. Cochez **tout** (Domaine, Privé, Public) → Suivant
9. Nom : **"Apache HTTP Server"** → Terminer

**Testez à nouveau dans le navigateur du smartphone.**

---

## 📱 Test 3 : Application Android

### Étape 1 : Recompilez (si pas encore fait)

**Dans Android Studio :**
```
1. Build → Clean Project
2. Build → Rebuild Project (attendez 1-2 minutes)
3. Run → Run 'app' (▶️)
```

---

### Étape 2 : Visitez tous les fragments

**Dans l'application sur le smartphone :**
```
1. Ouvrez l'app
2. Cliquez sur "Home" (icône maison)
3. Cliquez sur "Dashboard" (icône tableau de bord)
4. Cliquez sur "History" (icône historique)
5. Cliquez sur "Notifications" (icône cloche)
```

**Pourquoi ?**
- Les fragments ne sont créés que quand vous les visitez
- Les BroadcastReceivers ne sont enregistrés que si le fragment est créé
- En visitant tous les fragments, vous garantissez que tous les receivers sont actifs

---

### Étape 3 : Test complet

**Sur le smartphone avec l'app :**
```
1. Allez dans "Dashboard"
2. Entrez un numéro de téléphone (ou le vôtre pour tester)
3. Cliquez "Send Request"
4. Vérifiez : Toast "Location request sent to [numéro]"
5. Vérifiez : Contact ajouté avec "(pending)"
```

**Depuis un autre téléphone (ou le même) :**
```
Envoyez un SMS au premier téléphone avec ce texte exact :
POSITION:36.123456,10.654321;time:1730467935000
```

**Sur le smartphone avec l'app :**
```
Vous devriez voir :
✅ Toast : "Location received from [numéro]"
✅ Dashboard : "(pending)" disparaît → coordonnées affichées
```

**Maintenant allez dans chaque fragment :**
```
✅ Notifications : Nouvelle notification apparaît
✅ History : Nouvelle entrée en haut de la liste
✅ Home : Nouveau marqueur sur la carte
```

---

## 🔍 Test 4 : Vérifier les Logs

### Dans Android Studio :

**Ouvrez Logcat :**
```
View → Tool Windows → Logcat
```

**Filtrez par :** `SmsReceiver`

**Envoyez le SMS de test et observez :**

### ✅ Logs Attendus (SUCCÈS) :
```
D/SmsReceiver: === SmsReceiver triggered ===
D/SmsReceiver: SMS #0 received from: +1234567890
D/SmsReceiver: Message body: POSITION:36.123456,10.654321;time:1730467935000
D/SmsReceiver: *** LOCATION RESPONSE DETECTED from: +1234567890 ***
D/SmsReceiver: Location parsed successfully - Lat: 36.123456, Lon: 10.654321
D/LocationDatabase: Location updated for +1234567890 at 36.123456,10.654321
D/MySQLLocationService: ✓ Position sauvegardée: Position mise à jour avec succès
D/NotificationDatabase: Notification added for +1234567890
D/SmsReceiver: Location notification shown
D/SmsReceiver: Location update broadcast sent
```

**Ensuite, filtrez par :** `Location update received`

### ✅ Logs Attendus (SUCCÈS) :
```
D/DashboardFragment: Location update received for +1234567890: 36.123456, 10.654321
D/NotificationsFragment: Location update received for +1234567890: 36.123456, 10.654321
D/HistoryFragment: Location update received for +1234567890: 36.123456, 10.654321
D/HomeFragment: Received location update broadcast for +1234567890
```

**Si vous voyez seulement Dashboard et Notifications :**
- History et Home n'ont pas reçu le broadcast
- Cause : Les fragments n'étaient pas créés
- Solution : Visitez tous les fragments AVANT d'envoyer le SMS de test

---

## ❌ Problèmes Courants

### Problème 1 : Dashboard et Notifications OK, mais pas History et Home

**Cause :** Les fragments History et Home n'étaient pas créés quand le SMS est arrivé

**Solution :**
```
1. Ouvrez l'app
2. Visitez TOUS les fragments (Home, Dashboard, History, Notifications)
3. Retournez dans Dashboard
4. MAINTENANT envoyez le SMS de test
5. Allez dans History → Vous devriez voir la nouvelle entrée
6. Allez dans Home → Vous devriez voir le marqueur
```

**Explication :**
- Les BroadcastReceivers sont enregistrés dans `onCreateView()`
- Si vous n'avez jamais visité History, son receiver n'est pas enregistré
- Quand le SMS arrive, History ne reçoit pas le broadcast
- MAIS quand vous visitez History, `onResume()` charge les données depuis MySQL
- Donc vous verrez quand même les données, juste pas en temps réel

---

### Problème 2 : Erreur MySQL dans les logs

**Logs :**
```
E/MySQLLocationService: ✗ Erreur: Connection refused
```

**Cause :** Le smartphone ne peut pas se connecter au serveur MySQL

**Solution :**
1. Testez dans le navigateur du smartphone : `http://192.168.1.18/servicephp/get_all.php`
2. Si ça ne fonctionne pas, configurez le pare-feu (voir Test 2)
3. Vérifiez que Apache est démarré dans XAMPP

---

### Problème 3 : Map ne montre pas le marqueur

**Logs :**
```
W/HomeFragment: Map not ready yet
```

**Cause :** GoogleMap n'est pas encore initialisée

**Solution :**
- C'est normal si vous recevez le SMS avant que la carte soit chargée
- Allez dans Home → La carte se charge → Les marqueurs apparaissent
- Ou cliquez sur le bouton "Refresh" dans Home

---

### Problème 4 : History vide

**Vérifiez :**
1. Config.USE_MYSQL est true ?
2. MySQL fonctionne depuis le smartphone ?
3. Les données sont dans MySQL ?

**Test MySQL depuis le PC :**
```
http://localhost/servicephp/get_all.php
```

**Si vous voyez des données sur le PC mais pas sur le smartphone :**
- Problème de réseau ou pare-feu
- Suivez le Test 2 (Pare-feu)

---

## 📋 Checklist Complète

### Sur le PC :
- [ ] XAMPP installé
- [ ] Apache démarré (bouton vert)
- [ ] MySQL démarré (bouton vert)
- [ ] Fichiers PHP dans `C:\xampp\htdocs\servicephp\`
- [ ] Base de données créée (`setup_complet.bat`)
- [ ] Pare-feu configuré (Test 2)
- [ ] Test navigateur PC : `http://localhost/servicephp/get_all.php` → JSON OK

### Sur le Smartphone :
- [ ] Connecté au même WiFi que le PC
- [ ] Test navigateur smartphone : `http://192.168.1.18/servicephp/get_all.php` → JSON OK
- [ ] Application recompilée et installée
- [ ] Tous les fragments visités au moins une fois
- [ ] Permissions accordées (SMS, Localisation, Notifications)

### Test Final :
- [ ] Dashboard : Send Request → "(pending)" → coordonnées ✅
- [ ] Notifications : Nouvelle notification ✅
- [ ] History : Nouvelle entrée ✅
- [ ] Home : Nouveau marqueur ✅

---

## 🎯 Résumé des Actions

### Action 1 : Test MySQL
```
Smartphone → Chrome → http://192.168.1.18/servicephp/get_all.php
```
**Dites-moi ce que vous voyez !**

---

### Action 2 : Recompiler
```
Android Studio → Build → Rebuild Project → Run
```

---

### Action 3 : Visiter tous les fragments
```
App → Home → Dashboard → History → Notifications
```

---

### Action 4 : Test SMS
```
Dashboard → Send Request → Attendez SMS → Vérifiez tous les fragments
```

---

### Action 5 : Vérifier les Logs
```
Logcat → Filtrer "SmsReceiver" → Copiez les logs ici
```

---

**Commencez par l'Action 1 et dites-moi ce que vous voyez dans le navigateur du smartphone ! 📱**

