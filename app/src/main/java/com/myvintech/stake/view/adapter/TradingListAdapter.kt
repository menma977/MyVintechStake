package com.myvintech.stake.view.adapter
//import com.myvintech.stake.R
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.myvintech.stake.R
import com.myvintech.stake.model.TradingResult
import java.math.BigDecimal

class TradingListAdapter(private val context: Context, private val myDataset: ArrayList<TradingResult>) : RecyclerView.Adapter<TradingListAdapter.MyViewHolder>() {
  private var max: Int = 21

  init {
    myDataset.add(0, TradingResult(BigDecimal.ZERO, 0, BigDecimal.ZERO, false))
  }

  class MyViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
    val fund: TextView = layout.findViewById(R.id.fund)
    val high: TextView = layout.findViewById(R.id.high)
    val result: TextView = layout.findViewById(R.id.result)
    val status: TextView = layout.findViewById(R.id.status)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    return when (viewType) {
      0 -> {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.component_list_trading_header, parent, false)
        MyViewHolder(layout)
      }
      else -> {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.component_list_trading, parent, false)
        MyViewHolder(layout)
      }
    }
  }

  fun setMax(max: Int) {
    this.max = max+1
  }

  fun getMax(): Int {
    return max-1
  }

  override fun getItemViewType(position: Int): Int {
    return if (position == 0) 0 else 1
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    if (position == 0) return
    holder.fund.text = myDataset[position].fund.toPlainString()
    holder.high.text = myDataset[position].high.toString() + "%"
    holder.result.text = myDataset[position].result.toPlainString()
    if (!myDataset[position].status) {
      holder.status.text = "LOSE"
      holder.status.setTextColor(ContextCompat.getColor(context, R.color.Danger))
    } else {
      holder.status.text = "WIN"
      holder.status.setTextColor(ContextCompat.getColor(context, R.color.Success))
    }
  }

  override fun getItemCount() = myDataset.size

  fun addItem(item: TradingResult) {
    if(myDataset.size == max)
      myDataset.removeAt(myDataset.size-1)
    myDataset.add(1, item)
    this.notifyDataSetChanged()
    this.notifyItemRangeInserted(0, myDataset.size)
  }
}
