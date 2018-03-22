//
//  PAPRenderer.m
//  PreviewAudioPlayer
//

#import "PAPRenderer.h"
#import "APRAudioPlayer.h"
#import "AERenderer.h"
#import "AEModule.h"
#import "AEManagedValue.h"
#import "AEAudioUnitOutput.h"
#import "AEMixerModule.h"

@interface PAPRenderer ()

@property (nonatomic, strong, readonly) APRAudioPlayer *originalVocalAudioPlayer;
@property (nonatomic, strong, readonly) APRAudioPlayer *processedVocalAudioPlayer;
@property (nonatomic, strong, readonly) APRAudioPlayer *backgroundAudioPlayer;
@property (nonatomic, strong, readonly) NSArray<APRAudioPlayer *> *audioPlayers;

@property (nonatomic, strong, readonly) AEMixerModule *mixerModule;

@property (nonatomic, assign) float internalVocalAudioTrackVolume;
@property (nonatomic, assign) float internalBackgroundAudioTrackVolume;

@property (nonatomic, readonly) float originalVocalVolume;
@property (nonatomic, readonly) float processedVocalVolume;

@property BOOL shouldMix;

@property (nonatomic, strong, readonly) AEManagedValue *progressUpdateBlockManagedValue;

@end

@implementation PAPRenderer

- (instancetype)init {
    return [self initWithRenderer:[AERenderer new]];
}

- (instancetype)initWithRenderer:(AERenderer *)renderer {
    NSParameterAssert(renderer);
    self = [super init];
    if (self) {
        __weak typeof(self) weakSelf = self;
        _progressUpdateBlockManagedValue = [AEManagedValue new];
        _renderer = renderer;
        _renderer.block = ^(const AERenderContext * _Nonnull context) {
            __strong typeof(weakSelf) self = weakSelf;
            if (!self) {
                return;
            }

            if (APRAudioPlayerShouldProcessInput(self->_originalVocalAudioPlayer) && APRAudioPlayerShouldProcessInput(self->_processedVocalAudioPlayer)) {
                AEModuleProcess(self->_mixerModule, context);

                dispatch_async(dispatch_get_main_queue(), ^{
                    void(^progressUpdateBlock)(NSTimeInterval) = self->_progressUpdateBlockManagedValue.objectValue;
                    if (progressUpdateBlock) {
                        progressUpdateBlock(self.originalVocalAudioPlayer.currentTime);
                    }
                });
                AEBufferStackMix(context->stack, 2);
            }

            AERenderContextOutput(context, 1);
        };

        _originalVocalAudioPlayer = [[APRAudioPlayer alloc] initWithRenderer:_renderer];
        _processedVocalAudioPlayer = [[APRAudioPlayer alloc] initWithRenderer:_renderer];
        _backgroundAudioPlayer = [[APRAudioPlayer alloc] initWithRenderer:_renderer];
        _audioPlayers = @[_originalVocalAudioPlayer, _processedVocalAudioPlayer, _backgroundAudioPlayer];
        _shouldUseProcessedVocalTrack = NO;
        _mixerModule = [[AEMixerModule alloc] initWithRenderer:_renderer];
        _internalVocalAudioTrackVolume = 1.0;
        _internalBackgroundAudioTrackVolume = 1.0;
    }
    
    return self;
}

- (BOOL)prepareWithAudioTrackInfo:(PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:(NSError *__autoreleasing *)error {
    self.shouldMix = shouldMixAudio;

    BOOL success = [self getPlayersStatusWithAudioTrackInfo:audioTrackInfo shouldMixAudio:shouldMixAudio error:error];

    if (success) {
        APRAudioPlayerClearMixerModules(self.mixerModule);
        [self addAudioPlayerToMixerModuleWithAudioPlayer:self.originalVocalAudioPlayer volume:self.originalVocalVolume];
        [self addAudioPlayerToMixerModuleWithAudioPlayer:self.processedVocalAudioPlayer volume:self.processedVocalVolume];
        [self addAudioPlayerToMixerModuleWithAudioPlayer:self.backgroundAudioPlayer volume:self.backgroundAudioTrackVolume];
    }

    return success;
}

- (void)addAudioPlayerToMixerModuleWithAudioPlayer:(APRAudioPlayer *)audioPlayer volume:(float)volume {
    APRAudioPlayerRemoveFromMixerModule(audioPlayer, self.mixerModule);
    APRAudioPlayerAddToMixerModule(audioPlayer, self.mixerModule);
    APRAudioPlayerSetVolumeInMixerModule(audioPlayer, self.mixerModule, volume);
}

- (BOOL)start:(NSError *__autoreleasing *)error {
    for (APRAudioPlayer *player in self.audioPlayers) {
        if (![player start:error]) {
            return NO;
        }
    }
    return YES;
}

- (void)pause {
    for (APRAudioPlayer *player in self.audioPlayers) {
        [player pause];
    }
}

- (void)unpause {
    for (APRAudioPlayer *player in self.audioPlayers) {
        [player unpause];
    }
}

- (void)stop {
    for (APRAudioPlayer *player in self.audioPlayers) {
        [player stop];
    }

    if (self.progressUpdateBlock) {
        self.progressUpdateBlock(0);
    }
}


#pragma mark - Properties

- (void)setProgressUpdateBlock:(void (^)(NSTimeInterval currentTime))progressUpdateBlock {
    self.progressUpdateBlockManagedValue.objectValue = progressUpdateBlock;
}

- (void (^)(NSTimeInterval))progressUpdateBlock {
    return self.progressUpdateBlockManagedValue.objectValue;
}

- (void)setPlaybackFinishedBlock:(void (^)(NSURL *))playbackFinishedBlock {
    self.originalVocalAudioPlayer.playbackFinishedBlock = playbackFinishedBlock;
}

- (void (^)(NSURL *))playbackFinishedBlock {
    return self.originalVocalAudioPlayer.playbackFinishedBlock;
}

- (void)setPlaybackDuration:(NSTimeInterval)playbackDuration {
    for (APRAudioPlayer *player in self.audioPlayers) {
        player.playbackDuration = playbackDuration;
    }
}

- (NSTimeInterval)playbackDuration {
    if (self.shouldMix) {
        return MIN(self.originalVocalAudioPlayer.playbackDuration, MIN(self.processedVocalAudioPlayer.playbackDuration, self.backgroundAudioPlayer.playbackDuration));
    } else {
        return MIN(self.originalVocalAudioPlayer.playbackDuration, self.processedVocalAudioPlayer.playbackDuration);
    }
}

- (void)setPlaybackStartTime:(NSTimeInterval)playbackStartTime {
    for (APRAudioPlayer *player in self.audioPlayers) {
        player.playbackStartTime = playbackStartTime;
    }
}

- (NSTimeInterval)playbackStartTime {
    return self.originalVocalAudioPlayer.playbackStartTime;
}

- (NSTimeInterval)vocalAudioTrackDuration {
    return self.originalVocalAudioPlayer.duration;
}

- (NSTimeInterval)backgroundAudioTrackDuration {
    return self.backgroundAudioPlayer.duration;
}

- (NSTimeInterval)currentTime {
    return self.originalVocalAudioPlayer.currentTime;
}

- (void)setCurrentTime:(NSTimeInterval)currentTime {
    for (APRAudioPlayer *player in self.audioPlayers) {
        player.currentTime = currentTime;
    }

    if (self.progressUpdateBlock) {
        self.progressUpdateBlock(currentTime);
    }
}

- (void)setVocalAudioTrackVolume:(float)volume {
    volume = ClampedVolume(volume);
    self.internalVocalAudioTrackVolume = volume;
    [self updateVocalAudioTrackVolume];
}

- (void)updateVocalAudioTrackVolume {
    APRAudioPlayerSetVolumeInMixerModule(self.originalVocalAudioPlayer, self.mixerModule, self.originalVocalVolume);
    APRAudioPlayerSetVolumeInMixerModule(self.processedVocalAudioPlayer, self.mixerModule, self.processedVocalVolume);
}

- (float)vocalAudioTrackVolume {
    return self.internalVocalAudioTrackVolume;
}

- (void)setBackgroundAudioTrackVolume:(float)volume {
    volume = ClampedVolume(volume);
    self.internalBackgroundAudioTrackVolume = volume;
    APRAudioPlayerSetVolumeInMixerModule(self.backgroundAudioPlayer, self.mixerModule, volume);
}

- (float)backgroundAudioTrackVolume {
    return self.internalBackgroundAudioTrackVolume;
}

- (float)originalVocalVolume {
    return self.shouldUseProcessedVocalTrack ? 0 : self.internalVocalAudioTrackVolume;
}

- (float)processedVocalVolume {
    return self.shouldUseProcessedVocalTrack ? self.internalVocalAudioTrackVolume : 0;
}

- (void)setShouldUseProcessedVocalTrack:(BOOL)shouldUseProcessedVocalTrack {
    _shouldUseProcessedVocalTrack = shouldUseProcessedVocalTrack;
    [self updateVocalAudioTrackVolume];
}

- (NSURL *)selectedVocalTrackURL {
    return self.shouldUseProcessedVocalTrack ? self.processedVocalAudioPlayer.audioFileURL : self.originalVocalAudioPlayer.audioFileURL;
}

static const float ClampedVolume(float volume) {
    return MAX(0, MIN(1, volume));
}

- (BOOL)getPlayersStatusWithAudioTrackInfo:(PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:(NSError *__autoreleasing *)error {
    return [self.originalVocalAudioPlayer  prepareWithAudioFileAtURL:audioTrackInfo.originalVocalTrackURL  startTime:0 error:error]
    && [self.processedVocalAudioPlayer prepareWithAudioFileAtURL:audioTrackInfo.processedVocalTrackURL startTime:0 error:error]
    && [self.backgroundAudioPlayer prepareWithAudioFileAtURL:audioTrackInfo.backgroundTrackURL startTime:audioTrackInfo.recordingStartTime error:error];

}

- (void)configurePlayersWithShouldMixAudio:(BOOL)shouldMixAudio {
    if (!shouldMixAudio) {
        _audioPlayers = @[_originalVocalAudioPlayer, _processedVocalAudioPlayer];
    }
}

@end
