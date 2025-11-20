# Guide d'Utilisation - Tracking GPS Amélioré

## 🎯 Nouvelles Fonctionnalités

### 1. Permissions de Localisation Automatiques
L'application demande automatiquement les permissions de localisation au premier lancement de la carte.

**Actions**:
- Accepter les permissions `ACCESS_FINE_LOCATION` et `ACCESS_COARSE_LOCATION`
- La position actuelle s'affichera avec un marqueur bleu

### 2. Affichage de la Position Actuelle
Un marqueur bleu indique votre position actuelle sur la carte.

**Caractéristiques**:
- 🔵 Marqueur bleu = Votre position
- Mis à jour automatiquement
- Visible même sans tracking actif

### 3. Tracking avec Statistiques en Temps Réel

#### Démarrage du Tracking
1. Entrez un pseudo (optionnel)
2. Entrez un numéro de téléphone
3. Définissez l'intervalle (minimum 10 secondes)
4. Cliquez sur **▶️ Démarrer**

#### Affichage en Temps Réel
Pendant le tracking, vous verrez:
- **Durée**: Format HH:MM:SS (mis à jour chaque seconde)
- **Distance**: Distance totale en km
- **Vitesse moy**: Vitesse moyenne en km/h
- **Positions**: Nombre de points enregistrés

### 4. Marqueurs Différenciés

**Couleurs des marqueurs**:
- 🟢 **Vert**: Point de départ (premier point)
- 🔵 **Bleu**: Points intermédiaires
- 🔵 **Bleu clair**: Position actuelle

**Polyline**:
- Ligne bleue reliant tous les points
- Épaisseur: 10px pour meilleure visibilité
- Géodésique pour précision

### 5. Dialogue des Statistiques

#### Accès
Cliquez sur le bouton **📊 Stats** pour voir les statistiques complètes.

#### Informations Affichées
```
📊 STATISTIQUES DU TRAJET

⏱️ Durée: HH:MM:SS
📏 Distance: X.XX km
🚀 Vitesse moyenne: X.XX km/h
📍 Nombre de points: N
🟢 Point de départ: LAT, LON
🔴 Point d'arrivée: LAT, LON
```

### 6. Sauvegarde Automatique de l'État

**Données Sauvegardées**:
- Tous les points de trajectoire
- Heure de démarrage du tracking
- Distance totale parcourue

**Cas d'Usage**:
- Rotation d'écran: Les données sont restaurées
- Interruption accidentelle: Récupération possible

---

## 📋 Étapes d'Utilisation Complètes

### Scénario: Tracker un trajet

1. **Ouvrir l'application**
   - Accepter les permissions de localisation

2. **Configurer le tracking**
   ```
   Pseudo: "Mon Trajet"
   Numéro: "+216XXXXXXXX"
   Intervalle: 30 (secondes)
   ```

3. **Démarrer le tracking**
   - Cliquez sur **▶️ Démarrer**
   - La carte se réinitialise
   - Le timer commence

4. **Observer le trajet**
   - Voir les points s'ajouter en temps réel
   - Vérifier la distance et la vitesse
   - Voir le marqueur de départ (vert)

5. **Consulter les statistiques**
   - Cliquez sur **📊 Stats** à tout moment
   - Voir les statistiques complètes

6. **Arrêter le tracking**
   - Cliquez sur **⏹️ Arrêter**
   - Les données sont conservées

7. **Voir le trajet complet**
   - Cliquez sur **🗺️ Voir trajet complet**
   - La caméra s'ajuste pour voir tout le trajet

---

## 🔧 Paramètres Recommandés

### Pour une Marche à Pied
- **Intervalle**: 30-60 secondes
- **Raison**: Capture suffisante sans surcharge

### Pour une Voiture
- **Intervalle**: 10-30 secondes
- **Raison**: Meilleure précision à vitesse élevée

### Pour un Vélo
- **Intervalle**: 20-40 secondes
- **Raison**: Équilibre entre précision et batterie

---

## ⚠️ Conseils d'Utilisation

### Batterie
- Le tracking consomme beaucoup de batterie
- Utilisez un chargeur si possible
- Désactivez les autres services GPS

### Précision
- Activez le GPS haute précision dans les paramètres Android
- Allez à l'extérieur pour meilleure réception
- Évitez les zones urbaines denses

### Données
- Les données sont sauvegardées localement
- Utilisez le bouton **Export** pour sauvegarder
- Les statistiques sont calculées en temps réel

---

## 🐛 Dépannage

### La position actuelle ne s'affiche pas
1. Vérifier que les permissions sont accordées
2. Vérifier que le GPS est activé
3. Attendre quelques secondes pour la localisation

### La distance est incorrecte
1. Vérifier que plusieurs points ont été enregistrés
2. Vérifier l'intervalle de tracking
3. Vérifier la précision du GPS

### Le timer ne s'incrémente pas
1. Vérifier que le tracking est actif (🟢 Tracking actif)
2. Vérifier que le service n'a pas été arrêté
3. Redémarrer l'application

### Les données disparaissent après rotation
1. Les données sont sauvegardées automatiquement
2. Vérifier que `onSaveInstanceState` est appelé
3. Vérifier les logs pour les erreurs

---

## 📊 Formules Utilisées

### Durée
```
Durée = Heure actuelle - Heure de démarrage
Format: HH:MM:SS
```

### Distance
```
Distance = Σ distance(point[i], point[i+1])
Unité: mètres (affichée en km)
```

### Vitesse Moyenne
```
Vitesse = (Distance en km) / (Durée en heures)
Unité: km/h
```

---

## 🎨 Interface Utilisateur

### Boutons
- **▶️ Démarrer**: Lance le tracking
- **⏹️ Arrêter**: Arrête le tracking
- **🗺️ Voir trajet complet**: Affiche tout le trajet
- **📊 Stats**: Affiche les statistiques

### Indicateurs
- **🟢 Tracking actif**: Tracking en cours
- **🔴 Tracking inactif**: Tracking arrêté

### Carte
- **🔵 Bleu clair**: Position actuelle
- **🟢 Vert**: Point de départ
- **🔵 Bleu**: Points intermédiaires
- **Ligne bleue**: Trajet complet

---

**Version**: 1.0  
**Dernière mise à jour**: 2025-11-06  
**Status**: ✅ Production Ready

