//
//  PAPAudioTrackInfo.h
//  PreviewAudioPlayer
//

#import <Foundation/Foundation.h>

@interface PAPAudioTrackInfo : NSObject

@property (nonatomic, copy, readonly) NSURL *backgroundTrackURL;
@property (nonatomic, copy, readonly) NSURL *originalVocalTrackURL;
@property (nonatomic, copy, readonly) NSURL *processedVocalTrackURL;
@property (nonatomic, assign, readonly) NSTimeInterval recordingStartTime;

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithBackgroundTrackURL:(NSURL *)backgroundTrackURL originalVocalTrackURL:(NSURL *)originalVocalTrackURL processedVocalTrackURL:(NSURL *)processedVocalTrackURL recordingStartTime:(NSTimeInterval)recordingStartTime;

@end
