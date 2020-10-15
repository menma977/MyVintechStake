package com.myvintech.stake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.myvintech.stake.config.Popup
import com.myvintech.stake.controller.DogeController
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.User
import com.myvintech.stake.view.HomeActivity
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
  val user = User(this)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val cookie = user.getString("session");
    finishAffinity()
    if(!cookie.isEmpty()) {
      val doge = Doge(applicationContext)
      val body = HashMap<String, String>()
      body["a"] = "GetBalance"
      body["key"] = doge.tokenDoge()
      body["Currency"] = "doge"
      val json = DogeController(body).execute().get()
      if(json.getInt("code") == 200){
        val i = Intent(applicationContext, HomeActivity::class.java)
        return startActivity(i)
      }
    }
    val i = Intent(applicationContext, LoginActivity::class.java)
    startActivity(i)
  }

}