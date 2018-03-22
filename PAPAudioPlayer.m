//
//  PAPAudioPlayer.m
//  PreviewAudioPlayer
//

#import "PAPAudioPlayer.h"
#import "PAPRenderer.h"

#import "AERenderer.h"
#import "AEAudioUnitOutput.h"

@interface PAPAudioPlayer ()

@property (nonatomic, strong, readonly) AERenderer *renderer;
@property (nonatomic, strong, readonly) AEAudioUnitOutput *outputUnit;

@property (nonatomic, strong, readonly) PAPRenderer *previewRenderer;

@end

@implementation PAPAudioPlayer

- (instancetype)init {
    self = [super init];
    if (self) {
        _renderer = [AERenderer new];
        _outputUnit = [[AEAudioUnitOutput alloc] initWithRenderer:_renderer];
        _previewRenderer = [[PAPRenderer alloc] initWithRenderer:_renderer];
    }

    return self;
}

- (BOOL)prepareWithAudioTrackInfo:(PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:(NSError *__autoreleasing *)error {
    return [self.previewRenderer prepareWithAudioTrackInfo:audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:error];
}

- (BOOL)start:(NSError *__autoreleasing *)error {
    return [self.outputUnit start:error] && [self.previewRenderer start:error];
}

- (void)pause {
    [self.previewRenderer pause];
    [self.outputUnit stop];
}

- (BOOL)unpause:(NSError **)error {
    [self.previewRenderer unpause];
    return [self.outputUnit start:error];
}

- (void)stop {
    [self.previewRenderer stop];
    [self.outputUnit stop];
}


#pragma mark - 

- (void)setProgressUpdateBlock:(void (^)(NSTimeInterval))progressUpdateBlock {
    self.previewRenderer.progressUpdateBlock = progressUpdateBlock;
}

- (void (^)(NSTimeInterval))progressUpdateBlock {
    return self.previewRenderer.progressUpdateBlock;
}

- (void)setPlaybackFinishedBlock:(void (^)(NSURL *))playbackFinishedBlock {
    self.previewRenderer.playbackFinishedBlock = playbackFinishedBlock;
}

- (void (^)(NSURL *))playbackFinishedBlock {
    return self.previewRenderer.playbackFinishedBlock;
}

- (void)setPlaybackStartTime:(NSTimeInterval)playbackStartTime {
    self.previewRenderer.playbackStartTime = playbackStartTime;
}

- (NSTimeInterval)playbackStartTime {
    return self.previewRenderer.playbackStartTime;
}

- (void)setPlaybackDuration:(NSTimeInterval)playbackDuration {
    self.previewRenderer.playbackDuration = playbackDuration;
}

- (NSTimeInterval)playbackDuration {
    return self.previewRenderer.playbackDuration;
}

- (NSTimeInterval)vocalAudioTrackDuration {
    return self.previewRenderer.vocalAudioTrackDuration;
}

- (NSTimeInterval)backgroundAudioTrackDuration {
    return self.previewRenderer.backgroundAudioTrackDuration;
}

- (NSTimeInterval)currentTime {
    return self.previewRenderer.currentTime;
}

- (void)setCurrentTime:(NSTimeInterval)currentTime {
    self.previewRenderer.currentTime = currentTime;
}

- (void)setVocalAudioTrackVolume:(float)vocalAudioTrackVolume {
    self.previewRenderer.vocalAudioTrackVolume = vocalAudioTrackVolume;
}

- (float)vocalAudioTrackVolume {
    return self.previewRenderer.vocalAudioTrackVolume;
}

- (void)setBackgroundAudioTrackVolume:(float)backgroundAudioTrackVolume {
    self.previewRenderer.backgroundAudioTrackVolume = backgroundAudioTrackVolume;
}

- (float)backgroundAudioTrackVolume {
    return self.previewRenderer.backgroundAudioTrackVolume;
}

- (BOOL)shouldUseProcessedVocalTrack {
    return self.previewRenderer.shouldUseProcessedVocalTrack;
}

- (void)setShouldUseProcessedVocalTrack:(BOOL)shouldUseProcessedVocalTrack {
    self.previewRenderer.shouldUseProcessedVocalTrack = shouldUseProcessedVocalTrack;
}

- (NSURL *)selectedVocalTrackURL {
    return self.previewRenderer.selectedVocalTrackURL;
}

@end
