package yasminemassaoudi.grp3.fyourf;


import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import yasminemassaoudi.grp3.fyourf.MainActivity;
import yasminemassaoudi.grp3.fyourf.R;
import yasminemassaoudi.grp3.fyourf.ui.dashboard.DashboardFragment;

public class NavigationManager {

    private static NavigationManager instance;
    private FragmentActivity activity;

    private NavigationManager(FragmentActivity activity) {
        this.activity = activity;
    }

    public static NavigationManager getInstance(FragmentActivity activity) {
        if (instance == null) {
            instance = new NavigationManager(activity);
        }
        return instance;
    }

    public void navigateToFragment(Fragment fragment, int navItemId) {
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.navigateToFragment(fragment);
            mainActivity.getBottomNavigationView().setSelectedItemId(navItemId);
        }
    }

    public void navigateToNotifications() {
        navigateToFragment(new NotificationsFragment(), R.id.nav_notifications);
    }

    public void navigateToHistory() {
        navigateToFragment(new HistoryFragment(), R.id.nav_history);
    }

    public void navigateToDashboard() {
        navigateToFragment(new DashboardFragment(), R.id.nav_dashboard);
    }
}
