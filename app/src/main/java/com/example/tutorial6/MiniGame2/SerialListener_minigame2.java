package com.example.tutorial6.MiniGame2;

interface SerialListener_minigame2 {
    void onSerialConnect      ();
    void onSerialConnectError (Exception e);
    void onSerialRead         (byte[] data);
    void onSerialIoError      (Exception e);
}
