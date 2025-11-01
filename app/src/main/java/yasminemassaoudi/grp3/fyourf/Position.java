package yasminemassaoudi.grp3.fyourf;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Modèle de données pour une position géographique
 * Compatible avec la table MySQL Position
 */
public class Position implements Parcelable {
    
    private int idposition;
    private double longitude;
    private double latitude;
    private String numero;
    private String pseudo;
    private long timestamp;
    private String createdAt;
    private String updatedAt;
    
    // Constructeur vide
    public Position() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Constructeur complet
    public Position(int idposition, double longitude, double latitude, String numero, 
                   String pseudo, long timestamp, String createdAt, String updatedAt) {
        this.idposition = idposition;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
        this.pseudo = pseudo;
        this.timestamp = timestamp;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Constructeur simplifié (pour création)
    public Position(double longitude, double latitude, String numero, String pseudo) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
        this.pseudo = pseudo;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters et Setters
    public int getIdposition() {
        return idposition;
    }
    
    public void setIdposition(int idposition) {
        this.idposition = idposition;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public String getPseudo() {
        return pseudo;
    }
    
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Méthodes utilitaires
    
    /**
     * Vérifie si la position est valide
     */
    public boolean isValid() {
        // Vérifier que les coordonnées sont dans les limites valides
        if (latitude < -90 || latitude > 90) return false;
        if (longitude < -180 || longitude > 180) return false;
        
        // Vérifier que ce n'est pas une position par défaut invalide
        if (latitude == 0.0 && longitude == 0.0) return false;
        if (latitude == 999.0 && longitude == 999.0) return false;
        
        // Vérifier que le numéro n'est pas vide
        if (numero == null || numero.trim().isEmpty()) return false;
        
        return true;
    }
    
    /**
     * Retourne le nom d'affichage (pseudo ou numéro)
     */
    public String getDisplayName() {
        if (pseudo != null && !pseudo.trim().isEmpty()) {
            return pseudo;
        }
        return numero;
    }
    
    /**
     * Convertit en LocationDatabase.LocationEntry pour compatibilité
     */
    public LocationDatabase.LocationEntry toLocationEntry() {
        return new LocationDatabase.LocationEntry(
            numero,
            latitude,
            longitude,
            timestamp
        );
    }
    
    @Override
    public String toString() {
        return "Position{" +
                "id=" + idposition +
                ", numero='" + numero + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", lat=" + latitude +
                ", lon=" + longitude +
                ", timestamp=" + timestamp +
                '}';
    }
    
    // Implémentation Parcelable pour passer entre Activities
    
    protected Position(Parcel in) {
        idposition = in.readInt();
        longitude = in.readDouble();
        latitude = in.readDouble();
        numero = in.readString();
        pseudo = in.readString();
        timestamp = in.readLong();
        createdAt = in.readString();
        updatedAt = in.readString();
    }
    
    public static final Creator<Position> CREATOR = new Creator<Position>() {
        @Override
        public Position createFromParcel(Parcel in) {
            return new Position(in);
        }
        
        @Override
        public Position[] newArray(int size) {
            return new Position[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idposition);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(numero);
        dest.writeString(pseudo);
        dest.writeLong(timestamp);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }
}

