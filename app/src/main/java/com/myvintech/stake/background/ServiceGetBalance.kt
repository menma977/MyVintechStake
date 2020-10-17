package com.myvintech.stake.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.myvintech.stake.controller.DogeController
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.User
import org.json.JSONObject
import java.lang.Exception
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class ServiceGetBalance : Service() {
  private lateinit var json: JSONObject
  private lateinit var user: User
  private lateinit var doge: Doge
  private var startBackgroundService: Boolean = false

  override fun onBind(p0: Intent?): IBinder? {
    TODO("Not yet implemented")
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    onHandleIntent()

    return START_STICKY
  }

  private fun onHandleIntent() {
    user = User(this)
    doge = Doge(applicationContext)
    val cookie = user.getString("session")
    var time = System.currentTimeMillis()
    val trigger = Object()
    val body = HashMap<String, String>()

    Timer().schedule(100) {
      synchronized(trigger) {
        while (true) {
          val delta = System.currentTimeMillis() - time
          if (delta >= 15000) {
            time = System.currentTimeMillis()
            val privateIntent = Intent()
            if (startBackgroundService) {
              try {
                body["a"] = "GetBalance"
                body["key"] = doge.tokenDoge()
                body["s"] = cookie
                body["Currency"] = "doge"
                json = DogeController(body).call()
                if (json.getInt("code") == 200) {
                  if (json.getJSONObject("data").getString("Balance").isEmpty()) {
                    privateIntent.putExtra("balance", BigDecimal(0))
                  } else {
                    privateIntent.putExtra("balance", BigDecimal(json.getJSONObject("data").getString("Balance")))
                  }
                  privateIntent.action = "doge.api"
                  LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
                } else {
                  Thread.sleep(60000)
                }
              } catch (e: Exception) {
                Log.w("error", e.message.toString())
              }
            } else {
              break
            }
          }
        }
      }
    }
  }

  override fun onCreate() {
    super.onCreate()
    startBackgroundService = true
  }

  override fun onDestroy() {
    super.onDestroy()
    startBackgroundService = false
  }
}