package yasminemassaoudi.grp3.fyourf.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;
import yasminemassaoudi.grp3.fyourf.UserManager;

/**
 * Fragment pour afficher la liste des amis et leurs distances
 */
public class FriendsFragment extends Fragment {

    private RecyclerView recyclerViewFriends;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private FriendsAdapter adapter;
    private List<FriendItem> friendsList;
    private RequestQueue requestQueue;
    private UserManager userManager;
    private int currentUserId = 1; // À récupérer depuis SharedPreferences

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        // Initialiser les vues
        recyclerViewFriends = root.findViewById(R.id.recyclerViewFriends);
        progressBar = root.findViewById(R.id.progressBar);
        textViewEmpty = root.findViewById(R.id.textViewEmpty);

        // Initialiser la liste et l'adaptateur
        friendsList = new ArrayList<>();
        adapter = new FriendsAdapter(friendsList, getContext());
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFriends.setAdapter(adapter);

        // Initialiser Volley et UserManager
        requestQueue = Volley.newRequestQueue(getContext());
        userManager = new UserManager(getContext());

        // Charger les amis
        loadFriends();

        return root;
    }

    /**
     * Charger la liste des amis depuis le serveur
     */
    private void loadFriends() {
        progressBar.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);

        String url = "http://192.168.56.1/servicephp/connections/get_connections.php?user_id=" + currentUserId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressBar.setVisibility(View.GONE);
                        friendsList.clear();

                        try {
                            if (response.length() == 0) {
                                textViewEmpty.setVisibility(View.VISIBLE);
                                textViewEmpty.setText("Aucun ami trouvé");
                                return;
                            }

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject friendObj = response.getJSONObject(i);
                                FriendItem friend = new FriendItem(
                                        friendObj.getInt("friend_id"),
                                        friendObj.getString("friend_pseudo"),
                                        friendObj.getString("friend_status"),
                                        friendObj.optString("distance_meters", "N/A"),
                                        friendObj.optString("direction", "N/A")
                                );
                                friendsList.add(friend);
                            }

                            adapter.notifyDataSetChanged();
                            textViewEmpty.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        textViewEmpty.setVisibility(View.VISIBLE);
                        textViewEmpty.setText("Erreur de connexion");
                        Toast.makeText(getContext(), "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Rafraîchir la liste des amis
     */
    public void refreshFriends() {
        loadFriends();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}

