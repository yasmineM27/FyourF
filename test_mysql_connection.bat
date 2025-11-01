@echo off
chcp 65001 >nul
echo ========================================
echo TEST CONNEXION MYSQL - FyourF
echo ========================================
echo.

echo 1. Vérification Apache...
tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [✓] Apache est démarré
) else (
    echo [✗] Apache n'est pas démarré !
    echo.
    echo Solution : Ouvrez XAMPP Control Panel et cliquez sur Start pour Apache
    echo.
    pause
    exit
)

echo.
echo 2. Vérification MySQL...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [✓] MySQL est démarré
) else (
    echo [✗] MySQL n'est pas démarré !
    echo.
    echo Solution : Ouvrez XAMPP Control Panel et cliquez sur Start pour MySQL
    echo.
    pause
    exit
)

echo.
echo 3. Vérification fichiers PHP...
if exist "C:\xampp\htdocs\servicephp\get_all.php" (
    echo [✓] get_all.php existe
) else (
    echo [✗] get_all.php n'existe pas !
    echo.
    echo Solution : Exécutez fix_and_deploy.bat
    echo.
    pause
    exit
)

if exist "C:\xampp\htdocs\servicephp\add_position.php" (
    echo [✓] add_position.php existe
) else (
    echo [✗] add_position.php n'existe pas !
)

if exist "C:\xampp\htdocs\servicephp\config.php" (
    echo [✓] config.php existe
) else (
    echo [✗] config.php n'existe pas !
)

echo.
echo 4. Test connexion HTTP (localhost)...
curl -s http://localhost/servicephp/get_all.php > test_response.json 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [✓] Connexion HTTP réussie
    echo.
    echo Réponse :
    type test_response.json
    echo.
    del test_response.json
) else (
    echo [✗] Connexion HTTP échouée !
    echo.
    echo Vérifiez que Apache est bien démarré
)

echo.
echo 5. Votre adresse IP :
echo.
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    echo    %%a
)

echo.
echo ========================================
echo TESTEZ DEPUIS VOTRE SMARTPHONE :
echo.
echo Ouvrez Chrome et allez sur :
echo http://[VOTRE_IP]/servicephp/get_all.php
echo.
echo Remplacez [VOTRE_IP] par l'adresse IP ci-dessus
echo ========================================
echo.
pause

