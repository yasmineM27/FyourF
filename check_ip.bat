@echo off
color 0B
echo ========================================
echo   VERIFICATION DE L'ADRESSE IP
echo ========================================
echo.

REM Obtenir l'adresse IPv4
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set IP=%%a
    goto :found
)

:found
REM Supprimer les espaces
set IP=%IP: =%

echo [INFO] Votre adresse IPv4 actuelle:
echo.
echo        %IP%
echo.
echo ========================================
echo.

REM Afficher le contenu actuel de Config.java
echo [INFO] Configuration actuelle dans Config.java:
echo.
findstr "MYSQL_SERVER_IP" "app\src\main\java\yasminemassaoudi\grp3\fyourf\Config.java"
echo.
echo ========================================
echo.

echo [ACTION REQUISE]
echo.
echo 1. Verifiez que l'IP ci-dessus (%IP%) est correcte
echo.
echo 2. Si Config.java a une IP differente, vous devez:
echo    - Modifier Config.java manuellement
echo    - OU utiliser Android Studio pour changer l'IP
echo.
echo 3. Apres modification:
echo    - Recompilez l'application (Build ^> Rebuild Project)
echo    - Reinstallez sur le smartphone
echo.
echo ========================================
echo.

echo [TEST DE CONNEXION]
echo.
echo Testez cette URL dans le navigateur de votre smartphone:
echo.
echo    http://%IP%/servicephp/get_all.php
echo.
echo Si ca fonctionne dans le navigateur mais pas dans l'app:
echo    =^> L'IP dans Config.java est incorrecte
echo.
echo Si ca ne fonctionne pas dans le navigateur:
echo    =^> Probleme de reseau (WiFi, pare-feu)
echo.
echo ========================================
echo.

REM Tester si le serveur repond
echo [TEST SERVEUR LOCAL]
echo.
curl -s http://%IP%/servicephp/get_all.php >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Le serveur repond sur http://%IP%/servicephp/get_all.php
    echo.
    echo Resultat:
    curl -s http://%IP%/servicephp/get_all.php
) else (
    echo [ERREUR] Le serveur ne repond pas
    echo.
    echo Verifiez que:
    echo - Apache est demarre (XAMPP Control Panel)
    echo - Les fichiers PHP sont dans C:\xampp\htdocs\servicephp\
)

echo.
echo ========================================
echo.

pause

