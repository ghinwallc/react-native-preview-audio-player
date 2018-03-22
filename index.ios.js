/**
 * @providesModule PreviewAudioPlayer
 * @flow
 */
'use strict';

import { NativeModules, DeviceEventEmitter } from 'react-native';
import { SHOULD_MIX_AUDIO_LOCALLY } from '../../app/constants/Settings'
const NativePreviewController = NativeModules.PAPPreviewController;

/**
 * Component allowing to perform and export preview of specified audio file.
 */

 var PreviewAudioPlayer = {

   /**
    * Prepares audio player for playback using provided paths to the vocal audio file and background audio file.
    * @param  {originalVocalAudioFilePath: string}   originalVocalAudioFilePath:       Path to audio file that contains the original vocal recording.
    * @param  {processedVocalAudioFilePath: string}  processedVocalAudioFilePath:      Path to audio file that contains the processed vocal recording.
    * @param  {backgroundAudioFilePath: string}      backgroundAudioFilePath:          Path to audio file that contains background track.
    * @param  {recordingStartTime: string}           recordingStartTime:               The position in the background track time when recording started
    * @param  {(error: any) => void}                 callback:                         Callback called when method finishes. If an error occurs,
    *                                                                                  an error is passed as an argument of the callback, null otherwise.
    */
   prepare: function(originalVocalAudioFilePath: string, processedVocalAudioFilePath: string, backgroundAudioFilePath: string, recordingStartTime: number, callback: (error: any) => void) {
     NativePreviewController.prepare(originalVocalAudioFilePath, processedVocalAudioFilePath, backgroundAudioFilePath, recordingStartTime, SHOULD_MIX_AUDIO_LOCALLY, (error) => {
       if (callback) {
         callback(error)
       }
     })
   },

   /**
    * Starts preview at specified time using file paths provided in `prepare` method call.
    */
   playAt: function(startTime: number, callback: (error: any) => void) {
     NativePreviewController.playAt(startTime, (error) => {
       if (callback) {
         callback(error)
       }
     })
   },

   /**
    * Starts preview using file paths provided in `prepare` method call.
    */
   play: function(callback: (error: any) => void) {
     NativePreviewController.play((error) => {
       if (callback) {
         callback(error)
       }
     })
   },

   /**
    * Pauses preview.
    */
   pause: function(callback: () => void) {
     NativePreviewController.pause(() => {
       if (callback) {
         callback()
       }
     })
   },

   /**
    * Unpauses preview.
    * @param  {(error: any) => void}  error: Callback called when method finishes. If an error occurs,
    *                                        an error is passed as an argument of the callback, null otherwise.
    */
   unpause: function(callback: (error: any) => void) {
     NativePreviewController.unpause((error) => {
       if (callback) {
         callback(error)
       }
     })
   },

   /**
    * Stops preview.
    */
   stop: function(callback: () => void) {
     NativePreviewController.stop(() => {
       if (callback) {
         callback()
       }
     })
   },

   /**
    * Exports audio to file.
    * @param  {(error: any, outputFilePath: string) => void} callback: Callback called when method finished. If an error occurs,
    *                                                                  an error is passed as an argument of the callback, null otherwise. If there is
    *                                                                  no error second argument of callback contains path at which output file is stored.
    */
   export: function(callback: (error: any, outputFilePath: string) => void) {
     NativePreviewController.export(SHOULD_MIX_AUDIO_LOCALLY, (error, outputFilePath) => {
       if (callback) {
         callback(error, outputFilePath)
       }
     })
   },

   /**
    * Exports audio to a file at specified path.
    * @param  {outputFilePath: string} outputFilePath:                  Path at which result file should be stored.
    * @param  {(error: any, outputFilePath: string) => void} callback:  Callback called when method finished. If an error occurs,
    *                                                                   an error is passed as an argument of the callback, null otherwise. If there is
    *                                                                   no error second argument of callback contains path at which output file is stored.
    */
   exportToFileAtURL: function(outputFilePath: string, callback: (error: any, outputFilePath: string) => void) {
     NativePreviewController.exportToFileAtURL(SHOULD_MIX_AUDIO_LOCALLY, (error, outputFilePath) => {
       if (callback) {
         callback(error, outputFilePath)
       }
     })
   },

   /**
    * Exports preview to a file. Preview parameters can be changed using setPreviewStartTime and setPreviewDuration methods.
    * @param  {(error: any, outputFilePath: string) => void}  callback: Callback called when method finished. If an error occurs,
    *                                                                   an error is passed as an argument of the callback, null otherwise. If there is
    *                                                                   no error second argument of callback contains path at which output file is stored.
    */
   exportPreview: function(callback: (error: any, outputFilePath: string) => void) {
     NativePreviewController.exportPreview(SHOULD_MIX_AUDIO_LOCALLY, (error, outputFilePath) => {
       if (callback) {
         callback(error, outputFilePath)
       }
     })
   },

   /**
    * Exports preview to a file at specified path. Preview parameters can be changed using setPreviewStartTime and setPreviewDuration methods.
    * @param  {outputFilePath: string}   outputFilePath:                  Path at which result file should be stored.
    * @param  {(error: any, outputFilePath: string) = > void}  callback:  Callback called when method finishes. If an error occurs,
    *                                                                     an error is passed as an argument of the callback, null otherwise. If there is
    *                                                                     no error second argument of callback contains path at which output file is stored.
    */
   exportPreviewToFileAtPath: function(outputFilePath: string, callback: (error: any, outputFilePath: string) => void) {
     NativePreviewController.exportPreviewToFileAtURL(outputFilePath, (error, outputFilePath) => {
       if (callback) {
         callback(error, outputFilePath)
       }
     })
   },

   /**
    * Plays preview starting from specified time in vocal audio file and background audio file.
    * @param  {previewStartTime: number} previewStartTime:  Time in vocal audio file and background audio file from which
    *                                                       preview should start.
    * @param  {(error: any) => void} callback:              Callback called when method finishes. If an error occurs,
    *                                                       an error is passed as an argument of the callback, null otherwise.
    */
   playPreview: function(callback: (error: any) => void) {
     NativePreviewController.playPreview((error) => {
       if (callback) {
         callback(error)
       }
     })
   },

   /**
    * Sets preview start time.
    * @param  {previewStartTime: number} previewStartTime:  Time at which preview should start.
    * @param  {() => void} callback:                        Callback called when method finishes.
    */
   setPreviewStartTime: function(previewStartTime: number, callback: () => void) {
     NativePreviewController.setPreviewStartTime(previewStartTime, () => {
       if (callback) {
         callback()
       }
     })
   },

   /**
    * Sets preview start time and duration.
    * @param  {previewTime: number} previewTime:            Time at which preview should start.
    * @param  {duration: number} duration:                  Preview duration
    * @param  {() => void} callback:                        Callback called when method finishes.
    */
   setPreviewAt: function(previewTime: number, previewDuration: number, callback: () => void) {
     NativePreviewController.setPreviewAt(previewTime, previewDuration, () => {
       if (callback) {
         callback()
       }
     })
   },

   /**
    * Gets preview start time.
    * @param  {(previewStartTime: number) => void}  callback: Callback called when method finishes. First argument of
    *                                                         this callback is a preview start time.
    */
   getPreviewStartTime: function(callback: (previewStartTime: number) => void) {
     NativePreviewController.getPreviewStartTime((previewStartTime) => {
       if (callback) {
         callback(previewStartTime)
       }
     })
   },

   /**
    * Gets preview duration.
    * @param  {(previewDuration: number) => void}  Callback called when method finishes. Current preview duration
    *                                              is passed as first argument of this callback.
    */
   getPreviewDuration: function(callback: (previewDuration: number) => void) {
     NativePreviewController.getPreviewDuration((previewDuration) => {
       if (callback) {
         callback(previewDuration)
       }
     })
   },

   /**
    * Gets audio duration.
    * @param  {(duration: number) => void}  Callback called when method finishes. Current duration
    *                                       is passed as first argument of this callback.
    */
   getPlaybackDuration: function(callback: (duration: number) => void) {
     NativePreviewController.getPlaybackDuration((duration) => {
       if (callback) {
         callback(duration)
       }
     })
   },

   /**
    * Changes the current playback time to the new value
    * @param  {time: number} time:                 The new value for the current playback time
    * @param  {() => void} callback:               Callback called when method finishes.
    */
   seekToTime: function(time: number, callback: () => void) {
     NativePreviewController.seekToTime(time, () => {
       if (callback) {
         callback()
       }
     })
   },

   /**
    * Changes the current playback time by the specified time offset.
    * @param  {timeOffset: number} timeOffset:     The offset by which the current playback time will be changed
    * @param  {() => void} callback:               Callback called when method finishes.
    */
   seekByTimeOffset: function(timeOffset: number, callback: () => void) {
     NativePreviewController.seekByTimeOffset(timeOffset, () => {
       if (callback) {
         callback()
       }
     })
   },

/**
 *  Gets current playback time.
 */
   getCurrentTime: function(callback: (number) => void) {
    NativePreviewController.getCurrentTime(callback)
   },

   /**
    * Sets volumes of vocal and background audio tracks.
    * @param  {vocalAudioTrackVolume: number}   vocalAudioTrackVolume:            Vocal audio track volume to set. Accepts value from 0 to 1.
    * @param  {backgroundAudioTrackVolume: number}   backgroundAudioTrackVolume:  Background audio track volume to set. Accepts value from 0 to 1.
    * @param  {() => void}  callback:                                             Callback called when method finishes.
    */
   setVolume: function(vocalAudioTrackVolume: number, backgroundAudioTrackVolume: number, callback: () => void) {
     NativePreviewController.setVolume(vocalAudioTrackVolume, backgroundAudioTrackVolume, () => {
       if (callback) {
         callback()
       }
     })
   },

   setAudioPlaybackProgressUpdateSubscription: function(callback: (currentTime: number) => void) {
     if (this.audioPlaybackProgressUpdateSubscription) {
       this.audioPlaybackProgressUpdateSubscription.remove();
     }
     if (!callback) {
       return
     }

     this.audioPlaybackProgressUpdateSubscription = DeviceEventEmitter.addListener('previewAudioPlayerPlaybackProgressUpdate',
     (data) => {
       if (callback) {
         callback(data.currentTime);
       }
     })
   },

   setAudioPlaybackFinishedSubscription: function(callback: (finished: bool) => void) {
     if (this.audioPlaybackFinishedSubscription) {
       this.audioPlaybackFinishedSubscription.remove();
     }
     if (!callback) {
       return
     }

     this.audioPlaybackFinishedSubscription = DeviceEventEmitter.addListener('previewAudioPlayerPlaybackFinished',
     (data) => {
       if (callback) {
         callback(data.finished);
       }
     })
   },

   unsubscribeFromListening: function() {
     if (this.audioPlaybackProgressUpdateSubscription) {
       this.audioPlaybackProgressUpdateSubscription.remove();
     }
     if (this.audioPlaybackFinishedSubscription) {
       this.audioPlaybackFinishedSubscription.remove();
     }
   },

   switchToOriginalVocalTrack: function (callback: () => void) {
     NativePreviewController.switchToOriginalVocalTrack(callback || (() => {}))
   },

   switchToProcessedVocalTrack: function (callback: () => void) {
     NativePreviewController.switchToProcessedVocalTrack(callback || (() => {}))
   },

   getSelectedVocalTrackPath: function (callback: (vocalTrackPath: string) => void) {
     NativePreviewController.getSelectedVocalTrackPath(callback || (() => {}))
   }
 }

 module.exports = PreviewAudioPlayer;
