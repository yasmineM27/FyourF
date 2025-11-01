# 🌐 Guide de Test Réseau - FyourF

## ❌ Problème Actuel

**Symptôme :** "ERREUR MYSQL LORS DE CHARGEMENT" sur le smartphone

**Cause probable :** Le smartphone ne peut pas se connecter au serveur PC

---

## ✅ SOLUTION APPLIQUÉE

### Correction de l'IP dans Config.java

**AVANT :**
```java
public static String MYSQL_SERVER_IP = "192.168.1.100"; // ❌ MAUVAISE IP
```

**APRÈS :**
```java
public static String MYSQL_SERVER_IP = "192.168.1.18"; // ✅ BONNE IP
```

---

## 🔧 ÉTAPES DE RÉSOLUTION

### ÉTAPE 1 : Recompiler l'Application ⚠️ IMPORTANT

**Dans Android Studio :**

1. **Build → Clean Project**
   - Attendez la fin

2. **Build → Rebuild Project**
   - Attendez la fin (peut prendre 1-2 minutes)

3. **Run → Run 'app'** (ou cliquez sur ▶️)
   - Sélectionnez votre smartphone
   - Attendez l'installation

**L'application DOIT être recompilée pour prendre en compte la nouvelle IP !**

---

### ÉTAPE 2 : Test depuis le Navigateur du Smartphone

**Sur votre smartphone, ouvrez Chrome/Firefox et allez sur :**

```
http://192.168.1.18/servicephp/get_all.php
```

#### ✅ Résultat Attendu (Succès)

Vous devriez voir :
```json
{
  "success": true,
  "count": 7,
  "data": [...]
}
```

**Si vous voyez cela → Le réseau fonctionne ! Passez à l'étape 3.**

#### ❌ Résultat Possible (Échec)

**Erreur 1 : "Impossible de se connecter"**
```
Ce site est inaccessible
192.168.1.18 a mis trop de temps à répondre
```
→ Problème de réseau (voir ÉTAPE 3)

**Erreur 2 : "404 Not Found"**
```
Not Found
The requested URL was not found on this server.
```
→ Fichiers PHP mal placés (exécutez `fix_and_deploy.bat`)

**Erreur 3 : "Connection refused"**
```
ERR_CONNECTION_REFUSED
```
→ Apache n'est pas démarré

---

### ÉTAPE 3 : Vérifier le Réseau

#### A. Même WiFi ?

**Sur le smartphone :**
```
Paramètres → WiFi → Nom du réseau
```

**Sur le PC :**
```cmd
ipconfig
```

Cherchez "Carte réseau sans fil Wi-Fi" et vérifiez le nom du réseau.

**Les deux DOIVENT être sur le même WiFi !**

#### B. Vérifier l'IP du PC

**Sur le PC :**
```cmd
ipconfig
```

Cherchez :
```
Carte réseau sans fil Wi-Fi :
   Adresse IPv4. . . . . . . . . . . . . .: 192.168.1.18
```

**Si l'IP a changé, mettez à jour Config.java et recompilez !**

#### C. Désactiver le Pare-feu (Test)

**Sur le PC :**

1. **Panneau de configuration**
2. **Système et sécurité**
3. **Pare-feu Windows Defender**
4. **Activer ou désactiver le Pare-feu Windows Defender**
5. **Désactiver pour "Réseau privé"** (temporairement)

Ou via PowerShell (Admin) :
```powershell
Set-NetFirewallProfile -Profile Private -Enabled False
```

**Re-testez depuis le navigateur du smartphone.**

#### D. Créer une Règle de Pare-feu (Permanent)

Au lieu de désactiver le pare-feu, créez une règle :

**PowerShell (Admin) :**
```powershell
New-NetFirewallRule -DisplayName "Apache HTTP" -Direction Inbound -LocalPort 80 -Protocol TCP -Action Allow
```

Ou manuellement :
1. Pare-feu Windows Defender
2. Paramètres avancés
3. Règles de trafic entrant
4. Nouvelle règle
5. Port → TCP → 80
6. Autoriser la connexion
7. Nom : "Apache HTTP"

---

### ÉTAPE 4 : Tester l'Application Android

**Après avoir recompilé et installé :**

1. Ouvrez l'application FyourF
2. Allez dans **"History"**
3. Cliquez sur **"Refresh"**

#### ✅ Résultat Attendu

```
Toast: "Positions chargées depuis MySQL"
Liste affiche 7 positions
```

#### ❌ Si l'erreur persiste

Passez à l'ÉTAPE 5 (Logs)

---

### ÉTAPE 5 : Analyser les Logs Android

**Dans Android Studio :**

1. **View → Tool Windows → Logcat**
2. **Filtrer par :** `MySQLLocationService`
3. **Cliquez sur "Refresh" dans l'app**
4. **Observez les logs**

#### Logs Possibles

**✅ Succès :**
```
D/MySQLLocationService: ✓ Connexion réussie
D/MySQLLocationService: ✓ Positions chargées: 7
D/Loading: ✓ Positions récupérées avec succès
```

**❌ Erreur de connexion :**
```
E/MySQLLocationService: ✗ Erreur: Connection refused
E/MySQLLocationService: ✗ URL: http://192.168.1.18/servicephp/get_all.php
```
→ Problème de réseau (pare-feu, WiFi)

**❌ Erreur 404 :**
```
E/MySQLLocationService: ✗ Erreur: 404 Not Found
```
→ Fichiers PHP mal placés

**❌ Erreur timeout :**
```
E/MySQLLocationService: ✗ Erreur: timeout
```
→ Mauvaise IP ou serveur inaccessible

---

## 🧪 Tests de Diagnostic

### Test 1 : Ping depuis le Smartphone

**Installez une app "Network Tools" ou "Ping" sur le smartphone**

Pingez :
```
192.168.1.18
```

**Résultat attendu :**
```
Reply from 192.168.1.18: time=5ms
```

**Si timeout :**
→ Le smartphone ne peut pas atteindre le PC (WiFi, pare-feu)

### Test 2 : Vérifier Apache

**Sur le PC, ouvrez le navigateur :**
```
http://localhost/servicephp/get_all.php
```

**Doit fonctionner !**

### Test 3 : Vérifier depuis un autre appareil

**Sur un autre smartphone/tablette sur le même WiFi :**
```
http://192.168.1.18/servicephp/get_all.php
```

**Si ça fonctionne :**
→ Le problème vient de l'application Android (Config.java, compilation)

**Si ça ne fonctionne pas :**
→ Le problème vient du réseau (pare-feu)

---

## 📋 Checklist Complète

### Serveur
- [ ] Apache démarré (XAMPP Control Panel)
- [ ] MySQL démarré
- [ ] Fichiers PHP dans `C:\xampp\htdocs\servicephp\`
- [ ] Test PC : http://localhost/servicephp/get_all.php fonctionne
- [ ] IP vérifiée : `ipconfig` → 192.168.1.18

### Réseau
- [ ] PC et smartphone sur le même WiFi
- [ ] Pare-feu désactivé (test) ou règle créée
- [ ] Test navigateur smartphone : http://192.168.1.18/servicephp/get_all.php fonctionne

### Android
- [ ] Config.java : `MYSQL_SERVER_IP = "192.168.1.18"`
- [ ] Config.java : `USE_MYSQL = true`
- [ ] **Build → Clean Project** exécuté
- [ ] **Build → Rebuild Project** exécuté
- [ ] Application réinstallée sur le smartphone
- [ ] Permissions accordées (Localisation, Internet)

### Tests
- [ ] Navigateur PC : ✅
- [ ] Navigateur smartphone : ✅
- [ ] Application Android History → Refresh : ✅
- [ ] Logcat : pas d'erreur

---

## 🎯 Solution Rapide (Résumé)

### 1. Vérifier l'IP
```cmd
ipconfig
```
→ Notez l'IPv4 (ex: 192.168.1.18)

### 2. Mettre à jour Config.java
```java
public static String MYSQL_SERVER_IP = "192.168.1.18"; // Votre IP
```

### 3. Recompiler
```
Build → Clean Project
Build → Rebuild Project
Run → Run 'app'
```

### 4. Désactiver le pare-feu (test)
```
Panneau de configuration → Pare-feu → Désactiver (réseau privé)
```

### 5. Tester navigateur smartphone
```
http://192.168.1.18/servicephp/get_all.php
```

### 6. Tester l'app
```
History → Refresh
```

---

## 🆘 Dépannage Avancé

### Problème : L'IP change souvent

**Solution : IP statique**

1. Panneau de configuration → Réseau et Internet
2. Centre Réseau et partage
3. Modifier les paramètres de la carte
4. Clic droit sur WiFi → Propriétés
5. IPv4 → Propriétés
6. Utiliser l'adresse IP suivante :
   - IP : 192.168.1.18
   - Masque : 255.255.255.0
   - Passerelle : 192.168.1.1

### Problème : Pare-feu bloque toujours

**Solution : Règle spécifique**

```powershell
# PowerShell Admin
New-NetFirewallRule -DisplayName "FyourF Apache" `
  -Direction Inbound `
  -LocalPort 80 `
  -Protocol TCP `
  -Action Allow `
  -Profile Private
```

### Problème : Smartphone en 4G au lieu de WiFi

Vérifiez que le WiFi est activé et connecté au bon réseau.

---

## ✅ Résultat Final Attendu

### Sur PC (Navigateur)
```json
{"success":true,"count":7,"data":[...],"message":"Positions récupérées avec succès"}
```

### Sur Smartphone (Navigateur)
```json
{"success":true,"count":7,"data":[...],"message":"Positions récupérées avec succès"}
```

### Sur Smartphone (App)
```
✓ Toast: "Positions chargées depuis MySQL"
✓ 7 positions affichées
✓ Pas d'erreur
```

---

**Suivez ces étapes dans l'ordre et testez après chaque étape ! 🚀**

