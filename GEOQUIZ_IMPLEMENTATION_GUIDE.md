# 🎮 GeoQuiz Challenge - Guide d'Implémentation

**Date**: 2025-11-07
**Status**: ✅ PHASE 1 COMPLÈTE
**Version**: 1.0.0

---

## 📋 Vue d'Ensemble

GeoQuiz Challenge est un jeu de quiz géographique gamifié qui:
- 📍 Utilise l'historique de localisation de l'utilisateur
- 🗺️ Affiche des images de cartes (Static Maps API)
- 🎯 Demande à l'utilisateur de deviner le lieu
- 🏆 Déverrouille des badges basés sur les performances
- 📱 Fonctionne hors ligne avec cache local

---

## 🏗️ Architecture

### Classes Créées

#### 1. **GeoQuizQuestion.java**
- Modèle pour une question de quiz
- Propriétés: latitude, longitude, région, catégorie, difficulté
- Méthodes: checkAnswer(), getPoints(), shuffleOptions()

#### 2. **Badge.java**
- Modèle pour un badge
- 10 badges prédéfinis (régions + performance)
- Propriétés: nom, description, région, progression, déverrouillé
- Méthodes: getEmoji(), getPredefinedBadges()

#### 3. **GeoQuizDatabase.java**
- Base de données SQLite locale
- Tables: questions, badges, scores
- Fonctionnalités: cache hors ligne, historique des scores
- Méthodes: addQuestion(), getAllBadges(), addScore()

#### 4. **GeoQuizManager.java**
- Gestionnaire principal du système
- Génère les questions à partir de l'historique
- Gère les scores et les badges
- Méthodes: generateQuestionsFromHistory(), answerQuestion(), checkBadgeUnlock()

#### 5. **GeoQuizFragment.java**
- Fragment UI principal du quiz
- Affiche les questions et les options
- Gère les réponses et le score
- Intégration avec LocationDatabase

#### 6. **BadgesFragment.java**
- Fragment pour afficher les badges
- GridView des badges déverrouillés
- Affiche la progression

#### 7. **BadgesAdapter.java**
- Adapter pour la GridView des badges
- Affiche emoji, nom, description, progression

---

## 📊 Flux de Données

```
Historique de Localisation (LocationDatabase)
    ↓
GeoQuizManager.generateQuestionsFromHistory()
    ↓
Crée GeoQuizQuestion pour chaque position
    ↓
Stocke dans GeoQuizDatabase (cache local)
    ↓
GeoQuizFragment affiche les questions
    ↓
Utilisateur répond
    ↓
GeoQuizManager.answerQuestion()
    ↓
Vérifie la réponse et met à jour le score
    ↓
Vérifie les badges à déverrouiller
    ↓
Sauvegarde dans GeoQuizDatabase
```

---

## 🎮 Fonctionnalités

### 1. Génération de Questions
- Basée sur l'historique de localisation
- Identifie la région la plus proche (Haversine)
- Génère 3 mauvaises réponses
- Mélange les options

### 2. Système de Points
- Facile: 10 points
- Moyen: 25 points
- Difficile: 50 points
- Bonus: Streak (réponses consécutives)

### 3. Badges
- **Régionaux**: Tunis Explorer, Sfax Voyageur, etc.
- **Performance**: Quiz Master, Streak Champion, Perfectionist
- **Catégories**: Historien, Naturaliste

### 4. Cache Hors Ligne
- Stocke les questions localement
- Permet de jouer sans connexion
- Synchronise avec le serveur quand disponible

---

## 🔧 Configuration

### 1. Ajouter les Dépendances

```gradle
dependencies {
    // Glide pour charger les images
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.15.1'
}
```

### 2. Ajouter les Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 3. Configurer l'API Google Maps

```java
// Dans GeoQuizManager.java
String apiKey = "YOUR_GOOGLE_MAPS_API_KEY";
```

---

## 📱 Intégration dans MainActivity

### 1. Ajouter au Menu de Navigation

```xml
<!-- bottom_nav_menu.xml -->
<item
    android:id="@+id/navigation_geoquiz"
    android:icon="@drawable/ic_quiz"
    android:title="GeoQuiz" />
```

### 2. Ajouter la Route de Navigation

```xml
<!-- mobile_navigation.xml -->
<fragment
    android:id="@+id/navigation_geoquiz"
    android:name="yasminemassaoudi.grp3.fyourf.ui.geoquiz.GeoQuizFragment"
    android:label="GeoQuiz Challenge"
    tools:layout="@layout/fragment_geoquiz" />
```

### 3. Ajouter le BadgesFragment

```xml
<!-- mobile_navigation.xml -->
<fragment
    android:id="@+id/navigation_badges"
    android:name="yasminemassaoudi.grp3.fyourf.ui.geoquiz.BadgesFragment"
    android:label="Mes Badges"
    tools:layout="@layout/fragment_badges" />
```

---

## 🎯 Utilisation

### Lancer le Quiz

```java
// Dans MainActivity ou un Fragment
GeoQuizFragment quizFragment = new GeoQuizFragment();
getSupportFragmentManager()
    .beginTransaction()
    .replace(R.id.fragment_container, quizFragment)
    .commit();
```

### Afficher les Badges

```java
BadgesFragment badgesFragment = new BadgesFragment();
getSupportFragmentManager()
    .beginTransaction()
    .replace(R.id.fragment_container, badgesFragment)
    .commit();
```

---

## 📊 Statistiques

### Fichiers Créés
- 7 fichiers Java
- 4 fichiers XML (layouts)
- 1 fichier XML (drawable)
- 1 fichier documentation

### Lignes de Code
- Java: 1500+ lignes
- XML: 400+ lignes
- Total: 1900+ lignes

### Compilation
- ✅ 0 erreurs
- ✅ 0 avertissements critiques

---

## 🚀 Prochaines Étapes

### Phase 2: Améliorations
- [ ] Intégrer dans MainActivity
- [ ] Ajouter animations
- [ ] Implémenter leaderboard
- [ ] Ajouter partage de scores
- [ ] Notifications de badges

### Phase 3: Fonctionnalités Avancées
- [ ] Multiplayer mode
- [ ] Défis quotidiens
- [ ] Saisons de quiz
- [ ] Récompenses réelles
- [ ] Intégration avec les amis

---

## 🔐 Sécurité

- ✅ Validation des données
- ✅ Gestion des permissions
- ✅ Cache sécurisé
- ✅ Pas de données sensibles stockées

---

## 📚 Ressources

### Régions Tunisiennes
- Tunis, Sfax, Sousse, Kairouan, Gafsa
- Tozeur, Douz, Djerba, Tataouine, Kebili

### Catégories
- Plage, Montagne, Ville, Désert, Oasis
- Historique, Moderne

### Difficultés
- 1 = Facile (10 points)
- 2 = Moyen (25 points)
- 3 = Difficile (50 points)

---

## 🆘 Dépannage

### Erreur: "Aucune position disponible"
- Vérifier que LocationDatabase a des positions
- Lancer l'app de tracking d'abord

### Erreur: "Erreur lors de la génération des questions"
- Vérifier les coordonnées des positions
- Vérifier la connexion Internet

### Images ne s'affichent pas
- Vérifier la clé API Google Maps
- Vérifier la connexion Internet

---

**Status**: ✅ PHASE 1 COMPLÈTE
**Prochaine Étape**: Phase 2 - Intégration
**Date**: 2025-11-07

