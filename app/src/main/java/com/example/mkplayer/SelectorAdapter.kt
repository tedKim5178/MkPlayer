package com.example.mkplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// TODO :: DataBinding
class SelectorAdapter(private val datas: ArrayList<String>) :
    RecyclerView.Adapter<SelectorViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectorViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_selector, parent, false)
        val holder = SelectorViewHolder(view)
        listener?.also {
            holder.setOnItemClickListener(it)
        }
        return holder
    }

    override fun onBindViewHolder(holder: SelectorViewHolder, position: Int) {
        holder.resolutionHeightTv.text = datas[position]
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun getItem(position: Int): String {
        return datas[position]
    }
}

class SelectorViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private var listener: SelectorAdapter.OnItemClickListener? = null
    val resolutionHeightTv: TextView = v.findViewById(R.id.resolution_height)

    init {
        resolutionHeightTv.setOnClickListener {
            listener?.onItemClick(it, adapterPosition)
        }
    }

    fun setOnItemClickListener(listener: SelectorAdapter.OnItemClickListener) {
        this.listener = listener
    }
}