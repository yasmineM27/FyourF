package yasminemassaoudi.grp3.fyourf.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * ViewModel pour le Fragment des amis
 */
public class FriendsViewModel extends ViewModel {

    private final MutableLiveData<List<FriendItem>> friendsLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;

    public FriendsViewModel() {
        friendsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
    }

    public LiveData<List<FriendItem>> getFriendsLiveData() {
        return friendsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void setFriends(List<FriendItem> friends) {
        friendsLiveData.setValue(friends);
    }

    public void setError(String error) {
        errorLiveData.setValue(error);
    }

    public void setLoading(boolean loading) {
        loadingLiveData.setValue(loading);
    }
}

