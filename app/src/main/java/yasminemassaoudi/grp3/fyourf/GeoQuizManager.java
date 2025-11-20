package yasminemassaoudi.grp3.fyourf;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire principal du système GeoQuiz
 * Gère la génération de questions, les badges et les scores
 */
public class GeoQuizManager {
    private static final String TAG = "GeoQuizManager";
    
    private Context context;
    private GeoQuizDatabase database;
    private List<GeoQuizQuestion> currentQuestions;
    private List<Badge> badges;
    private int currentScore;
    private int correctAnswers;
    private int currentStreak;
    private int maxStreak;

    // Régions de Tunisie
    private static final String[] REGIONS = {
        "Tunis", "Sfax", "Sousse", "Kairouan", "Gafsa", "Tozeur", "Douz",
        "Djerba", "Tataouine", "Kebili", "Kasserine", "Sidi Bouzid"
    };

    // Catégories
    private static final String[] CATEGORIES = {
        "Plage", "Montagne", "Ville", "Désert", "Oasis", "Historique", "Moderne"
    };

    public GeoQuizManager(Context context) {
        this.context = context;
        this.currentQuestions = new ArrayList<>();
        this.badges = new ArrayList<>();
        this.currentScore = 0;
        this.correctAnswers = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;

        try {
            this.database = new GeoQuizDatabase(context);
            initializeBadges();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation du gestionnaire", e);
            // Continuer sans base de données
            this.database = null;
        }
    }

    /**
     * Initialise les badges prédéfinis
     */
    private void initializeBadges() {
        try {
            if (database == null) {
                Log.w(TAG, "Base de données non disponible");
                return;
            }

            // Récupérer les badges existants une seule fois
            List<Badge> existingBadges = database.getAllBadges();
            Map<Integer, Badge> existingMap = new HashMap<>();
            if (existingBadges != null) {
                for (Badge b : existingBadges) {
                    if (b != null) {
                        existingMap.put(b.getId(), b);
                    }
                }
            }

            // Ajouter les badges prédéfinis
            Badge[] predefinedBadges = Badge.getPredefinedBadges();
            if (predefinedBadges != null) {
                for (Badge badge : predefinedBadges) {
                    if (badge != null) {
                        if (existingMap.containsKey(badge.getId())) {
                            // Badge existe déjà
                            badges.add(existingMap.get(badge.getId()));
                        } else {
                            // Nouveau badge
                            database.addBadge(badge);
                            badges.add(badge);
                        }
                    }
                }
            }
            Log.d(TAG, "Badges initialisés: " + badges.size());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation des badges", e);
            // Ajouter les badges prédéfinis sans base de données
            Badge[] predefinedBadges = Badge.getPredefinedBadges();
            if (predefinedBadges != null) {
                for (Badge badge : predefinedBadges) {
                    if (badge != null) {
                        badges.add(badge);
                    }
                }
            }
        }
    }

    /**
     * Génère des questions basées sur l'historique de localisation
     */
    public List<GeoQuizQuestion> generateQuestionsFromHistory(List<Position> positions, int count) {
        try {
            currentQuestions.clear();

            if (positions == null || positions.isEmpty()) {
                Log.w(TAG, "Aucune position disponible");
                return currentQuestions;
            }

            // Limiter le nombre de positions
            int limit = Math.min(count, positions.size());
            List<Position> selectedPositions = new ArrayList<>(positions.subList(0, limit));
            Collections.shuffle(selectedPositions);

            for (Position pos : selectedPositions) {
                if (pos != null) {
                    GeoQuizQuestion question = createQuestionFromPosition(pos);
                    if (question != null) {
                        currentQuestions.add(question);
                        if (database != null) {
                            try {
                                database.addQuestion(question);
                            } catch (Exception e) {
                                Log.w(TAG, "Erreur lors de l'ajout de la question à la base de données", e);
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "Généré " + currentQuestions.size() + " questions");
            return currentQuestions;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la génération des questions", e);
            return currentQuestions;
        }
    }

    /**
     * Crée une question à partir d'une position
     */
    private GeoQuizQuestion createQuestionFromPosition(Position position) {
        try {
            String region = getClosestRegion(position.getLatitude(), position.getLongitude());
            String category = CATEGORIES[(int)(Math.random() * CATEGORIES.length)];
            int difficulty = (int)(Math.random() * 3) + 1;

            GeoQuizQuestion question = new GeoQuizQuestion(
                    (int)System.currentTimeMillis(),
                    position.getLatitude(),
                    position.getLongitude(),
                    "Lieu " + position.getLatitude() + ", " + position.getLongitude(),
                    region,
                    region,
                    category,
                    difficulty
            );

            // Générer les options
            question.addOption(region);
            for (int i = 0; i < 3; i++) {
                String wrongRegion = REGIONS[(int)(Math.random() * REGIONS.length)];
                if (!wrongRegion.equals(region)) {
                    question.addOption(wrongRegion);
                }
            }
            question.shuffleOptions();

            // Générer les URLs des images
            String mapUrl = generateMapImageUrl(position.getLatitude(), position.getLongitude());
            question.setMapImageUrl(mapUrl);

            return question;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la création de la question", e);
            return null;
        }
    }

    /**
     * Génère l'URL de l'image de la carte
     * NOTE: Google Maps Static API requires proper API key configuration
     * Using placeholder approach instead
     */
    private String generateMapImageUrl(double latitude, double longitude) {
        // Since the API key doesn't have Static Maps API enabled,
        // we'll use a placeholder URL that will be handled by Glide
        // The actual map image will be generated locally in the fragment
        String region = getClosestRegion(latitude, longitude);

        // Return a special marker that indicates we should use a local placeholder
        return "local_map:" + region + ":" + latitude + ":" + longitude;
    }

    /**
     * Trouve la région la plus proche
     */
    private String getClosestRegion(double latitude, double longitude) {
        // Coordonnées approximatives des régions tunisiennes
        Map<String, double[]> regionCoords = new HashMap<>();
        regionCoords.put("Tunis", new double[]{36.8065, 10.1815});
        regionCoords.put("Sfax", new double[]{34.7406, 10.7603});
        regionCoords.put("Sousse", new double[]{35.8256, 10.6369});
        regionCoords.put("Kairouan", new double[]{35.6781, 9.9197});
        regionCoords.put("Gafsa", new double[]{34.4258, 8.7839});
        regionCoords.put("Tozeur", new double[]{33.9197, 8.1339});
        regionCoords.put("Douz", new double[]{33.4647, 9.0339});
        regionCoords.put("Djerba", new double[]{33.8869, 10.9369});
        regionCoords.put("Tataouine", new double[]{32.9289, 10.4547});

        String closestRegion = "Tunis";
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<String, double[]> entry : regionCoords.entrySet()) {
            double[] coords = entry.getValue();
            double distance = calculateDistance(latitude, longitude, coords[0], coords[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closestRegion = entry.getKey();
            }
        }

        return closestRegion;
    }

    /**
     * Calcule la distance entre deux points (Haversine)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Traite la réponse de l'utilisateur
     */
    public boolean answerQuestion(GeoQuizQuestion question, String userAnswer) {
        try {
            if (question == null || userAnswer == null) {
                Log.w(TAG, "Question ou réponse null");
                return false;
            }

            boolean isCorrect = question.checkAnswer(userAnswer);

            if (database != null) {
                try {
                    database.updateQuestion(question);
                } catch (Exception e) {
                    Log.w(TAG, "Erreur lors de la mise à jour de la question", e);
                }
            }

            if (isCorrect) {
                currentScore += question.getPoints();
                correctAnswers++;
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
                checkBadgeUnlock();
            } else {
                currentStreak = 0;
            }

            return isCorrect;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du traitement de la réponse", e);
            return false;
        }
    }

    /**
     * Vérifie et déverrouille les badges
     */
    private void checkBadgeUnlock() {
        try {
            if (badges == null || badges.isEmpty()) {
                return;
            }

            for (Badge badge : badges) {
                if (badge != null && !badge.isUnlocked()) {
                    int progress = calculateBadgeProgress(badge);
                    badge.setProgress(progress);

                    if (progress >= 100) {
                        badge.setUnlocked(true);
                        if (database != null) {
                            try {
                                database.updateBadge(badge);
                            } catch (Exception e) {
                                Log.w(TAG, "Erreur lors de la mise à jour du badge", e);
                            }
                        }
                        Log.d(TAG, "Badge déverrouillé: " + badge.getName());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la vérification des badges", e);
        }
    }

    /**
     * Calcule la progression d'un badge
     */
    private int calculateBadgeProgress(Badge badge) {
        try {
            if (badge == null || currentQuestions == null || currentQuestions.isEmpty()) {
                return 0;
            }

            int correctInRegion = 0;
            for (GeoQuizQuestion q : currentQuestions) {
                if (q != null && q.isCorrect() && badge.getRegion() != null && q.getRegion() != null && q.getRegion().equals(badge.getRegion())) {
                    correctInRegion++;
                }
            }

            if (badge.getRequiredQuestions() <= 0) {
                return 0;
            }

            return (correctInRegion * 100) / badge.getRequiredQuestions();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du calcul de la progression du badge", e);
            return 0;
        }
    }

    /**
     * Termine la session et sauvegarde le score
     */
    public void endSession() {
        try {
            if (database != null) {
                database.addScore(currentScore, correctAnswers, currentQuestions.size());
            }
            Log.d(TAG, "Session terminée - Score: " + currentScore + ", Correctes: " + correctAnswers);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la fin de la session", e);
        }
    }

    /**
     * Réinitialise la session
     */
    public void resetSession() {
        currentScore = 0;
        correctAnswers = 0;
        currentStreak = 0;
        currentQuestions.clear();
    }

    /**
     * Déduit des points du score (pour les hints, pénalités, etc.)
     */
    public void deductPoints(int points) {
        currentScore = Math.max(0, currentScore - points);
        Log.d(TAG, "Points déduits: " + points + ", Score actuel: " + currentScore);
    }

    // Getters
    public List<GeoQuizQuestion> getCurrentQuestions() { return currentQuestions; }
    public List<Badge> getBadges() { return badges; }
    public int getCurrentScore() { return currentScore; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getCurrentStreak() { return currentStreak; }
    public int getMaxStreak() { return maxStreak; }
    public int getTotalQuestions() { return currentQuestions.size(); }
    public double getAccuracy() {
        return currentQuestions.isEmpty() ? 0 : (correctAnswers * 100.0) / currentQuestions.size();
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }
}

