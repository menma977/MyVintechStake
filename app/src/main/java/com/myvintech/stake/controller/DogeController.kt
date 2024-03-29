package com.myvintech.stake.controller

import com.myvintech.stake.model.Url
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class DogeController(private var bodyValue: FormBody.Builder) : Callable<JSONObject> {
  override fun call(): JSONObject {
    return try {
      val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
      val body = bodyValue.build()
      val request = Request.Builder().url(Url.doge()).post(body).build()
      val response = client.newCall(request).execute()
      return when {
        response.isSuccessful -> {
          val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
          val inputData: String = input.readLine()
          val convertJSON = JSONObject(inputData)
          when {
            convertJSON.toString().contains("ChanceTooHigh") -> {
              JSONObject().put("code", 500).put("data", "Chance Too High")
            }
            convertJSON.toString().contains("ChanceTooLow") -> {
              JSONObject().put("code", 500).put("data", "Chance Too Low")
            }
            convertJSON.toString().contains("InsufficientFunds") -> {
              JSONObject().put("code", 500).put("data", "Insufficient Funds")
            }
            convertJSON.toString().contains("NoPossibleProfit") -> {
              JSONObject().put("code", 500).put("data", "No Possible Profit")
            }
            convertJSON.toString().contains("MaxPayoutExceeded") -> {
              JSONObject().put("code", 500).put("data", "Max Payout Exceeded")
            }
            convertJSON.toString().contains("999doge") -> {
              JSONObject().put("code", 500).put("data", "Invalid request On Server Wait 5 minute to try again")
            }
            convertJSON.toString().contains("error") -> {
              JSONObject().put("code", 500).put("data", "Invalid request")
            }
            convertJSON.toString().contains("TooFast") -> {
              JSONObject().put("code", 500).put("data", "Too Fast")
            }
            convertJSON.toString().contains("TooSmall") -> {
              JSONObject().put("code", 500).put("data", "Too Small")
            }
            convertJSON.toString().contains("LoginRequired") -> {
              JSONObject().put("code", 500).put("data", "Login Required")
            }
            else -> {
              JSONObject().put("code", 200).put("data", convertJSON)
            }
          }
        }
        else -> {
          JSONObject().put("code", 500).put("data", "Unstable connection / Response Not found")
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      JSONObject().put("code", 500).put("data", "error connection")
    }
  }
}