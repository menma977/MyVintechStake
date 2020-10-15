package com.myvintech.stake

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.myvintech.stake.config.Security

class LoginActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    val password = "Password"
    val en = Security().encrypt(password)
    println(en)
    println(Security().decrypt(en))
  }
}