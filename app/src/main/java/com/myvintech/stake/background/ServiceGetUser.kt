package com.myvintech.stake.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.myvintech.stake.controller.WebController
import com.myvintech.stake.model.User
import org.json.JSONObject
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.schedule

class ServiceGetUser : Service() {
  private lateinit var json: JSONObject
  private lateinit var user: User
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
    var time = System.currentTimeMillis()
    val trigger = Object()

    Timer().schedule(100) {
      synchronized(trigger) {
        while (true) {
          val delta = System.currentTimeMillis() - time
          if (delta >= 5000) {
            time = System.currentTimeMillis()
            val privateIntent = Intent()
            if (startBackgroundService) {
              json = WebController.Get("user.index", user.getString("token")).call()
              if (json.getInt("code") == 200) {
                privateIntent.putExtra("username", json.getJSONObject("data").getString("username"))
                privateIntent.putExtra("walletDeposit", json.getJSONObject("data").getString("walletDeposit"))
                privateIntent.putExtra("walletWithdraw", json.getJSONObject("data").getString("walletWithdraw"))
                privateIntent.putExtra("isStake", json.getJSONObject("data").getString("isStake"))
              } else {
                if (json.getString("data").contains("Unauthenticated.")) {
                  privateIntent.putExtra("isLogout", true)
                } else {
                  privateIntent.putExtra("isLogout", false)
                  sleep(5000)
                }
              }

              privateIntent.action = "web.api"
              LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
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