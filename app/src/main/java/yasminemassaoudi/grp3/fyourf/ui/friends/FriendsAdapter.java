package yasminemassaoudi.grp3.fyourf.ui.friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import yasminemassaoudi.grp3.fyourf.R;

/**
 * Adaptateur pour afficher la liste des amis
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<FriendItem> friendsList;
    private Context context;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(FriendItem friend);
    }

    public FriendsAdapter(List<FriendItem> friendsList, Context context) {
        this.friendsList = friendsList;
        this.context = context;
    }

    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendItem friend = friendsList.get(position);

        holder.textViewPseudo.setText(friend.getFriendPseudo());
        holder.textViewStatus.setText(friend.getFriendStatus());
        holder.textViewDistance.setText(friend.getDistanceMeters());
        holder.textViewDirection.setText(friend.getDirection());

        // Définir la couleur du statut
        if ("online".equals(friend.getFriendStatus())) {
            holder.imageViewStatus.setColorFilter(context.getResources().getColor(R.color.green));
        } else if ("away".equals(friend.getFriendStatus())) {
            holder.imageViewStatus.setColorFilter(context.getResources().getColor(R.color.yellow));
        } else {
            holder.imageViewStatus.setColorFilter(context.getResources().getColor(R.color.gray));
        }

        // Clic sur l'ami
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPseudo;
        TextView textViewStatus;
        TextView textViewDistance;
        TextView textViewDirection;
        ImageView imageViewStatus;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPseudo = itemView.findViewById(R.id.textViewPseudo);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDistance = itemView.findViewById(R.id.textViewDistance);
            textViewDirection = itemView.findViewById(R.id.textViewDirection);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
        }
    }
}

