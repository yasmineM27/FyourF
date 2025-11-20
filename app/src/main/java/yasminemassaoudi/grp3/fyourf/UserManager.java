package yasminemassaoudi.grp3.fyourf;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 👥 UserManager - Gère les utilisateurs et les connexions
 */
public class UserManager {

    private static final String TAG = "UserManager";
    private Context context;
    private RequestQueue requestQueue;
    private String baseUrl;

    public interface UserCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public interface UsersListCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    public interface ConnectionsCallback {
        void onSuccess(List<User> friends);
        void onError(String error);
    }

    /**
     * Classe interne pour représenter un utilisateur
     */
    public static class User {
        public int id;
        public String numero;
        public String pseudo;
        public String email;
        public String phone;
        public String status;
        public double latitude;
        public double longitude;

        public User(int id, String numero, String pseudo, String email, String phone, String status) {
            this.id = id;
            this.numero = numero;
            this.pseudo = pseudo;
            this.email = email;
            this.phone = phone;
            this.status = status;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", numero='" + numero + '\'' +
                    ", pseudo='" + pseudo + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

    public UserManager(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.baseUrl = MySQLConfig.MYSQL_BASE_URL;
    }

    /**
     * Créer un nouvel utilisateur
     */
    public void createUser(String numero, String pseudo, String email, String phone, UserCallback callback) {
        String url = baseUrl + "/users/create_user.php";

        JSONObject params = new JSONObject();
        try {
            params.put("numero", numero);
            params.put("pseudo", pseudo);
            params.put("email", email);
            params.put("phone", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                params,
                response -> {
                    Log.d(TAG, "✓ Utilisateur créé: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "✗ Erreur création utilisateur: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Récupérer un utilisateur par numéro
     */
    public void getUserByNumero(String numero, UserCallback callback) {
        String url = baseUrl + "/users/get_user.php?numero=" + numero;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "✓ Utilisateur récupéré: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "✗ Erreur récupération utilisateur: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public void getAllUsers(UsersListCallback callback) {
        String url = baseUrl + "/users/get_all_users.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<User> users = new ArrayList<>();
                        JSONArray usersArray = response.getJSONArray("users");

                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject userObj = usersArray.getJSONObject(i);
                            User user = new User(
                                    userObj.getInt("id"),
                                    userObj.getString("numero"),
                                    userObj.getString("pseudo"),
                                    userObj.optString("email", ""),
                                    userObj.optString("phone", ""),
                                    userObj.optString("status", "offline")
                            );
                            users.add(user);
                        }

                        Log.d(TAG, "✓ " + users.size() + " utilisateurs récupérés");
                        callback.onSuccess(users);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "✗ Erreur récupération utilisateurs: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Ajouter une connexion entre deux utilisateurs
     */
    public void addConnection(int userId1, int userId2, UserCallback callback) {
        String url = baseUrl + "/connections/add_connection.php";

        JSONObject params = new JSONObject();
        try {
            params.put("user1_id", userId1);
            params.put("user2_id", userId2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                params,
                response -> {
                    Log.d(TAG, "✓ Connexion ajoutée");
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "✗ Erreur ajout connexion: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Récupérer les amis connectés d'un utilisateur
     */
    public void getConnectedFriends(int userId, ConnectionsCallback callback) {
        String url = baseUrl + "/connections/get_connections.php?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<User> friends = new ArrayList<>();
                        JSONArray friendsArray = response.getJSONArray("friends");

                        for (int i = 0; i < friendsArray.length(); i++) {
                            JSONObject friendObj = friendsArray.getJSONObject(i);
                            User friend = new User(
                                    friendObj.getInt("id"),
                                    friendObj.getString("numero"),
                                    friendObj.getString("pseudo"),
                                    friendObj.optString("email", ""),
                                    friendObj.optString("phone", ""),
                                    friendObj.optString("status", "offline")
                            );
                            friends.add(friend);
                        }

                        Log.d(TAG, "✓ " + friends.size() + " amis récupérés");
                        callback.onSuccess(friends);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "✗ Erreur récupération amis: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Mettre à jour le statut d'un utilisateur
     */
    public void updateUserStatus(int userId, String status, UserCallback callback) {
        String url = baseUrl + "/users/update_user.php";

        JSONObject params = new JSONObject();
        try {
            params.put("id", userId);
            params.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                params,
                response -> {
                    Log.d(TAG, "✓ Statut mis à jour: " + status);
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "✗ Erreur mise à jour statut: " + error.getMessage());
                    callback.onError(error.getMessage());
                }
        );

        requestQueue.add(request);
    }
}

