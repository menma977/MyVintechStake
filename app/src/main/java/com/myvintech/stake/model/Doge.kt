package com.myvintech.stake.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.myvintech.stake.config.Security

@SuppressLint("CommitPrefEdits")
class Doge(context: Context) {
  private val sharedPreferences: SharedPreferences
  private val sharedPreferencesEditor: SharedPreferences.Editor

  companion object {
    private const val userData = "user"
  }

  init {
    sharedPreferences = context.getSharedPreferences(userData, Context.MODE_PRIVATE)
    sharedPreferencesEditor = sharedPreferences.edit()
  }

  fun setString(id: String, value: String) {
    sharedPreferencesEditor.putString(id, Security().encrypt(value))
    sharedPreferencesEditor.commit()
  }

  fun getString(id: String): String {
    return if (sharedPreferences.getString(id, "").toString().isEmpty()) {
      ""
    } else {
      Security().decrypt(sharedPreferences.getString(id, "").toString())
    }
  }

  fun clear() {
    sharedPreferences.edit().clear().apply()
    sharedPreferencesEditor.clear()
  }

  fun tokenDoge(): String {
    return "afd295e94fd841648cada262363d79e7"
  }
}