package fm.ghinwa.previewaudioplayer.implementation.player;

import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;


class SinglePreviewMediaPlayer extends MediaPlayer {

    private List<OnSinglePreviewPlayerSeekCompleteListener> onSeekCompleteListeners;

    SinglePreviewMediaPlayer() {
        super();
        onSeekCompleteListeners = new ArrayList<>();
        super.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                notifySeekCompleteListeners();
            }
        });
    }

    void addOnSeekCompletedListener(OnSinglePreviewPlayerSeekCompleteListener listener) {
        onSeekCompleteListeners.add(listener);
    }

    void removeListener(OnSinglePreviewPlayerSeekCompleteListener listener) {
        onSeekCompleteListeners.remove(listener);
    }

    @Override
    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
        throw new UnsupportedOperationException("Unsupported operation use add listener instead.");
    }

    @Override
    public void release() {
        super.release();
        onSeekCompleteListeners.clear();
    }

    private void notifySeekCompleteListeners() {
        List<OnSinglePreviewPlayerSeekCompleteListener> listenersCopy = new ArrayList<>(onSeekCompleteListeners);
        for (OnSinglePreviewPlayerSeekCompleteListener listener : listenersCopy) {
            listener.onSeekComplete(this);
        }
    }
}
