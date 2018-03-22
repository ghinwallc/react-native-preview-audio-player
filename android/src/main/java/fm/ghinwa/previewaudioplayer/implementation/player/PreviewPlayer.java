package fm.ghinwa.previewaudioplayer.implementation.player;


import android.media.MediaPlayer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fm.ghinwa.previewaudioplayer.implementation.util.Logger;
import fm.ghinwa.previewaudioplayer.implementation.util.TimeUnitConverterUtil;

public class PreviewPlayer implements MediaPlayer.OnCompletionListener {

    private static final String TAG = PreviewPlayer.class.getSimpleName();

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    private static final int MAX_VOLUME = 1;

    private SinglePreviewMediaPlayer originalVocalPlayer;
    private SinglePreviewMediaPlayer processedVocalPlayer;
    private SinglePreviewMediaPlayer backgroundTrackPlayer;

    private SinglePreviewMediaPlayer[] mediaPlayers;

    private final PlayerProgressThread progressThread;

    private boolean isOriginalVocalUsed;

    private int recordingStartTimeInDefaultTimeUnit;
    private int previewStartTimeInDefaultTimeUnit;
    private int previewDurationInDefaultTimeUnit;
    private int previewEndTimeInDefaultTimeUnit;

    private float currentVocalVolume;
    private float currentBackgroundTrackVolume;

    private boolean isPrepared = false;

    private volatile boolean isInPreviewMode;
    private ProgressUpdateListener progressUpdateListener;
    private OnPlaybackCompletedListener onPlaybackCompletedListener;

    public PreviewPlayer() {
        currentVocalVolume = MAX_VOLUME;
        currentBackgroundTrackVolume = MAX_VOLUME;
        isOriginalVocalUsed = true;

        progressThread = new PlayerProgressThread(this);
        progressThread.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        completePlayback();
    }

    public void setPlaybackCompletedListener(OnPlaybackCompletedListener onPlaybackCompletedListener) {
        this.onPlaybackCompletedListener = onPlaybackCompletedListener;
    }

    void completePlayback() {
        stop();
        if (onPlaybackCompletedListener != null) {
            onPlaybackCompletedListener.onPlaybackComplete();
        }
    }

    public void prepare(String originalRecordingPath, String processedInputPath,
                        String backgroundAudioFilePathString, float recordingStartTime, TimeUnit sourceTimeUnit) throws IOException {
        if (isPrepared) {
            release();
        }

        backgroundTrackPlayer = new SinglePreviewMediaPlayer();
        originalVocalPlayer = new SinglePreviewMediaPlayer();
        processedVocalPlayer = new SinglePreviewMediaPlayer();

        backgroundTrackPlayer.setOnCompletionListener(this);

        mediaPlayers = new SinglePreviewMediaPlayer[]{backgroundTrackPlayer, originalVocalPlayer, processedVocalPlayer};

        backgroundTrackPlayer.setDataSource(backgroundAudioFilePathString);
        originalVocalPlayer.setDataSource(originalRecordingPath);
        processedVocalPlayer.setDataSource(processedInputPath);

        recordingStartTimeInDefaultTimeUnit = toDefaultTimeUnit(recordingStartTime, sourceTimeUnit);

        initMediaPlayers();

        isPrepared = true;
    }

    public void play(final OnCompletionListener onCompletionListener) {
        if (getCurrentTimeMillis() != 0) {
            playAt(0, DEFAULT_TIME_UNIT, onCompletionListener);
        } else {
            executeOnAllPlayers(new PlayerExecuteCommand() {
                @Override
                public void execute(SinglePreviewMediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
            isInPreviewMode = false;
            onCompletionListener.onComplete();
        }
        progressThread.unPause();
    }

    public void playAt(float time, TimeUnit sourceTimeUnit, final OnCompletionListener onCompletionListener) {
        final int timeInDefaultPlayerUnit = toDefaultTimeUnit(time, sourceTimeUnit);
        seekTo(timeInDefaultPlayerUnit, new OnCompletionListener() {
            @Override
            public void onComplete() {
                executeOnAllPlayers(new PlayerExecuteCommand() {
                    @Override
                    public void execute(SinglePreviewMediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
                isInPreviewMode = false;
                onCompletionListener.onComplete();
            }
        });
        progressThread.unPause();
    }

    /**
     * This method won't call stop() on media players to do not enter Stopped state.
     * Instead it will reset media players to initial state so it's ready to use.
     *
     * @see <a href = "https://developer.android.com/reference/android/media/MediaPlayer.html">
     * https://developer.android.com/reference/android/media/MediaPlayer.html</a>
     */
    public void stop() {
        pause();
        if (isInPreviewMode) {
            isInPreviewMode = false;
        }
        seekTo(0, new OnCompletionListener() {
            @Override
            public void onComplete() {
                //no-op
            }
        });
    }

    public void pause() {
        executeOnAllPlayers(new PlayerExecuteCommand() {
            @Override
            public void execute(SinglePreviewMediaPlayer mediaPlayer) {
                mediaPlayer.pause();
            }
        });
        progressThread.pause();
    }

    public void unPause(final OnCompletionListener onCompletionListener) {
        seekTo(getCurrentTimeMillis(), new OnCompletionListener() {
            @Override
            public void onComplete() {
                executeOnAllPlayers(new PlayerExecuteCommand() {
                    @Override
                    public void execute(SinglePreviewMediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
                onCompletionListener.onComplete();
            }
        });
        progressThread.unPause();
    }

    public void playPreview(final OnCompletionListener onCompletionListener) {
        seekTo(previewStartTimeInDefaultTimeUnit, new OnCompletionListener() {
            @Override
            public void onComplete() {
                executeOnAllPlayers(new PlayerExecuteCommand() {
                    @Override
                    public void execute(SinglePreviewMediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
                isInPreviewMode = true;
                onCompletionListener.onComplete();
            }
        });
        progressThread.unPause();
    }

    public void setPreviewStartTime(float time, TimeUnit sourceTimeUnit) {
        previewStartTimeInDefaultTimeUnit = toDefaultTimeUnit(time, sourceTimeUnit);
        previewEndTimeInDefaultTimeUnit = previewStartTimeInDefaultTimeUnit + previewDurationInDefaultTimeUnit;
    }

    public float getPreviewStartTime(TimeUnit resultTimeUnit) {
        return TimeUnitConverterUtil.toResultTimeUnitFloat(previewStartTimeInDefaultTimeUnit, DEFAULT_TIME_UNIT, resultTimeUnit);
    }

    public void setPreviewDuration(float duration, TimeUnit sourceUnit) {
        previewDurationInDefaultTimeUnit = toDefaultTimeUnit(duration, sourceUnit);
        previewEndTimeInDefaultTimeUnit = previewStartTimeInDefaultTimeUnit + previewDurationInDefaultTimeUnit;
    }

    public float getPreviewDuration(TimeUnit resultUnit) {
        return TimeUnitConverterUtil.toResultTimeUnitFloat(previewDurationInDefaultTimeUnit, DEFAULT_TIME_UNIT, resultUnit);
    }

    public float getPlaybackDuration(TimeUnit resultUnit) {
        int playbackDurationInDefaultUnit = backgroundTrackPlayer.getDuration() - recordingStartTimeInDefaultTimeUnit;
        return TimeUnitConverterUtil.toResultTimeUnitFloat(playbackDurationInDefaultUnit, DEFAULT_TIME_UNIT, resultUnit);
    }

    public void seekTo(float time, TimeUnit sourceTimeUnit, OnCompletionListener onCompletionListener) {
        int timeInDefaultPlayerUnit = (int) TimeUnitConverterUtil.toResultTimeUnitLong(time, sourceTimeUnit, DEFAULT_TIME_UNIT);
        seekTo(timeInDefaultPlayerUnit, onCompletionListener);
    }

    int getCurrentTimeMillis() {
        return backgroundTrackPlayer.getCurrentPosition() - recordingStartTimeInDefaultTimeUnit;
    }

    public float getCurrentTime(TimeUnit sourceTimeUnit) {
        return TimeUnitConverterUtil.toResultTimeUnitFloat(getCurrentTimeMillis(), DEFAULT_TIME_UNIT, sourceTimeUnit);
    }

    int getPreviewEndTimeInMillis() {
        return previewEndTimeInDefaultTimeUnit;
    }

    public void setVolume(float vocalAudioTrackVolume, float backgroundTrackVolume) {
        currentVocalVolume = vocalAudioTrackVolume;
        currentBackgroundTrackVolume = backgroundTrackVolume;

        backgroundTrackPlayer.setVolume(currentBackgroundTrackVolume, currentBackgroundTrackVolume);
        if (isOriginalVocalUsed) {
            originalVocalPlayer.setVolume(currentVocalVolume, currentVocalVolume);
        } else {
            processedVocalPlayer.setVolume(currentVocalVolume, currentVocalVolume);
        }
    }

    public void switchToOriginalVocalTrack() {
        isOriginalVocalUsed = true;
        originalVocalPlayer.setVolume(currentVocalVolume, currentVocalVolume);
        processedVocalPlayer.setVolume(0, 0);
    }


    public void switchToProcessedVocalTrack() {
        isOriginalVocalUsed = false;
        originalVocalPlayer.setVolume(0, 0);
        processedVocalPlayer.setVolume(currentVocalVolume, currentVocalVolume);
    }

    public void setProgressUpdateListener(ProgressUpdateListener progressUpdateListener) {

        this.progressUpdateListener = progressUpdateListener;
    }

    void onProgressUpdate(int currentProgressMillis) {
        if (progressUpdateListener != null) {
            progressUpdateListener.onPlayerProgressUpdate(currentProgressMillis);
        }
    }

    public boolean isOriginalVocalUsed() {
        return isOriginalVocalUsed;
    }

    public float getCurrentVocalVolume() {
        return currentVocalVolume;
    }

    public float getCurrentBackgroundTrackVolume() {
        return currentBackgroundTrackVolume;
    }

    public void release() {
        progressThread.pause();
        executeOnAllPlayers(new PlayerExecuteCommand() {
            @Override
            public void execute(SinglePreviewMediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        isPrepared = false;
    }

    @Override
    protected void finalize() throws Throwable {
        if (isPrepared) {
            release();
            Logger.e(TAG, "PreviewPlayer.release() was not called!");
        }
        super.finalize();
    }

    boolean isInPreviewMode() {
        return isInPreviewMode;
    }

    private void initMediaPlayers() throws IOException {
        initMediaPlayer(backgroundTrackPlayer, recordingStartTimeInDefaultTimeUnit, currentBackgroundTrackVolume);
        initMediaPlayer(originalVocalPlayer, 0, isOriginalVocalUsed ? currentVocalVolume : 0);
        initMediaPlayer(processedVocalPlayer, 0, isOriginalVocalUsed ? 0 : currentVocalVolume);
    }

    /**
     * Method will make sure that media player is fully initialized so calling play will have smallest
     * delay possible.
     */
    private void initMediaPlayer(MediaPlayer mediaPlayer, int startTimeInDefaultTimeUnit, float volume) throws IOException {
        mediaPlayer.prepare();
        mediaPlayer.setVolume(0, 0);
        mediaPlayer.start();
        mediaPlayer.pause();
        mediaPlayer.seekTo(startTimeInDefaultTimeUnit);
        mediaPlayer.setVolume(volume, volume);
    }

    private void seekTo(int timeInPlayerUnit, final OnCompletionListener onCompletionListener) {

        OnSinglePreviewPlayerSeekCompleteListener listener = new OnSinglePreviewPlayerSeekCompleteListener() {

            private int mediaPlayersCount = mediaPlayers.length;

            @Override
            public void onSeekComplete(SinglePreviewMediaPlayer mp) {
                mediaPlayersCount--;
                if (mediaPlayersCount == 0) {
                    onCompletionListener.onComplete();
                }
                mp.removeListener(this);
            }
        };

        backgroundTrackPlayer.addOnSeekCompletedListener(listener);
        originalVocalPlayer.addOnSeekCompletedListener(listener);
        processedVocalPlayer.addOnSeekCompletedListener(listener);

        backgroundTrackPlayer.seekTo(recordingStartTimeInDefaultTimeUnit + timeInPlayerUnit);
        originalVocalPlayer.seekTo(timeInPlayerUnit);
        processedVocalPlayer.seekTo(timeInPlayerUnit);
    }

    private void executeOnAllPlayers(PlayerExecuteCommand playerExecuteCommand) {
        for (SinglePreviewMediaPlayer mediaPlayer : mediaPlayers) {
            playerExecuteCommand.execute(mediaPlayer);
        }
    }

    private int toDefaultTimeUnit(float time, TimeUnit sourceTimeUnit) {
        return (int) TimeUnitConverterUtil.toResultTimeUnitLong(time, sourceTimeUnit, DEFAULT_TIME_UNIT);
    }
}