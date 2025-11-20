@echo off
REM ============================================
REM Script de Vérification des Données MySQL
REM ============================================
REM Serveur: localhost
REM Base de données: fyourf_db
REM Utilisateur: root
REM Mot de passe: (vide)
REM ============================================

setlocal enabledelayedexpansion

set MYSQL_HOST=localhost
set MYSQL_USER=root
set MYSQL_PASS=
set MYSQL_DB=fyourf_db

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         VÉRIFICATION DES DONNÉES MYSQL - FyourF               ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Test de connexion
echo 🔍 Test de connexion MySQL...
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERREUR: Impossible de se connecter à MySQL
    echo    Vérifier que MySQL est en cours d'exécution
    pause
    exit /b 1
)
echo ✅ Connexion réussie!
echo.

REM Vérifier les positions
echo 📍 POSITIONS
echo ─────────────────────────────────────────────────────────────────
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Positions' FROM positions;"
echo.
echo Dernières positions:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT idposition, numero, pseudo, latitude, longitude FROM positions ORDER BY timestamp DESC LIMIT 5;"
echo.

REM Vérifier les questions
echo 📚 QUESTIONS DU QUIZ
echo ─────────────────────────────────────────────────────────────────
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Questions' FROM geoquiz_questions;"
echo.
echo Questions par région:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT region, COUNT(*) as count FROM geoquiz_questions GROUP BY region;"
echo.

REM Vérifier les scores
echo 🎯 SCORES
echo ─────────────────────────────────────────────────────────────────
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Scores' FROM geoquiz_scores;"
echo.
echo Statistiques des scores:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT AVG(total_points) as 'Moyenne Points', MAX(total_points) as 'Max Points', AVG(accuracy) as 'Précision Moyenne' FROM geoquiz_scores;"
echo.

REM Vérifier les badges
echo 🏆 BADGES
echo ─────────────────────────────────────────────────────────────────
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT COUNT(*) as 'Total Badges' FROM geoquiz_badges;"
echo.
echo Badges déverrouillés:
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT COUNT(*) as 'Badges Déverrouillés' FROM geoquiz_badges WHERE unlocked = 1;"
echo.

REM Résumé complet
echo 📊 RÉSUMÉ COMPLET
echo ─────────────────────────────────────────────────────────────────
mysql -h %MYSQL_HOST% -u %MYSQL_USER% %MYSQL_DB% -e "SELECT 'Positions' as table_name, COUNT(*) as total FROM positions UNION ALL SELECT 'Questions', COUNT(*) FROM geoquiz_questions UNION ALL SELECT 'Scores', COUNT(*) FROM geoquiz_scores UNION ALL SELECT 'Badges', COUNT(*) FROM geoquiz_badges;"
echo.

echo ✅ Vérification terminée!
echo.
pause

