package com.example.tutorial6;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.drawable.GradientDrawable;


import com.example.tutorial6.R;
import com.example.tutorial6.Score;

import java.util.List;

public class LeaderboardAdapter_minigame2 extends RecyclerView.Adapter<LeaderboardAdapter_minigame2.LeaderboardViewHolder> {

    private List<Score> scoresList;

    public LeaderboardAdapter_minigame2(List<Score> scoresList) {
        this.scoresList = scoresList;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        Score score = scoresList.get(position);
        String displayText = String.format("user: %s, score: %d, email: %s",
                score.getName(), score.getFinal_score(), score.getEmail());
        holder.nameTextView.setText(displayText);

        // Apply different text color and font size to top 3 results
        if (position == 0) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gold));
            holder.nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        } else if (position == 1) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.silver));
            holder.nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        } else if (position == 2) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.bronze));
            holder.nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        } else {
            holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.defaultColor));
            holder.nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }
    }


    @Override
    public int getItemCount() {
        return scoresList.size();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;

        LeaderboardViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.name_text_view);
        }
    }
}
