package com.myvintech.stake.config

import android.content.Context
import android.view.Gravity
import android.widget.Toast

class Popup(context: Context) {
  private var toast: Toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT)
  fun show(message: String, duration: Int) {
    toast.setText(message)
    toast.duration = duration
    toast.setGravity(Gravity.BOTTOM, 0, 50)
    toast.show()
  }
}