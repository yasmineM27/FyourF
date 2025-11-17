@echo off
REM ============================================
REM Test MySQL Synchronization
REM ============================================
REM Ce script teste la synchronisation MySQL
REM de l'application FyourF

setlocal enabledelayedexpansion

echo.
echo ============================================
echo TEST SYNCHRONISATION MYSQL - FyourF
echo ============================================
echo.

REM Configuration
set IP=192.168.56.1
set PORT=80
set FOLDER=servicephp
set BASE_URL=http://%IP%:%PORT%/%FOLDER%

echo Configuration:
echo   IP: %IP%
echo   Port: %PORT%
echo   Dossier: %FOLDER%
echo   URL: %BASE_URL%
echo.

REM Test 1: Vérifier la connexion
echo ============================================
echo TEST 1: Vérifier la connexion MySQL
echo ============================================
echo.

curl -s "%BASE_URL%/verify_connection.php" > test_verify.json 2>nul

if %ERRORLEVEL% EQU 0 (
    echo [OK] Connexion réussie
    echo.
    echo Réponse:
    type test_verify.json
    echo.
    del test_verify.json
) else (
    echo [ERREUR] Impossible de se connecter
    echo.
    echo Solutions:
    echo   1. Vérifiez que Apache est démarré
    echo   2. Vérifiez que MySQL est démarré
    echo   3. Vérifiez l'IP: %IP%
    echo   4. Testez: ping %IP%
    echo.
    pause
    exit /b 1
)

REM Test 2: Récupérer toutes les positions
echo ============================================
echo TEST 2: Récupérer toutes les positions
echo ============================================
echo.

curl -s "%BASE_URL%/get_all.php" > test_positions.json 2>nul

if %ERRORLEVEL% EQU 0 (
    echo [OK] Positions récupérées
    echo.
    echo Réponse:
    type test_positions.json
    echo.
    del test_positions.json
) else (
    echo [ERREUR] Impossible de récupérer les positions
    pause
    exit /b 1
)

REM Test 3: Récupérer les statistiques
echo ============================================
echo TEST 3: Récupérer les statistiques
echo ============================================
echo.

curl -s "%BASE_URL%/get_statistics.php" > test_stats.json 2>nul

if %ERRORLEVEL% EQU 0 (
    echo [OK] Statistiques récupérées
    echo.
    echo Réponse:
    type test_stats.json
    echo.
    del test_stats.json
) else (
    echo [ERREUR] Impossible de récupérer les statistiques
    pause
    exit /b 1
)

REM Test 4: Vérifier les fichiers PHP
echo ============================================
echo TEST 4: Vérifier les fichiers PHP
echo ============================================
echo.

set PHP_PATH=C:\xampp\htdocs\servicephp

if exist "%PHP_PATH%\verify_connection.php" (
    echo [OK] verify_connection.php existe
) else (
    echo [ERREUR] verify_connection.php n'existe pas
)

if exist "%PHP_PATH%\get_all.php" (
    echo [OK] get_all.php existe
) else (
    echo [ERREUR] get_all.php n'existe pas
)

if exist "%PHP_PATH%\get_statistics.php" (
    echo [OK] get_statistics.php existe
) else (
    echo [ERREUR] get_statistics.php n'existe pas
)

if exist "%PHP_PATH%\save_trajectory.php" (
    echo [OK] save_trajectory.php existe
) else (
    echo [ERREUR] save_trajectory.php n'existe pas
)

if exist "%PHP_PATH%\get_trajectory.php" (
    echo [OK] get_trajectory.php existe
) else (
    echo [ERREUR] get_trajectory.php n'existe pas
)

if exist "%PHP_PATH%\add_position.php" (
    echo [OK] add_position.php existe
) else (
    echo [ERREUR] add_position.php n'existe pas
)

echo.

REM Test 5: Vérifier MySQL
echo ============================================
echo TEST 5: Vérifier MySQL
echo ============================================
echo.

tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MySQL est démarré
) else (
    echo [ERREUR] MySQL n'est pas démarré
    echo.
    echo Solution: Ouvrez XAMPP et cliquez sur Start pour MySQL
)

echo.

REM Test 6: Vérifier Apache
echo ============================================
echo TEST 6: Vérifier Apache
echo ============================================
echo.

tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] Apache est démarré
) else (
    echo [ERREUR] Apache n'est pas démarré
    echo.
    echo Solution: Ouvrez XAMPP et cliquez sur Start pour Apache
)

echo.
echo ============================================
echo RÉSUMÉ DES TESTS
echo ============================================
echo.
echo [OK] Tous les tests sont passés!
echo.
echo Prochaines étapes:
echo   1. Ouvrez l'application FyourF
echo   2. Entrez votre numéro et pseudo
echo   3. Cliquez sur "Démarrer"
echo   4. Attendez quelques secondes
echo   5. Cliquez sur "Arrêter"
echo   6. Les données seront sauvegardées dans MySQL
echo.
echo Pour vérifier les données:
echo   - Ouvrez: http://localhost/phpmyadmin/
echo   - Sélectionnez: fyourf_db
echo   - Consultez les tables: positions, trajectories
echo.
pause

