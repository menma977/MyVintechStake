package com.myvintech.stake

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.myvintech.stake.config.Loading
import com.myvintech.stake.config.Popup
import com.myvintech.stake.controller.DogeController
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.User
import com.myvintech.stake.view.HomeActivity
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class LoginActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var doge: Doge
  private lateinit var loading: Loading
  private lateinit var move: Intent
  private lateinit var toast: Popup
  private lateinit var versionText: TextView
  private lateinit var usernameInput: EditText
  private lateinit var passwordInput: EditText
  private lateinit var loginButton: Button
  private lateinit var json: JSONObject
  private var body = HashMap<String, String>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    user = User(this)
    doge = Doge(this)
    loading = Loading(this)
    toast = Popup(this)

    loading.openDialog()

    versionText = findViewById(R.id.textViewVersion)
    usernameInput = findViewById(R.id.editTextUsername)
    passwordInput = findViewById(R.id.editTextPassword)
    loginButton = findViewById(R.id.buttonLogin)

    loginButton.setOnClickListener {
      loading.openDialog()
      when {
        !validatePermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
          loading.closeDialog()
          doRequestPermission()
        }
        usernameInput.text.isEmpty() -> {
          loading.closeDialog()
          toast.show("username required", Toast.LENGTH_LONG)
          usernameInput.requestFocus()
        }
        passwordInput.text.isEmpty() -> {
          loading.closeDialog()
          toast.show("password required", Toast.LENGTH_LONG)
          passwordInput.requestFocus()
        }
        else -> {
          onLoginDoge(usernameInput.text.toString(), passwordInput.text.toString())
        }
      }
    }

    loading.closeDialog()
  }

  private fun onLoginDoge(username: String, password: String) {
    body["a"] = "Login"
    body["key"] = doge.tokenDoge()
    body["username"] = username
    body["password"] = password
    body["Totp"] = "''"
    Timer().schedule(500) {
      json = DogeController(body).execute().get()
      if (json.getInt("code") == 200) {
        runOnUiThread {
          user.setString("session", json.getJSONObject("data").getString("SessionCookie"))
          doge.setString("username", username)
          doge.setString("password", password)
          loading.closeDialog()
          move = Intent(applicationContext, HomeActivity::class.java)
          finishAffinity()
          startActivity(move)
        }
      } else {
        runOnUiThread {
          loading.closeDialog()
          toast.show(json.getString("data"), Toast.LENGTH_LONG)
        }
      }
    }
  }

  private fun doRequestPermission() {
    requestPermissions(
      arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
      ), 100
    )
  }

  private fun validatePermission(): Boolean {
    return when {
      ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED -> {
        false
      }
      ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED -> {
        false
      }
      else -> {
        true
      }
    }
  }
}