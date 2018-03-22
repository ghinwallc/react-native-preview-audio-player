//
//  PAPExportingConfiguration.h
//  PreviewAudioPlayer
//

#import <Foundation/Foundation.h>

@interface PAPExportingConfiguration : NSObject

+ (instancetype)exportingConfigurationWithStartTime:(NSTimeInterval)startTime duration:(NSTimeInterval)duration audioTrackVolume:(float)audioTrackVolume backgroundTrackVolume:(float)backgroundTrackVolume;

+ (instancetype)exportingConfigurationWithStartTime:(NSTimeInterval)startTime duration:(NSTimeInterval)duration audioTrackVolume:(float)audioTrackVolume;

@property (nonatomic, assign, readonly) NSTimeInterval startTime;
@property (nonatomic, assign, readonly) NSTimeInterval duration;

@property (nonatomic, assign, readonly) float audioTrackVolume;
@property (nonatomic, assign, readonly) float backgroundTrackVolume;

@end
