@echo off
echo ========================================
echo  SIMULATION GPS - TRAJET EN CIRCUIT
echo ========================================
echo.
echo Ce script va simuler un trajet GPS en boucle
echo pour tester les nouvelles fonctionnalites:
echo.
echo  - Polylines colorees par vitesse
echo  - Detection de circuit ferme
echo  - Detection de pause
echo  - Marqueurs varies
echo.
echo Assurez-vous que:
echo  1. L'emulateur est lance
echo  2. L'application FyourF est ouverte
echo  3. Vous etes sur l'ecran TrackingActivity
echo  4. Le tracking est DEMARRE
echo.
pause

echo.
echo [1/20] Depart - Position initiale (Paris, Tour Eiffel)
adb shell "geo fix 2.2945 48.8584"
timeout /t 5 /nobreak

echo [2/20] Deplacement lent vers le nord (vitesse ~5 km/h)
adb shell "geo fix 2.2950 48.8590"
timeout /t 5 /nobreak

echo [3/20] Acceleration - vitesse moyenne (vitesse ~15 km/h)
adb shell "geo fix 2.2960 48.8600"
timeout /t 5 /nobreak

echo [4/20] Vitesse moyenne (vitesse ~25 km/h)
adb shell "geo fix 2.2975 48.8615"
timeout /t 5 /nobreak

echo [5/20] Acceleration - vitesse rapide (vitesse ~35 km/h)
adb shell "geo fix 2.2995 48.8635"
timeout /t 5 /nobreak

echo [6/20] Vitesse rapide (vitesse ~45 km/h)
adb shell "geo fix 2.3020 48.8660"
timeout /t 5 /nobreak

echo [7/20] Vitesse maximale (vitesse ~55 km/h)
adb shell "geo fix 2.3050 48.8690"
timeout /t 5 /nobreak

echo [8/20] Virage vers l'est - ralentissement (vitesse ~40 km/h)
adb shell "geo fix 2.3080 48.8700"
timeout /t 5 /nobreak

echo [9/20] Continue vers l'est (vitesse ~30 km/h)
adb shell "geo fix 2.3110 48.8705"
timeout /t 5 /nobreak

echo [10/20] Ralentissement (vitesse ~20 km/h)
adb shell "geo fix 2.3130 48.8708"
timeout /t 5 /nobreak

echo.
echo ========================================
echo  SIMULATION DE PAUSE
echo ========================================
echo Les 3 prochains points seront tres lents
echo pour declencher la detection de pause
echo.

echo [11/20] Tres lent - debut pause (vitesse ~0.5 km/h)
adb shell "geo fix 2.3132 48.8709"
timeout /t 5 /nobreak

echo [12/20] Tres lent - pause (vitesse ~0.3 km/h)
adb shell "geo fix 2.3133 48.8709"
timeout /t 5 /nobreak

echo [13/20] Tres lent - pause confirmee (vitesse ~0.2 km/h)
adb shell "geo fix 2.3134 48.8710"
timeout /t 5 /nobreak

echo.
echo ========================================
echo  REPRISE DU MOUVEMENT
echo ========================================
echo.

echo [14/20] Reprise - acceleration (vitesse ~18 km/h)
adb shell "geo fix 2.3145 48.8715"
timeout /t 5 /nobreak

echo [15/20] Virage vers le sud (vitesse ~30 km/h)
adb shell "geo fix 2.3150 48.8700"
timeout /t 5 /nobreak

echo [16/20] Continue vers le sud (vitesse ~38 km/h)
adb shell "geo fix 2.3145 48.8680"
timeout /t 5 /nobreak

echo [17/20] Virage vers l'ouest - retour (vitesse ~35 km/h)
adb shell "geo fix 2.3120 48.8665"
timeout /t 5 /nobreak

echo [18/20] Continue vers l'ouest (vitesse ~28 km/h)
adb shell "geo fix 2.3080 48.8650"
timeout /t 5 /nobreak

echo [19/20] Ralentissement - approche du depart (vitesse ~15 km/h)
adb shell "geo fix 2.3040 48.8620"
timeout /t 5 /nobreak

echo.
echo ========================================
echo  RETOUR AU POINT DE DEPART
echo ========================================
echo Le prochain point est a moins de 50m du depart
echo Cela devrait declencher la detection de circuit!
echo.

echo [20/20] Retour au depart - CIRCUIT FERME! (vitesse ~8 km/h)
adb shell "geo fix 2.2948 48.8586"
timeout /t 5 /nobreak

echo.
echo ========================================
echo  SIMULATION TERMINEE!
echo ========================================
echo.
echo Vous devriez maintenant voir:
echo  - Des segments colores (vert, jaune, orange, rouge)
echo  - Un marqueur orange de pause (point 11-13)
echo  - Un marqueur cyan de reprise (point 14)
echo  - Un marqueur rouge "Circuit Ferme!" (point 20)
echo  - Une ligne pointillee verte vers le depart
echo  - Le cercle de detection change de couleur
echo.
echo Cliquez sur "Statistiques" pour voir:
echo  - Distance parcourue
echo  - Distance a vol d'oiseau
echo  - Vitesse moyenne, max, actuelle
echo  - Temps de pause
echo  - Statut circuit: Ferme
echo.
pause

