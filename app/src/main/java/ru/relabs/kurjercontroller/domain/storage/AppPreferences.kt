package ru.relabs.kurjercontroller.domain.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.domain.models.DeviceId
import ru.relabs.kurjercontroller.domain.providers.FirebaseToken

/**
 * Created by Daniil Kurchanov on 05.12.2019.
 */
class AppPreferences(
    private val sharedPreferences: SharedPreferences
) {
    fun saveAuthToken(token: String) =
        sharedPreferences.edit { putString(TOKEN_KEY, token) }

    fun getAuthToken(): String? =
        sharedPreferences.getString(TOKEN_KEY, UNKNOWN_TOKEN)
            ?.takeIf { it != UNKNOWN_TOKEN }

    fun resetAuthToken() =
        sharedPreferences.edit { remove(TOKEN_KEY) }

    fun saveCurrentUserLogin(userLogin: UserLogin) =
        sharedPreferences.edit { putString(KEY_CURRENT_USER_ID, userLogin.login) }

    fun getCurrentUserLogin(): UserLogin? =
        sharedPreferences.getString(KEY_CURRENT_USER_ID, UNKNOWN_CURRENT_USER_ID)
            .takeIf { it != UNKNOWN_CURRENT_USER_ID }
            ?.let { UserLogin(it) }

    fun resetCurrentUserLogin() =
        sharedPreferences.edit { remove(KEY_CURRENT_USER_ID) }

    fun getDeviceUUID(): DeviceId? =
        sharedPreferences.getString(DEVICE_UUID_KEY, UNKNOWN_DEVICE_UUID)
            .takeIf { it != UNKNOWN_DEVICE_UUID }
            ?.let { DeviceId(it) }

    fun saveDeviceUUID(deviceUUID: DeviceId) = sharedPreferences.edit()
        .putString(DEVICE_UUID_KEY, deviceUUID.id)
        .apply()

    fun setUserAutologinEnabled(remember: Boolean) = sharedPreferences.edit()
        .putBoolean(AUTO_LOGIN_ENABLED_KEY, remember)
        .apply()

    fun getUserAutologinEnabled(): Boolean = sharedPreferences
        .getBoolean(AUTO_LOGIN_ENABLED_KEY, false)

    fun getFirebaseToken(): FirebaseToken? = sharedPreferences
        .getString(FIREBASE_TOKEN_KEY, UNKNOWN_FIREBASE_TOKEN)
        .takeIf { it != UNKNOWN_FIREBASE_TOKEN }
        ?.let { FirebaseToken(it) }

    fun saveFirebaseToken(token: FirebaseToken) = sharedPreferences.edit()
        .putString(FIREBASE_TOKEN_KEY, token.token)
        .apply()


    companion object {
        const val AUTO_LOGIN_ENABLED_KEY = "autologin_enabled"
        const val TOKEN_KEY = "token"
        const val UNKNOWN_TOKEN = "__unknown_token"
        const val KEY_CURRENT_USER_ID = "current_user"
        const val UNKNOWN_CURRENT_USER_ID = "__unknown_login"
        const val DEVICE_UUID_KEY = "device_uuid"
        const val UNKNOWN_DEVICE_UUID = "__unknown_device_uuid"
        const val FIREBASE_TOKEN_KEY = "firebase_token"
        const val UNKNOWN_FIREBASE_TOKEN = "__unknown_firebase_token"
    }
}