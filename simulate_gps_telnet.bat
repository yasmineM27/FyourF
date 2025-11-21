@echo off
chcp 65001 >nul
echo ========================================
echo  SIMULATION GPS - MÉTHODE TELNET
echo ========================================
echo.
echo Ce script utilise la console telnet de l'émulateur
echo pour envoyer les positions GPS.
echo.
echo Assurez-vous que:
echo  1. L'émulateur est lancé
echo  2. L'application FyourF est ouverte
echo  3. Vous êtes sur TrackingActivity
echo  4. Le tracking est DÉMARRÉ
echo.
pause

echo.
echo Recherche du port telnet de l'émulateur...
adb devices

echo.
echo ========================================
echo  ENVOI DES POSITIONS GPS
echo ========================================
echo.

echo [1/20] Départ - Position initiale
(
echo geo fix 2.2945 48.8584
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [2/20] Déplacement lent vers le nord
(
echo geo fix 2.2950 48.8590
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [3/20] Accélération - vitesse moyenne
(
echo geo fix 2.2960 48.8600
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [4/20] Vitesse moyenne
(
echo geo fix 2.2975 48.8615
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [5/20] Accélération - vitesse rapide
(
echo geo fix 2.2995 48.8635
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [6/20] Vitesse rapide
(
echo geo fix 2.3020 48.8660
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [7/20] Vitesse maximale
(
echo geo fix 2.3050 48.8690
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [8/20] Virage vers l'est - ralentissement
(
echo geo fix 2.3080 48.8700
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [9/20] Continue vers l'est
(
echo geo fix 2.3110 48.8705
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [10/20] Ralentissement
(
echo geo fix 2.3130 48.8708
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo.
echo ========================================
echo  SIMULATION DE PAUSE
echo ========================================
echo.

echo [11/20] Très lent - début pause
(
echo geo fix 2.3132 48.8709
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [12/20] Très lent - pause
(
echo geo fix 2.3133 48.8709
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [13/20] Très lent - pause confirmée
(
echo geo fix 2.3134 48.8710
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo.
echo ========================================
echo  REPRISE DU MOUVEMENT
echo ========================================
echo.

echo [14/20] Reprise - accélération
(
echo geo fix 2.3145 48.8715
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [15/20] Virage vers le sud
(
echo geo fix 2.3150 48.8700
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [16/20] Retour vers le départ
(
echo geo fix 2.3100 48.8650
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [17/20] Continue vers le départ
(
echo geo fix 2.3050 48.8620
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [18/20] Approche du départ
(
echo geo fix 2.3000 48.8600
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [19/20] Très proche du départ
(
echo geo fix 2.2970 48.8590
timeout /t 2 >nul
) | telnet localhost 5554

timeout /t 3 >nul

echo [20/20] RETOUR AU DEPART - Circuit fermé!
(
echo geo fix 2.2948 48.8586
timeout /t 2 >nul
) | telnet localhost 5554

echo.
echo ========================================
echo  SIMULATION TERMINÉE!
echo ========================================
echo.
echo Vous devriez maintenant voir:
echo  - Segments colorés (vert/jaune/orange/rouge)
echo  - Marqueur de pause (orange)
echo  - Marqueur de reprise (cyan)
echo  - Marqueur "Circuit Fermé!" (rouge)
echo  - Ligne pointillée vers le départ
echo.
pause

