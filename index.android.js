/* eslint valid-jsdoc: "error" */
/* eslint max-len: ["error", 100, { "ignoreComments": true }] */

/**
 * @providesModule PreviewAudioPlayer
 * @flow
 */

import { NativeModules, DeviceEventEmitter } from 'react-native'

const NativePreviewController = NativeModules.PreviewAudioPlayerManager

/**
 * Component allowing to perform and export preview of specified audio file.
 */
class PreviewAudioPlayer {
  /**
   * callbackWithOptionalError
   * @callback callbackWithOptionalError
   * @param {*} error
   */

   /**
    * emptyCallback
    * @callback emptyCallback
    */

  /**
   * Prepares audio player for playback using provided paths to the vocal audio file and background audio file.s
   * @param {string} originalVocalAudioFilePath Path to audio file that contains the original vocal recording.
   * @param {string} processedVocalAudioFilePath Path to audio file that contains the processed vocal recording.
   * @param {string} backgroundAudioFilePath Path to audio file that contains background track.
   * @param {string} recordingStartTime The position in the background track time when recording started
   * @param {callbackWithOptionalError} callback Callback called when method finishes. If an error occurs,
   *                                   an error is passed as an argument of the callback, null otherwise.
   * @returns {undefined}
   */
  prepare(originalVocalAudioFilePath: string, processedVocalAudioFilePath: string, backgroundAudioFilePath: string, recordingStartTime: number, callback: (error: any) => void) {
    NativePreviewController.prepare(
      originalVocalAudioFilePath, processedVocalAudioFilePath, backgroundAudioFilePath,
      recordingStartTime, (error) => {
        if (!callback) { return }
        callback(error)
      })
  }


  /**
   * Starts preview at specified time using file paths provided in `prepare` method call.
   * @param {number} startTime time when preview playing should start
   * @param {callbackWithOptionalError} callback callback called when method finishes
   * @returns {undefined}
   */
  playAt(startTime: number, callback: (error: any) => void) {
    NativePreviewController.playAt(startTime, (error) => {
      if (callback) {
        callback(error)
      }
    })
  }

  /**
   * Starts preview using file paths provided in `prepare` method call.
   * @param {callbackWithOptionalError} callback callback called when method finishes
   * @returns {undefined}
   */
  play(callback: (error: any) => void) {
    NativePreviewController.play((error) => {
      if (callback) {
        callback(error)
      }
    })
  }

  /**
   * Pauses preview.
   * @param {emptyCallback} callback callback called when method finishes
   * @returns {undefined}
   */
  pause(callback: () => void) {
    NativePreviewController.pause(() => {
      if (callback) {
        callback()
      }
    })
  }

  /**
   * Unpauses preview.
   * @param {callbackWithOptionalError} callback Callback called when method finishes. If an error occurs,
   *                                             an error is passed as an argument of the callback, null otherwise.
   * @returns {undefined}
   */
  unpause(callback: (error: any) => void) {
    NativePreviewController.unPause((error) => {
      if (callback) {
        callback(error)
      }
    })
  }

  /**
   * Stops preview.
   * @param {emptyCallback} callback callback called when method finishes
   * @returns {undefined}
   */
  stop(callback: () => void) {
    NativePreviewController.stop(() => {
      if (callback) {
        callback()
      }
    })
  }

  /**
   * Exports audio to file.
   * @param  {callbackWithOptionalError} callback Callback called when method finished. If an error occurs,
   *                                              an error is passed as an argument of the callback, null otherwise.
   *                                              If there is no error second argument of callback contains path at which output file is stored.
   * @returns {undefined}
   */
  export(callback: (error: any, outputFilePath: string) => void) {
    NativePreviewController.export((error, outputFilePath) => {
      if (callback) {
        callback(error, outputFilePath)
      }
    })
  }


  /**
   * Plays preview starting from specified time in vocal audio file and background audio file.
   * @param  {previewStartTime: number} previewStartTime:  Time in vocal audio file and background audio file from which
   *                                                       preview should start.
   * @param  {(error: any) => void} callback:              Callback called when method finishes. If an error occurs,
   *                                                       an error is passed as an argument of the callback, null otherwise.
   */
  playPreview(callback: (error: any) => void) {
    NativePreviewController.playPreview((error) => {
      if (callback) {
        callback(error)
      }
    })
  }

  /**
   * Sets preview start time.
   * @param  {previewStartTime: number} previewStartTime:  Time at which preview should start.
   * @param  {() => void} callback:                        Callback called when method finishes.
   */
  setPreviewStartTime(previewStartTime: number, callback: () => void) {
    NativePreviewController.setPreviewStartTime(previewStartTime, () => {
      if (callback) {
        callback()
      }
    })
  }

  /**
   * Sets preview start time and duration.
   * @param  {previewTime: number} previewTime:            Time at which preview should start.
   * @param  {duration: number} duration:                  Preview duration
   * @param  {() => void} callback:                        Callback called when method finishes.
   */
  setPreviewAt(previewTime: number, previewDuration: number, callback: () => void) {
    NativePreviewController.setPreviewAt(previewTime, previewDuration, () => {
      if (callback) {
        callback()
      }
    })
  }

  /**
   * Gets preview start time.
   * @param  {(previewStartTime: number) => void}  callback: Callback called when method finishes. First argument of
   *                                                         this callback is a preview start time.
   */
  getPreviewStartTime(callback: (previewStartTime: number) => void) {
    NativePreviewController.getPreviewStartTime((previewStartTime) => {
      if (callback) {
        callback(previewStartTime)
      }
    })
  }

  /**
   * Gets preview duration.
   * @param  {(previewDuration: number) => void}  Callback called when method finishes. Current preview duration
   *                                              is passed as first argument of this callback.
   */
  getPreviewDuration(callback: (previewDuration: number) => void) {
    NativePreviewController.getPreviewDuration((previewDuration) => {
      if (callback) {
        callback(previewDuration)
      }
    })
  }

  /**
   * Gets audio duration.
   * @param  {(duration: number) => void}  Callback called when method finishes. Current duration
   *                                       is passed as first argument of this callback.
   */
  getPlaybackDuration(callback: (duration: number) => void) {
    NativePreviewController.getPlaybackDuration((duration) => {
      if (callback) {
        callback(duration)
      }
    })
  }

  /**
   * Changes the current playback time to the new value
   * @param  {time: number} time:                 The new value for the current playback time
   * @param  {() => void} callback:               Callback called when method finishes.
   */
  seekToTime(time: number, callback: () => void) {
    NativePreviewController.seekToTime(time, () => {
      if (callback) {
        callback()
      }
    })
  }


  /**
   *  Gets current playback time.
   */
   getCurrentTime(callback: (number) => void) {
     NativePreviewController.getCurrentTime(callback)
   }

   /**
    * Sets volumes of vocal and background audio tracks.
    * @param  {vocalAudioTrackVolume: number}   vocalAudioTrackVolume:            Vocal audio track volume to set. Accepts value from 0 to 1.
    * @param  {backgroundAudioTrackVolume: number}   backgroundAudioTrackVolume:  Background audio track volume to set. Accepts value from 0 to 1.
    * @param  {() => void}  callback:                                             Callback called when method finishes.
    */
   setVolume(vocalAudioTrackVolume: number, backgroundAudioTrackVolume: number, callback: () => void) {
     NativePreviewController.setVolume(vocalAudioTrackVolume, backgroundAudioTrackVolume, () => {
       if (callback) {
         callback()
       }
     })
   }

   setAudioPlaybackProgressUpdateSubscription(callback: (currentTime: number) => void) {
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
   }

   setAudioPlaybackFinishedSubscription(callback: (finished: bool) => void) {
     if (this.audioPlaybackFinishedSubscription) {
       this.audioPlaybackFinishedSubscription.remove();
     }
     if (!callback) {
       return
     }

     this.audioPlaybackFinishedSubscription = DeviceEventEmitter.addListener('previewAudioPlayerPlaybackFinished',
     (data) => {
       if (callback) {
         callback(true);
       }
     })
   }

   unsubscribeFromListening() {
     if (this.audioPlaybackProgressUpdateSubscription) {
       this.audioPlaybackProgressUpdateSubscription.remove();
     }
     if (this.audioPlaybackFinishedSubscription) {
       this.audioPlaybackFinishedSubscription.remove();
     }
   }

   switchToOriginalVocalTrack(callback: () => void) {
     NativePreviewController.switchToOriginalVocalTrack(callback || (() => {}))
   }

   switchToProcessedVocalTrack(callback: () => void) {
     NativePreviewController.switchToProcessedVocalTrack(callback || (() => {}))
   }
}

module.exports = new PreviewAudioPlayer()
