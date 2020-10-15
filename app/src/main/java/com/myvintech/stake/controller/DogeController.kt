package com.myvintech.stake.controller

import android.os.AsyncTask
import com.myvintech.stake.config.MapToJson
import com.myvintech.stake.model.Url
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class DogeController(private var bodyValue: HashMap<String, String>) : AsyncTask<Void, Void, JSONObject>() {
  override fun doInBackground(vararg p0: Void): JSONObject {
    return try {
      val client = OkHttpClient.Builder().build()
      val mediaType: MediaType = "application/x-www-form-urlencoded".toMediaType()
      val body = MapToJson().map(bodyValue).toRequestBody(mediaType)
      val request = Request.Builder().url(Url.doge()).post(body).build()
      val response = client.newCall(request).execute()

      return when {
        response.isSuccessful -> {
          val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
          val inputData: String = input.readLine()
          val convertJSON = JSONObject(inputData)
          when {
            convertJSON.toString().contains("ChanceTooHigh") -> {
              JSONObject().put("code", 404).put("data", "Chance Too High")
            }
            convertJSON.toString().contains("ChanceTooLow") -> {
              JSONObject().put("code", 404).put("data", "Chance Too Low")
            }
            convertJSON.toString().contains("InsufficientFunds") -> {
              JSONObject().put("code", 404).put("data", "Insufficient Funds")
            }
            convertJSON.toString().contains("NoPossibleProfit") -> {
              JSONObject().put("code", 404).put("data", "No Possible Profit")
            }
            convertJSON.toString().contains("MaxPayoutExceeded") -> {
              JSONObject().put("code", 404).put("data", "Max Payout Exceeded")
            }
            convertJSON.toString().contains("999doge") -> {
              JSONObject().put("code", 404).put("data", "Invalid request On Server Wait 5 minute to try again")
            }
            convertJSON.toString().contains("error") -> {
              JSONObject().put("code", 404).put("data", "Invalid request")
            }
            convertJSON.toString().contains("TooFast") -> {
              JSONObject().put("code", 404).put("data", "Too Fast")
            }
            convertJSON.toString().contains("TooSmall") -> {
              JSONObject().put("code", 404).put("data", "Too Small")
            }
            convertJSON.toString().contains("LoginRequired") -> {
              JSONObject().put("code", 404).put("data", "Login Required")
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
      JSONObject().put("code", 500).put("data", "error connection")
    }
  }
}