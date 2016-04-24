package com.nlefler.glucloser.a.util

import android.content.Context
import android.util.Base64
import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by nathan on 4/12/16.
 */
class EncryptedPrefsStorageHelper(val crypto: Crypto, val ctx: Context, val prefsName: String, val entityName: String) {
    fun store(key: String, value: String) {
        if (!crypto.isAvailable) {
            return
        }
        val entity = Entity(entityName)
        try {
            val encryptedToken = crypto.encrypt(value.toByteArray(), entity)
            val encryptedBase64Token = Base64.encodeToString(encryptedToken, Base64.DEFAULT)

            val sharedPreferences = ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(key, encryptedBase64Token)
            editor.apply()
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: CryptoInitializationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun fetch(key: String): String? {
        if (!crypto.isAvailable) {
            return null
        }

        var json: String? = null
        try {
            val sharedPref = ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

            val encryptedBase64Token = sharedPref.getString(key, null)
            val entity = Entity(entityName)
            val encryptedToken = Base64.decode(encryptedBase64Token, Base64.DEFAULT)

            if (encryptedToken.size > 0) {
                json = crypto.decrypt(encryptedToken, entity).toString(Charset.forName("UTF-8"))
            }
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: CryptoInitializationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        finally {
            return json
        }
    }
}
