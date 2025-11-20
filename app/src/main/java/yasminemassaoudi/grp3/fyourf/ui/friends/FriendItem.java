package yasminemassaoudi.grp3.fyourf.ui.friends;

/**
 * Modèle de données pour un ami
 */
public class FriendItem {
    private int friendId;
    private String friendPseudo;
    private String friendStatus;
    private String distanceMeters;
    private String direction;

    public FriendItem(int friendId, String friendPseudo, String friendStatus, String distanceMeters, String direction) {
        this.friendId = friendId;
        this.friendPseudo = friendPseudo;
        this.friendStatus = friendStatus;
        this.distanceMeters = distanceMeters;
        this.direction = direction;
    }

    // Getters
    public int getFriendId() {
        return friendId;
    }

    public String getFriendPseudo() {
        return friendPseudo;
    }

    public String getFriendStatus() {
        return friendStatus;
    }

    public String getDistanceMeters() {
        return distanceMeters;
    }

    public String getDirection() {
        return direction;
    }

    // Setters
    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public void setFriendPseudo(String friendPseudo) {
        this.friendPseudo = friendPseudo;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    public void setDistanceMeters(String distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}

