package com.myvintech.stake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.myvintech.stake.controller.DogeController
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.User
import com.myvintech.stake.view.HomeActivity
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
  private lateinit var move: Intent
  private lateinit var json: JSONObject
  private val executor = Executors.newFixedThreadPool(10)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val user = User(this)
    val cookie = user.getString("session")
    if (cookie.isNotEmpty()) {
      val doge = Doge(this)
      val body = HashMap<String, String>()
      body["a"] = "GetBalance"
      body["key"] = doge.tokenDoge()
      body["s"] = cookie
      body["Currency"] = "doge"
      json = executor.submit(DogeController(body)).get()
      Toast.makeText(this, json.getString("code").toString(), Toast.LENGTH_LONG).show()
      if (json.getInt("code") == 200) {
        move = Intent(this, HomeActivity::class.java)
        val data = json.getJSONObject("data")
        move.putExtra("balance", BigDecimal(data.getString("Balance")))
        return startActivity(move)
      }
      Toast.makeText(this, json.getString("data"), Toast.LENGTH_LONG).show()
    }
    move = Intent(this, LoginActivity::class.java)
    startActivity(move)
  }

}