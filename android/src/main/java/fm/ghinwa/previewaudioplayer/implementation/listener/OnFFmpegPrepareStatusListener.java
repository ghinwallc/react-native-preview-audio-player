package fm.ghinwa.previewaudioplayer.implementation.listener;

public interface OnFFmpegPrepareStatusListener {

    void onFfmpegPrepareError(String message);

    void onFfmpegPrepareCompleted();
}
