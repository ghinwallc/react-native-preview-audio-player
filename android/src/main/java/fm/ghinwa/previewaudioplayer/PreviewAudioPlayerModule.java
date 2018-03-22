package fm.ghinwa.previewaudioplayer;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fm.ghinwa.previewaudioplayer.implementation.PreviewAudioPlayerManager;
import fm.ghinwa.previewaudioplayer.implementation.listener.OnFFmpegPrepareStatusListener;
import fm.ghinwa.previewaudioplayer.implementation.listener.OnFfmpegCommandStatusListener;
import fm.ghinwa.previewaudioplayer.implementation.player.OnCompletionListener;
import fm.ghinwa.previewaudioplayer.implementation.player.OnPlaybackCompletedListener;
import fm.ghinwa.previewaudioplayer.implementation.player.ProgressUpdateListener;
import fm.ghinwa.previewaudioplayer.implementation.util.TimeUnitConverterUtil;

public class PreviewAudioPlayerModule extends ReactContextBaseJavaModule implements ProgressUpdateListener, OnPlaybackCompletedListener {

    private static final String MODULE_NAME = "PreviewAudioPlayerManager";

    private static final String PREVIEW_AUDIO_PLAYER_PROGRESS_UPDATE_EVENT_NAME = "previewAudioPlayerPlaybackProgressUpdate";
    private static final String PREVIEW_AUDIO_PLAYER_PROGRESS_CURRENT_TIME_KEY = "currentTime";

    private static final String PREVIEW_AUDIO_PLAYER_PLAYBACK_FINISHED_EVENT_NAME = "previewAudioPlayerPlaybackFinished";

    private static final TimeUnit DEFAULT_JS_TIME_UNIT = TimeUnit.SECONDS;

    private final PreviewAudioPlayerManager previewAudioPlayerManager;

    public PreviewAudioPlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        previewAudioPlayerManager = new PreviewAudioPlayerManager(reactContext);
        previewAudioPlayerManager.setPlayerProgressUpdateListener(this);
        previewAudioPlayerManager.setOnPlaybackCompletedListener(this);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void onPlayerProgressUpdate(int currentTimeMillis) {
        WritableMap writableMap = Arguments.createMap();
        float timeInDefaultJsUnit = TimeUnitConverterUtil.toResultTimeUnitFloat(currentTimeMillis, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        writableMap.putDouble(PREVIEW_AUDIO_PLAYER_PROGRESS_CURRENT_TIME_KEY, timeInDefaultJsUnit);
        sendEvent(PREVIEW_AUDIO_PLAYER_PROGRESS_UPDATE_EVENT_NAME, writableMap);
    }

    @Override
    public void onPlaybackComplete() {
        sendEvent(PREVIEW_AUDIO_PLAYER_PLAYBACK_FINISHED_EVENT_NAME, null);
    }

    @ReactMethod
    public void prepare(String originalRecordingPath, String processedInputPath,
                        String backgroundAudioFilePathString, int recordingStartTime, final Callback callback) {
        try {
            previewAudioPlayerManager.prepare(originalRecordingPath, processedInputPath,
                    backgroundAudioFilePathString, recordingStartTime, DEFAULT_JS_TIME_UNIT, new OnFFmpegPrepareStatusListener() {
                        @Override
                        public void onFfmpegPrepareError(String message) {
                            callback.invoke(message);
                        }

                        @Override
                        public void onFfmpegPrepareCompleted() {
                            callback.invoke();
                        }
                    });
        } catch (IOException e) {
            callback.invoke(e.getMessage());
        }
    }

    /**
     * Releases resources associated with this PreviewPlayer object.
     * It is considered good practice to call this method when you're
     * done using the PreviewPlayer.
     */
    @ReactMethod
    public void release() {
        previewAudioPlayerManager.release();
    }

    @ReactMethod
    public void playAt(float startTime, final Callback callback) {
        previewAudioPlayerManager.playAt(startTime, DEFAULT_JS_TIME_UNIT, new OnCompletionListener() {
            @Override
            public void onComplete() {
                callback.invoke();
            }
        });
    }

    @ReactMethod
    public void play(final Callback callback) {
        previewAudioPlayerManager.play(new OnCompletionListener() {
            @Override
            public void onComplete() {
                callback.invoke();
            }
        });
    }

    @ReactMethod
    public void pause(Callback callback) {
        previewAudioPlayerManager.pause();
        callback.invoke();
    }

    @ReactMethod
    public void unPause(final Callback callback) {
        previewAudioPlayerManager.unPause(new OnCompletionListener() {
            @Override
            public void onComplete() {
                callback.invoke();
            }
        });
    }

    @ReactMethod
    public void stop(Callback callback) {
        previewAudioPlayerManager.stop();
        callback.invoke();
    }

    @ReactMethod
    public void export(final Callback callback) {
        previewAudioPlayerManager.createPreviewFile(new OnFfmpegCommandStatusListener() {
            @Override
            public void onFfmpegCommandError(String message) {
                callback.invoke(message, null);
            }

            @Override
            public void onFfmpegCommandCompleted(String outputFilePath) {
                callback.invoke(null, outputFilePath);
            }
        });
    }

    @ReactMethod
    public void playPreview(final Callback callback) {
        previewAudioPlayerManager.playPreview(new OnCompletionListener() {
            @Override
            public void onComplete() {
                callback.invoke();
            }
        });
    }

    @ReactMethod
    public void setPreviewStartTime(float previewStartTime, Callback callback) {
        previewAudioPlayerManager.setPreviewStartTime(previewStartTime, DEFAULT_JS_TIME_UNIT);
        callback.invoke();
    }

    @ReactMethod
    public void setPreviewAt(float previewTime, float previewDuration, Callback callback) {
        previewAudioPlayerManager.setPreviewStartTime(previewTime, DEFAULT_JS_TIME_UNIT);
        previewAudioPlayerManager.setPreviewDuration(previewDuration, DEFAULT_JS_TIME_UNIT);
        callback.invoke();
    }

    @ReactMethod
    public void getPreviewStartTime(Callback callback) {
        callback.invoke(
                previewAudioPlayerManager.getPreviewStartTime(DEFAULT_JS_TIME_UNIT)
        );
    }

    @ReactMethod
    public void getPreviewDuration(Callback callback) {
        callback.invoke(previewAudioPlayerManager.getPreviewDuration(DEFAULT_JS_TIME_UNIT));
    }

    @ReactMethod
    public void getPlaybackDuration(Callback callback) {
        callback.invoke(previewAudioPlayerManager.getPlaybackDuration(DEFAULT_JS_TIME_UNIT));
    }

    @ReactMethod
    public void seekToTime(float time, final Callback callback) {
        previewAudioPlayerManager.seekToTime(time, DEFAULT_JS_TIME_UNIT, new OnCompletionListener() {
            @Override
            public void onComplete() {
                callback.invoke();
            }
        });
    }

    @ReactMethod
    public void getCurrentTime(Callback callback) {
        callback.invoke(previewAudioPlayerManager.getCurrentTime(DEFAULT_JS_TIME_UNIT));
    }

    @ReactMethod
    public void setVolume(float vocalTrackVolume, float backgroundTrackVolume, Callback callback) {
        previewAudioPlayerManager.setVolume(vocalTrackVolume, backgroundTrackVolume);
        callback.invoke();
    }

    @ReactMethod
    public void switchToOriginalVocalTrack(Callback callback) {
        previewAudioPlayerManager.switchToOriginalVocalTrack();
        callback.invoke();
    }

    @ReactMethod
    public void switchToProcessedVocalTrack(Callback callback) {
        previewAudioPlayerManager.switchToProcessedVocalTrack();
        callback.invoke();
    }

    /**
     * Use WritableMap for sending event with multiple parameters.
     */
    private void sendEvent(String eventName, @Nullable Object object) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, object);
    }
}
