package yasminemassaoudi.grp3.fyourf.ui.geoquiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import yasminemassaoudi.grp3.fyourf.GeoQuizManager;
import yasminemassaoudi.grp3.fyourf.GeoQuizQuestion;
import yasminemassaoudi.grp3.fyourf.LocationDatabase;
import yasminemassaoudi.grp3.fyourf.Position;
import yasminemassaoudi.grp3.fyourf.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment principal pour le jeu GeoQuiz
 */
public class GeoQuizFragment extends Fragment {
    private static final String TAG = "GeoQuizFragment";

    private GeoQuizManager quizManager;
    private LocationDatabase locationDatabase;
    private List<GeoQuizQuestion> questions;
    private int currentQuestionIndex = 0;

    // UI Components
    private ImageView mapImageView;
    private TextView questionNumberTextView;
    private TextView regionTextView;
    private TextView categoryTextView;
    private TextView difficultyTextView;
    private RadioGroup optionsRadioGroup;
    private Button submitButton;
    private Button nextButton;
    private Button hintButton;
    private ProgressBar progressBar;
    private TextView scoreTextView;
    private TextView streakTextView;
    private TextView timerTextView;
    private ProgressBar imageLoadingProgressBar;

    // Timer variables
    private android.os.CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 30000; // 30 seconds
    private int hintsRemaining = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geoquiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d(TAG, "✓ GeoQuizFragment onViewCreated called");
        try {
            initializeUI(view);
            android.util.Log.d(TAG, "✓ UI initialized");
            initializeManagers();
            android.util.Log.d(TAG, "✓ Managers initialized");
            loadQuestions();
            android.util.Log.d(TAG, "✓ Questions loaded");
            displayQuestion();
            android.util.Log.d(TAG, "✓ Question displayed");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeUI(View view) {
        try {
            mapImageView = view.findViewById(R.id.mapImageView);
            questionNumberTextView = view.findViewById(R.id.questionNumberTextView);
            regionTextView = view.findViewById(R.id.regionTextView);
            categoryTextView = view.findViewById(R.id.categoryTextView);
            difficultyTextView = view.findViewById(R.id.difficultyTextView);
            optionsRadioGroup = view.findViewById(R.id.optionsRadioGroup);
            submitButton = view.findViewById(R.id.submitButton);
            nextButton = view.findViewById(R.id.nextButton);
            hintButton = view.findViewById(R.id.hintButton);
            progressBar = view.findViewById(R.id.progressBar);
            scoreTextView = view.findViewById(R.id.scoreTextView);
            streakTextView = view.findViewById(R.id.streakTextView);
            timerTextView = view.findViewById(R.id.timerTextView);
            imageLoadingProgressBar = view.findViewById(R.id.imageLoadingProgressBar);

            if (submitButton != null) {
                submitButton.setOnClickListener(v -> submitAnswer());
            }
            if (nextButton != null) {
                nextButton.setOnClickListener(v -> nextQuestion());
            }
            if (hintButton != null) {
                hintButton.setOnClickListener(v -> useHint());
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error initializing UI", e);
            throw e;
        }
    }

    private void initializeManagers() {
        try {
            quizManager = new GeoQuizManager(requireContext());
            locationDatabase = new LocationDatabase(requireContext());
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error initializing managers", e);
            throw e;
        }
    }

    private void loadQuestions() {
        try {
            List<LocationDatabase.LocationEntry> locations = locationDatabase.getAllLocations();
            if (locations.isEmpty()) {
                Toast.makeText(requireContext(), "Aucune position disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            // Convert LocationEntry to Position objects for GeoQuizManager
            List<Position> positions = new ArrayList<>();
            for (LocationDatabase.LocationEntry entry : locations) {
                Position pos = new Position(entry.longitude, entry.latitude, entry.phone, "");
                pos.setTimestamp(entry.timestamp);
                positions.add(pos);
            }
            questions = quizManager.generateQuestionsFromHistory(positions, 10);
            if (questions.isEmpty()) {
                Toast.makeText(requireContext(), "Erreur lors de la génération des questions", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayQuestion() {
        try {
            if (questions == null || questions.isEmpty() || currentQuestionIndex >= questions.size()) {
                endQuiz();
                return;
            }

            GeoQuizQuestion question = questions.get(currentQuestionIndex);

            // Mettre à jour les informations
            if (questionNumberTextView != null) {
                questionNumberTextView.setText("Question " + (currentQuestionIndex + 1) + "/" + questions.size());
            }
            if (regionTextView != null) {
                regionTextView.setText("Région: " + (question.getRegion() != null ? question.getRegion() : "N/A"));
            }
            if (categoryTextView != null) {
                categoryTextView.setText("Catégorie: " + (question.getCategory() != null ? question.getCategory() : "N/A"));
            }
            if (difficultyTextView != null) {
                difficultyTextView.setText("Difficulté: " + question.getDifficultyText());
            }

            // Charger l'image de la carte
            if (mapImageView != null && question.getMapImageUrl() != null && !question.getMapImageUrl().isEmpty()) {
                try {
                    String imageUrl = question.getMapImageUrl();
                    android.util.Log.d(TAG, "Loading image URL: " + imageUrl);

                    // Check if it's a local map placeholder
                    if (imageUrl.startsWith("local_map:")) {
                        loadLocalMapImage(imageUrl);
                    } else {
                        // Try to load from URL (for future use with proper API key)
                        loadRemoteMapImage(imageUrl);
                    }
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error loading image", e);
                    // Set a placeholder color if image fails to load
                    if (mapImageView != null) {
                        mapImageView.setBackgroundColor(android.graphics.Color.LTGRAY);
                    }
                    if (imageLoadingProgressBar != null) {
                        imageLoadingProgressBar.setVisibility(View.GONE);
                    }
                }
            } else if (mapImageView != null) {
                // No image URL, set placeholder color
                android.util.Log.w(TAG, "No image URL available");
                mapImageView.setBackgroundColor(android.graphics.Color.LTGRAY);
                if (imageLoadingProgressBar != null) {
                    imageLoadingProgressBar.setVisibility(View.GONE);
                }
            }

            // Afficher les options
            if (optionsRadioGroup != null) {
                optionsRadioGroup.removeAllViews();
                List<String> options = question.getOptions();
                if (options != null) {
                    for (String option : options) {
                        RadioButton radioButton = new RadioButton(requireContext());
                        radioButton.setText(option);
                        radioButton.setTag(option);
                        optionsRadioGroup.addView(radioButton);
                    }
                }
            }

            // Mettre à jour la barre de progression
            if (progressBar != null && questions.size() > 0) {
                progressBar.setProgress((currentQuestionIndex + 1) * 100 / questions.size());
            }

            // Mettre à jour le score
            if (scoreTextView != null && quizManager != null) {
                scoreTextView.setText("Score: " + quizManager.getCurrentScore());
            }

            // Mettre à jour le streak
            if (streakTextView != null && quizManager != null) {
                streakTextView.setText("Streak: " + quizManager.getCurrentStreak());
            }

            // Réinitialiser les boutons
            if (submitButton != null) {
                submitButton.setEnabled(true);
            }
            if (nextButton != null) {
                nextButton.setEnabled(false);
            }
            if (optionsRadioGroup != null) {
                optionsRadioGroup.clearCheck();
            }

            // Démarrer le timer
            startTimer();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error displaying question", e);
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void submitAnswer() {
        try {
            if (optionsRadioGroup == null || questions == null || questions.isEmpty()) {
                Toast.makeText(requireContext(), "Erreur: données manquantes", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Veuillez sélectionner une réponse", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = optionsRadioGroup.findViewById(selectedId);
            if (selectedRadioButton == null) {
                Toast.makeText(requireContext(), "Erreur: réponse invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            String userAnswer = (String) selectedRadioButton.getTag();
            if (userAnswer == null) {
                Toast.makeText(requireContext(), "Erreur: tag invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            GeoQuizQuestion question = questions.get(currentQuestionIndex);
            if (quizManager == null) {
                Toast.makeText(requireContext(), "Erreur: gestionnaire non initialisé", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCorrect = quizManager.answerQuestion(question, userAnswer);

            // Afficher le résultat avec animation
            if (isCorrect) {
                Toast.makeText(requireContext(), "✅ Correct! +" + question.getPoints() + " points", Toast.LENGTH_SHORT).show();
                selectedRadioButton.setTextColor(getResources().getColor(R.color.green));
                animateCorrectAnswer(selectedRadioButton);
                animateScoreUpdate();
            } else {
                Toast.makeText(requireContext(), "❌ Incorrect! La réponse est: " + question.getCorrectAnswer(), Toast.LENGTH_SHORT).show();
                selectedRadioButton.setTextColor(getResources().getColor(R.color.red));
                animateIncorrectAnswer(selectedRadioButton);
            }

            if (submitButton != null) {
                submitButton.setEnabled(false);
            }
            if (nextButton != null) {
                nextButton.setEnabled(true);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error submitting answer", e);
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void nextQuestion() {
        try {
            currentQuestionIndex++;
            displayQuestion();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error moving to next question", e);
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Charge une image de carte locale (placeholder avec région)
     */
    private void loadLocalMapImage(String mapData) {
        try {
            // Parse: local_map:region:latitude:longitude
            String[] parts = mapData.split(":");
            if (parts.length >= 2) {
                String region = parts[1];

                // Create a bitmap with region name
                android.graphics.Bitmap bitmap = createMapBitmap(region);

                if (mapImageView != null && bitmap != null) {
                    mapImageView.setImageBitmap(bitmap);
                    android.util.Log.d(TAG, "Local map image loaded for region: " + region);
                }

                if (imageLoadingProgressBar != null) {
                    imageLoadingProgressBar.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error loading local map image", e);
            if (mapImageView != null) {
                mapImageView.setBackgroundColor(android.graphics.Color.LTGRAY);
            }
            if (imageLoadingProgressBar != null) {
                imageLoadingProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Crée un bitmap avec le nom de la région
     */
    private android.graphics.Bitmap createMapBitmap(String region) {
        try {
            int width = 400;
            int height = 300;

            // Create bitmap with light blue background
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);

            // Draw background gradient
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setColor(android.graphics.Color.parseColor("#E3F2FD")); // Light blue
            canvas.drawRect(0, 0, width, height, paint);

            // Draw border
            paint.setColor(android.graphics.Color.parseColor("#0f5da7")); // Dark blue
            paint.setStrokeWidth(3);
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            canvas.drawRect(0, 0, width, height, paint);

            // Draw region name
            paint.setColor(android.graphics.Color.parseColor("#0f5da7"));
            paint.setTextSize(48);
            paint.setTextAlign(android.graphics.Paint.Align.CENTER);
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));

            canvas.drawText(region, width / 2, height / 2 + 20, paint);

            // Draw coordinates
            paint.setTextSize(16);
            paint.setColor(android.graphics.Color.parseColor("#666666"));
            canvas.drawText("📍 Localisation", width / 2, height / 2 + 60, paint);

            return bitmap;
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error creating map bitmap", e);
            return null;
        }
    }

    /**
     * Charge une image de carte depuis une URL distante
     */
    private void loadRemoteMapImage(String imageUrl) {
        try {
            // Show loading indicator
            if (imageLoadingProgressBar != null) {
                imageLoadingProgressBar.setVisibility(View.VISIBLE);
            }

            // Set background color first
            if (mapImageView != null) {
                mapImageView.setBackgroundColor(android.graphics.Color.LTGRAY);
            }

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_dialog_map)
                    .error(android.R.drawable.ic_dialog_info)
                    .centerCrop()
                    .timeout(15000)
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                        @Override
                        public void onResourceReady(android.graphics.drawable.Drawable resource, com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                            if (mapImageView != null) {
                                mapImageView.setImageDrawable(resource);
                                android.util.Log.d(TAG, "Remote image loaded successfully");
                            }
                            if (imageLoadingProgressBar != null) {
                                imageLoadingProgressBar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                            if (imageLoadingProgressBar != null) {
                                imageLoadingProgressBar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onLoadFailed(android.graphics.drawable.Drawable errorDrawable) {
                            android.util.Log.e(TAG, "Failed to load remote image from URL: " + imageUrl);
                            if (mapImageView != null) {
                                mapImageView.setBackgroundColor(android.graphics.Color.LTGRAY);
                            }
                            if (imageLoadingProgressBar != null) {
                                imageLoadingProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });

            android.util.Log.d(TAG, "Remote image loading started for URL: " + imageUrl);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error loading remote image", e);
            if (mapImageView != null) {
                mapImageView.setBackgroundColor(android.graphics.Color.LTGRAY);
            }
            if (imageLoadingProgressBar != null) {
                imageLoadingProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Animate correct answer with scale and rotation
     */
    private void animateCorrectAnswer(View view) {
        try {
            android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();

            // Scale animation
            android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
            android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
            scaleX.setDuration(500);
            scaleY.setDuration(500);

            // Rotation animation
            android.animation.ObjectAnimator rotation = android.animation.ObjectAnimator.ofFloat(view, "rotation", 0f, 5f, -5f, 0f);
            rotation.setDuration(500);

            animatorSet.playTogether(scaleX, scaleY, rotation);
            animatorSet.start();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error animating correct answer", e);
        }
    }

    /**
     * Animate incorrect answer with shake effect
     */
    private void animateIncorrectAnswer(View view) {
        try {
            android.animation.ObjectAnimator shake = android.animation.ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 0f);
            shake.setDuration(500);
            shake.start();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error animating incorrect answer", e);
        }
    }

    /**
     * Animate score update with scale effect
     */
    private void animateScoreUpdate() {
        try {
            if (scoreTextView != null) {
                android.animation.AnimatorSet animatorSet = new android.animation.AnimatorSet();

                android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(scoreTextView, "scaleX", 1f, 1.2f, 1f);
                android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(scoreTextView, "scaleY", 1f, 1.2f, 1f);
                scaleX.setDuration(400);
                scaleY.setDuration(400);

                animatorSet.playTogether(scaleX, scaleY);
                animatorSet.start();
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error animating score update", e);
        }
    }

    /**
     * Start countdown timer for the question
     */
    private void startTimer() {
        try {
            // Cancel previous timer if exists
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            countDownTimer = new android.os.CountDownTimer(TIMER_DURATION, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long secondsRemaining = millisUntilFinished / 1000;
                    if (timerTextView != null) {
                        timerTextView.setText("⏱️ " + secondsRemaining + "s");

                        // Change color when time is running out
                        if (secondsRemaining <= 5) {
                            timerTextView.setTextColor(getResources().getColor(R.color.red));
                        } else {
                            timerTextView.setTextColor(getResources().getColor(R.color.dark_blue));
                        }
                    }
                }

                @Override
                public void onFinish() {
                    if (timerTextView != null) {
                        timerTextView.setText("⏱️ 0s");
                        timerTextView.setTextColor(getResources().getColor(R.color.red));
                    }
                    // Auto-submit if no answer selected
                    Toast.makeText(requireContext(), "⏰ Temps écoulé!", Toast.LENGTH_SHORT).show();
                    nextQuestion();
                }
            };
            countDownTimer.start();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error starting timer", e);
        }
    }

    /**
     * Use a hint to reveal one incorrect answer
     */
    private void useHint() {
        try {
            if (hintsRemaining <= 0) {
                Toast.makeText(requireContext(), "❌ Pas de hints restants!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (questions == null || currentQuestionIndex >= questions.size()) {
                return;
            }

            GeoQuizQuestion question = questions.get(currentQuestionIndex);
            List<String> options = question.getOptions();
            String correctAnswer = question.getCorrectAnswer();

            // Find an incorrect option to remove
            for (int i = 0; i < optionsRadioGroup.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) optionsRadioGroup.getChildAt(i);
                String option = (String) radioButton.getTag();

                if (!option.equals(correctAnswer)) {
                    radioButton.setEnabled(false);
                    radioButton.setAlpha(0.5f);
                    radioButton.setText(option + " ❌");

                    hintsRemaining--;
                    if (hintButton != null) {
                        hintButton.setText("💡 Hint (" + hintsRemaining + ")");
                    }

                    Toast.makeText(requireContext(), "💡 Une réponse incorrecte a été éliminée! (-10 points)", Toast.LENGTH_SHORT).show();

                    // Deduct points for using hint
                    if (quizManager != null) {
                        quizManager.deductPoints(10);
                        if (scoreTextView != null) {
                            scoreTextView.setText("Score: " + quizManager.getCurrentScore());
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error using hint", e);
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void endQuiz() {
        try {
            if (quizManager == null) {
                Toast.makeText(requireContext(), "Erreur: gestionnaire non initialisé", Toast.LENGTH_SHORT).show();
                return;
            }

            quizManager.endSession();

            // Afficher le résumé
            String summary = String.format(
                    "Quiz Terminé!\n\n" +
                    "Score: %d points\n" +
                    "Correctes: %d/%d\n" +
                    "Précision: %.1f%%\n" +
                    "Meilleur Streak: %d",
                    quizManager.getCurrentScore(),
                    quizManager.getCorrectAnswers(),
                    quizManager.getTotalQuestions(),
                    quizManager.getAccuracy(),
                    quizManager.getMaxStreak()
            );

            Toast.makeText(requireContext(), summary, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error ending quiz", e);
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel timer when fragment is destroyed
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (quizManager != null) {
            quizManager.close();
        }
        if (locationDatabase != null) {
            locationDatabase.close();
        }
    }
}

