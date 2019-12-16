package com.whitecloud.livesocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.whitecloud.livesocket.model.SocketLogBean
import java.util.ArrayList

class SocketLogAdapter: RecyclerView.Adapter<SocketLogAdapter.ItemHolder>() {

    val dataList = ArrayList<SocketLogBean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.socket_log_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val logBean = dataList[position]
        holder.who.text = logBean.who
        holder.time.text = logBean.time
        holder.data.text = logBean.data
    }

    override fun getItemCount(): Int = dataList.size

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var who: TextView = itemView.findViewById(R.id.log_who) as TextView
        var time: TextView = itemView.findViewById(R.id.log_time) as TextView
        var data: TextView = itemView.findViewById(R.id.log_data) as TextView
    }
}
