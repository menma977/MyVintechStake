package com.myvintech.stake.config

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object Security {

  private val secretKey = "a*WGA8FjZm@i-GiInlKjq+ybYXX+upZD@c%shM1?S8bA"
  private val salt = "aUx3OFYmLVBjSUpGaEhXSg=="
  private val iv = "aWhaeHNjYWohSiZVREBxNw=="

  fun encrypt(strToEncrypt: String) :  String?
  {
      val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

      val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
      val spec =  PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 255*64, 256)
      val tmp = factory.generateSecret(spec)
      val secretKey =  SecretKeySpec(tmp.encoded, "AES")

      val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
      return Base64.encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
  }

  fun decrypt(strToDecrypt : String) : String? {
      val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

      val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
      val spec =  PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 255*64, 256)
      val tmp = factory.generateSecret(spec);
      val secretKey =  SecretKeySpec(tmp.encoded, "AES")

      val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
      return  String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))

  }
}