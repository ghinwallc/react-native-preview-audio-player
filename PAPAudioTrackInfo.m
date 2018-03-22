//
//  PAPAudioTrackInfo.m
//  PreviewAudioPlayer
//

#import "PAPAudioTrackInfo.h"

@implementation PAPAudioTrackInfo

- (instancetype)initWithBackgroundTrackURL:(NSURL *)backgroundTrackURL originalVocalTrackURL:(NSURL *)originalVocalTrackURL processedVocalTrackURL:(NSURL *)processedVocalTrackURL recordingStartTime:(NSTimeInterval)recordingStartTime {
    self = [super init];

    if (self) {
        NSParameterAssert(backgroundTrackURL);
        NSParameterAssert(originalVocalTrackURL);
        NSParameterAssert(processedVocalTrackURL);

        _backgroundTrackURL     = [backgroundTrackURL copy];
        _originalVocalTrackURL  = [originalVocalTrackURL copy];
        _processedVocalTrackURL = [processedVocalTrackURL copy];
        _recordingStartTime     = recordingStartTime;
    }
    return self;
}

@end
