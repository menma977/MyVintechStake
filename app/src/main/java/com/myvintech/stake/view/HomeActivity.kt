package com.myvintech.stake.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.myvintech.stake.R
import com.myvintech.stake.config.BitCoinFormat
import com.myvintech.stake.config.Security
import com.myvintech.stake.model.User
import java.math.BigDecimal
import java.math.BigInteger

class HomeActivity : AppCompatActivity() {
  private lateinit var textUsername: TextView
  private lateinit var textBalance: TextView
  private lateinit var balance: BigDecimal

  private lateinit var user: User

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_home)
    user = User(applicationContext)

    var b:String = intent.getStringExtra("balance")
    Toast.makeText(applicationContext, b,
      Toast.LENGTH_LONG).show()
    if(b.isNullOrEmpty()) b = "0"
    balance = b.toBigDecimal()


    textUsername = findViewById(R.id.textViewUsername)
    textBalance = findViewById(R.id.textViewBalance)
    textUsername.text = Security.decrypt(user.getString("username"))
    textBalance.text = BitCoinFormat.decimalToDoge(balance).toPlainString()
  }
}