import 'package:apple_sign_in/src/result.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'apple_sign_in_platform_interface.dart';

/// An implementation of [AppleSignInPlatform] that uses method channels.
class MethodChannelAppleSignIn extends AppleSignInPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('com.tivetechnology.apple_sign_in');

  @override
  Future<AppleSignInResult> signInWithApple() async {
    final result = await methodChannel.invokeMethod('signInWithApple');
    return AppleSignInResult(
      isNewUser: result['additionalUserInfo']['isNewUser'],
    );
  }
}
