package yasminemassaoudi.grp3.fyourf.ui.geoquiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import yasminemassaoudi.grp3.fyourf.Badge;
import yasminemassaoudi.grp3.fyourf.R;

import java.util.List;

/**
 * Adapter pour afficher les badges dans une GridView
 */
public class BadgesAdapter extends BaseAdapter {
    private Context context;
    private List<Badge> badges;
    private LayoutInflater inflater;

    public BadgesAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return badges.size();
    }

    @Override
    public Object getItem(int position) {
        return badges.get(position);
    }

    @Override
    public long getItemId(int position) {
        return badges.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_badge, parent, false);
            holder = new ViewHolder();
            holder.badgeImageView = convertView.findViewById(R.id.badgeImageView);
            holder.badgeNameTextView = convertView.findViewById(R.id.badgeNameTextView);
            holder.badgeDescriptionTextView = convertView.findViewById(R.id.badgeDescriptionTextView);
            holder.progressBar = convertView.findViewById(R.id.badgeProgressBar);
            holder.progressTextView = convertView.findViewById(R.id.badgeProgressTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Badge badge = badges.get(position);

        // Afficher l'emoji du badge
        holder.badgeImageView.setText(badge.getEmoji());
        holder.badgeImageView.setTextSize(40);

        // Afficher le nom et la description
        holder.badgeNameTextView.setText(badge.getName());
        holder.badgeDescriptionTextView.setText(badge.getDescription());

        // Afficher la progression
        holder.progressBar.setProgress(badge.getProgress());
        holder.progressTextView.setText(badge.getProgress() + "%");

        // Changer la couleur si déverrouillé
        if (badge.isUnlocked()) {
            convertView.setAlpha(1.0f);
            holder.badgeImageView.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            convertView.setAlpha(0.5f);
            holder.badgeImageView.setTextColor(context.getResources().getColor(R.color.gray));
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView badgeImageView;
        TextView badgeNameTextView;
        TextView badgeDescriptionTextView;
        ProgressBar progressBar;
        TextView progressTextView;
    }
}

