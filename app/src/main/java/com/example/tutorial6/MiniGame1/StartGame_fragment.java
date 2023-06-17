package com.example.tutorial6.MiniGame1;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.tutorial6.R;

public class StartGame_fragment extends Fragment {

    private static final long COUNTDOWN_DURATION = 5000; // 5 seconds
    private static final int PROGRESS_BAR_MAX = 100;

    private boolean gameStarted = false;
    private TextView countdownText;
    private ProgressBar progressBar;

    public StartGame_fragment() {
        // Required empty public constructor
    }

    public static StartGame_fragment newInstance() {
        return new StartGame_fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_game_fragment, container, false);

        countdownText = view.findViewById(R.id.countdown_text);
        progressBar = view.findViewById(R.id.progress_bar);

        Button startButton = view.findViewById(R.id.start_btn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameStarted) {
                    gameStarted = true;
                    startCountdown();
                }
            }
        });

        return view;
    }

    private void startCountdown() {
        new CountDownTimer(COUNTDOWN_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                launchTerminalFragment();
            }
        }.start();
    }

    private void updateUI(long millisUntilFinished) {
        // Calculate the progress in percentage
        int progress = (int) ((COUNTDOWN_DURATION - millisUntilFinished) * PROGRESS_BAR_MAX / COUNTDOWN_DURATION);
        progressBar.setProgress(progress);

        // Update the countdown text
        int secondsRemaining = (int) (millisUntilFinished / 1000);
        countdownText.setText(String.valueOf(secondsRemaining));

        // Animate the countdown text
        ObjectAnimator animator = ObjectAnimator.ofFloat(countdownText, View.ALPHA, 0f, 1f);
        animator.setDuration(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void launchTerminalFragment() {
        // Replace the current fragment with TerminalFragment_minigame1
        Bundle args = getArguments();
        Fragment fragment = new TerminalFragment_minigame1();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal").addToBackStack(null).commit();
    }
}
