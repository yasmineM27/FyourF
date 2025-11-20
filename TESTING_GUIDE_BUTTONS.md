# 🧪 Guide de Test Complet - Tous les Boutons

## 📋 Préparation

### Avant de Commencer
- [ ] Application compilée (BUILD SUCCESSFUL)
- [ ] Émulateur démarré
- [ ] App installée: `.\gradlew.bat installDebug`
- [ ] GPS activé sur l'émulateur
- [ ] Permissions GPS accordées
- [ ] MySQL démarré
- [ ] Apache démarré
- [ ] Terminal logcat ouvert: `adb logcat | grep TrackingActivity`

---

## 🧪 Test 1: Bouton "▶️ Démarrer"

### Étapes
```
1. Ouvrir l'application FyourF
2. Entrer numéro: +21612345678
3. Entrer pseudo: TestUser (ou laisser vide)
4. Entrer intervalle: 30 (ou laisser par défaut)
5. Cliquer sur "▶️ Démarrer"
```

### Vérifications ✅

**UI:**
- [ ] Bouton "▶️ Démarrer" devient grisé (disabled)
- [ ] Bouton "⏹️ Arrêter" devient actif (vert)
- [ ] Champs de saisie deviennent grisés
- [ ] Statut change à "🟢 Tracking actif" (vert)
- [ ] Toast: "✅ Tracking démarré - Intervalle: 30s"

**Logs:**
```
D/TrackingActivity: Start button clicked
D/TrackingActivity: Tracking démarré - Numero: +21612345678 | Intervalle: 30000ms
D/TrackingActivity: UI State updated - Tracking: true
```

**Carte:**
- [ ] Marqueur bleu visible (position actuelle)
- [ ] Carte centrée sur Tunis

---

## 🧪 Test 2: Attendre les Positions

### Étapes
```
1. Laisser le tracking actif pendant 60+ secondes
2. Observer la carte
3. Observer les statistiques
```

### Vérifications ✅

**Positions:**
- [ ] Positions: 2+ (augmente toutes les 30s)
- [ ] Marqueurs verts et bleus sur la carte
- [ ] Polyline bleue reliant les points

**Statistiques:**
- [ ] Durée: 00:01:00+ (augmente chaque seconde)
- [ ] Distance: 0.XX km (augmente avec les positions)
- [ ] Vitesse moy: X.XX km/h (calculée)

**Logs:**
```
D/TrackingActivity: Position ajoutée à la carte: LatLng(36.8065, 10.1815)
D/TrackingActivity: Distance totale: 50m
```

---

## 🧪 Test 3: Bouton "⏹️ Arrêter"

### Étapes
```
1. Après 60+ secondes de tracking
2. Cliquer sur "⏹️ Arrêter"
```

### Vérifications ✅

**UI:**
- [ ] Bouton "▶️ Démarrer" redevient actif
- [ ] Bouton "⏹️ Arrêter" devient grisé
- [ ] Champs de saisie redeviennent actifs
- [ ] Statut change à "🔴 Tracking inactif" (rouge)
- [ ] Toast: "✅ Tracking arrêté"

**Logs:**
```
D/TrackingActivity: Stop button clicked
D/TrackingActivity: Arrêt du tracking...
D/TrackingActivity: Tracking arrêté - Points: 2 | Distance: 50m
D/TrackingActivity: UI State updated - Tracking: false
D/TrackingSyncManager: Sauvegarde du trajet...
D/TrackingSyncManager: Trajet sauvegardé avec succès
```

**MySQL:**
- [ ] Nouvelle ligne dans `trajectories`
- [ ] Positions sauvegardées dans `positions`

---

## 🧪 Test 4: Bouton "🗺️ Voir trajet complet"

### Étapes
```
1. Après avoir arrêté le tracking
2. Cliquer sur "🗺️ Voir trajet complet"
```

### Vérifications ✅

**Carte:**
- [ ] Zoom automatique sur le trajet
- [ ] Tous les points visibles
- [ ] Polyline visible

**Toast:**
- [ ] "🗺️ Trajet complet: 2 positions"

**Logs:**
```
D/TrackingActivity: View trajectory button clicked
D/TrackingActivity: Trajet complet affiché: 2 points
```

---

## 🧪 Test 5: Bouton "📊 Stats"

### Étapes
```
1. Après avoir arrêté le tracking
2. Cliquer sur "📊 Stats"
```

### Vérifications ✅

**Dialog:**
- [ ] Titre: "📊 Statistiques du Trajet"
- [ ] Affiche: Durée, Distance, Vitesse, Nombre de points
- [ ] Affiche: Coordonnées départ et arrivée
- [ ] Bouton "Fermer"
- [ ] Bouton "💾 Sauvegarder"

**Contenu:**
```
📊 STATISTIQUES DU TRAJET

⏱️ Durée: 00:01:00
📏 Distance: 0.05 km
🚀 Vitesse moyenne: 3.00 km/h
📍 Nombre de points: 2
🟢 Départ: 36.806500, 10.181500
🔴 Arrivée: 36.806700, 10.181700

💾 Statut: Prêt à être sauvegardé
```

**Logs:**
```
D/TrackingActivity: Stats button clicked
D/TrackingActivity: Dialog statistiques affiché
```

---

## 🧪 Test 6: Bouton "💾 Sauvegarder" (dans Dialog)

### Étapes
```
1. Dialog statistiques ouvert
2. Cliquer sur "💾 Sauvegarder"
```

### Vérifications ✅

**Toast:**
- [ ] "✅ Trajet sauvegardé avec succès"

**Logs:**
```
D/TrackingActivity: Sauvegarde depuis dialog
D/TrackingSyncManager: Sauvegarde du trajet...
D/TrackingSyncManager: Trajet sauvegardé avec succès
```

**MySQL:**
- [ ] Nouvelle ligne dans `trajectories`

---

## 🧪 Test 7: Cas d'Erreur - Numéro Vide

### Étapes
```
1. Laisser le champ numéro vide
2. Cliquer sur "▶️ Démarrer"
```

### Vérifications ✅

**Toast:**
- [ ] "❌ Veuillez entrer un numéro"

**Logs:**
```
D/TrackingActivity: Start button clicked
W/TrackingActivity: Numero vide
```

**UI:**
- [ ] Tracking ne démarre pas
- [ ] Boutons restent dans l'état initial

---

## 🧪 Test 8: Cas d'Erreur - Intervalle Invalide

### Étapes
```
1. Entrer intervalle: abc (texte invalide)
2. Cliquer sur "▶️ Démarrer"
```

### Vérifications ✅

**Toast:**
- [ ] "❌ Intervalle invalide"

**Logs:**
```
E/TrackingActivity: Erreur parsing intervalle: ...
```

---

## 🧪 Test 9: Cas d'Erreur - Intervalle Trop Court

### Étapes
```
1. Entrer intervalle: 5 (< 10 secondes)
2. Cliquer sur "▶️ Démarrer"
```

### Vérifications ✅

**Toast:**
- [ ] "❌ Intervalle minimum: 10 secondes"

---

## 🧪 Test 10: Cas d'Erreur - Aucune Position

### Étapes
```
1. Cliquer "▶️ Démarrer"
2. Immédiatement cliquer "⏹️ Arrêter" (< 1 seconde)
3. Cliquer "🗺️ Voir trajet complet"
```

### Vérifications ✅

**Toast:**
- [ ] "⚠️ Aucune position enregistrée"

**Logs:**
```
W/TrackingActivity: viewFullTrajectory: Aucun point
```

---

## 🧪 Test 11: Pseudo Vide (Auto-généré)

### Étapes
```
1. Laisser pseudo vide
2. Entrer numéro: +21612345678
3. Cliquer "▶️ Démarrer"
```

### Vérifications ✅

**Logs:**
```
D/TrackingActivity: Pseudo auto-généré: User_1234567890
```

**MySQL:**
- [ ] Pseudo dans `trajectories` = "User_1234567890"

---

## 🧪 Test 12: Permissions Manquantes

### Étapes
```
1. Révoquer permissions GPS dans les paramètres
2. Cliquer "▶️ Démarrer"
```

### Vérifications ✅

**Toast:**
- [ ] "❌ Permissions GPS requises"

**Logs:**
```
W/TrackingActivity: Permissions GPS requises
```

---

## 📊 Tableau de Résumé

| Test | Bouton | Résultat | Status |
|------|--------|----------|--------|
| 1 | ▶️ Démarrer | Tracking démarre | ✅ |
| 2 | - | Positions enregistrées | ✅ |
| 3 | ⏹️ Arrêter | Tracking arrête | ✅ |
| 4 | 🗺️ Voir trajet | Carte zoom | ✅ |
| 5 | 📊 Stats | Dialog affichée | ✅ |
| 6 | 💾 Sauvegarder | Données sauvegardées | ✅ |
| 7 | ▶️ (Erreur) | Numéro vide | ✅ |
| 8 | ▶️ (Erreur) | Intervalle invalide | ✅ |
| 9 | ▶️ (Erreur) | Intervalle court | ✅ |
| 10 | 🗺️ (Erreur) | Aucune position | ✅ |
| 11 | ▶️ (Pseudo) | Auto-généré | ✅ |
| 12 | ▶️ (Permissions) | Erreur permissions | ✅ |

---

## 🔍 Commandes Utiles

### Voir les Logs
```bash
adb logcat | grep "TrackingActivity"
```

### Voir les Erreurs
```bash
adb logcat | grep "ERROR\|Exception"
```

### Voir les Clics
```bash
adb logcat | grep "button clicked"
```

### Vérifier MySQL
```bash
mysql -u root -p fyourf_db
SELECT * FROM trajectories ORDER BY created_at DESC LIMIT 1;
```

---

## ✅ Résultat Final

Si tous les tests passent:
- ✅ Tous les boutons fonctionnent
- ✅ Gestion d'erreurs complète
- ✅ Logs détaillés
- ✅ Données sauvegardées dans MySQL
- ✅ Application prête pour la production

---

**Date**: 2025-11-06
**Status**: ✅ GUIDE DE TEST COMPLET

