require 'json'

Pod::Spec.new do |s|
  # NPM package specification
  package = JSON.parse(File.read(File.join(File.dirname(__FILE__), 'package.json')))

  s.name           = 'AudioSessionManager'
  s.version        = package['version']
  s.license        = 'MIT'
  s.summary        = 'Audio session manager for React Native'
  s.author         = { 'Ghinwa dev team' => 'dev@ghinwa.me' }
  s.homepage       = "https://github.com/ghinwallc/react-native-audio-session-manager"
  s.source         = { :git => 'https://github.com/ghinwallc/react-native-audio-session-manager.git', :tag => "v#{s.version}"}
  s.platform       = :ios, '8.0'
  s.preserve_paths = '*.js'
  s.frameworks     = 'AVFoundation'

  s.dependency 'React'

  s.source_files = './*.{h,m}'
end

