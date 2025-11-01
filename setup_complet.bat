@echo off
color 0A
echo ========================================
echo   INSTALLATION COMPLETE - FyourF
echo ========================================
echo.

REM Detecter XAMPP ou WAMP
set XAMPP_PATH=C:\xampp
set WAMP_PATH=C:\wamp64

if exist "%XAMPP_PATH%" (
    echo [OK] XAMPP detecte
    set SERVER_PATH=%XAMPP_PATH%
    set HTDOCS=%XAMPP_PATH%\htdocs
    set MYSQL_BIN=%XAMPP_PATH%\mysql\bin\mysql.exe
) else if exist "%WAMP_PATH%" (
    echo [OK] WAMP detecte
    set SERVER_PATH=%WAMP_PATH%
    set HTDOCS=%WAMP_PATH%\www
    set MYSQL_BIN=%WAMP_PATH%\bin\mysql\mysql8.0.31\bin\mysql.exe
) else (
    echo [ERREUR] XAMPP ou WAMP non trouve!
    echo Veuillez installer XAMPP ou WAMP d'abord.
    pause
    exit /b 1
)

echo.
echo ========================================
echo ETAPE 1/4 : Deploiement des fichiers PHP
echo ========================================
echo.

if not exist "%HTDOCS%\servicephp" (
    mkdir "%HTDOCS%\servicephp"
)

xcopy /Y /E "%~dp0servicephp\*.*" "%HTDOCS%\servicephp\"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Fichiers PHP copies dans %HTDOCS%\servicephp\
) else (
    echo [ERREUR] Echec de la copie des fichiers PHP
    pause
    exit /b 1
)

echo.
echo ========================================
echo ETAPE 2/4 : Creation de la base de donnees
echo ========================================
echo.

echo Tentative de connexion a MySQL...
echo (Appuyez sur Entree si demande de mot de passe)
echo.

"%MYSQL_BIN%" -u root -p < "%~dp0database_simple.sql"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Base de donnees creee
) else (
    echo [ATTENTION] Erreur lors de la creation de la BDD
    echo Vous pouvez la creer manuellement avec phpMyAdmin
)

echo.
echo ========================================
echo ETAPE 3/4 : Insertion des donnees de test
echo ========================================
echo.

"%MYSQL_BIN%" -u root -p < "%~dp0insert_test_data.sql"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Donnees de test inserees
) else (
    echo [ATTENTION] Erreur lors de l'insertion des donnees
)

echo.
echo ========================================
echo ETAPE 4/4 : Test de connexion
echo ========================================
echo.

set IP=192.168.1.18
set URL=http://%IP%/servicephp/get_all.php

echo Test de l'URL: %URL%
echo.

where curl >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    curl -s %URL%
    echo.
) else (
    echo curl non disponible, ouverture dans le navigateur...
    start %URL%
)

echo.
echo ========================================
echo   INSTALLATION TERMINEE !
echo ========================================
echo.
echo Prochaines etapes:
echo.
echo 1. Verifiez que Apache et MySQL sont demarres (XAMPP Control Panel)
echo.
echo 2. Testez dans votre navigateur:
echo    http://192.168.1.18/servicephp/get_all.php
echo.
echo 3. Vous devriez voir:
echo    {"success":true,"count":7,"data":[...],...}
echo.
echo 4. Dans Android Studio:
echo    - Ouvrez Config.java
echo    - Verifiez que MYSQL_SERVER_IP = "192.168.1.18"
echo    - Compilez et installez l'app
echo.
echo 5. Dans l'application:
echo    - Onglet History
echo    - Cliquez Refresh
echo    - Les positions devraient apparaitre!
echo.
echo ========================================
echo.

pause

