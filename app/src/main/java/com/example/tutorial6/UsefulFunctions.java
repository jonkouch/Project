package com.example.tutorial6;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UsefulFunctions {
    private static final String TAG = "UsefulFunctions";

    public void saveUserScore(int score) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            // get the user's ID
            String uid = firebaseUser.getUid();

            // create a Map to hold the updated score
            Map<String, Object> scoreUpdate = new HashMap<>();
            scoreUpdate.put("score", score);

            // update the user's score in Firestore
            db.collection("users").document(uid)
                    .set(scoreUpdate)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User score updated successfully"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating user score", e));
        }
    }


    public interface FirestoreCallback {
        void onCallback(int score);
    }

    public static void getUserScore(String uid, FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Get the score from the document
                                Long score = document.getLong("score");
                                if (score != null) {
                                    callback.onCallback(score.intValue());
                                } else {
                                    // If the score is null, return 0
                                    callback.onCallback(0);
                                }
                            } else {
                                // If no document is found, return 0
                                callback.onCallback(0);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

}
