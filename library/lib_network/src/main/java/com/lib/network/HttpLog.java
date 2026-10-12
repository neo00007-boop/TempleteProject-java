package com.lib.network;

import android.util.Log;

final class HttpLog {
    private static final String SUFFIX = "_LOG_TAG";
    static volatile boolean enabled;

    private HttpLog() {
    }

    static void d(String tag, String msg) {
        if (enabled) {
            Log.d(tag + SUFFIX, msg);
        }
    }
}
