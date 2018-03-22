/**
 * @providesModule PreviewAudioPlayer
 */
'use strict'

import { Platform } from 'react-native'

const AudioKit = (Platform.OS === 'ios')
  ? require('./index.ios.js')
  : require('./index.android.js')

export default AudioKit
