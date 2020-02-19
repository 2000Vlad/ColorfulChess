package com.training.colorfulchess;

import android.util.Log;

public class LogUtil {
    public static void messageViewModel(String message) {
        Log.i("DEBUG", message);
    }

    public static void messageController(String message) {
        Log.i("CONTROLLER", message);
    }

}
