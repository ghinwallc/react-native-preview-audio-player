//
//  PAPAudioExporter.m
//  PreviewAudioPlayer
//

#import "PAPAudioExporter.h"
#import "PAPRenderer.h"

#import "AERenderer.h"
#import "AEAudioFileOutput.h"
#import "PAPExportingConfiguration.h"

#import <AVFoundation/AVFoundation.h>

@interface PAPAudioExporter ()

@property (nonatomic, strong, readonly) AERenderer *renderer;
@property (nonatomic, strong, readonly) PAPRenderer *previewRenderer;

@property (nonatomic, strong) AEAudioFileOutput *fileOutput;

@end

@implementation PAPAudioExporter

- (instancetype)init {
    self = [self initWithRenderer:[AERenderer new]];
    return self;
}

- (instancetype)initWithRenderer:(AERenderer *)renderer {
    NSParameterAssert(renderer);
    self = [super init];
    if (self) {
        _renderer = renderer;
        _previewRenderer = [[PAPRenderer alloc] initWithRenderer:renderer];
    }
    return self;
}

- (BOOL)prepareWithAudioTrackInfo:(PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:(NSError *__autoreleasing *)error {
    return [self.previewRenderer prepareWithAudioTrackInfo:audioTrackInfo shouldMixAudio:shouldMixAudio error:error];
}

- (void)exportUsingConfiguration:(PAPExportingConfiguration *)configuration withOutputFileURL:(NSURL *)outputFileURL shouldMixAudio:(BOOL)shouldMixAudio completionBlock:(void (^)(BOOL, NSURL *, NSError *))completionBlock {
    completionBlock = completionBlock ?: ^(BOOL _1, NSURL *_2, NSError *_3){};

    if (self.fileOutput) {
        completionBlock(NO, nil, nil);
        return;
    }

    completionBlock = completionBlock ?: ^(BOOL _1, NSURL *_2, NSError *_3){};
    NSError *error;

    [self.previewRenderer configurePlayersWithShouldMixAudio:shouldMixAudio];

    self.previewRenderer.vocalAudioTrackVolume = configuration.audioTrackVolume;
    self.previewRenderer.backgroundAudioTrackVolume = configuration.backgroundTrackVolume;
    self.previewRenderer.playbackStartTime = configuration.startTime;
    self.previewRenderer.currentTime = configuration.startTime;

    NSTimeInterval duration = MIN(configuration.duration, MAX(self.previewRenderer.vocalAudioTrackDuration - configuration.startTime, 0));

    self.fileOutput = [self fileOutputWithURL:outputFileURL error:&error];
    if (self.fileOutput && [self.previewRenderer start:&error]) {
        __weak typeof(self) weakSelf = self;
        [self.fileOutput runForDuration:duration
            completionBlock:^(NSError * _Nullable error) {
                __strong typeof(weakSelf) self = weakSelf;
                [self.fileOutput finishWriting];
                self.fileOutput = nil;
                if (!error) {
                    completionBlock(YES, outputFileURL, nil);
                } else {
                    completionBlock(NO, nil, error);
                }
            }];
    } else {
        completionBlock(NO, nil, error);
    }
}


#pragma mark -

- (AEAudioFileOutput *)fileOutputWithURL:(NSURL *)outputFileURL error:(NSError **)error {
    return [[AEAudioFileOutput alloc] initWithRenderer:self.renderer URL:outputFileURL type:AEAudioFileTypeM4A sampleRate:44100.0 channelCount:2 error:error];
}


#pragma mark - Properties

- (BOOL)shouldUseProcessedVocalTrack {
    return self.previewRenderer.shouldUseProcessedVocalTrack;
}

- (void)setShouldUseProcessedVocalTrack:(BOOL)shouldUseProcessedVocalTrack {
    self.previewRenderer.shouldUseProcessedVocalTrack = shouldUseProcessedVocalTrack;
}

@end
