package com.myvintech.stake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.myvintech.stake.config.Popup
import com.myvintech.stake.controller.DogeController
import com.myvintech.stake.controller.DogeControllerQ
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.User
import com.myvintech.stake.view.HomeActivity
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val user = User(applicationContext)
    val cookie = user.getString("session");
    if(cookie.isNotEmpty()) {
      val doge = Doge(applicationContext)
      val body = HashMap<String, String>()
      body["a"] = "GetBalance"
      body["key"] = doge.tokenDoge()
      body["s"] = cookie
      body["Currency"] = "doge"
      //val json = DogeController(body).execute().get()
      val executor = Executors.newFixedThreadPool(10)
      val json = executor.submit(DogeControllerQ(body)).get();
      //val json = DogeControllerQ(body).call()
      if(json.getInt("code") == 200){
        val i = Intent(applicationContext, HomeActivity::class.java)
        val data = json.getJSONObject("data")
        i.putExtra("balance", BigDecimal(data.getString("Balance")).toString())
        return startActivity(i)
      }
      Toast.makeText(applicationContext, json.getString("data") ,Toast.LENGTH_LONG).show()
    }
    val i = Intent(applicationContext, LoginActivity::class.java)
    startActivity(i)
  }

}