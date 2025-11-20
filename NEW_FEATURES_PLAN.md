# 🚀 Plan - Nouvelles Fonctionnalités FyourF

## 📋 Demandes Utilisateur

### 1. ✅ Ajouter d'Autres Fonctionnalités
### 2. ✅ Changer Splash Screen et Icon
### 3. ✅ Améliorer Logique (2 Users - Distance Restante)
### 4. ✅ Enregistrer dans MySQL

---

## 🎯 Fonctionnalités à Ajouter

### 1. 🎨 Splash Screen & Icon

#### Splash Screen
- [ ] Créer SplashActivity.java
- [ ] Créer layout splash_activity.xml
- [ ] Ajouter animation
- [ ] Configurer dans AndroidManifest.xml
- [ ] Ajouter logo personnalisé

#### App Icon
- [ ] Créer nouveau logo (PNG)
- [ ] Générer icônes pour toutes les résolutions
- [ ] Mettre à jour mipmap-*
- [ ] Mettre à jour ic_launcher.xml

---

### 2. 👥 Fonctionnalité Multi-Utilisateurs

#### Distance Restante Entre 2 Users
```
User A (Tunis) -------- 330 km -------- User B (Sfax)
```

**Fonctionnalités**:
- [ ] Afficher distance entre 2 utilisateurs
- [ ] Afficher temps estimé
- [ ] Afficher direction
- [ ] Afficher sur la carte
- [ ] Notifications en temps réel

#### Tables MySQL Nécessaires
```sql
-- Table users
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero VARCHAR(20) UNIQUE,
    pseudo VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table user_connections
CREATE TABLE user_connections (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user1_id INT,
    user2_id INT,
    status ENUM('pending', 'connected', 'blocked'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id),
    FOREIGN KEY (user2_id) REFERENCES users(id)
);

-- Table user_distances
CREATE TABLE user_distances (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user1_id INT,
    user2_id INT,
    distance DOUBLE,
    time_remaining INT,
    direction DOUBLE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id),
    FOREIGN KEY (user2_id) REFERENCES users(id)
);
```

---

### 3. 📊 Nouvelles Fonctionnalités

#### A. Partage de Position en Temps Réel
- [ ] Partager position avec d'autres users
- [ ] Voir position des amis
- [ ] Notifications quand ami proche
- [ ] Historique des rencontres

#### B. Groupes/Équipes
- [ ] Créer groupe
- [ ] Ajouter membres
- [ ] Voir tous les membres sur la carte
- [ ] Chat groupe

#### C. Géofencing
- [ ] Créer zones de sécurité
- [ ] Alertes quand sort de zone
- [ ] Alertes quand entre dans zone

#### D. Historique Amélioré
- [ ] Filtrer par date
- [ ] Filtrer par utilisateur
- [ ] Exporter en PDF/CSV
- [ ] Statistiques détaillées

#### E. Notifications Avancées
- [ ] Ami proche (< 1 km)
- [ ] Ami très proche (< 100 m)
- [ ] Ami a quitté la zone
- [ ] Ami a atteint destination

#### F. Statistiques Sociales
- [ ] Utilisateurs les plus proches
- [ ] Trajets les plus fréquents
- [ ] Temps passé avec chaque ami
- [ ] Classement des utilisateurs

---

## 🗄️ Structure MySQL

### Tables à Créer
1. `users` - Informations utilisateurs
2. `user_connections` - Connexions entre users
3. `user_distances` - Distances en temps réel
4. `user_groups` - Groupes/équipes
5. `group_members` - Membres des groupes
6. `geofences` - Zones de sécurité
7. `notifications` - Notifications
8. `meeting_history` - Historique des rencontres

### Tables Existantes à Modifier
1. `positions` - Ajouter user_id
2. `trajectories` - Ajouter user_id

---

## 📱 Classes Java à Créer

### 1. SplashActivity.java
```
Affiche splash screen au démarrage
Charge ressources
Redirige vers MainActivity
```

### 2. UserManager.java
```
Gère les utilisateurs
Récupère liste des amis
Gère connexions
```

### 3. DistanceManager.java
```
Calcule distance entre 2 users
Calcule temps restant
Gère notifications
```

### 4. MultiUserTrackingActivity.java
```
Affiche plusieurs users sur la carte
Affiche distances
Affiche directions
```

### 5. UserConnectionsFragment.java
```
Liste des amis
Ajouter/supprimer amis
Voir distance
```

### 6. GroupsFragment.java
```
Liste des groupes
Créer groupe
Voir membres
```

### 7. GeofenceManager.java
```
Créer zones
Vérifier si dans zone
Envoyer alertes
```

---

## 🔧 PHP Scripts à Créer

### 1. users/
- `create_user.php` - Créer utilisateur
- `get_user.php` - Récupérer utilisateur
- `update_user.php` - Mettre à jour
- `delete_user.php` - Supprimer

### 2. connections/
- `add_connection.php` - Ajouter ami
- `get_connections.php` - Lister amis
- `remove_connection.php` - Supprimer ami
- `get_distance.php` - Distance entre 2 users

### 3. groups/
- `create_group.php` - Créer groupe
- `add_member.php` - Ajouter membre
- `get_members.php` - Lister membres
- `delete_group.php` - Supprimer groupe

### 4. geofences/
- `create_geofence.php` - Créer zone
- `check_geofence.php` - Vérifier si dans zone
- `get_geofences.php` - Lister zones
- `delete_geofence.php` - Supprimer zone

### 5. notifications/
- `send_notification.php` - Envoyer notification
- `get_notifications.php` - Récupérer notifications
- `mark_as_read.php` - Marquer comme lu

---

## 📊 Priorités

### Phase 1 (Immédiat)
1. ✅ Splash Screen
2. ✅ App Icon
3. ✅ Tables MySQL (users, connections)
4. ✅ Distance entre 2 users

### Phase 2 (Court terme)
1. ✅ UserManager.java
2. ✅ DistanceManager.java
3. ✅ UserConnectionsFragment
4. ✅ Afficher sur carte

### Phase 3 (Moyen terme)
1. ✅ Groupes
2. ✅ Géofencing
3. ✅ Notifications avancées
4. ✅ Historique amélioré

### Phase 4 (Long terme)
1. ✅ Statistiques sociales
2. ✅ Chat
3. ✅ Intégrations
4. ✅ Optimisations

---

## 📈 Bénéfices

✅ **Multi-utilisateurs** - Partager position
✅ **Temps réel** - Distance actualisée
✅ **Notifications** - Alertes intelligentes
✅ **Sécurité** - Géofencing
✅ **Statistiques** - Analyse complète
✅ **Social** - Connexions entre users

---

**Date**: 2025-11-07
**Status**: PLAN CRÉÉ
**Prochaine Étape**: Implémenter Phase 1

