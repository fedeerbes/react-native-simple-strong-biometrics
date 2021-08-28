package com.reactnativesimplestrongbiometrics

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import java.security.InvalidKeyException
import java.security.UnrecoverableKeyException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.crypto.IllegalBlockSizeException


class SimpleStrongBiometricsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private lateinit var cryptographyManager: CryptographyManager
  private lateinit var initializationVector: ByteArray
  private val delimiter = "]"
  private val Tag = "SimpleStrongBiometrics"

  override fun getName() = "SimpleStrongBiometrics"

  init {
    cryptographyManager = CryptographyManager()
    Log.d(Tag, "Init");
  }

  private fun prefs(name: String): SharedPreferences? {
    return reactApplicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
  }

  private fun sharedPreferences(options: ReadableMap): String {
    var name = if (options.hasKey("sharedPreferencesName")) options.getString("sharedPreferencesName") else "shared_preferences"
    if (name == null) {
      name = "shared_preferences"
    }
    return name
  }

  private fun hasSetupBiometricCredential(): Boolean {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val reactApplicationContext = reactApplicationContext
        val biometricManager: BiometricManager = BiometricManager.from(reactApplicationContext)
        val canAuthenticate: Int = biometricManager.canAuthenticate()
        canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
      } else {
        false
      }
    } catch (e: Exception) {
      false
    }
  }

  private fun showDialog(strings: HashMap<String, Any>, cryptoObject: BiometricPrompt.CryptoObject, callback: BiometricPrompt.AuthenticationCallback) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      UiThreadUtil.runOnUiThread(Runnable {
        kotlin.run {
          try {
            val activity = currentActivity
            if(activity == null) {
              callback.onAuthenticationError(BiometricConstants.ERROR_CANCELED, if(strings.containsKey("cancelled")) strings["cancelled"].toString() else "Authentication was cancelled")
              return@run
            }
            val fragmentActivity = currentActivity as FragmentActivity?
            val executor: Executor = Executors.newSingleThreadExecutor()
            val biometricPrompt = BiometricPrompt(fragmentActivity!!, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
              .setTitle(if(strings.containsKey("header")) strings["header"].toString() else "Unlock with your fingerprint")
              .setDescription(if(strings.containsKey("description")) strings["description"].toString() else "Confirm biometric to continue")
              .setConfirmationRequired(false)
              .setNegativeButtonText(if(strings.containsKey("cancel")) strings["cancel"].toString() else "Cancel") // e.g. "Use Account Password"
              .build()
            biometricPrompt.authenticate(promptInfo, cryptoObject)
          } catch (e: Exception) {}
        }
      })
    }
  }

  private fun decrypt(cryptoObject: BiometricPrompt.CryptoObject?, value: String, promise: Promise){
    try {
      val inputs = value?.split(delimiter)
      val cipherText = Base64.decode(inputs!![1], Base64.DEFAULT)
      val decryptedValue = cryptographyManager.decryptData(cipherText, cryptoObject?.cipher!!)
      promise.resolve(decryptedValue)
    } catch (e: Exception) {
    }
  }

  @ReactMethod
  fun hasStrongBiometricEnabled(promise: Promise) = promise.resolve(hasSetupBiometricCredential())

  @ReactMethod
  fun getItem(key: String, options: ReadableMap, promise: Promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasSetupBiometricCredential()) {

      val strings = if (options.hasKey("dialogStrings")) {
        options.getMap("dialogStrings")?.toHashMap() ?: HashMap()
      } else {
        HashMap()
      }
      val sharedPreferencesKey = sharedPreferences(options)
      val value = prefs(sharedPreferencesKey)?.getString(key, null)
      val inputs = value?.split(delimiter)
      val inputsSize = inputs?.size ?: 0
      if (inputsSize < 2) {
        promise.reject("DecryptionFailed", "NO_MATCHING_KEY");
      }

      try {
        val iv = Base64.decode(inputs!![0], Base64.DEFAULT)
        val cipher = cryptographyManager.getInitializedCipherForDecryption(iv)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.d(Tag, "$errorCode :: $errString")
            promise.reject(errorCode.toString(), errString.toString());
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Log.d(Tag, "Authentication failed for an unknown reason")
            reactApplicationContext.getJSModule(RCTDeviceEventEmitter::class.java)
              .emit("E_AUTHENTICATION_NOT_RECOGNIZED", "FINGERPRINT_NOT_RECOGNIZED")
          }

          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Log.d(Tag, "Authentication was successful")
            decrypt(result.cryptoObject, value, promise)
          }
        }

        showDialog(strings, BiometricPrompt.CryptoObject(cipher), callback)
      } catch (e: InvalidKeyException) {
        promise.reject(e)
      } catch (e: UnrecoverableKeyException) {
        promise.reject(e)
      } catch (e: IllegalBlockSizeException) {
        promise.reject(e)
      } catch (e: SecurityException) {
        promise.reject(e);
      } catch (e: Exception) {
        promise.reject(e);
      }
    } else {
      promise.reject("E_BIOMETRIC_NOT_SUPPORTED", "BIOMETRIC_NOT_SUPPORTED")
    }
  }

  @ReactMethod
  fun setItem(key: String, value: String, options: ReadableMap, promise: Promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasSetupBiometricCredential()) {

      if(key.isEmpty() || value.isEmpty()) {
        promise.reject("MISSING_PARAMETERS", "MISSING_KEY_VALUE")
      }

      try {
        val sharedPreferencesName = sharedPreferences(options)
        val cipher = cryptographyManager.getInitializedCipherForEncryption()
        val encryptedData = cryptographyManager.encryptData(value, cipher)
        val cipherText = encryptedData.ciphertext
        initializationVector = encryptedData.initializationVector
        val base64IV = Base64.encodeToString(initializationVector, Base64.DEFAULT)
        val base64Cipher = Base64.encodeToString(cipherText, Base64.DEFAULT)
        val string = "$base64IV]$base64Cipher"
        val editor = prefs(sharedPreferencesName)?.edit()
        editor?.putString(key, string)?.apply()
        promise.resolve(null)
      } catch (e: InvalidKeyException) {
        promise.reject(e)
      } catch (e: UnrecoverableKeyException) {
        promise.reject(e)
      } catch (e: IllegalBlockSizeException) {
        promise.reject(e)
      } catch (e: SecurityException) {
        promise.reject(e);
      } catch (e: Exception) {
        promise.reject(e);
      }
    } else {
      promise.reject("E_BIOMETRIC_NOT_SUPPORTED", "BIOMETRIC_NOT_SUPPORTED")
    }
  }

  @ReactMethod
  fun deleteItem(key: String, options: ReadableMap, promise: Promise) {
    try {
      val name = sharedPreferences(options)
      val editor = prefs(name)!!.edit()
      editor.remove(key).apply()
      promise.resolve(null)
    } catch (e: Exception) {
      promise.reject(e);
    }
  }
}
