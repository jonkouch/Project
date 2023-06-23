package com.example.tutorial6;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorial6.R;
import com.example.tutorial6.Score;

import java.util.List;

public class LeaderboardAdapter_minigame1 extends RecyclerView.Adapter<LeaderboardAdapter_minigame1.LeaderboardViewHolder> {

    private List<Score> scoresList;

    public LeaderboardAdapter_minigame1(List<Score> scoresList) {
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

        // Apply different colors to top 3 results
        if (position == 0) {
            holder.nameTextView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gold));
            holder.nameTextView.setTextSize(20);
        } else if (position == 1) {
            holder.nameTextView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.silver));
            holder.nameTextView.setTextSize(18);
        } else if (position == 2) {
            holder.nameTextView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.bronze));
            holder.nameTextView.setTextSize(16);
        } else {
            holder.nameTextView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.defaultColor));
            holder.nameTextView.setTextSize(14);
        }

        holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
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
