package com.myvintech.stake.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.myvintech.stake.model.User
import com.myvintech.stake.view.modal.CustomDialog
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
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
  private lateinit var fundLinearLayout: LinearLayout
  private lateinit var highLinearLayout: LinearLayout
  private lateinit var resultLinearLayout: LinearLayout
  private lateinit var statusLinearLayout: LinearLayout
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
  private var body = HashMap<String, String>()
  private var maxRow = 10

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

    balance = intent.getSerializableExtra("balance") as BigDecimal
    maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance).multiply(BigDecimal(0.01))).multiply(percentTable[high.toInt() + 1].toBigDecimal())
    payIn = balance.multiply(BigDecimal(0.01))

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
    fundLinearLayout = findViewById(R.id.linearLayoutFund)
    highLinearLayout = findViewById(R.id.linearLayoutHigh)
    resultLinearLayout = findViewById(R.id.linearLayoutResult)
    statusLinearLayout = findViewById(R.id.linearLayoutStatus)

    textUsername.text = Security.decrypt(user.getString("username"))
    textBalance.text = BitCoinFormat.decimalToDoge(balance).toPlainString()
    textFund.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
    textProbability.text = "Possibility: ${(high + BigDecimal(5)) * BigDecimal(10)}%"

    walletDeposit = user.getString("walletDeposit")
    walletWithdraw = user.getString("walletWithdraw")

    setDefaultView()

    buttonDespot.setOnClickListener {
      CustomDialog.deposit(this, walletDeposit)
    }

    buttonLogout.setOnClickListener {
      loading.openDialog()
      user.clear()
      doge.clear()
      Timer().schedule(1000) {
        WebController.Get("user.logout", user.getString("token")).call()
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
        high = BigDecimal(progress + 1)
        percent = percentTable[progress]
        maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(payIn.multiply(percent.toBigDecimal())).multiply(payInMultiple))
        textFund.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
        textProbability.text = "Possibility: ${high.toInt() * 10}%"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    buttonStake.setOnClickListener {
      if (editTextAmount.text.isEmpty()) {
        Popup(this).show("Amount required", Toast.LENGTH_SHORT)
      } else {
        payIn = bitCoinFormat.dogeToDecimal(editTextAmount.text.toString().toBigDecimal())
        onBot()
      }
    }

    loading.closeDialog()
  }

  private fun onBot() {
    loading.openDialog()
    Timer().schedule(100) {
      body["a"] = "PlaceBet"
      body["s"] = user.getString("session")
      body["Low"] = "0"
      body["High"] = ((high + BigDecimal(1)).multiply(BigDecimal(10)).multiply(BigDecimal(10000)) - BigDecimal(600)).toPlainString()
      body["PayIn"] = payIn.toPlainString()
      body["ProtocolVersion"] = "2"
      body["ClientSeed"] = seed
      body["Currency"] = "doge"
      json = DogeController(body).call()
      loading.closeDialog()
      if (json.getInt("code") == 200) {
        seed = json.getJSONObject("data")["Next"].toString()
        val puyOut = json.getJSONObject("data")["PayOut"].toString().toBigDecimal()
        var balanceRemaining = json.getJSONObject("data")["StartingBalance"].toString().toBigDecimal()
        val profit = puyOut - payIn
        balanceRemaining += profit
        val winBot = profit > BigDecimal(0)

        runOnUiThread {
          balance = balanceRemaining
          textBalance.text = bitCoinFormat.decimalToDoge(balance).toPlainString()

          setView(bitCoinFormat.decimalToDoge(payIn).toPlainString(), fundLinearLayout, false, winBot)
          setView("${(seekBar.progress + 1) * 10}%", highLinearLayout, false, winBot)
          setView(bitCoinFormat.decimalToDoge(puyOut).toPlainString(), resultLinearLayout, false, winBot)
          if (winBot) {
            setView("WIN", statusLinearLayout, false, winBot)
            buttonStake.visibility = Button.GONE
            textStatus.text = "WIN"
            textStatus.setTextColor(getColor(R.color.Success))
          } else {
            setView("LOSE", statusLinearLayout, false, winBot)
            textStatus.text = "LOSE"
            textStatus.setTextColor(getColor(R.color.Danger))

            payInMultiple = BigDecimal(2)

            maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(payIn.multiply(percent.toBigDecimal())).multiply(payInMultiple))
            textFund.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
            editTextAmount.isEnabled = true

            seekBar.progress = seekBar.progress + 1
          }

          editTextAmount.setText("")
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          Popup(applicationContext).show(json.getString("data"), Toast.LENGTH_SHORT)
          loading.closeDialog()
        }
      }
    }
  }

  private fun setDefaultView() {
    setView("Fund", fundLinearLayout, isNew = true, isWin = false)
    setView("Possibility", highLinearLayout, isNew = true, isWin = false)
    setView("Result", resultLinearLayout, isNew = true, isWin = false)
    setView("Status", statusLinearLayout, isNew = true, isWin = false)

    for (i in 0 until maxRow) {
      setView("", fundLinearLayout, isNew = true, isWin = false)
      setView("", highLinearLayout, isNew = true, isWin = false)
      setView("", resultLinearLayout, isNew = true, isWin = false)
      setView("", statusLinearLayout, isNew = true, isWin = false)
    }
  }

  private fun setListTargetMaximum() {
    percentTable.add(1.0)
    percentTable.add(1.6)
    percentTable.add(2.4)
    percentTable.add(4.1)
    percentTable.add(9.0)
  }

  private fun setView(value: String, linearLayout: LinearLayout, isNew: Boolean, isWin: Boolean) {
    val template = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val valueView = TextView(applicationContext)
    valueView.text = value
    valueView.gravity = Gravity.CENTER
    valueView.layoutParams = template
    if (isNew) {
      valueView.setTextColor(getColor(R.color.colorAccent))
    } else {
      if (isWin) {
        valueView.setTextColor(getColor(R.color.Success))
        buttonStake.visibility = Button.GONE
        user.setBoolean("isWin", true)
      } else {
        valueView.setTextColor(getColor(R.color.Danger))
      }
    }

    if ((linearLayout.childCount - 1) == maxRow) {
      linearLayout.removeViewAt(linearLayout.childCount - 1)
      linearLayout.addView(valueView, 1)
    } else {
      linearLayout.addView(valueView)
    }
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
        loading.openDialog()
        user.clear()
        doge.clear()
        Timer().schedule(1000) {
          WebController.Get("user.logout", user.getString("token")).call()
          move = Intent(applicationContext, MainActivity::class.java)
          runOnUiThread {
            startActivity(move)
            loading.closeDialog()
            finishAffinity()
          }
        }
      } else {
        walletDeposit = intent.getStringExtra("walletDeposit").toString()
        walletWithdraw = intent.getStringExtra("walletWithdraw").toString()
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