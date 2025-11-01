# 💡 Exemples d'utilisation de Supabase dans FyourF

## 📋 Table des matières
1. [Exemple 1 : Insérer une localisation](#exemple-1--insérer-une-localisation)
2. [Exemple 2 : Récupérer toutes les localisations](#exemple-2--récupérer-toutes-les-localisations)
3. [Exemple 3 : Intégration dans SmsReceiver](#exemple-3--intégration-dans-smsreceiver)
4. [Exemple 4 : Intégration dans HomeFragment](#exemple-4--intégration-dans-homefragment)
5. [Exemple 5 : Parser JSON manuellement](#exemple-5--parser-json-manuellement)

---

## Exemple 1 : Insérer une localisation

### Code simple
```java
// Dans n'importe quelle activité ou fragment
SupabaseLocationService supabase = new SupabaseLocationService();

// Insérer une localisation
String phone = "+21612345678";
double latitude = 36.8065;  // Tunis
double longitude = 10.1815;

supabase.addOrUpdateLocation(phone, latitude, longitude);
Log.d("Supabase", "Localisation envoyée à Supabase");
```

### Code avec gestion d'erreurs
```java
try {
    SupabaseLocationService supabase = new SupabaseLocationService();
    
    String phone = "+21612345678";
    double latitude = 36.8065;
    double longitude = 10.1815;
    
    supabase.addOrUpdateLocation(phone, latitude, longitude);
    
    Toast.makeText(this, "Localisation sauvegardée", Toast.LENGTH_SHORT).show();
    Log.d("Supabase", "✓ Localisation sauvegardée pour " + phone);
    
} catch (Exception e) {
    Log.e("Supabase", "✗ Erreur lors de la sauvegarde", e);
    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
}
```

---

## Exemple 2 : Récupérer toutes les localisations

### Code simple
```java
SupabaseLocationService supabase = new SupabaseLocationService();

supabase.getAllLocations().thenAccept(locations -> {
    // Cette partie s'exécute quand les données sont reçues
    runOnUiThread(() -> {
        Log.d("Supabase", "Nombre de localisations : " + locations.size());
        
        for (SupabaseLocationService.LocationEntry location : locations) {
            Log.d("Supabase", "Phone: " + location.phone);
            Log.d("Supabase", "Lat: " + location.latitude + ", Lon: " + location.longitude);
        }
    });
});
```

### Code avec gestion d'erreurs
```java
SupabaseLocationService supabase = new SupabaseLocationService();

supabase.getAllLocations()
    .thenAccept(locations -> {
        // Succès
        runOnUiThread(() -> {
            if (locations != null && !locations.isEmpty()) {
                Log.d("Supabase", "✓ " + locations.size() + " localisations récupérées");
                
                for (SupabaseLocationService.LocationEntry location : locations) {
                    String message = String.format(
                        "Phone: %s, Lat: %.4f, Lon: %.4f, Time: %d",
                        location.phone,
                        location.latitude,
                        location.longitude,
                        location.timestamp
                    );
                    Log.d("Supabase", message);
                }
                
                Toast.makeText(this, 
                    locations.size() + " localisations trouvées", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Supabase", "Aucune localisation trouvée");
                Toast.makeText(this, "Aucune localisation", Toast.LENGTH_SHORT).show();
            }
        });
    })
    .exceptionally(throwable -> {
        // Erreur
        runOnUiThread(() -> {
            Log.e("Supabase", "✗ Erreur lors de la récupération", throwable);
            Toast.makeText(this, 
                "Erreur : " + throwable.getMessage(), 
                Toast.LENGTH_LONG).show();
        });
        return null;
    });
```

---

## Exemple 3 : Intégration dans SmsReceiver

### Modification de SmsReceiver.java

```java
public class SmsReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SmsReceiver";
    private SupabaseLocationService supabaseService;  // ← AJOUTER
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // ... code existant ...
        
        // Initialiser Supabase
        supabaseService = new SupabaseLocationService();  // ← AJOUTER
        
        // ... reste du code ...
    }
    
    private void handleLocationResponse(Context context, String messageBody, String senderNumber) {
        try {
            Log.d(TAG, "Processing location response from: " + senderNumber);
            
            // Parse message format: POSITION:lat,lon;time:timestamp
            String[] parts = messageBody.split(";");
            if (parts.length < 1) {
                Log.e(TAG, "Invalid message format");
                return;
            }
            
            // Extract coordinates
            String positionPart = parts[0].replace("POSITION:", "").trim();
            String[] coords = positionPart.split(",");
            
            if (coords.length < 2) {
                Log.e(TAG, "Invalid coordinate format");
                return;
            }
            
            double latitude = Double.parseDouble(coords[0].trim());
            double longitude = Double.parseDouble(coords[1].trim());
            
            Log.d(TAG, "Parsed coordinates: " + latitude + ", " + longitude);
            
            // Sauvegarder dans SQLite (code existant)
            LocationDatabase locationDb = new LocationDatabase(context);
            locationDb.addLocation(senderNumber, latitude, longitude);
            
            // ← AJOUTER : Sauvegarder dans Supabase
            supabaseService.addOrUpdateLocation(senderNumber, latitude, longitude);
            Log.d(TAG, "✓ Localisation sauvegardée dans Supabase");
            
            // Sauvegarder dans NotificationDatabase (code existant)
            NotificationDatabase notificationDb = new NotificationDatabase(context);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
            notificationDb.addNotification(senderNumber, latitude, longitude, timestamp);
            
            // Afficher notification (code existant)
            NotificationHelper.showLocationNotification(context, senderNumber, latitude, longitude);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling location response", e);
        }
    }
}
```

---

## Exemple 4 : Intégration dans HomeFragment

### Modification de HomeFragment.java

```java
public class HomeFragment extends Fragment implements OnMapReadyCallback {
    
    private GoogleMap googleMap;
    private TextView friendCountText;
    private Button refreshMapBtn;
    private LocationDatabase locationDatabase;
    private SupabaseLocationService supabaseService;  // ← AJOUTER
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        friendCountText = view.findViewById(R.id.friendCountText);
        refreshMapBtn = view.findViewById(R.id.refreshMapBtn);
        
        locationDatabase = new LocationDatabase(getContext());
        supabaseService = new SupabaseLocationService();  // ← AJOUTER
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Setup refresh button
        refreshMapBtn.setOnClickListener(v -> {
            loadFriendLocationsFromSupabase();  // ← MODIFIER
            Toast.makeText(getContext(), "Map refreshed from Supabase", Toast.LENGTH_SHORT).show();
        });
        
        return view;
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        
        // Load locations from Supabase instead of SQLite
        loadFriendLocationsFromSupabase();  // ← MODIFIER
    }
    
    // ← NOUVELLE MÉTHODE : Charger depuis Supabase
    private void loadFriendLocationsFromSupabase() {
        if (googleMap == null) return;
        
        googleMap.clear();
        
        supabaseService.getAllLocations()
            .thenAccept(locations -> {
                getActivity().runOnUiThread(() -> {
                    if (locations == null || locations.isEmpty()) {
                        friendCountText.setText("Friends: 0");
                        Toast.makeText(getContext(), 
                            "No locations found", 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    friendCountText.setText("Friends: " + locations.size());
                    
                    LatLng firstLocation = null;
                    
                    for (SupabaseLocationService.LocationEntry location : locations) {
                        LatLng position = new LatLng(location.latitude, location.longitude);
                        
                        // Format timestamp
                        String timeStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(new Date(location.timestamp));
                        
                        // Add marker
                        googleMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(location.phone)
                            .snippet("Last seen: " + timeStr)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        
                        if (firstLocation == null) {
                            firstLocation = position;
                        }
                    }
                    
                    // Move camera to first location
                    if (firstLocation != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12));
                    }
                    
                    Toast.makeText(getContext(), 
                        locations.size() + " locations loaded from Supabase", 
                        Toast.LENGTH_SHORT).show();
                });
            })
            .exceptionally(throwable -> {
                getActivity().runOnUiThread(() -> {
                    Log.e("HomeFragment", "Error loading locations from Supabase", throwable);
                    Toast.makeText(getContext(), 
                        "Error loading from Supabase: " + throwable.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    
                    // Fallback to SQLite
                    loadFriendLocationsFromSQLite();
                });
                return null;
            });
    }
    
    // ← GARDER L'ANCIENNE MÉTHODE comme fallback
    private void loadFriendLocationsFromSQLite() {
        if (googleMap == null) return;
        
        googleMap.clear();
        
        List<LocationDatabase.LocationEntry> locations = locationDatabase.getAllLocations();
        friendCountText.setText("Friends: " + locations.size());
        
        LatLng firstLocation = null;
        
        for (LocationDatabase.LocationEntry location : locations) {
            LatLng position = new LatLng(location.latitude, location.longitude);
            
            String timeStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(location.timestamp));
            
            googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(location.phoneNumber)
                .snippet("Last seen: " + timeStr));
            
            if (firstLocation == null) {
                firstLocation = position;
            }
        }
        
        if (firstLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12));
        }
    }
}
```

---

## Exemple 5 : Parser JSON manuellement

### Utilisation de JsonParser

```java
// Exemple 1 : Parser une réponse JSON
String jsonResponse = "[{\"phone\":\"+123\",\"latitude\":36.8,\"longitude\":10.1,\"timestamp\":1234567890000}]";

List<JsonParser.LocationEntry> locations = JsonParser.parseLocationHistory(jsonResponse);

for (JsonParser.LocationEntry location : locations) {
    Log.d("JSON", "Phone: " + location.phone);
    Log.d("JSON", "Lat: " + location.latitude);
    Log.d("JSON", "Lon: " + location.longitude);
}

// Exemple 2 : Créer un JSON pour l'insertion
JSONObject json = JsonParser.createLocationJson("+21612345678", 36.8065, 10.1815);
Log.d("JSON", "JSON créé : " + json.toString());

// Exemple 3 : Valider un JSON
String jsonToValidate = "[{\"phone\":\"+123\"}]";
boolean isValid = JsonParser.isValidJsonArray(jsonToValidate);
Log.d("JSON", "JSON valide ? " + isValid);

// Exemple 4 : Parser un seul objet JSON
try {
    JSONObject jsonObject = new JSONObject("{\"phone\":\"+123\",\"latitude\":36.8,\"longitude\":10.1,\"timestamp\":1234567890000}");
    JsonParser.LocationEntry location = JsonParser.parseLocationEntry(jsonObject);
    
    if (location != null) {
        Log.d("JSON", "Localisation parsée : " + location.toString());
    }
} catch (JSONException e) {
    Log.e("JSON", "Erreur de parsing", e);
}
```

---

## 🎯 Cas d'usage complets

### Cas 1 : Synchroniser SQLite → Supabase

```java
// Uploader toutes les localisations SQLite vers Supabase
public void syncLocalToSupabase(Context context) {
    LocationDatabase localDb = new LocationDatabase(context);
    SupabaseLocationService supabase = new SupabaseLocationService();
    
    List<LocationDatabase.LocationEntry> localLocations = localDb.getAllLocations();
    
    Log.d("Sync", "Uploading " + localLocations.size() + " locations to Supabase");
    
    for (LocationDatabase.LocationEntry location : localLocations) {
        supabase.addOrUpdateLocation(
            location.phoneNumber,
            location.latitude,
            location.longitude
        );
    }
    
    Toast.makeText(context, 
        localLocations.size() + " locations uploaded to Supabase", 
        Toast.LENGTH_SHORT).show();
}
```

### Cas 2 : Synchroniser Supabase → SQLite

```java
// Télécharger toutes les localisations Supabase vers SQLite
public void syncSupabaseToLocal(Context context) {
    LocationDatabase localDb = new LocationDatabase(context);
    SupabaseLocationService supabase = new SupabaseLocationService();
    
    supabase.getAllLocations().thenAccept(locations -> {
        Log.d("Sync", "Downloading " + locations.size() + " locations from Supabase");
        
        for (SupabaseLocationService.LocationEntry location : locations) {
            localDb.addLocation(
                location.phone,
                location.latitude,
                location.longitude
            );
        }
        
        Toast.makeText(context, 
            locations.size() + " locations downloaded from Supabase", 
            Toast.LENGTH_SHORT).show();
    });
}
```

### Cas 3 : Synchronisation bidirectionnelle

```java
// Synchroniser dans les deux sens
public void fullSync(Context context) {
    // 1. Upload local → Supabase
    syncLocalToSupabase(context);
    
    // 2. Download Supabase → local (après un délai)
    new Handler().postDelayed(() -> {
        syncSupabaseToLocal(context);
    }, 2000);
}
```

---

## 📊 Résumé des méthodes disponibles

### SupabaseLocationService

| Méthode | Description | Paramètres | Retour |
|---------|-------------|------------|--------|
| `addOrUpdateLocation()` | Ajouter/mettre à jour une localisation | phone, lat, lon | void |
| `getAllLocations()` | Récupérer toutes les localisations | - | CompletableFuture<List<LocationEntry>> |

### JsonParser

| Méthode | Description | Paramètres | Retour |
|---------|-------------|------------|--------|
| `parseLocationHistory()` | Parser un tableau JSON | jsonResponse | List<LocationEntry> |
| `parseLocationEntry()` | Parser un objet JSON | jsonObject | LocationEntry |
| `createLocationJson()` | Créer un JSON | phone, lat, lon | JSONObject |
| `isValidJsonArray()` | Valider un JSON | jsonResponse | boolean |

---

**Pour plus d'informations, consultez :**
- `SUPABASE_SETUP.md` - Documentation complète
- `GUIDE_RAPIDE_SUPABASE.md` - Guide pas à pas
- `RESUME_INTEGRATION_SUPABASE.md` - Résumé de l'intégration

