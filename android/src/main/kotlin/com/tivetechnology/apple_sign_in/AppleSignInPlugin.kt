package com.tivetechnology.apple_sign_in

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.gson.Gson
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import org.json.JSONObject

/** AppleSignInPlugin */
class AppleSignInPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private val tag = "apple_sign_in"
    private var mainActivity: Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel =
            MethodChannel(flutterPluginBinding.binaryMessenger, "com.tivetechnology.apple_sign_in")
        channel.setMethodCallHandler(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        mainActivity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {}

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

    override fun onDetachedFromActivity() {}

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "signInWithApple") {
            Log.d(tag, "activitySignIn")
            val auth = FirebaseAuth.getInstance()
            val provider = OAuthProvider.newBuilder("apple.com")
            provider.scopes = arrayOf("email", "name").toMutableList()
            val pending = auth.pendingAuthResult

            if (pending != null) {
                pending.addOnSuccessListener { authResult ->
                    Log.d(tag, "checkPending:onSuccess:$authResult")
                    result.success(parseAuthResult(authResult))
                }.addOnFailureListener { e ->
                    Log.w(tag, "checkPending:onFailure", e)
                    result.error("checkPending:onFailure", e.toString(), e.toString())
                }
            } else {
                Log.d(tag, "pending: null")
                auth.startActivityForSignInWithProvider(mainActivity!!, provider.build())
                    .addOnSuccessListener { authResult ->
                        // Sign-in successful!
                        Log.d(tag, "activitySignIn:onSuccess:${authResult.user}")
                        result.success(parseAuthResult(authResult))
                    }
                    .addOnFailureListener { e ->
                        Log.w(tag, "activitySignIn:onFailure", e)
                        result.error("activitySignIn:onFailure", e.toString(), e.toString())
                    }
            }
        } else {
            Log.d(tag, "activitySignIn")
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun parseAuthResult(authResult: AuthResult): Map<String, Any> {
        val output: MutableMap<String, Any> = HashMap()
        output[Constants.ADDITIONAL_USER_INFO] = parseAdditionalUserInfo(authResult.additionalUserInfo)!!
        output[Constants.AUTH_CREDENTIAL] = parseAuthCredential(authResult.credential)!!
        output[Constants.USER] = parseFirebaseUser(authResult.user)!!
        return output
    }

    private fun parseAdditionalUserInfo(additionalUserInfo: AdditionalUserInfo?): Map<String, Any?>? {
        if (additionalUserInfo == null) {
            return null
        }
        val output: MutableMap<String, Any?> = HashMap()
        output[Constants.IS_NEW_USER] = additionalUserInfo.isNewUser
        output[Constants.PROFILE] = additionalUserInfo.profile
        output[Constants.PROVIDER_ID] = additionalUserInfo.providerId
        output[Constants.USERNAME] = additionalUserInfo.username
        return output
    }

    private fun parseAuthCredential(authCredential: AuthCredential?): Map<String, Any?>? {
        if (authCredential == null) {
            return null
        }
        val authCredentialHashCode = authCredential.hashCode()
        val output: MutableMap<String, Any?> = HashMap()
        output[Constants.PROVIDER_ID] = authCredential.provider
        output[Constants.SIGN_IN_METHOD] = authCredential.signInMethod
        output[Constants.TOKEN] = authCredentialHashCode
        if (authCredential is OAuthCredential) {
            output[Constants.ACCESS_TOKEN] = authCredential.accessToken
        }
        return output
    }

    private fun parseFirebaseUser(firebaseUser: FirebaseUser?): Map<String, Any?>? {
        if (firebaseUser == null) {
            return null
        }
        val output: MutableMap<String, Any?> = HashMap()
        val metadata: MutableMap<String, Any> = HashMap()
        output[Constants.DISPLAY_NAME] = firebaseUser.displayName
        output[Constants.EMAIL] = firebaseUser.email
        output[Constants.EMAIL_VERIFIED] = firebaseUser.isEmailVerified
        output[Constants.IS_ANONYMOUS] = firebaseUser.isAnonymous

        val userMetadata = firebaseUser.metadata
        if (userMetadata != null) {
            metadata[Constants.CREATION_TIME] = firebaseUser.metadata!!.creationTimestamp
            metadata[Constants.LAST_SIGN_IN_TIME] = firebaseUser.metadata!!.lastSignInTimestamp
        }
        output[Constants.METADATA] = metadata
        output[Constants.PHONE_NUMBER] = firebaseUser.phoneNumber
        output[Constants.PHOTO_URL] = parsePhotoUrl(firebaseUser.photoUrl)
        output[Constants.PROVIDER_DATA] = parseUserInfoList(firebaseUser.providerData)
        output[Constants.REFRESH_TOKEN] = "" // native does not provide refresh tokens
        output[Constants.UID] = firebaseUser.uid
        output[Constants.TENANT_ID] = firebaseUser.tenantId
        return output
    }

    private fun parsePhotoUrl(photoUri: Uri?): String? {
        if (photoUri == null) {
            return null
        }
        val photoUrl: String = photoUri.toString()

        // Return null if the URL is an empty string
        if (photoUrl == "") return null

        return photoUrl
    }

    private fun parseUserInfoList(
        userInfoList: List<UserInfo>?
    ): List<Map<String?, Any?>?> {
        val output: MutableList<Map<String?, Any?>?> = ArrayList()
        if (userInfoList == null) {
            return output
        }
        for (userInfo in ArrayList<UserInfo>(userInfoList)) {
            if (FirebaseAuthProvider.PROVIDER_ID != userInfo.providerId) {
                output.add(parseUserInfo(userInfo))
            }
        }
        return output
    }

    private fun parseUserInfo(userInfo: UserInfo): Map<String?, Any?> {
        val output: MutableMap<String?, Any?> = HashMap()
        output[Constants.DISPLAY_NAME] = userInfo.displayName
        output[Constants.EMAIL] = userInfo.email
        output[Constants.PHONE_NUMBER] = userInfo.phoneNumber
        output[Constants.PHOTO_URL] = parsePhotoUrl(userInfo.photoUrl)
        output[Constants.PROVIDER_ID] = userInfo.providerId
        output[Constants.UID] = userInfo.uid
        return output
    }
}
