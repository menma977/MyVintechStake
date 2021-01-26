package com.myvintech.stake.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myvintech.stake.MainActivity
import com.myvintech.stake.R
import com.myvintech.stake.background.ServiceGetBalance
import com.myvintech.stake.background.ServiceGetUser
import com.myvintech.stake.config.BitCoinFormat
import com.myvintech.stake.config.Loading
import com.myvintech.stake.config.Popup
import com.myvintech.stake.config.Security
import com.myvintech.stake.controller.DogeController
import com.myvintech.stake.controller.WebController
import com.myvintech.stake.model.Doge
import com.myvintech.stake.model.TradingResult
import com.myvintech.stake.model.User
import com.myvintech.stake.view.adapter.TradingListAdapter
import com.myvintech.stake.view.modal.CustomDialog
import okhttp3.FormBody
import org.json.JSONObject
import java.lang.Exception
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HomeActivity : AppCompatActivity() {
  private lateinit var move: Intent
  private lateinit var textUsername: TextView
  private lateinit var textBalance: TextView
  private lateinit var textStatus: TextView
  private lateinit var textFund: TextView
  private lateinit var textProbability: TextView
  private lateinit var editTextAmount: EditText
  private lateinit var buttonStake: Button
  private lateinit var buttonStop: Button
  private lateinit var buttonDespot: Button
  private lateinit var buttonWithdrawAll: Button
  private lateinit var buttonLogout: Button
  private lateinit var seekBar: SeekBar
  private lateinit var balance: BigDecimal
  private lateinit var user: User
  private lateinit var doge: Doge
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var percentTable: ArrayList<Double>
  private lateinit var json: JSONObject
  private lateinit var tradingList: RecyclerView
  private lateinit var listTradingAdapter: TradingListAdapter
  private lateinit var viewManager: RecyclerView.LayoutManager
  private lateinit var webApiIntent: Intent
  private lateinit var dogeApiIntent: Intent
  private lateinit var walletDeposit: String
  private lateinit var walletWithdraw: String
  private var percent = 1.0
  private var maxBalance = BigDecimal(0)
  private var payIn: BigDecimal = BigDecimal(0)
  private var payInMultiple: BigDecimal = BigDecimal(1)
  private var high = BigDecimal(0)
  private var seed = (0..99999).random().toString()
  private var isWin = false
  private val myDataset = ArrayList<TradingResult>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    user = User(this)
    doge = Doge(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat
    percentTable = ArrayList()
    setListTargetMaximum()
    loading.openDialog()

    textUsername = findViewById(R.id.textViewUsername)
    textBalance = findViewById(R.id.textViewBalance)
    textStatus = findViewById(R.id.textViewStatus)
    textFund = findViewById(R.id.textViewFund)
    textProbability = findViewById(R.id.textViewProbability)
    seekBar = findViewById(R.id.seekBar)
    buttonStake = findViewById(R.id.buttonStake)
    buttonStop = findViewById(R.id.buttonStop)
    buttonDespot = findViewById(R.id.buttonDeposit)
    buttonWithdrawAll = findViewById(R.id.buttonWithdrawAll)
    buttonLogout = findViewById(R.id.buttonLogout)
    editTextAmount = findViewById(R.id.editTextAmount)
    viewManager = LinearLayoutManager(this)
    listTradingAdapter = TradingListAdapter(applicationContext, myDataset)

    getDataUser()

    tradingList = findViewById<RecyclerView>(R.id.tradingList).apply {
      layoutManager = viewManager
      adapter = listTradingAdapter
    }

    buttonDespot.setOnClickListener {
      CustomDialog.deposit(this, walletDeposit)
    }

    buttonWithdrawAll.visibility = Button.GONE

    buttonWithdrawAll.setOnClickListener {
      loading.openDialog()
      val body = FormBody.Builder()
      body.addEncoded("a", "Withdraw")
      body.addEncoded("s", user.getString("session"))
      body.addEncoded("Amount", "0")
      body.addEncoded("Address", user.getString("walletWithdraw"))
      body.addEncoded("Currency", "doge")
      Timer().schedule(100) {
        json = DogeController(body).call()
        if (json.getInt("code") == 200) {
          runOnUiThread {
            Popup(applicationContext).show("withdrawal in queue", Toast.LENGTH_LONG)
            loading.closeDialog()
          }
        } else {
          runOnUiThread {
            Popup(applicationContext).show(json.getString("data"), Toast.LENGTH_LONG)
            loading.closeDialog()
          }
        }
      }
    }

    buttonLogout.setOnClickListener {
      loading.openDialog()
      Timer().schedule(1000) {
        WebController.Get("user.logout", user.getString("token")).call()
        user.clear()
        doge.clear()
        move = Intent(applicationContext, MainActivity::class.java)
        runOnUiThread {
          startActivity(move)
          loading.closeDialog()
          finishAffinity()
        }
      }
    }

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        high = BigDecimal(progress)
        percent = percentTable[progress]
        maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(payIn.multiply(payInMultiple).multiply(percent.toBigDecimal())))
        textFund.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
        textProbability.text = "Possibility: ${(high.toInt() + 5) * 10}%"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    buttonStop.setOnClickListener {
      val builder = AlertDialog.Builder(this)
      builder.setTitle("confirmation")
      builder.setMessage("You are sure ?")
      builder.setCancelable(true)
      builder.setPositiveButton("Yes") { dialogInterface, _ ->
        loading.openDialog()
        Timer().schedule(1000) {
          val body = FormBody.Builder()
          body.addEncoded("sessionDoge", user.getString("session"))
          body.addEncoded("walletWithdraw", user.getString("walletWithdraw"))
          json = WebController.Post("stake.stop", user.getString("token"), body).call()
          if (json.getInt("code") == 200) {
            runOnUiThread {
              dialogInterface.dismiss()
              Popup(applicationContext).show(json.getString("data"), Toast.LENGTH_LONG)
              move = Intent(applicationContext, MainActivity::class.java)
              loading.closeDialog()
              startActivity(move)
              finishAffinity()
            }
          } else {
            runOnUiThread {
              Popup(applicationContext).show(json.getString("data"), Toast.LENGTH_LONG)
              loading.closeDialog()
              dialogInterface.dismiss()
            }
          }
        }
      }
      builder.setNegativeButton("Cancel") { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
      builder.create().show()
    }

    buttonStake.setOnClickListener {
      loading.openDialog()
      buttonStake.visibility = Button.GONE
      when {
        editTextAmount.text.isEmpty() -> {
          Popup(this).show("Amount required", Toast.LENGTH_SHORT)
          buttonStake.visibility = Button.VISIBLE
          loading.closeDialog()
        }
        bitCoinFormat.dogeToDecimal(editTextAmount.text.toString().toBigDecimal()) > maxBalance -> {
          Toast.makeText(this, "Doge you can input should not be more than ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}", Toast.LENGTH_LONG).show()
          buttonStake.visibility = Button.VISIBLE
          loading.closeDialog()
        }
        else -> {
          payIn = bitCoinFormat.dogeToDecimal(editTextAmount.text.toString().toBigDecimal())
          onStake()
        }
      }
    }

    walletDeposit = user.getString("walletDeposit")
    walletWithdraw = user.getString("walletWithdraw")

    balance = intent.getSerializableExtra("balance") as BigDecimal
  }

  private fun onStake() {
    val body = FormBody.Builder()
    body.addEncoded("fund", payIn.toPlainString())
    body.addEncoded("possibility", seekBar.progress.toString())
    body.addEncoded("high", ((high + BigDecimal(5)).multiply(BigDecimal(10)).multiply(BigDecimal(10000)) - BigDecimal(600)).toPlainString())
    body.addEncoded("sessionDoge", user.getString("session"))
    body.addEncoded("seeds", seed)
    body.addEncoded("walletWithdraw", user.getString("walletWithdraw"))
    Timer().schedule(1000) {
      json = WebController.Post("stake.tread", user.getString("token"), body).call()
      Log.i("response", json.toString())
      if (json.getInt("code") == 200) {
        seed = json.getJSONObject("data").getString("seeds")
        val puyOut = json.getJSONObject("data").getString("payout").toBigDecimal()
        val balanceRemaining = json.getJSONObject("data").getString("balanceRemaining").toBigDecimal()
        val winBot = json.getJSONObject("data").getBoolean("isWin")

        runOnUiThread {
          user.setString("fund", payIn.toPlainString())

          balance = balanceRemaining
          textBalance.text = bitCoinFormat.decimalToDoge(balance).toPlainString()

          listTradingAdapter.addItem(
            TradingResult(
              bitCoinFormat.decimalToDoge(payIn), (seekBar.progress + 5) * 10, bitCoinFormat.decimalToDoge(puyOut), winBot
            )
          )

          if (winBot) {
            buttonStake.visibility = Button.GONE
            textStatus.text = "WIN"
            textStatus.setTextColor(ContextCompat.getColor(applicationContext, R.color.Success))
            user.setString("status", "WIN")
          } else {
            textStatus.text = "LOSE"
            textStatus.setTextColor(ContextCompat.getColor(applicationContext, R.color.Danger))

            payInMultiple = BigDecimal(2)

            maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(payIn.multiply(percent.toBigDecimal())).multiply(payInMultiple))
            textFund.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
            buttonStake.visibility = Button.VISIBLE

            seekBar.progress = seekBar.progress + 1
          }
          editTextAmount.setText("")
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          buttonStake.visibility = Button.VISIBLE
          Popup(applicationContext).show(json.getString("data"), Toast.LENGTH_SHORT)
          loading.closeDialog()
        }
      }
    }
  }

  private fun getDataUser() {
    Timer().schedule(1000) {
      json = WebController.Get("user.index", user.getString("token")).call()
      if (json.getInt("code") == 200) {
        user.setBoolean("isStake", json.getJSONObject("data").getBoolean("isStake"))
        if (json.getJSONObject("data").getBoolean("isStake")) {
          runOnUiThread {
            buttonStake.visibility = Button.GONE
            buttonStop.visibility = Button.GONE
          }
        }
        try {
          user.setBoolean("stake", true)
          user.setString("fund", json.getJSONObject("data").getJSONObject("lastStake").getString("fund"))
          user.setString("possibility", json.getJSONObject("data").getJSONObject("lastStake").getString("possibility"))
          user.setString("result", json.getJSONObject("data").getJSONObject("lastStake").getString("result"))
          user.setString("status", json.getJSONObject("data").getJSONObject("lastStake").getString("status"))
        } catch (e: Exception) {
          user.setBoolean("stake", false)
          user.setString("fund", "")
          user.setString("possibility", "")
          user.setString("result", "")
          user.setString("status", "LOSE")
        }

        if (user.getBoolean("stake")) {
          payIn = user.getString("fund").toBigDecimal()
          high = user.getString("possibility").toBigDecimal()
          seekBar.progress = user.getString("possibility").toInt() + 1
          if (user.getString("status").contains("WIN")) {
            isWin = true
          }
          payInMultiple = BigDecimal(2)
          maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(payIn).multiply(payInMultiple)).multiply(percentTable[high.toInt()].toBigDecimal())
        } else {
          maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance).multiply(BigDecimal(0.01))).multiply(percentTable[high.toInt()].toBigDecimal())
          payIn = balance.multiply(BigDecimal(0.01))
        }

        runOnUiThread {
          textUsername.text = Security.decrypt(user.getString("username"))
          textBalance.text = BitCoinFormat.decimalToDoge(balance).toPlainString()
          textFund.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
          textProbability.text = "Possibility: ${(high + BigDecimal(5)) * BigDecimal(10)}%"
        }

        getListView()
      } else {
        runOnUiThread {
          Popup(applicationContext).show("table not read correctly. please close the application and reopen it", Toast.LENGTH_LONG)
        }
      }
    }
  }

  private fun getListView() {
    Timer().schedule(100) {
      json = WebController.Get("stake.index", user.getString("token")).call()
      runOnUiThread {
        if (!json.getJSONObject("data").isNull("lastStake")) {
          for (i in 0 until json.getJSONObject("data").getJSONArray("listStake").length()) {
            json.getJSONObject("data").getJSONArray("listStake").length()
            val data = json.getJSONObject("data").getJSONArray("listStake").getJSONObject(i)
            val fund = bitCoinFormat.decimalToDoge(data.getString("fund").toBigDecimal())
            val possibility = (data.getInt("possibility") + 5) * 10
            val result = bitCoinFormat.decimalToDoge(data.getString("result").toBigDecimal())
            val status = data.getString("status") == "WIN"
            listTradingAdapter.addItem(TradingResult(fund, possibility, result, status))
          }
        }
        loading.closeDialog()
      }
    }
  }

  private fun setListTargetMaximum() {
    percentTable.add(1.0)
    percentTable.add(1.6)
    percentTable.add(2.4)
    percentTable.add(4.1)
    percentTable.add(9.0)
  }

  override fun onStart() {
    super.onStart()
    webApiIntent = Intent(applicationContext, ServiceGetUser::class.java)
    startService(webApiIntent)

    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(getUserService, IntentFilter("web.api"))

    dogeApiIntent = Intent(applicationContext, ServiceGetBalance::class.java)
    startService(dogeApiIntent)

    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(getBalanceService, IntentFilter("doge.api"))
  }

  override fun onBackPressed() {
    super.onBackPressed()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(getUserService)
    stopService(webApiIntent)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(getBalanceService)
    stopService(dogeApiIntent)
  }

  override fun onStop() {
    super.onStop()
    stopService(webApiIntent)
    stopService(dogeApiIntent)
  }

  private var getUserService: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.getBooleanExtra("isLogout", false)) {
        Timer().schedule(1000) {
          WebController.Get("user.logout", user.getString("token")).call()
          user.clear()
          doge.clear()
          move = Intent(applicationContext, MainActivity::class.java)
          runOnUiThread {
            startActivity(move)
            finishAffinity()
          }
        }
      } else {
        walletDeposit = intent.getStringExtra("walletDeposit").toString()
        walletWithdraw = intent.getStringExtra("walletWithdraw").toString()

        if (intent.getBooleanExtra("stake", false)) {
          if (user.getString("status").contains("WIN")) {
            isWin = true
          }
          payInMultiple = BigDecimal(2)
          maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(user.getString("fund").toBigDecimal()).multiply(payInMultiple)).multiply(percentTable[high.toInt()].toBigDecimal())
        } else {
          maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance).multiply(BigDecimal(0.01))).multiply(percentTable[high.toInt()].toBigDecimal())
        }

        textUsername.text = Security.decrypt(user.getString("username"))
        textBalance.text = BitCoinFormat.decimalToDoge(balance).toPlainString()
        val viewFund = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
        textFund.text = viewFund
        val viewProbability = "Possibility: ${(high + BigDecimal(5)) * BigDecimal(10)}%"
        textProbability.text = viewProbability

        println("===========================================")
        println(user.getBoolean("isStake").toString())
        println(user.getString("status"))
        println("===========================================")

        if (user.getBoolean("isStake") && user.getString("status") == "WIN") {
          buttonStake.visibility = Button.GONE
          buttonStop.visibility = Button.GONE
        } else if (user.getBoolean("isStake") && user.getString("status") == "LOSE") {
          buttonStake.visibility = Button.GONE
        } else if (user.getString("status") == "WIN") {
          buttonStake.visibility = Button.GONE
          buttonStop.visibility = Button.VISIBLE
        } else {
          buttonStake.visibility = Button.VISIBLE
          buttonStop.visibility = Button.VISIBLE
        }
      }
    }
  }
  private var getBalanceService: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance = intent.getSerializableExtra("balance") as BigDecimal
      textBalance.text = bitCoinFormat.decimalToDoge(balance).toPlainString()
    }
  }
}