package com.myvintech.stake.view.adapter

//import com.myvintech.stake.R
import android.content.ClipData.Item
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myvintech.stake.R
import com.myvintech.stake.model.TradingResult
import java.math.BigDecimal


class TradingListAdapter(private val myDataset: ArrayList<TradingResult>) :
    RecyclerView.Adapter<TradingListAdapter.MyViewHolder>() {

    init {
        myDataset.add(0, TradingResult(BigDecimal.ZERO,0, BigDecimal.ZERO, false))
    }

    class MyViewHolder(val layout: View) : RecyclerView.ViewHolder(layout){
        val fund:TextView = layout.findViewById(R.id.fund)
        val high:TextView = layout.findViewById(R.id.high)
        val result:TextView = layout.findViewById(R.id.result)
        val status:ImageView = layout.findViewById(R.id.status)
    }

    class MyViewHolderHeader(val layout: View) : RecyclerView.ViewHolder(layout){}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return when (viewType) {
            0 -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.component_list_trading_header, parent, false)
                return MyViewHolder(layout)
            }
            else -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.component_list_trading, parent, false)
                return MyViewHolder(layout)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) 0 else 1
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if(position == 0) return
        holder.fund.text = myDataset[position].fund.toPlainString() + "DOGE"
        holder.high.text = myDataset[position].high.toString()+"%"
        holder.result.text = myDataset[position].result.toPlainString() + "DOGE"
        holder.status.setImageResource(
            if (myDataset[position].status)
                android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun getItemCount() = myDataset.size

    fun addItem(item: TradingResult){
        myDataset.add(item)
        this.notifyDataSetChanged()
        this.notifyItemRangeInserted(0, myDataset.size)
    }
}
