package com.example.tutorial6;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tutorial6.MiniGame1.MainActivity_minigame1;
import com.example.tutorial6.MiniGame2.MainActivity_minigame2;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;
import android.util.Log;
import android.widget.TextView;
import com.example.tutorial6.UsefulFunctions.*;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class StartScreenActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 5;
    GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "StartScreenActivity";
    private static final int RC_SIGN_IN = 9001;  // Unique request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen_layout);

        // Check and request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_CONNECT)) {
                // Show an explanation to the user
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
            }
        } else {
            // Permission has already been granted
        }
        

        Button mini_game_1_button = findViewById(R.id.mini_game_1_button);
        Button mini_game_2_button = findViewById(R.id.mini_game_2_button);
        Button leaderboard_minigame1_button = findViewById(R.id.leaderboard_minigame1_button);
        Button leaderboard_minigame2_button = findViewById(R.id.leaderboard_minigame2_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("259849047586-nccraoagbt3tma1pbp9m1427fv0scu4q.apps.googleusercontent.com")  // your client id here
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }
            }
        });

        // Check if a Firebase user is signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is signed in, show user's stats
            updateUI(currentUser);
        } else {
            // No user is signed in, show sign-in button
            signInButton.setVisibility(View.VISIBLE);
        }

        mini_game_1_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreenActivity.this, MainActivity_minigame1.class);
                startActivity(intent);
            }
        });

        mini_game_2_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreenActivity.this, MainActivity_minigame2.class);
                startActivity(intent);
            }
        });

        leaderboard_minigame1_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreenActivity.this, minigame1_leaderboard.class);
                startActivity(intent);
            }
        });

        leaderboard_minigame2_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreenActivity.this, minigame2_leaderboard.class);
                startActivity(intent);
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Sign in to Firebase with Google account
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Signed in successfully, update UI
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }






    private void updateUI(Object account) {
        String userId, name, email;

        // Hide the sign-in button
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setVisibility(View.GONE);

        if (account instanceof GoogleSignInAccount) {
            // Get GoogleSignInAccount from the parameter
            GoogleSignInAccount googleAccount = (GoogleSignInAccount) account;
            userId = googleAccount.getId();
            name = googleAccount.getDisplayName();
            email = googleAccount.getEmail();
        } else if (account instanceof FirebaseUser) {
            // Handle FirebaseUser
            FirebaseUser firebaseUser = (FirebaseUser) account;
            userId = firebaseUser.getUid();
            name = firebaseUser.getDisplayName();
            email = firebaseUser.getEmail();
        } else {
            // If account is neither GoogleSignInAccount nor FirebaseUser, do nothing
            return;
        }

        // Fetch and Display Best Scores
        fetchAndDisplayBestScores(userId, name, email);
    }


    private void fetchAndDisplayBestScores(String userId, String name, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Display user email in the personal details card
        TextView userEmail = findViewById(R.id.tv_user_email);
        userEmail.setText("Email: " + email);

        // Display user name in the personal details card
        TextView userName = findViewById(R.id.tv_user_name);
        userName.setText("Name: " + name);

        // TextViews for the best scores
        TextView bestScoreGame1 = findViewById(R.id.tv_best_score_game1);
        TextView bestScoreGame2 = findViewById(R.id.tv_best_score_game2);

        // Fetching best score for game 1
        db.collection("users").document(userId).collection("scores_game1")
                .orderBy("final_score", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String bestScoreGame1Text = "N/A";
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            bestScoreGame1Text = "Best Score Game 1: " + documentSnapshot.getDouble("final_score");
                        }
                        bestScoreGame1.setText(bestScoreGame1Text);
                    }
                });

        // Fetching best score for game 2
        db.collection("users").document(userId).collection("scores_game2")
                .orderBy("final_score", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String bestScoreGame2Text = "N/A";
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            bestScoreGame2Text = "Best Score Game 2: " + documentSnapshot.getDouble("final_score");
                        }
                        bestScoreGame2.setText(bestScoreGame2Text);
                    }
                });
    }




    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    // You can now perform tasks that require this permission
                } else {
                    // Permission denied
                    // Disable the functionality that depends on this permission
                }
                return;
            }
            // Other 'case' lines to check for other
            // permissions this app might request
        }
    }
}