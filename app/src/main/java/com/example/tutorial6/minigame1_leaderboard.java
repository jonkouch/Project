package com.example.tutorial6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class minigame1_leaderboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame1_leaderboard);  // Set your layout here

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.recycler_view_minigame1);

        db.collection("scores")
                .orderBy("final_score", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Score> scoresList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                String email = document.getString("email");
                                int final_score = document.getLong("final_score").intValue();

                                Score score = new Score(name, email, final_score);
                                scoresList.add(score);
                            }

                            // Create and set the adapter for the RecyclerView
                            LeaderboardAdapter_minigame1 adapter = new LeaderboardAdapter_minigame1(scoresList);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(minigame1_leaderboard.this));
                        } else {
                            String TAG = "1";
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}