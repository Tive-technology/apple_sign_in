import 'apple_sign_in_platform_interface.dart';
import 'result.dart';

class AppleSignIn {
  Future<AppleSignInResult> signInWithApple() {
    return AppleSignInPlatform.instance.signInWithApple();
  }
}
