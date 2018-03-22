package fm.ghinwa.previewaudioplayer.implementation.player;


import android.os.Handler;
import android.os.Looper;

class PlayerProgressThread extends Thread {

    private static final float UPDATES_PER_SECOND = 60;
    private static final float UPDATE_TIME = 1000 / UPDATES_PER_SECOND;

    private final PreviewPlayer previewPlayer;
    private final Object pauseLock = new Object();
    private final Handler handler;

    private volatile boolean isPaused = true;

    PlayerProgressThread(PreviewPlayer previewPlayer) {
        this.previewPlayer = previewPlayer;
        handler = new Handler(Looper.getMainLooper());
    }

    void unPause() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    public void pause() {
        isPaused = true;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {

            handlePause();

            long startTime = System.currentTimeMillis();

            runRepeatableJob();

            long endTime = System.currentTimeMillis();
            long deltaTime = (long) (UPDATE_TIME - (endTime - startTime));

            if (deltaTime > 0) {
                try {
                    Thread.sleep(deltaTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void runRepeatableJob() {
        final int currentPlaybackTimeMillis = previewPlayer.getCurrentTimeMillis();

        updateProgress(currentPlaybackTimeMillis);

        boolean shouldFinishPreview = previewPlayer.isInPreviewMode() && currentPlaybackTimeMillis > previewPlayer.getPreviewEndTimeInMillis();
        if (shouldFinishPreview) {
            previewPlayer.stop();
            notifyPlaybackCompleted();
        }
    }

    private void updateProgress(final int currentPlaybackTimeMillis) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                previewPlayer.onProgressUpdate(currentPlaybackTimeMillis);
            }
        });
    }

    private void notifyPlaybackCompleted() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                previewPlayer.completePlayback();
            }
        });
    }

    private void handlePause() {
        if (isPaused) {
            synchronized (pauseLock) {
                try {
                    while (isPaused) {
                        pauseLock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
