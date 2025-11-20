@echo off
REM ============================================
REM Script de vérification des données MySQL
REM ============================================

setlocal enabledelayedexpansion

echo.
echo ========================================
echo   VERIFICATION DES DONNEES MYSQL
echo ========================================
echo.

REM Configuration
set MYSQL_HOST=192.168.178.115
set MYSQL_USER=root
set MYSQL_PASS=
set MYSQL_DB=fyourf_db
set MYSQL_PORT=3306

echo Configuration:
echo - Serveur: %MYSQL_HOST%
echo - Port: %MYSQL_PORT%
echo - Base de donnees: %MYSQL_DB%
echo - Utilisateur: %MYSQL_USER%
echo.

REM Test de connexion
echo Test de connexion...
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT 1;" >nul 2>&1

if %errorlevel% equ 0 (
    echo [OK] Connexion reussie!
    echo.
) else (
    echo [ERREUR] Impossible de se connecter a MySQL
    echo Assurez-vous que:
    echo - MySQL est en cours d'execution
    echo - L'adresse IP est correcte: %MYSQL_HOST%
    echo - Le port est correct: %MYSQL_PORT%
    pause
    exit /b 1
)

REM ============================================
REM 1. VERIFIER LES POSITIONS
REM ============================================
echo.
echo ========================================
echo 1. POSITIONS (Historique de localisation)
echo ========================================
echo.

mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Positions' FROM positions;"

echo.
echo Dernieres positions:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT idposition, numero, pseudo, latitude, longitude, FROM_UNIXTIME(timestamp/1000) as date FROM positions ORDER BY timestamp DESC LIMIT 5;"

REM ============================================
REM 2. VERIFIER LES QUESTIONS DU QUIZ
REM ============================================
echo.
echo ========================================
echo 2. QUESTIONS DU QUIZ
echo ========================================
echo.

mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Questions' FROM geoquiz_questions;"

echo.
echo Dernieres questions:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT id, user_id, region, category, difficulty, correct_answer FROM geoquiz_questions ORDER BY id DESC LIMIT 5;"

REM ============================================
REM 3. VERIFIER LES SCORES
REM ============================================
echo.
echo ========================================
echo 3. SCORES DU QUIZ
echo ========================================
echo.

mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Scores' FROM geoquiz_scores;"

echo.
echo Derniers scores:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT id, user_id, total_points, correct_answers, total_questions, accuracy FROM geoquiz_scores ORDER BY score_date DESC LIMIT 5;"

REM ============================================
REM 4. VERIFIER LES BADGES
REM ============================================
echo.
echo ========================================
echo 4. BADGES
echo ========================================
echo.

mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Badges' FROM geoquiz_badges;"

echo.
echo Badges deverrouilles:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT id, user_id, badge_id, unlocked, progress FROM geoquiz_badges WHERE unlocked = 1 LIMIT 5;"

REM ============================================
REM 5. RESUME STATISTIQUE
REM ============================================
echo.
echo ========================================
echo 5. RESUME STATISTIQUE
echo ========================================
echo.

mysql -h %MYSQL_HOST% -u %MYSQL_USER% -P %MYSQL_PORT% %MYSQL_DB% -e "SELECT (SELECT COUNT(*) FROM positions) as 'Positions', (SELECT COUNT(*) FROM geoquiz_questions) as 'Questions', (SELECT COUNT(*) FROM geoquiz_scores) as 'Scores', (SELECT COUNT(*) FROM geoquiz_badges) as 'Badges';"

echo.
echo ========================================
echo Verification terminee!
echo ========================================
echo.

pause

