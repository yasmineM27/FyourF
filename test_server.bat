@echo off
echo ========================================
echo Test du Serveur PHP - FyourF
echo ========================================
echo.

set IP=192.168.1.18
set URL=http://%IP%/servicephp/get_all.php

echo Test de connexion au serveur...
echo URL: %URL%
echo.

REM Tester avec curl si disponible
where curl >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [Test avec curl]
    curl -s %URL%
    echo.
    echo.
) else (
    echo [curl non disponible, utilisez le navigateur]
    echo Ouvrez cette URL dans votre navigateur:
    echo %URL%
    echo.
    start %URL%
)

echo ========================================
echo Instructions:
echo ========================================
echo.
echo 1. Si vous voyez du JSON avec "success": true
echo    =^> Le serveur fonctionne correctement!
echo.
echo 2. Si vous voyez "404 Not Found"
echo    =^> Executez deploy_php.bat d'abord
echo.
echo 3. Si vous voyez "Connection refused"
echo    =^> Verifiez que Apache est demarre (XAMPP/WAMP)
echo.
echo 4. Si vous voyez "Database connection failed"
echo    =^> Verifiez que MySQL est demarre
echo    =^> Executez database_simple.sql
echo.

pause

