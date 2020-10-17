package com.myvintech.stake.controller

import com.myvintech.stake.config.MapToJson
import com.myvintech.stake.model.Url
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WebController {
  class Post(private var targetUrl: String, private var token: String, private var bodyValue: HashMap<String, String>) : Callable<JSONObject> {
    override fun call(): JSONObject {
      return try {
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
        val mediaType: MediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = MapToJson().map(bodyValue).toRequestBody(mediaType)
        val request = Request.Builder()
        request.url(Url.web() + targetUrl.replace(".", "/"))
        request.post(body)
        if (token.isNotEmpty()) {
          request.addHeader("Authorization", "Bearer $token")
        }
        request.addHeader("X-Requested-With", "XMLHttpRequest")
        val response: Response = client.newCall(request.build()).execute()
        val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
        val inputData: String = input.readLine()
        val convertJSON = JSONObject(inputData)
        return when {
          response.isSuccessful -> {
            JSONObject().put("code", 200).put("data", convertJSON)
          }
          else -> {
            when {
              convertJSON.toString().contains("errors") -> {
                JSONObject().put("code", 500).put("data", convertJSON.getJSONObject("errors").getJSONArray(convertJSON.getJSONObject("errors").names()[0].toString())[0])
              }
              convertJSON.toString().contains("message") -> {
                JSONObject().put("code", 500).put("data", convertJSON.getString("message"))
              }
              else -> {
                JSONObject().put("code", 500).put("data", convertJSON)
              }
            }
          }
        }
      } catch (e: Exception) {
        JSONObject().put("code", 500).put("data", e.message)
      }
    }
  }

  class Get(private var targetUrl: String, private var token: String) : Callable<JSONObject> {
    override fun call(): JSONObject {
      return try {
        val client = OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
        val request = Request.Builder()
        request.url(Url.web() + targetUrl.replace(".", "/"))
        request.method("GET", null)
        if (token.isNotEmpty()) {
          request.addHeader("Authorization", "Bearer $token")
        }
        request.addHeader("X-Requested-With", "XMLHttpRequest")
        val response = client.newCall(request.build()).execute()
        val input = BufferedReader(InputStreamReader(response.body!!.byteStream()))
        val inputData: String = input.readLine()
        val convertJSON = JSONObject(inputData)

        if (response.isSuccessful) {
          JSONObject().put("code", 200).put("data", convertJSON)
        } else {
          when {
            convertJSON.toString().contains("errors") -> {
              JSONObject().put("code", 500).put("data", convertJSON.getJSONObject("errors").getJSONArray(convertJSON.getJSONObject("errors").names()[0].toString())[0])
            }
            convertJSON.toString().contains("message") -> {
              JSONObject().put("code", 500).put("data", convertJSON.getString("message"))
            }
            else -> {
              JSONObject().put("code", 500).put("data", convertJSON)
            }
          }
        }
      } catch (e: Exception) {
        JSONObject().put("code", 500).put("data", e.message)
      }
    }
  }
}