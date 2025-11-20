package yasminemassaoudi.grp3.fyourf.ui.geoquiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import yasminemassaoudi.grp3.fyourf.Badge;
import yasminemassaoudi.grp3.fyourf.GeoQuizManager;
import yasminemassaoudi.grp3.fyourf.R;

import java.util.List;

/**
 * Fragment pour afficher les badges déverrouillés
 */
public class BadgesFragment extends Fragment {
    private static final String TAG = "BadgesFragment";

    private GeoQuizManager quizManager;
    private GridView badgesGridView;
    private TextView totalBadgesTextView;
    private TextView unlockedBadgesTextView;
    private BadgesAdapter badgesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_badges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            initializeUI(view);
            initializeManager();
            displayBadges();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in onViewCreated", e);
            android.widget.Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeUI(View view) {
        try {
            badgesGridView = view.findViewById(R.id.badgesGridView);
            totalBadgesTextView = view.findViewById(R.id.totalBadgesTextView);
            unlockedBadgesTextView = view.findViewById(R.id.unlockedBadgesTextView);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error initializing UI", e);
            throw e;
        }
    }

    private void initializeManager() {
        try {
            quizManager = new GeoQuizManager(requireContext());
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error initializing manager", e);
            throw e;
        }
    }

    private void displayBadges() {
        try {
            if (quizManager == null) {
                android.widget.Toast.makeText(requireContext(), "Erreur: gestionnaire non initialisé", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            List<Badge> badges = quizManager.getBadges();
            if (badges == null || badges.isEmpty()) {
                if (totalBadgesTextView != null) {
                    totalBadgesTextView.setText("Total: 0");
                }
                if (unlockedBadgesTextView != null) {
                    unlockedBadgesTextView.setText("Déverrouillés: 0");
                }
                return;
            }

            // Compter les badges déverrouillés
            int unlockedCount = 0;
            for (Badge badge : badges) {
                if (badge != null && badge.isUnlocked()) {
                    unlockedCount++;
                }
            }

            // Mettre à jour les textes
            if (totalBadgesTextView != null) {
                totalBadgesTextView.setText("Total: " + badges.size());
            }
            if (unlockedBadgesTextView != null) {
                unlockedBadgesTextView.setText("Déverrouillés: " + unlockedCount);
            }

            // Afficher les badges
            if (badgesGridView != null) {
                badgesAdapter = new BadgesAdapter(requireContext(), badges);
                badgesGridView.setAdapter(badgesAdapter);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error displaying badges", e);
            android.widget.Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (quizManager != null) {
            quizManager.close();
        }
    }
}

