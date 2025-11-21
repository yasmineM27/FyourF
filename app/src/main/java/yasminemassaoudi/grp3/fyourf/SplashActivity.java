package yasminemassaoudi.grp3.fyourf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 🎨 Splash Screen Activity
 * Affiche le logo et les informations de l'application au démarrage
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 secondes
    private ImageView logoImageView;
    private TextView appNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialiser les vues
        logoImageView = findViewById(R.id.splash_logo);
        appNameTextView = findViewById(R.id.splash_app_name);

        // Charger les animations
        loadAnimations();

        // Rediriger vers MainActivity après le délai
        new Handler(Looper.getMainLooper()).postDelayed(
                this::navigateToMainActivity,
                SPLASH_DURATION
        );
    }

    /**
     * Charge et démarre les animations du splash screen
     */
    private void loadAnimations() {
        try {
            // Animation du logo (zoom in)
            Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
            logoImageView.startAnimation(logoAnimation);

            // Animation du nom de l'app (fade in)
            Animation nameAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            nameAnimation.setStartOffset(500);
            appNameTextView.startAnimation(nameAnimation);

            // Animation de la version (slide up)
            Animation versionAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            versionAnimation.setStartOffset(1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers MainActivity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        
        // Animation de transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Empêcher de revenir en arrière depuis le splash screen
        super.onBackPressed();
    }
}

