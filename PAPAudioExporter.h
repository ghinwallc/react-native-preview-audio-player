//
//  PAPAudioExporter.h
//  PreviewAudioPlayer
//

#import <Foundation/Foundation.h>
#import "PAPAudioTrackInfo.h"

@class AERenderer, PAPExportingConfiguration;

@interface PAPAudioExporter : NSObject

@property (nonatomic, assign) BOOL shouldUseProcessedVocalTrack;

- (instancetype)initWithRenderer:(AERenderer *)renderer NS_DESIGNATED_INITIALIZER;

- (BOOL)prepareWithAudioTrackInfo:(PAPAudioTrackInfo *)audioTrackInfo shouldMixAudio:(BOOL)shouldMixAudio error:(NSError **)error;

- (void)exportUsingConfiguration:(PAPExportingConfiguration *)configuration withOutputFileURL:(NSURL *)outputFileURL shouldMixAudio:(BOOL)shouldMixAudio completionBlock:(void (^)(BOOL success, NSURL *outputFileURL, NSError *error))completionBlock;

@end
