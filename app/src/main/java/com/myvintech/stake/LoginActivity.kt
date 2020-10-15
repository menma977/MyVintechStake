package com.myvintech.stake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myvintech.stake.config.Security
import java.util.*
import kotlin.concurrent.schedule

class LoginActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    val password = "Password123456"
    val en = Security().encrypt(password)
    println(en)
    println(Security().decrypt(en))
  }
}