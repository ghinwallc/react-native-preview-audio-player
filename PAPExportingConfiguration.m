//
//  PAPExportingConfiguration.m
//  PreviewAudioPlayer
//

#import "PAPExportingConfiguration.h"

@interface PAPExportingConfiguration ()

@property (nonatomic, assign) NSTimeInterval startTime;
@property (nonatomic, assign) NSTimeInterval duration;

@property (nonatomic, assign) float audioTrackVolume;
@property (nonatomic, assign) float backgroundTrackVolume;

@end

@implementation PAPExportingConfiguration

+ (instancetype)exportingConfigurationWithStartTime:(NSTimeInterval)startTime duration:(NSTimeInterval)duration audioTrackVolume:(float)audioTrackVolume backgroundTrackVolume:(float)backgroundTrackVolume {
    PAPExportingConfiguration *configuration = [PAPExportingConfiguration new];
    configuration.startTime = startTime;
    configuration.duration = duration;
    configuration.audioTrackVolume = audioTrackVolume;
    configuration.backgroundTrackVolume = backgroundTrackVolume;
    return configuration;
}

+ (instancetype)exportingConfigurationWithStartTime:(NSTimeInterval)startTime duration:(NSTimeInterval)duration audioTrackVolume:(float)audioTrackVolume {
    PAPExportingConfiguration *configuration = [PAPExportingConfiguration new];
    configuration.startTime = startTime;
    configuration.duration = duration;
    configuration.audioTrackVolume = audioTrackVolume;
    return configuration;
}

@end
