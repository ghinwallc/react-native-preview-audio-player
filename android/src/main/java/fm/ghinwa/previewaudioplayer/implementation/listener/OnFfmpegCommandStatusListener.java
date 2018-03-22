package fm.ghinwa.previewaudioplayer.implementation.listener;

public interface OnFfmpegCommandStatusListener {

    void onFfmpegCommandError(String message);

    void onFfmpegCommandCompleted(String outputFilePath);
}
