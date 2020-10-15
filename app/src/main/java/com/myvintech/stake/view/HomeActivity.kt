package com.myvintech.stake.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.myvintech.stake.R
import com.myvintech.stake.config.BitCoinFormat
import com.myvintech.stake.config.Security
import com.myvintech.stake.model.User
import java.math.BigDecimal
import com.myvintech.stake.view.modal.CustomDialog

class HomeActivity : AppCompatActivity() {
  private lateinit var textUsername: TextView
  private lateinit var textBalance: TextView
  private lateinit var balance: BigDecimal
  private lateinit var user: User

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_home)
    user = User(applicationContext)

    balance = intent.getSerializableExtra("balance") as BigDecimal


    textUsername = findViewById(R.id.textViewUsername)
    textBalance = findViewById(R.id.textViewBalance)
    textUsername.text = Security.decrypt(user.getString("username"))
    textBalance.text = BitCoinFormat.decimalToDoge(balance).toPlainString()

    textBalance.setOnClickListener(View.OnClickListener {
      CustomDialog.withdraw(this, "23")
    })
  }
}