package com.example.tutorial6;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
        if (account instanceof GoogleSignInAccount) {
            // Get GoogleSignInAccount from the parameter
            GoogleSignInAccount googleAccount = (GoogleSignInAccount) account;
            String name = googleAccount.getDisplayName();
            String email = googleAccount.getEmail();
            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setVisibility(View.GONE);  // Hide the sign-in button
            // Call your method to get the user score
            UsefulFunctions.getUserScore(googleAccount.getId(), new FirestoreCallback() {
                @Override
                public void onCallback(int score) {
                    String userInfo = "User: " + name + "\nEmail: " + email + "\nScore: " + score;
                    TextView userMailScore = findViewById(R.id.user_info);
                    userMailScore.setText(userInfo);
                    userMailScore.setVisibility(View.VISIBLE);  // Show the TextView
                }
            });
        }
        else if (account instanceof FirebaseUser) {
            // Get FirebaseUser from the parameter
            FirebaseUser firebaseUser = (FirebaseUser) account;
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();
            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setVisibility(View.GONE);  // Hide the sign-in button
            // Call your method to get the user score
            UsefulFunctions.getUserScore(firebaseUser.getUid(), new FirestoreCallback() {
                @Override
                public void onCallback(int score) {
                    String userInfo = "User: " + name + "\nEmail: " + email + "\nScore: " + score;
                    TextView userMailScore = findViewById(R.id.user_info);
                    userMailScore.setText(userInfo);
                    userMailScore.setVisibility(View.VISIBLE);  // Show the TextView
                }
            });
        }
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