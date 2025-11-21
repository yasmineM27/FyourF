package yasminemassaoudi.grp3.fyourf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Activity qui affiche le résumé du quiz après sa complétion
 */
public class QuizSummaryActivity extends AppCompatActivity {
    private static final String TAG = "QuizSummaryActivity";

    // UI Components
    private TextView finalScoreTextView;
    private TextView correctAnswersTextView;
    private TextView accuracyTextView;
    private TextView maxStreakTextView;
    private LinearLayout unlockedBadgesContainer;
    private Button retryButton;
    private Button homeButton;

    // Data from Intent
    private int finalScore;
    private int correctAnswers;
    private int totalQuestions;
    private double accuracy;
    private int maxStreak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_summary);

        // Récupérer les données de l'Intent
        getDataFromIntent();

        // Initialiser l'UI
        initializeUI();

        // Afficher les données
        displaySummary();

        // Afficher les badges déverrouillés
        displayUnlockedBadges();

        // Configurer les boutons
        setupButtons();
    }

    /**
     * Récupère les données passées par l'Intent
     */
    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            finalScore = intent.getIntExtra("FINAL_SCORE", 0);
            correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0);
            totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0);
            accuracy = intent.getDoubleExtra("ACCURACY", 0.0);
            maxStreak = intent.getIntExtra("MAX_STREAK", 0);
        }
    }

    /**
     * Initialise les composants UI
     */
    private void initializeUI() {
        finalScoreTextView = findViewById(R.id.finalScoreTextView);
        correctAnswersTextView = findViewById(R.id.correctAnswersTextView);
        accuracyTextView = findViewById(R.id.accuracyTextView);
        maxStreakTextView = findViewById(R.id.maxStreakTextView);
        unlockedBadgesContainer = findViewById(R.id.unlockedBadgesContainer);
        retryButton = findViewById(R.id.retryButton);
        homeButton = findViewById(R.id.homeButton);
    }

    /**
     * Affiche le résumé du quiz
     */
    private void displaySummary() {
        // Score final
        if (finalScoreTextView != null) {
            finalScoreTextView.setText("Score: " + finalScore + " points");
        }

        // Réponses correctes
        if (correctAnswersTextView != null) {
            correctAnswersTextView.setText(correctAnswers + "/" + totalQuestions);
        }

        // Précision
        if (accuracyTextView != null) {
            accuracyTextView.setText(String.format("%.1f%%", accuracy));
        }

        // Meilleur streak
        if (maxStreakTextView != null) {
            maxStreakTextView.setText(String.valueOf(maxStreak));
        }
    }

    /**
     * Affiche les badges déverrouillés pendant cette session
     */
    private void displayUnlockedBadges() {
        if (unlockedBadgesContainer == null) {
            return;
        }

        try {
            GeoQuizManager quizManager = new GeoQuizManager(this);
            List<Badge> badges = quizManager.getBadges();

            // Filtrer les badges déverrouillés
            boolean hasUnlockedBadges = false;
            for (Badge badge : badges) {
                if (badge.isUnlocked()) {
                    hasUnlockedBadges = true;
                    addBadgeView(badge);
                }
            }

            // Si aucun badge déverrouillé, afficher un message
            if (!hasUnlockedBadges) {
                TextView noBadgesTextView = new TextView(this);
                noBadgesTextView.setText("Aucun nouveau badge déverrouillé");
                noBadgesTextView.setTextSize(16);
                noBadgesTextView.setTextColor(getResources().getColor(R.color.gray));
                noBadgesTextView.setPadding(16, 16, 16, 16);
                unlockedBadgesContainer.addView(noBadgesTextView);
            }

            quizManager.close();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Erreur lors de l'affichage des badges", e);
        }
    }

    /**
     * Ajoute une vue de badge au conteneur
     */
    private void addBadgeView(Badge badge) {
        // Créer une vue horizontale pour le badge
        LinearLayout badgeLayout = new LinearLayout(this);
        badgeLayout.setOrientation(LinearLayout.HORIZONTAL);
        badgeLayout.setPadding(16, 12, 16, 12);
        badgeLayout.setBackgroundColor(getResources().getColor(R.color.gray_light));
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 12);
        badgeLayout.setLayoutParams(layoutParams);

        // Emoji du badge
        TextView emojiTextView = new TextView(this);
        emojiTextView.setText(badge.getEmoji());
        emojiTextView.setTextSize(32);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emojiParams.setMargins(0, 0, 24, 0);
        emojiTextView.setLayoutParams(emojiParams);
        badgeLayout.addView(emojiTextView);

        // Conteneur pour nom et description
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textContainer.setLayoutParams(textParams);

        // Nom du badge
        TextView nameTextView = new TextView(this);
        nameTextView.setText(badge.getName());
        nameTextView.setTextSize(16);
        nameTextView.setTextColor(getResources().getColor(R.color.dark_blue));
        nameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        textContainer.addView(nameTextView);

        // Description du badge
        TextView descriptionTextView = new TextView(this);
        descriptionTextView.setText(badge.getDescription());
        descriptionTextView.setTextSize(14);
        descriptionTextView.setTextColor(getResources().getColor(R.color.gray));
        textContainer.addView(descriptionTextView);

        badgeLayout.addView(textContainer);
        unlockedBadgesContainer.addView(badgeLayout);
    }

    /**
     * Configure les boutons
     */
    private void setupButtons() {
        // Bouton Rejouer
        if (retryButton != null) {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retourner au quiz
                    finish();
                }
            });
        }

        // Bouton Accueil
        if (homeButton != null) {
            homeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retourner à MainActivity
                    Intent intent = new Intent(QuizSummaryActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        // Retourner à MainActivity au lieu de revenir au quiz
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

