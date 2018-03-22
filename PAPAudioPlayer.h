//
//  PAPAudioPlayer.h
//  PreviewAudioPlayer
//

#import <Foundation/Foundation.h>
#import "PAPAudioTrackInfo.h"

@interface PAPAudioPlayer : NSObject

@property (nonatomic, assign) NSTimeInterval playbackStartTime;
@property (nonatomic, assign) NSTimeInterval playbackDuration;
@property (nonatomic, assign, readonly) NSTimeInterval vocalAudioTrackDuration;
@property (nonatomic, assign, readonly) NSTimeInterval backgroundAudioTrackDuration;

@property (nonatomic, assign) NSTimeInterval currentTime;

@property (nonatomic, assign) float vocalAudioTrackVolume;
@property (nonatomic, assign) float backgroundAudioTrackVolume;

@property (nonatomic, assign) BOOL shouldUseProcessedVocalTrack;
@property (nonatomic, copy, readonly) NSURL *selectedVocalTrackURL;

@property (nonatomic, copy) void(^playbackFinishedBlock)(NSURL *backgroundTrackURL);
@property (nonatomic, copy) void(^progressUpdateBlock)(NSTimeInterval currentTime);

- (BOOL)prepareWithAudioTrackInfo:(PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:(NSError **)error;

- (BOOL)start:(NSError **)error;

- (void)pause;
- (BOOL)unpause:(NSError **)error;

- (void)stop;

@end
