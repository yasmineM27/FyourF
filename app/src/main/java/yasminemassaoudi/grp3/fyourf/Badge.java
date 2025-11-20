package yasminemassaoudi.grp3.fyourf;

import java.io.Serializable;

/**
 * Modèle pour un badge de GeoQuiz
 * Les badges sont débloqués en accomplissant des défis
 */
public class Badge implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private String region; // Région associée (ex: "Tunis", "Sfax")
    private String category; // Catégorie (ex: "Explorateur", "Voyageur")
    private int iconResId; // ID de la ressource drawable
    private int requiredQuestions; // Nombre de questions correctes requises
    private int requiredRegionVisits; // Nombre de visites dans la région
    private boolean unlocked;
    private long unlockedDate;
    private int progress; // Progression vers le déblocage (0-100)

    public Badge() {
        this.unlocked = false;
        this.progress = 0;
    }

    public Badge(int id, String name, String description, String region, String category,
                 int requiredQuestions, int requiredRegionVisits) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.region = region;
        this.category = category;
        this.requiredQuestions = requiredQuestions;
        this.requiredRegionVisits = requiredRegionVisits;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public int getRequiredQuestions() { return requiredQuestions; }
    public void setRequiredQuestions(int requiredQuestions) { this.requiredQuestions = requiredQuestions; }

    public int getRequiredRegionVisits() { return requiredRegionVisits; }
    public void setRequiredRegionVisits(int requiredRegionVisits) { this.requiredRegionVisits = requiredRegionVisits; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (unlocked) {
            this.unlockedDate = System.currentTimeMillis();
            this.progress = 100;
        }
    }

    public long getUnlockedDate() { return unlockedDate; }
    public void setUnlockedDate(long unlockedDate) { this.unlockedDate = unlockedDate; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) {
        this.progress = Math.min(100, Math.max(0, progress));
    }

    /**
     * Badges prédéfinis pour la Tunisie
     */
    public static Badge[] getPredefinedBadges() {
        return new Badge[]{
            // Badges par région
            new Badge(1, "🏙️ Tunis Explorer", "Répondez correctement à 5 questions sur Tunis", "Tunis", "Explorateur", 5, 3),
            new Badge(2, "🏖️ Sfax Voyageur", "Répondez correctement à 5 questions sur Sfax", "Sfax", "Voyageur", 5, 3),
            new Badge(3, "🏜️ Sahara Voyageur", "Répondez correctement à 5 questions sur le Sahara", "Sahara", "Voyageur", 5, 3),
            new Badge(4, "🏔️ Montagne Alpiniste", "Répondez correctement à 5 questions sur les montagnes", "Montagnes", "Alpiniste", 5, 3),
            new Badge(5, "🌊 Côte Marin", "Répondez correctement à 5 questions sur les plages", "Côte", "Marin", 5, 3),

            // Badges de performance
            new Badge(6, "⭐ Quiz Master", "Répondez correctement à 50 questions", "Global", "Master", 50, 0),
            new Badge(7, "🔥 Streak Champion", "Répondez correctement à 10 questions consécutives", "Global", "Champion", 10, 0),
            new Badge(8, "🎯 Perfectionist", "Répondez correctement à 100% des questions d'une session", "Global", "Perfectionist", 20, 0),

            // Badges de catégorie
            new Badge(9, "🏛️ Historien", "Répondez correctement à 10 questions sur les sites historiques", "Global", "Historien", 10, 0),
            new Badge(10, "🌳 Naturaliste", "Répondez correctement à 10 questions sur la nature", "Global", "Naturaliste", 10, 0),
        };
    }

    /**
     * Obtient l'emoji du badge
     */
    public String getEmoji() {
        if (name.contains("Tunis")) return "🏙️";
        if (name.contains("Sfax")) return "🏖️";
        if (name.contains("Sahara")) return "🏜️";
        if (name.contains("Montagne")) return "🏔️";
        if (name.contains("Côte")) return "🌊";
        if (name.contains("Master")) return "⭐";
        if (name.contains("Streak")) return "🔥";
        if (name.contains("Perfectionist")) return "🎯";
        if (name.contains("Historien")) return "🏛️";
        if (name.contains("Naturaliste")) return "🌳";
        return "🏆";
    }

    @Override
    public String toString() {
        return "Badge{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", unlocked=" + unlocked +
                ", progress=" + progress +
                '}';
    }
}

