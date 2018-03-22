package fm.ghinwa.previewaudioplayer.implementation;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fm.ghinwa.previewaudioplayer.implementation.listener.OnFFmpegPrepareStatusListener;
import fm.ghinwa.previewaudioplayer.implementation.listener.OnFfmpegCommandStatusListener;
import fm.ghinwa.previewaudioplayer.implementation.player.OnCompletionListener;
import fm.ghinwa.previewaudioplayer.implementation.player.OnPlaybackCompletedListener;
import fm.ghinwa.previewaudioplayer.implementation.player.PreviewPlayer;
import fm.ghinwa.previewaudioplayer.implementation.player.ProgressUpdateListener;
import fm.ghinwa.previewaudioplayer.implementation.util.FileUtils;
import fm.ghinwa.previewaudioplayer.implementation.util.Logger;

public class PreviewAudioPlayerManager {

    private static final String TAG = PreviewAudioPlayerManager.class.getSimpleName();

    private static final String FFMPEG_INPUT_PARAMETER = "-i";
    private static final String FFMPEG_COMPLEX_FILTER_PARAMETER = "-filter_complex";
    private static final String FFMPEG_OVERWRITE_OUTPUT_FILES_PARAMETER = "-y";
    private static final String FFMPEG_FIRST_TRACK_VOLUME_PARAMETER = "[0:a]volume=";
    private static final String FFMPEG_FIRST_TRACK_IDENTIFIER = "[a0]";
    private static final String FFMPEG_SECOND_TRACK_IDENTIFIER = "[a1]";
    private static final String FFMPEG_MIX_TRACKS_PARAMETER = FFMPEG_FIRST_TRACK_IDENTIFIER + FFMPEG_SECOND_TRACK_IDENTIFIER + "amix=inputs=2:duration=first";
    private static final String FFMPEG_BITRATE_PARAMETER = "-b:a";
    private static final String FFMPEG_BITRATE_VALUE = "64k";
    private static final String FFMPEG_SECOND_TRACK_VOLUME_PARAMETER = "[1:a]volume=";
    private static final String FFMPEG_FILTER_END = ";";

    private final PreviewPlayer previewPlayer;
    private final FFmpeg ffmpeg;
    private final Context context;

    // TODO: 01.02.2017 recordingStartTimeInMillis are not used for now

    private String originalRecordingPath;
    private String processedInputPath;
    private String backgroundAudioFilePathString;
    private boolean isPrepared;
    private boolean isFfmpegBinaryLoaded = false;

    public PreviewAudioPlayerManager(Context context) {
        this.context = context;
        ffmpeg = FFmpeg.getInstance(context.getApplicationContext());
        previewPlayer = new PreviewPlayer();
    }

    public void prepare(String originalRecordingPath, String processedInputPath,
                        String backgroundAudioFilePathString, float recordingStartTime,
                        TimeUnit sourceTimeUnit, final OnFFmpegPrepareStatusListener onFFmpegPrepareStatusListener)
            throws IOException {

        this.originalRecordingPath = originalRecordingPath;
        this.processedInputPath = processedInputPath;
        this.backgroundAudioFilePathString = backgroundAudioFilePathString;

        previewPlayer.prepare(originalRecordingPath, processedInputPath, backgroundAudioFilePathString, recordingStartTime, sourceTimeUnit);

        if (isFfmpegBinaryLoaded) {
            onFFmpegPrepareStatusListener.onFfmpegPrepareCompleted();
        } else {
            try {
                ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                    @Override
                    public void onFailure() {
                        Logger.d(TAG, "Error while loading ffmpeg");
                        onFFmpegPrepareStatusListener.onFfmpegPrepareError("Error while loading ffmpeg");
                    }

                    @Override
                    public void onSuccess() {
                        Logger.d(TAG, "Ffmpeg loaded");
                        isPrepared = true;
                        isFfmpegBinaryLoaded = true;
                        Log.d("test", Thread.currentThread().getName());
                        onFFmpegPrepareStatusListener.onFfmpegPrepareCompleted();
                    }

                    @Override
                    public void onStart() {
                        //no-op
                    }

                    @Override
                    public void onFinish() {
                        //no-op
                    }
                });
            } catch (Exception ex) {
                onFFmpegPrepareStatusListener.onFfmpegPrepareError(ex.getLocalizedMessage());
            }
        }
    }

    public void createPreviewFile(final OnFfmpegCommandStatusListener onFfmpegCommandStatusListener) {
        if (!isPrepared) {
            onFfmpegCommandStatusListener.onFfmpegCommandError(
                    "Prepare should be called before creating preview file");
        } else {

            try {
                final String outputFilePath = FileUtils.createNewFilePathForFileIfNull(context,
                        FileUtils.FILENAME_MIXED_PREFIX);

                ffmpeg.execute(createFfmpegCommand(outputFilePath), new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        Logger.d(TAG, "Preview file created: " + message);
                        onFfmpegCommandStatusListener.onFfmpegCommandCompleted(outputFilePath);
                    }

                    @Override
                    public void onProgress(String message) {
                        Logger.d(TAG, "Creating preview file progress: " + message);
                    }

                    @Override
                    public void onFailure(String message) {
                        Logger.d(TAG, "Error while creating preview file" + message);
                        onFfmpegCommandStatusListener.onFfmpegCommandError("Error while creating preview file" + message);
                    }

                    @Override
                    public void onStart() {
                        //no-op
                    }

                    @Override
                    public void onFinish() {
                        //no-op
                    }
                });
            } catch (Exception ex) {
                onFfmpegCommandStatusListener.onFfmpegCommandError(ex.getLocalizedMessage());
            }
        }
    }

    public void play(OnCompletionListener onCompletionListener) {
        previewPlayer.play(onCompletionListener);
    }

    private String[] createFfmpegCommand(String outputFilePath) {
        return new String[]{
                FFMPEG_INPUT_PARAMETER, backgroundAudioFilePathString,
                FFMPEG_INPUT_PARAMETER, previewPlayer.isOriginalVocalUsed() ? originalRecordingPath : processedInputPath,
                FFMPEG_COMPLEX_FILTER_PARAMETER,
                FFMPEG_FIRST_TRACK_VOLUME_PARAMETER + previewPlayer.getCurrentBackgroundTrackVolume() +
                        FFMPEG_FIRST_TRACK_IDENTIFIER + FFMPEG_FILTER_END +
                        FFMPEG_SECOND_TRACK_VOLUME_PARAMETER + previewPlayer.getCurrentVocalVolume() +
                        FFMPEG_SECOND_TRACK_IDENTIFIER + FFMPEG_FILTER_END + FFMPEG_MIX_TRACKS_PARAMETER,
                FFMPEG_BITRATE_PARAMETER, FFMPEG_BITRATE_VALUE,
                FFMPEG_OVERWRITE_OUTPUT_FILES_PARAMETER, outputFilePath
        };
    }

    public void stop() {
        previewPlayer.stop();
    }

    public void playAt(float startTime, TimeUnit timeUnit, OnCompletionListener onCompletionListener) {
        previewPlayer.playAt(startTime, timeUnit, onCompletionListener);
    }

    public void pause() {
        previewPlayer.pause();
    }

    public void unPause(OnCompletionListener onCompletionListener) {
        previewPlayer.unPause(onCompletionListener);
    }

    public void playPreview(OnCompletionListener onCompletionListener) {
        previewPlayer.playPreview(onCompletionListener);
    }

    public void setPreviewStartTime(float previewStartTime, TimeUnit defaultJsTimeUnit) {
        previewPlayer.setPreviewStartTime(previewStartTime, defaultJsTimeUnit);
    }

    public void setPreviewDuration(float duration, TimeUnit defaultJsTimeUnit) {
        previewPlayer.setPreviewDuration(duration, defaultJsTimeUnit);
    }

    public float getPreviewStartTime(TimeUnit timeUnit) {
        return previewPlayer.getPreviewStartTime(timeUnit);
    }

    public float getPreviewDuration(TimeUnit timeUnit) {
        return previewPlayer.getPreviewDuration(timeUnit);
    }

    public float getPlaybackDuration(TimeUnit timeUnit) {
        return previewPlayer.getPlaybackDuration(timeUnit);
    }

    public void seekToTime(float time, TimeUnit timeUnit, OnCompletionListener onCompletionListener) {
        previewPlayer.seekTo(time, timeUnit, onCompletionListener);
    }

    public float getCurrentTime(TimeUnit timeUnit) {
        return previewPlayer.getCurrentTime(timeUnit);
    }

    public void setVolume(float vocalTrackVolume, float backgroundTrackVolume) {
        previewPlayer.setVolume(vocalTrackVolume, backgroundTrackVolume);
    }

    public void switchToOriginalVocalTrack() {
        previewPlayer.switchToOriginalVocalTrack();
    }

    public void switchToProcessedVocalTrack() {
        previewPlayer.switchToProcessedVocalTrack();
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void release() {
        previewPlayer.release();
    }

    public void setPlayerProgressUpdateListener(ProgressUpdateListener progressUpdateListener) {
        previewPlayer.setProgressUpdateListener(progressUpdateListener);
    }

    public void setOnPlaybackCompletedListener(OnPlaybackCompletedListener onPlaybackCompletedListener) {
        previewPlayer.setPlaybackCompletedListener(onPlaybackCompletedListener);
    }
}