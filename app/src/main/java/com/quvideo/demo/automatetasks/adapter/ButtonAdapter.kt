package com.quvideo.demo.automatetasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quvideo.demo.automatetasks.ButtonItems
import com.quvideo.demo.automatetasks.databinding.HomeItemButtonBinding

class ButtonAdapter(
  private val items: List<ButtonItems>,
  private val clickListener: OnItemClickListener
) : RecyclerView.Adapter<ButtonAdapter.ViewHolder>() {

  class ViewHolder(val binding: HomeItemButtonBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding = HomeItemButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = items[position]
    holder.binding.button.text = item.name
    holder.binding.button.setOnClickListener {
      // Handle button click
      clickListener.onItemClick(item)

    }
  }

  override fun getItemCount(): Int = items.size
}

interface OnItemClickListener {
  fun onItemClick(item: ButtonItems)
}