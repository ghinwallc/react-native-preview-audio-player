//
//  PAPRenderer.h
//  PreviewAudioPlayer
//

#import <Foundation/Foundation.h>
#import "AERenderer.h"
#import "PAPAudioTrackInfo.h"

@interface PAPRenderer : NSObject

- (instancetype)initWithRenderer:(AERenderer *)renderer NS_DESIGNATED_INITIALIZER;

@property (nonatomic, assign) NSTimeInterval playbackDuration;
@property (nonatomic, assign) NSTimeInterval playbackStartTime;
@property (nonatomic, assign, readonly) NSTimeInterval vocalAudioTrackDuration;
@property (nonatomic, assign, readonly) NSTimeInterval backgroundAudioTrackDuration;

@property (nonatomic, assign) NSTimeInterval currentTime;

@property (nonatomic, assign) float vocalAudioTrackVolume;
@property (nonatomic, assign) float backgroundAudioTrackVolume;

@property (nonatomic, assign) BOOL shouldUseProcessedVocalTrack;
@property (nonatomic, copy, readonly) NSURL *selectedVocalTrackURL;

@property (nonatomic, copy) void (^progressUpdateBlock)(NSTimeInterval currentTime);
@property (nonatomic, copy) void (^playbackFinishedBlock)(NSURL *fileURL);

@property (nonatomic, strong, readonly) AERenderer *renderer;

- (BOOL)prepareWithAudioTrackInfo:(PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:(NSError **)error;
- (void)configurePlayersWithShouldMixAudio:(BOOL)shouldMixAudio;

- (BOOL)start:(NSError **)error;

- (void)pause;
- (void)unpause;

- (void)stop;

@end
