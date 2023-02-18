import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'apple_sign_in_method_channel.dart';
import 'result.dart';

abstract class AppleSignInPlatform extends PlatformInterface {
  /// Constructs a AppleSignInPlatform.
  AppleSignInPlatform() : super(token: _token);

  static final Object _token = Object();

  static AppleSignInPlatform _instance = MethodChannelAppleSignIn();

  /// The default instance of [AppleSignInPlatform] to use.
  ///
  /// Defaults to [MethodChannelAppleSignIn].
  static AppleSignInPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AppleSignInPlatform] when
  /// they register themselves.
  static set instance(AppleSignInPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<AppleSignInResult> signInWithApple() {
    throw UnimplementedError('signInWithApple() has not been implemented.');
  }
}
