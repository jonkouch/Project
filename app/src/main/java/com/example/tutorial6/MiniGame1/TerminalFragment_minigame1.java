package com.example.tutorial6.MiniGame1;

import static android.content.ContentValues.TAG;

import com.example.tutorial6.StartScreenActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import com.example.tutorial6.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TerminalFragment_minigame1 extends Fragment implements ServiceConnection, SerialListener_minigame1 {

    private enum Connected {False, Pending, True}

    private static final long COUNTDOWN_DURATION = 5000; // 5 seconds

    private String deviceAddress;
    private SerialService_minigame1 service;

    private TextView receiveText;


    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private String newline = TextUtil_minigame1.newline_crlf;
    private int lastValue = -1;

    private Handler handler = new Handler();
    private Runnable runnable;


    int chartIndex;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    boolean timeFlag = true;
    View view;
    int stepNumber = 0;
    int final_result;
    boolean maxChangeFlag = false;

    private Button homeBtn;

    boolean sendToFirebase = true;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");

        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser current_user = firebaseAuth.getCurrentUser();
                if (current_user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + current_user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }

        };
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService_minigame1.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService_minigame1.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService_minigame1.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService_minigame1.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_terminal_minigame1, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());


        chartIndex = 0;


        return view;
    }


    /*
     * Serial + UI
     */
    private String[] clean_str(String[] stringsArr) {
        for (int i = 0; i < stringsArr.length; i++) {
            stringsArr[i] = stringsArr[i].replaceAll(" ", "");
        }


        return stringsArr;
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket_minigame1 socket = new SerialSocket_minigame1(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }


    private void startCountdown() {
        new CountDownTimer(COUNTDOWN_DURATION, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                timeFlag = false;
                TextView num_of_steps_predicted = (TextView) view.findViewById(R.id.num_of_steps_predicted);

                num_of_steps_predicted.setText("Final Step Number: " + (int) stepNumber);
                final_result = stepNumber;

                if (sendToFirebase) {
                    sendToFirebase = false;

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    if (currentUser != null) {
                        // User is signed in
                        String userId = currentUser.getUid();
                        String userName = currentUser.getDisplayName();
                        String userEmail = currentUser.getEmail();

                        Log.d(TAG, "onAuthStateChanged:signed_in:" + userId);

                        // Create a new user score with fields
                        Map<String, Object> user_score = new HashMap<>();
                        user_score.put("name", userName);
                        user_score.put("email", userEmail);
                        user_score.put("final_score", final_result);

                        // Add a new document to the general scores collection
                        db.collection("scores_game1").add(user_score)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);
                                    }
                                });

                        // Add a new document to the user's scores collection
                        db.collection("users").document(userId).collection("scores_game1").add(user_score)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "User's DocumentSnapshot added with ID: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document to user's scores", e);
                                    }
                                });

                    } else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                    }
                    homeBtn = view.findViewById(R.id.home_btn_minigame1);
                    homeBtn.setVisibility(View.VISIBLE);
                    homeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), StartScreenActivity.class);
                            startActivity(intent);
                        }
                    });

                }
            }
        }.start();
    }


    private void receive(byte[] message) {
        if (timeFlag) {
            String msg = new String(message);
            if (newline.equals(TextUtil_minigame1.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                String msg_to_save = msg;
                msg_to_save = msg.replace(TextUtil_minigame1.newline_crlf, TextUtil_minigame1.emptyString);
                // check message length
                if (msg_to_save.length() > 1) {
                    // split message string by ',' char
                    String[] parts = msg_to_save.split(",");
                    // function to trim blank spaces
                    parts = clean_str(parts);
                    float N = (float) Math.sqrt(Math.pow(Float.parseFloat(parts[0]), 2) + Math.pow(Float.parseFloat(parts[1]), 2) + Math.pow(Float.parseFloat(parts[2]), 2));


                    TextView num_of_steps_predicted = (TextView) view.findViewById(R.id.num_of_steps_predicted);

                    if (!Python.isStarted()) {
                        Python.start(new AndroidPlatform(getContext()));
                    }
                    Python py = Python.getInstance();
                    PyObject pyobj = py.getModule("steps");
                    PyObject obj = pyobj.callAttr("step_number", N);
                    if (Float.parseFloat(obj.toString()) > 0)
                        startCountdown();

                    // Inside your method
                    Integer currentVal = Integer.parseInt(obj.toString());
                    Integer stp = currentVal-1;
                    Integer stp2 = currentVal;

                    // If currentVal is different from lastValue, update the TextView and schedule the second update
                    if(!currentVal.equals(lastValue)){
                        lastValue = currentVal;

                        if(stp.intValue() > 0)
                            num_of_steps_predicted.setText("STEPS!!  : " + stp);

                        // Create a new runnable task for updating to stp2
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                num_of_steps_predicted.setText("STEPS!!  : " + stp2);
                            }
                        };

                        // Delay the update to stp2
                        handler.postDelayed(runnable, 340); // delay of 300ms (0.3 seconds)
                    }


                    int steps = Integer.parseInt(obj.toString());
                    if (steps > stepNumber) {
                        stepNumber = steps;
                        maxChangeFlag = true;
                    } else
                        maxChangeFlag = false;

                    if (maxChangeFlag) {
                        if (stepNumber < 10) {

                        } else if (stepNumber < 20) {

                        } else if (stepNumber < 30) {

                        } else {

                        }
                    }
                }
            }
        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
            receive(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }
}
