package fm.ghinwa.previewaudioplayer.implementation.util;

import android.util.Log;

public final class Logger {

    private static final boolean LOGGING_ENABLED = true;

    private Logger() {
        throw new AssertionError();
    }

    public static void d(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }
}