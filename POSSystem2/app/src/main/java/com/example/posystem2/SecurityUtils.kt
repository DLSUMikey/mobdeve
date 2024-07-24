package com.example.posystem2

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.MessageDigest
import java.util.Base64

object SecurityUtils {

    private const val KEY_ALIAS = "my_key_alias"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    init {
        generateSecretKey()
    }

    private fun generateSecretKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val encryptionIv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptionIv + encryptedData)
    }

    fun decrypt(data: String): String {
        val dataBytes = Base64.getDecoder().decode(data)
        val encryptionIv = dataBytes.sliceArray(0 until 12)
        val encryptedData = dataBytes.sliceArray(12 until dataBytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, encryptionIv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData, Charsets.UTF_8)
    }

    fun hash(data: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
