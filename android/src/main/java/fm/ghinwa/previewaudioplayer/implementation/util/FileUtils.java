package fm.ghinwa.previewaudioplayer.implementation.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class FileUtils {

    public static final String FILENAME_MIXED_PREFIX = "mixed";

    private static final String AAC_FORMAT_SUFFIX = ".aac";

    private FileUtils() {
        throw new AssertionError();
    }

    public static String createNewFilePathForFileIfNull(Context context, String prefixForNullPath) throws IOException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String filename = prefixForNullPath + TimeUnit.SECONDS.toSeconds(System.currentTimeMillis());
            return context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath()
                    + File.separatorChar + filename + AAC_FORMAT_SUFFIX;
        } else {
            throw new IOException("External storage not found.");
        }
    }
}