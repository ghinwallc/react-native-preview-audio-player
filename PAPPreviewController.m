//
//  PAPPreviewController.m
//  PreviewAudioPlayer
//

#import "PAPPreviewController.h"
#import "APRAudioPlayer.h"
#import "PAPRenderer.h"
#import "AERenderer.h"
#import "AEAudioUnitOutput.h"
#import "AEAudioUnitInputModule.h"
#import "AEAudioFileOutput.h"

#import "PAPAudioPlayer.h"
#import "PAPAudioExporter.h"
#import "PAPExportingConfiguration.h"

#import <React/RCTUtils.h>
#import <React/RCTEventDispatcher.h>

#import "PAPAudioTrackInfo.h"

#import <AVFoundation/AVFoundation.h>


@interface PAPPreviewController ()

@property (nonatomic, assign) CGFloat previewStartTime;
@property (nonatomic, assign) CGFloat previewDuration;

@property (nonatomic, strong, readonly) PAPAudioPlayer *audioPlayer;
@property (nonatomic, strong, readonly) PAPAudioExporter *audioExporter;

@end


@implementation PAPPreviewController

RCT_EXPORT_MODULE()

RCT_EXTERN_REMAP_METHOD(prepare, prepareWithOriginalVocalAudioFileAtURL:(NSURL *)originalVocalAudioFileURL processedVocalAudioFileAtURL:(NSURL *)processedVocalAudioFileURL backgroundAudioFileAtURL:(NSURL *)backgroundAudioFileAtURL recordingStartTime:(nonnull NSNumber *)recordingStartTime shouldMixAudio:(BOOL)shouldMixAudio callback:(RCTResponseSenderBlock)callback)

RCT_EXTERN_REMAP_METHOD(playAt, playAt:(CGFloat)startTime callback:(RCTResponseSenderBlock)callback)

@synthesize bridge = _bridge;


- (instancetype)init {
    self = [super init];
    if (self) {
        __weak typeof(self) weakSelf = self;
        _audioExporter = [PAPAudioExporter new];
        _audioPlayer = [PAPAudioPlayer new];
        _audioPlayer.progressUpdateBlock = ^(NSTimeInterval currentTime) {
            __strong typeof(weakSelf) self = weakSelf;
            if (!self) {
                return;
            }
            [self.bridge.eventDispatcher sendDeviceEventWithName:@"previewAudioPlayerPlaybackProgressUpdate" body:@{ @"currentTime" : @(currentTime) }];
        };
        _audioPlayer.playbackFinishedBlock = ^(NSURL *fileURL) {
            __strong typeof(weakSelf) self = weakSelf;
            if (!self) {
                return;
            }
            [self.bridge.eventDispatcher sendDeviceEventWithName:@"previewAudioPlayerPlaybackFinished" body:@{ @"finished" : @YES }];
        };
    }

    return self;
}

RCT_EXPORT_METHOD(prepareWithOriginalVocalAudioFileAtURL:(NSURL *)originalVocalAudioFileURL processedVocalAudioFileAtURL:(NSURL *)processedVocalAudioFileURL backgroundAudioFileAtURL:(NSURL *)backgroundAudioFileAtURL recordingStartTime:(nonnull NSNumber *)recordingStartTime shouldMixAudio:(BOOL)shouldMixAudio callback:(RCTResponseSenderBlock)callback) {
    PAPAudioTrackInfo *audioTrackInfo = [[PAPAudioTrackInfo alloc] initWithBackgroundTrackURL:backgroundAudioFileAtURL originalVocalTrackURL:originalVocalAudioFileURL processedVocalTrackURL:processedVocalAudioFileURL recordingStartTime:recordingStartTime.doubleValue];

    callback = callback ?: ^(NSArray *_){};

    [self prepareWithAudioTrackInfo:audioTrackInfo shouldMixAudio:shouldMixAudio callback:callback];
}

- (void)prepareWithAudioTrackInfo:(nonnull PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio callback:(nonnull RCTResponseSenderBlock)callback {
    __block NSError *error;

    BOOL (^tryToPrepareAudioPlayer)() = ^{
        BOOL result = [self.audioPlayer prepareWithAudioTrackInfo:audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:&error];

        self.audioPlayer.playbackStartTime = 0;
        self.audioPlayer.playbackDuration = self.audioPlayer.vocalAudioTrackDuration;

        return result;
    };
    BOOL (^tryToPrepareAudioExporter)() = ^{
        return [self.audioExporter prepareWithAudioTrackInfo:audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:&error];
    };

    BOOL success = tryToPrepareAudioPlayer() && tryToPrepareAudioExporter();

    if (success) {
        callback(@[[NSNull null]]);
        
    } else {
        callback(@[RCTJSErrorFromNSError(error)]);
    }
}

RCT_EXPORT_METHOD(play:(RCTResponseSenderBlock)callback) {
    callback = callback ?: ^(NSArray *_){};
    NSError *error;
    
    self.audioPlayer.playbackStartTime = 0;
    self.audioPlayer.playbackDuration = self.audioPlayer.vocalAudioTrackDuration;

    if ([self.audioPlayer start:&error]) {
        callback(@[[NSNull null]]);
    } else {
        callback(@[RCTJSErrorFromNSError(error)]);
    }
}

RCT_EXPORT_METHOD(playAt:(CGFloat)startTime callback:(RCTResponseSenderBlock)callback) {
    callback = callback ?: ^(NSArray *_){};
    NSError *error;

    self.audioPlayer.playbackStartTime = startTime;
    self.audioPlayer.playbackDuration = self.audioPlayer.vocalAudioTrackDuration;

    if ([self.audioPlayer start:&error]) {
        callback(@[[NSNull null]]);
    } else {
        callback(@[RCTJSErrorFromNSError(error)]);
    }
}

RCT_EXPORT_METHOD(pause:(RCTResponseSenderBlock)callback) {
    [self.audioPlayer pause];
    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(unpause:(RCTResponseSenderBlock)callback) {
    callback = callback ?: ^(NSArray *_){};
    NSError *error;

    if ([self.audioPlayer unpause:&error]) {
        callback(@[[NSNull null]]);
    } else {
        callback(@[RCTJSErrorFromNSError(error)]);
    }
}

RCT_EXPORT_METHOD(stop:(RCTResponseSenderBlock)callback) {
    [self.audioPlayer stop];
    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(switchToOriginalVocalTrack:(RCTResponseSenderBlock)callback) {
    self.audioPlayer.shouldUseProcessedVocalTrack = NO;
    self.audioExporter.shouldUseProcessedVocalTrack = NO;

    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(switchToProcessedVocalTrack:(RCTResponseSenderBlock)callback) {
    self.audioPlayer.shouldUseProcessedVocalTrack = YES;
    self.audioExporter.shouldUseProcessedVocalTrack = YES;

    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(getSelectedVocalTrackPath:(RCTResponseSenderBlock)callback) {
    callback(@[[self.audioPlayer.selectedVocalTrackURL path]]);
}

#pragma mark - Exporting

- (NSURL *)URLForExportOutputFile {
    NSURL *URL = [NSURL fileURLWithPath:[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject]];
    URL = [URL URLByAppendingPathComponent:[[NSUUID UUID] UUIDString]];
    URL = [URL URLByAppendingPathExtension:@"m4a"];
    return URL;
}

RCT_EXPORT_METHOD(export:(BOOL)shouldMixAudio callback:(RCTResponseSenderBlock)callback) {
    NSURL *URL = [self URLForExportOutputFile];

    if (shouldMixAudio) {
        [self exportToFileAtURL:URL withStartTime:0 duration:self.audioPlayer.backgroundAudioTrackDuration shouldMixAudio:shouldMixAudio callback:callback];
    } else {
        [self exportToFileAtURL:URL withStartTime:0 duration:self.audioPlayer.vocalAudioTrackDuration shouldMixAudio:shouldMixAudio callback:callback];
    }
}

RCT_EXPORT_METHOD(exportPreview:(BOOL)shouldMixAudio callback:(RCTResponseSenderBlock)callback) {
    NSURL *URL = [self URLForExportOutputFile];
    [self exportToFileAtURL:URL withStartTime:self.previewStartTime duration:self.previewDuration shouldMixAudio:shouldMixAudio callback:callback];
}

RCT_EXPORT_METHOD(exportToFileAtURL:(NSURL *)fileURL withStartTime:(NSTimeInterval)startTime duration:(NSTimeInterval)duration shouldMixAudio:(BOOL)shouldMixAudio callback:(RCTResponseSenderBlock)callback) {
    callback = callback ?: ^(NSArray *_){};
    
    PAPExportingConfiguration *exportingConfiguration = [self getConfigurationWithStartTime:startTime duration:duration shouldMixAudio:shouldMixAudio];
    
    [self.audioExporter exportUsingConfiguration:exportingConfiguration withOutputFileURL:fileURL shouldMixAudio:shouldMixAudio completionBlock:^(BOOL success, NSURL *outputFileURL, NSError *error) {
         if (success) {
             callback(@[[NSNull null], outputFileURL.path]);
         } else {
             callback(@[RCTJSErrorFromNSError(error)]);
         }
     }];
}


#pragma mark - Preview Playback

RCT_EXPORT_METHOD(playPreview:(RCTResponseSenderBlock)callback) {
    callback = callback ?: ^(NSArray *_){};
    NSError *error;

    self.audioPlayer.playbackStartTime = self.previewStartTime;
    self.audioPlayer.playbackDuration = self.previewDuration;

    if ([self.audioPlayer start:&error]) {
        callback(@[[NSNull null]]);
    } else {
        callback(@[RCTJSErrorFromNSError(error)]);
    }
}

RCT_EXPORT_METHOD(setPreviewAt:(CGFloat)previewTime withDuration:(CGFloat)duration callback:(RCTResponseSenderBlock)callback) {
    [self setPreviewDuration:duration callback:nil];
    [self setPreviewStartTime:previewTime callback:nil];

    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(setPreviewStartTime:(CGFloat)previewStartTime callback:(RCTResponseSenderBlock)callback) {
    CGFloat recordingLength = self.audioPlayer.vocalAudioTrackDuration;
    CGFloat minPreviewStartTime = 0;
    CGFloat maxPreviewStartTime = MAX(minPreviewStartTime, recordingLength - self.previewDuration);

    self.previewStartTime = MAX(minPreviewStartTime, MIN(maxPreviewStartTime, previewStartTime));

    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(getPreviewStartTime:(RCTResponseSenderBlock)callback) {
    NSParameterAssert(callback);
    callback(@[@(self.previewStartTime)]);
}

RCT_EXPORT_METHOD(setPreviewDuration:(CGFloat)previewDuration callback:(RCTResponseSenderBlock)callback) {
    CGFloat recordingLength = self.audioPlayer.vocalAudioTrackDuration;

    self.previewDuration = MIN(previewDuration, recordingLength);

    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(getPreviewDuration:(RCTResponseSenderBlock)callback) {
    NSParameterAssert(callback);
    callback(@[@(self.previewDuration)]);
}

RCT_EXPORT_METHOD(getPlaybackDuration:(RCTResponseSenderBlock)callback) {
    NSParameterAssert(callback);
    callback(@[@(self.audioPlayer.playbackDuration)]);
}

#pragma mark - Volume

RCT_EXPORT_METHOD(setVolume:(CGFloat)vocalAudioTrackVolume backgroundTrackVolume:(CGFloat)backgroundAudioTrackVolume callback:(RCTResponseSenderBlock)callback) {
    self.audioPlayer.vocalAudioTrackVolume = vocalAudioTrackVolume;
    self.audioPlayer.backgroundAudioTrackVolume = backgroundAudioTrackVolume;
    if (callback) {
        callback(@[[NSNull null]]);
    }
}


#pragma mark - Seek

RCT_EXPORT_METHOD(seekToTime:(CGFloat)time callback:(RCTResponseSenderBlock)callback) {
    self.audioPlayer.currentTime = time;
    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(seekByTimeOffset:(CGFloat)timeOffset callback:(RCTResponseSenderBlock)callback) {
    self.audioPlayer.currentTime += timeOffset;
    if (callback) {
        callback(@[[NSNull null]]);
    }
}

RCT_EXPORT_METHOD(getCurrentTime:(RCTResponseSenderBlock)callback) {
    callback(@[@(self.audioPlayer.currentTime)]);
}

#pragma mark -

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (PAPExportingConfiguration *)getConfigurationWithStartTime:(NSTimeInterval)startTime duration:(NSTimeInterval)duration shouldMixAudio:(BOOL)shouldMixAudio {

    if (shouldMixAudio) {
        return [PAPExportingConfiguration exportingConfigurationWithStartTime:startTime duration:duration audioTrackVolume:self.audioPlayer.vocalAudioTrackVolume backgroundTrackVolume:self.audioPlayer.backgroundAudioTrackVolume];
    } else {
        return [PAPExportingConfiguration exportingConfigurationWithStartTime:startTime duration:duration audioTrackVolume:self.audioPlayer.vocalAudioTrackVolume];
    }
}

@end
