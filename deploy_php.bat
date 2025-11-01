@echo off
echo ========================================
echo Deploiement des fichiers PHP - FyourF
echo ========================================
echo.

REM Detecter XAMPP ou WAMP
set XAMPP_PATH=C:\xampp\htdocs
set WAMP_PATH=C:\wamp64\www

if exist "%XAMPP_PATH%" (
    echo [OK] XAMPP detecte: %XAMPP_PATH%
    set DEST_PATH=%XAMPP_PATH%
) else if exist "%WAMP_PATH%" (
    echo [OK] WAMP detecte: %WAMP_PATH%
    set DEST_PATH=%WAMP_PATH%
) else (
    echo [ERREUR] XAMPP ou WAMP non trouve!
    echo.
    echo Veuillez installer XAMPP ou WAMP, ou modifier ce script.
    pause
    exit /b 1
)

echo.
echo Copie des fichiers PHP...
echo Source: %~dp0servicephp
echo Destination: %DEST_PATH%\servicephp
echo.

REM Creer le dossier de destination s'il n'existe pas
if not exist "%DEST_PATH%\servicephp" (
    mkdir "%DEST_PATH%\servicephp"
    echo [OK] Dossier cree: %DEST_PATH%\servicephp
)

REM Copier tous les fichiers PHP
xcopy /Y /E "%~dp0servicephp\*.*" "%DEST_PATH%\servicephp\"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo [SUCCESS] Fichiers copies avec succes!
    echo ========================================
    echo.
    echo Testez maintenant:
    echo http://192.168.1.18/servicephp/get_all.php
    echo.
) else (
    echo.
    echo [ERREUR] Echec de la copie!
    echo Verifiez les permissions ou executez en tant qu'administrateur.
    echo.
)

pause

