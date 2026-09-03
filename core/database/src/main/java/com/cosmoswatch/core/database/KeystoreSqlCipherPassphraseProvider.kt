package com.cosmoswatch.core.database

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeystoreSqlCipherPassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : SqlCipherPassphraseProvider {

    override fun getPassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encodedCiphertext = prefs.getString(KEY_PASSPHRASE, null)
        val encodedIv = prefs.getString(KEY_IV, null)

        return if (encodedCiphertext != null && encodedIv != null) {
            decrypt(Base64.decode(encodedCiphertext, Base64.NO_WRAP), Base64.decode(encodedIv, Base64.NO_WRAP))
        } else {
            generateAndStorePassphrase(prefs)
        }
    }

    private fun generateAndStorePassphrase(prefs: SharedPreferences): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES).also(SecureRandom()::nextBytes)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        }
        val ciphertext = cipher.doFinal(passphrase)

        prefs.edit {
            putString(KEY_PASSPHRASE, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
        }

        return passphrase
    }

    private fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        }
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEYSTORE_KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val PREFS_NAME = "cosmoswatch_database_secrets"
        const val KEY_PASSPHRASE = "encrypted_passphrase"
        const val KEY_IV = "passphrase_iv"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEYSTORE_KEY_ALIAS = "cosmoswatch_db_passphrase_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val PASSPHRASE_LENGTH_BYTES = 32
    }
}
