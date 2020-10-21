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
import com.myvintech.stake.controller.WebController
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.User
import com.myvintech.stake.view.HomeActivity
import okhttp3.FormBody
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
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
  private lateinit var updateButton: Button
  private lateinit var json: JSONObject

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
    updateButton = findViewById(R.id.buttonNewApp)

    versionText.text = "Version ${BuildConfig.VERSION_NAME}"

    if (intent.getBooleanExtra("isUpdate", false)) {
      updateButton.visibility = Button.VISIBLE
      loginButton.visibility = Button.GONE
      usernameInput.visibility = EditText.GONE
      passwordInput.visibility = EditText.GONE
    } else {
      updateButton.visibility = Button.GONE
      loginButton.visibility = Button.VISIBLE
    }

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
          onLogin(usernameInput.text.toString(), passwordInput.text.toString())
        }
      }
    }

    updateButton.setOnClickListener {
      Toast.makeText(this, "update", Toast.LENGTH_LONG).show()
    }

    loading.closeDialog()
  }

  private fun onLogin(username: String, password: String) {
    val body = FormBody.Builder()
    body.addEncoded("username", username)
    body.addEncoded("password", password)
    Timer().schedule(500) {
      json = WebController.Post("login.android", "", body).call()
      println(json)
      if (json.getInt("code") == 200) {
        runOnUiThread {
          user.setString("token", json.getJSONObject("data").getString("token"))
          user.setString("session", json.getJSONObject("data").getString("session"))
          user.setString("walletDeposit", json.getJSONObject("data").getString("walletDeposit"))
          user.setString("walletWithdraw", json.getJSONObject("data").getString("walletWithdraw"))
          user.setBoolean("isStake", json.getJSONObject("data").getBoolean("isStake"))
          try {
            user.setBoolean("stake", true)
            user.setString("fund", json.getJSONObject("data").getJSONObject("lastStake").getString("fund"))
            user.setString("possibility", json.getJSONObject("data").getJSONObject("lastStake").getString("possibility"))
            user.setString("result", json.getJSONObject("data").getJSONObject("lastStake").getString("result"))
            user.setString("status", json.getJSONObject("data").getJSONObject("lastStake").getString("status"))
          } catch (e: Exception) {
            user.setBoolean("stake", false)
            user.setString("fund", "")
            user.setString("possibility", "0")
            user.setString("result", "")
            user.setString("status", "LOSE")
          }
          doge.setString("username", username)
          doge.setString("password", password)
          move = Intent(applicationContext, HomeActivity::class.java)
          loading.closeDialog()
          move.putExtra("balance", BigDecimal(json.getJSONObject("data").getString("balance")))
          startActivity(move)
          finishAffinity()
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
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
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
      ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED -> {
        false
      }
      else -> {
        true
      }
    }
  }
}