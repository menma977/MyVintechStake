package com.myvintech.stake

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.myvintech.stake.controller.DogeController
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.User
import com.myvintech.stake.view.HomeActivity
import okhttp3.FormBody
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
  private lateinit var move: Intent
  private lateinit var json: JSONObject

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val user = User(this)
    val cookie = user.getString("session")
    Timer().schedule(500) {
      when {
        cookie.isNotEmpty() -> {
          val doge = Doge(applicationContext)
          val body = FormBody.Builder()
          body.addEncoded("a", "GetBalance")
          body.addEncoded("key", doge.tokenDoge())
          body.addEncoded("s", cookie)
          body.addEncoded("Currency", "doge")
          json = DogeController(body).call()
          if (json.getInt("code") == 200) {
            move = Intent(applicationContext, HomeActivity::class.java)
            val data = json.getJSONObject("data")
            move.putExtra("balance", BigDecimal(data.getString("Balance")))
            startActivity(move)
            finishAffinity()
          } else {
            Toast.makeText(applicationContext, json.getString("data"), Toast.LENGTH_LONG).show()
          }
        }
        else -> {
          move = Intent(applicationContext, LoginActivity::class.java)
          startActivity(move)
          finishAffinity()
        }
      }
    }
  }
}