# 🔧 Guide d'Intégration GeoQuiz - Étapes Détaillées

**Date**: 2025-11-07
**Status**: 📋 À FAIRE

---

## 📋 Étapes d'Intégration

### Étape 1: Ajouter les Dépendances

#### Fichier: `app/build.gradle.kts`

```gradle
dependencies {
    // Glide pour charger les images
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.15.1'
}
```

---

### Étape 2: Ajouter le Menu Item

#### Fichier: `app/src/main/res/menu/bottom_nav_menu.xml`

```xml
<item
    android:id="@+id/navigation_geoquiz"
    android:icon="@drawable/ic_quiz"
    android:title="GeoQuiz" />

<item
    android:id="@+id/navigation_badges"
    android:icon="@drawable/ic_badges"
    android:title="Badges" />
```

---

### Étape 3: Créer les Icônes

#### Fichier: `app/src/main/res/drawable/ic_quiz.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M19,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM9,17H7v-7h2V17zM13,17h-2V7h2V17zM17,17h-2v-4h2V17z" />
</vector>
```

#### Fichier: `app/src/main/res/drawable/ic_badges.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8zM12.5,13H11v6h1.5zM12,5.5c-0.83,0 -1.5,0.67 -1.5,1.5s0.67,1.5 1.5,1.5 1.5,-0.67 1.5,-1.5 -0.67,-1.5 -1.5,-1.5z" />
</vector>
```

---

### Étape 4: Ajouter les Routes de Navigation

#### Fichier: `app/src/main/res/navigation/mobile_navigation.xml`

```xml
<fragment
    android:id="@+id/navigation_geoquiz"
    android:name="yasminemassaoudi.grp3.fyourf.ui.geoquiz.GeoQuizFragment"
    android:label="GeoQuiz Challenge"
    tools:layout="@layout/fragment_geoquiz" />

<fragment
    android:id="@+id/navigation_badges"
    android:name="yasminemassaoudi.grp3.fyourf.ui.geoquiz.BadgesFragment"
    android:label="Mes Badges"
    tools:layout="@layout/fragment_badges" />
```

---

### Étape 5: Mettre à Jour MainActivity

#### Fichier: `app/src/main/java/yasminemassaoudi/grp3/fyourf/MainActivity.java`

```java
import yasminemassaoudi.grp3.fyourf.ui.geoquiz.GeoQuizFragment;
import yasminemassaoudi.grp3.fyourf.ui.geoquiz.BadgesFragment;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();

        // Configurer BottomNavigationView
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);

        // Gérer les clics sur les items du menu
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_geoquiz) {
                navController.navigate(R.id.navigation_geoquiz);
                return true;
            } else if (itemId == R.id.navigation_badges) {
                navController.navigate(R.id.navigation_badges);
                return true;
            }
            
            return false;
        });
    }
}
```

---

### Étape 6: Ajouter les Strings

#### Fichier: `app/src/main/res/values/strings.xml`

```xml
<string name="title_geoquiz">GeoQuiz Challenge</string>
<string name="title_badges">Mes Badges</string>
<string name="geoquiz_question">Où a été prise cette photo?</string>
<string name="geoquiz_submit">Valider</string>
<string name="geoquiz_next">Suivant</string>
<string name="geoquiz_correct">✅ Correct!</string>
<string name="geoquiz_incorrect">❌ Incorrect!</string>
<string name="geoquiz_score">Score: %d</string>
<string name="geoquiz_streak">Streak: %d</string>
<string name="geoquiz_accuracy">Précision: %.1f%%</string>
<string name="badge_unlocked">🏆 Badge Déverrouillé!</string>
```

---

### Étape 7: Configurer la Base de Données MySQL

```bash
# Exécuter le script SQL
mysql -h 192.168.56.1 -u root -p fyourf_db < geoquiz_mysql_setup.sql
```

---

### Étape 8: Compiler et Tester

```bash
# Compiler
.\gradlew.bat compileDebugJavaWithJavac

# Installer
.\gradlew.bat installDebug

# Lancer
adb shell am start -n yasminemassaoudi.grp3.fyourf/.MainActivity
```

---

## ✅ Checklist de Vérification

- [ ] Dépendances Glide ajoutées
- [ ] Menu items ajoutés
- [ ] Icônes créées
- [ ] Routes de navigation ajoutées
- [ ] MainActivity mise à jour
- [ ] Strings ajoutées
- [ ] Base de données MySQL configurée
- [ ] Compilation réussie
- [ ] Onglet GeoQuiz visible
- [ ] Onglet Badges visible
- [ ] Quiz fonctionne
- [ ] Badges s'affichent

---

## 🧪 Tests

### Test 1: Lancer le Quiz
1. Cliquer sur l'onglet "GeoQuiz"
2. Vérifier que les questions s'affichent
3. Répondre à une question
4. Vérifier que le score augmente

### Test 2: Afficher les Badges
1. Cliquer sur l'onglet "Badges"
2. Vérifier que les badges s'affichent
3. Vérifier la progression

### Test 3: Hors Ligne
1. Désactiver Internet
2. Lancer le quiz
3. Vérifier que les questions en cache s'affichent
4. Réactiver Internet
5. Vérifier la synchronisation

---

## 🔗 Fichiers Impliqués

### Fichiers à Modifier
- `app/build.gradle.kts` - Ajouter Glide
- `app/src/main/res/menu/bottom_nav_menu.xml` - Ajouter items
- `app/src/main/res/navigation/mobile_navigation.xml` - Ajouter routes
- `app/src/main/java/yasminemassaoudi/grp3/fyourf/MainActivity.java` - Ajouter navigation
- `app/src/main/res/values/strings.xml` - Ajouter strings

### Fichiers Créés
- `app/src/main/res/drawable/ic_quiz.xml`
- `app/src/main/res/drawable/ic_badges.xml`

### Fichiers Existants
- `GeoQuizFragment.java`
- `BadgesFragment.java`
- `GeoQuizManager.java`
- `GeoQuizDatabase.java`
- `fragment_geoquiz.xml`
- `fragment_badges.xml`

---

## 🚀 Prochaines Étapes

1. ✅ Créer les classes Java
2. ✅ Créer les layouts XML
3. ✅ Créer la base de données
4. ✅ Créer les scripts PHP
5. 📋 Intégrer dans MainActivity
6. 📋 Compiler et tester
7. 📋 Ajouter animations
8. 📋 Implémenter leaderboard UI

---

**Status**: 📋 À FAIRE
**Priorité**: 🔴 HAUTE
**Temps Estimé**: 30 minutes

