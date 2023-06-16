package com.example.tutorial6.MiniGame1;

interface SerialListener_minigame1 {
    void onSerialConnect      ();
    void onSerialConnectError (Exception e);
    void onSerialRead         (byte[] data);
    void onSerialIoError      (Exception e);
}
