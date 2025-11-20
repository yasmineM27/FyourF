# ============================================
# Script PowerShell de vérification MySQL
# ============================================

# Configuration
$MYSQL_HOST = "192.168.178.115"
$MYSQL_USER = "root"
$MYSQL_PASS = ""
$MYSQL_DB = "fyourf_db"
$MYSQL_PORT = 3306

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION DES DONNEES MYSQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "- Serveur: $MYSQL_HOST" -ForegroundColor Yellow
Write-Host "- Port: $MYSQL_PORT" -ForegroundColor Yellow
Write-Host "- Base de donnees: $MYSQL_DB" -ForegroundColor Yellow
Write-Host "- Utilisateur: $MYSQL_USER" -ForegroundColor Yellow
Write-Host ""

# Fonction pour exécuter une requête MySQL
function Execute-MySQLQuery {
    param(
        [string]$Query,
        [string]$Description
    )
    
    Write-Host $Description -ForegroundColor Green
    
    try {
        $cmd = "mysql -h $MYSQL_HOST -u $MYSQL_USER -P $MYSQL_PORT $MYSQL_DB -e `"$Query`""
        Invoke-Expression $cmd
        Write-Host ""
    } catch {
        Write-Host "Erreur: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
    }
}

# Test de connexion
Write-Host "Test de connexion..." -ForegroundColor Cyan
try {
    $cmd = "mysql -h $MYSQL_HOST -u $MYSQL_USER -P $MYSQL_PORT $MYSQL_DB -e `"SELECT 1;`""
    Invoke-Expression $cmd | Out-Null
    Write-Host "[OK] Connexion reussie!" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "[ERREUR] Impossible de se connecter a MySQL" -ForegroundColor Red
    Write-Host "Assurez-vous que:" -ForegroundColor Yellow
    Write-Host "- MySQL est en cours d'execution" -ForegroundColor Yellow
    Write-Host "- L'adresse IP est correcte: $MYSQL_HOST" -ForegroundColor Yellow
    Write-Host "- Le port est correct: $MYSQL_PORT" -ForegroundColor Yellow
    exit 1
}

# ============================================
# 1. VERIFIER LES POSITIONS
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "1. POSITIONS (Historique de localisation)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Execute-MySQLQuery "SELECT COUNT(*) as 'Total Positions' FROM positions;" "Total de positions:"

Execute-MySQLQuery "SELECT idposition, numero, pseudo, latitude, longitude, FROM_UNIXTIME(timestamp/1000) as date FROM positions ORDER BY timestamp DESC LIMIT 5;" "Dernieres positions:"

# ============================================
# 2. VERIFIER LES QUESTIONS DU QUIZ
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "2. QUESTIONS DU QUIZ" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Execute-MySQLQuery "SELECT COUNT(*) as 'Total Questions' FROM geoquiz_questions;" "Total de questions:"

Execute-MySQLQuery "SELECT id, user_id, region, category, difficulty, correct_answer FROM geoquiz_questions ORDER BY id DESC LIMIT 5;" "Dernieres questions:"

# ============================================
# 3. VERIFIER LES SCORES
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "3. SCORES DU QUIZ" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Execute-MySQLQuery "SELECT COUNT(*) as 'Total Scores' FROM geoquiz_scores;" "Total de scores:"

Execute-MySQLQuery "SELECT id, user_id, total_points, correct_answers, total_questions, accuracy FROM geoquiz_scores ORDER BY score_date DESC LIMIT 5;" "Derniers scores:"

# ============================================
# 4. VERIFIER LES BADGES
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "4. BADGES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Execute-MySQLQuery "SELECT COUNT(*) as 'Total Badges' FROM geoquiz_badges;" "Total de badges:"

Execute-MySQLQuery "SELECT id, user_id, badge_id, unlocked, progress FROM geoquiz_badges WHERE unlocked = 1 LIMIT 5;" "Badges deverrouilles:"

# ============================================
# 5. RESUME STATISTIQUE
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "5. RESUME STATISTIQUE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Execute-MySQLQuery "SELECT (SELECT COUNT(*) FROM positions) as 'Positions', (SELECT COUNT(*) FROM geoquiz_questions) as 'Questions', (SELECT COUNT(*) FROM geoquiz_scores) as 'Scores', (SELECT COUNT(*) FROM geoquiz_badges) as 'Badges';" "Resume:"

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Verification terminee!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

