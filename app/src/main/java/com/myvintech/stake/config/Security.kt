package com.myvintech.stake.config

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Security {
  private val bytes = ByteArray(32)
  fun encrypt(value: String): String {
    val secretKeySpec = secretKey(value)
    val charArray = value.toCharArray()
    for (i in charArray.indices) {
      bytes[i] = charArray[i].toByte()
    }
    val ivParameterSpec = IvParameterSpec(bytes)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
    val encryptedValue = cipher.doFinal(value.toByteArray())
    return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
  }

  fun decrypt(value: String): String {
    val secretKeySpec = secretKey(value)
    val charArray = value.toCharArray()
    for (i in charArray.indices) {
      bytes[i] = charArray[i].toByte()
    }
    val ivParameterSpec = IvParameterSpec(bytes)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
    val decryptedByteValue = cipher.doFinal(Base64.decode(value, Base64.DEFAULT))
    return String(decryptedByteValue)
  }

  private fun secretKey(value: String): SecretKeySpec {
    return SecretKeySpec(value.toByteArray(), "AES")
  }
}