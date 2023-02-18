class AppleSignInResult {
  final bool isNewUser;

  AppleSignInResult({required this.isNewUser});

  Map<String, Object?> toJson() {
    return {
      'isNewUser': isNewUser,
    };
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is AppleSignInResult &&
          runtimeType == other.runtimeType &&
          isNewUser == other.isNewUser;

  @override
  int get hashCode => isNewUser.hashCode;

  @override
  String toString() {
    return 'AppleSignInResult(isNewUser: $isNewUser)';
  }
}
