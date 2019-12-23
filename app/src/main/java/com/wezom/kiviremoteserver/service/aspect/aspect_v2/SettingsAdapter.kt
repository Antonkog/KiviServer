package com.wezom.kiviremoteserver.service.aspect.aspect_v2

import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.wezom.kiviremoteserver.R

class SettingsAdapter(val backClickListener: View.OnClickListener, var items: List<Pair<String, Int>>) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aspect_settings, parent, false)
        val holder = SettingsViewHolder(view)
        holder.card.setOnFocusChangeListener { _, hasFocus -> holder.scaleCard(hasFocus) }
        return holder
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bindData(items[position])
        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                holder.pb.progress = holder.pb.progress + 1
                holder.tvValue.text = holder.pb.progress.toString()
                return@setOnKeyListener true
            }
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                holder.pb.progress = holder.pb.progress - 1
                holder.tvValue.text = holder.pb.progress.toString()
                return@setOnKeyListener true
            }
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                backClickListener.onClick(v)
                return@setOnKeyListener true
            }

            false
        }

        if (position == itemCount / 2) {
            holder.card.requestFocus()
        }

    }

    override fun getItemCount(): Int = items.size

    inner class SettingsViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val card = view.findViewById<CardView>(R.id.card)
        val tvTitle = view.findViewById<TextView>(R.id.text_picker)
        val tvValue = view.findViewById<TextView>(R.id.tv_value)
        val pb = view.findViewById<ProgressBar>(R.id.pb)
        val ivLeft = view.findViewById<ImageView>(R.id.iv_arrow_left)
        val ivRight = view.findViewById<ImageView>(R.id.iv_arrow_right)

        fun bindData(data: Pair<String, Int>) {
            card.setCardBackgroundColor(if (card.isFocused) Color.parseColor("#ee000000") else Color.TRANSPARENT)
            tvTitle.text = data.first
            tvValue.text = data.second.toString()
            pb.progress = data.second
        }

        fun scaleCard(hasFocus: Boolean) {
            card.setCardBackgroundColor(if (hasFocus) Color.parseColor("#ee000000") else Color.TRANSPARENT)
            //tvTitle.visibility = if (hasFocus) View.VISIBLE else View.GONE
            tvValue.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
            pb.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
            ivLeft.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
            ivRight.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
        }
    }
}