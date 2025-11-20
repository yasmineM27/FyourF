package yasminemassaoudi.grp3.fyourf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modèle pour une question de GeoQuiz
 * Représente une question basée sur un lieu de l'historique
 */
public class GeoQuizQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private double latitude;
    private double longitude;
    private String locationName;
    private String correctAnswer;
    private List<String> options;
    private String mapImageUrl;
    private String streetViewUrl;
    private long timestamp;
    private int difficulty; // 1=facile, 2=moyen, 3=difficile
    private String region; // Région/Gouvernorat (ex: "Tunis", "Sfax")
    private String category; // Catégorie (ex: "Plage", "Montagne", "Ville")
    private boolean answered;
    private boolean correct;

    public GeoQuizQuestion() {
        this.options = new ArrayList<>();
        this.answered = false;
        this.correct = false;
    }

    public GeoQuizQuestion(int id, double latitude, double longitude, String locationName,
                          String correctAnswer, String region, String category, int difficulty) {
        this();
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.correctAnswer = correctAnswer;
        this.region = region;
        this.category = category;
        this.difficulty = difficulty;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public void addOption(String option) {
        if (!this.options.contains(option)) {
            this.options.add(option);
        }
    }

    public String getMapImageUrl() { return mapImageUrl; }
    public void setMapImageUrl(String mapImageUrl) { this.mapImageUrl = mapImageUrl; }

    public String getStreetViewUrl() { return streetViewUrl; }
    public void setStreetViewUrl(String streetViewUrl) { this.streetViewUrl = streetViewUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isAnswered() { return answered; }
    public void setAnswered(boolean answered) { this.answered = answered; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    /**
     * Mélange les options pour l'affichage
     */
    public void shuffleOptions() {
        Collections.shuffle(this.options);
    }

    /**
     * Vérifie si la réponse est correcte
     */
    public boolean checkAnswer(String userAnswer) {
        this.answered = true;
        this.correct = userAnswer.equalsIgnoreCase(this.correctAnswer);
        return this.correct;
    }

    /**
     * Obtient le texte de difficulté
     */
    public String getDifficultyText() {
        switch (difficulty) {
            case 1: return "Facile";
            case 2: return "Moyen";
            case 3: return "Difficile";
            default: return "Inconnu";
        }
    }

    /**
     * Obtient les points basés sur la difficulté
     */
    public int getPoints() {
        if (!correct) return 0;
        switch (difficulty) {
            case 1: return 10;
            case 2: return 25;
            case 3: return 50;
            default: return 0;
        }
    }

    @Override
    public String toString() {
        return "GeoQuizQuestion{" +
                "id=" + id +
                ", locationName='" + locationName + '\'' +
                ", region='" + region + '\'' +
                ", category='" + category + '\'' +
                ", difficulty=" + difficulty +
                ", answered=" + answered +
                ", correct=" + correct +
                '}';
    }
}

