package fm.ghinwa.previewaudioplayer.implementation.player;


public class SingleOffsetPreviewMediaPlayer extends SinglePreviewMediaPlayer {

    private final int millisecsOffset;

    public SingleOffsetPreviewMediaPlayer(int milliSecondsOffset) {
        this.millisecsOffset = milliSecondsOffset;
    }

    @Override
    public int getCurrentPosition() {
        return super.getCurrentPosition() - millisecsOffset;
    }


}
