package com.wezom.kiviremoteserver.service.aspect.recycler

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper

class AspectInputsAdapter(private val items: List<InputSourceHelper.INPUT_PORT>,
                          val inputsHelper: EnvironmentInputsHelper,
                          val onBackClick: () -> Unit) : RecyclerView.Adapter<AspectInputsAdapter.InputsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aspect_input, parent, false)
        val holder = InputsViewHolder(view)
        holder.container.setOnFocusChangeListener { _, hasFocus -> holder.onFocusChanged(hasFocus) }
        return holder
    }

    override fun onBindViewHolder(holder: InputsViewHolder, position: Int) {
        if (position == 0) {
            holder.container.nextFocusLeftId = holder.container.id
        }

        if (position == items.size - 1) {
            holder.container.nextFocusRightId = holder.container.id
        }

        holder.bindData(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class InputsViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.ll_root)
        private val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val ivIndicator: ImageView = view.findViewById(R.id.iv_indicator)
        private var focusedDrawable = R.drawable.ic_focused_hdmi
        private var unfocusedDrawable = R.drawable.ic_unfocused_hdmi

        fun bindData(data: InputSourceHelper.INPUT_PORT) {
            when (data.id) {
                1 -> {
                    //ATV
                    focusedDrawable = R.drawable.ic_focused_analog
                    unfocusedDrawable = R.drawable.ic_unfocused_analog
                    tvTitle.setText(R.string.inputs_type_atv)
                }
                2 -> {
                    //AV
                    focusedDrawable = R.drawable.ic_focused_av
                    unfocusedDrawable = R.drawable.ic_unfocused_av
                    tvTitle.setText(R.string.inputs_type_av)
                }
                28 -> {
                    //TV
                    focusedDrawable = R.drawable.ic_focused_t2
                    unfocusedDrawable = R.drawable.ic_unfocused_t2
                    tvTitle.setText(R.string.inputs_type_tv)

                }
                46 -> {
                    //DVB-S2
                    focusedDrawable = R.drawable.ic_focused_satellite
                    unfocusedDrawable = R.drawable.ic_unfocused_satellite
                    tvTitle.setText(R.string.inputs_type_dvb_s2)

                }
                47 -> {
                    //DVB-C
                    focusedDrawable = R.drawable.ic_focused_cable
                    unfocusedDrawable = R.drawable.ic_unfocused_cable
                    tvTitle.setText(R.string.inputs_type_dvb_c)
                }
                else -> {
                    focusedDrawable = R.drawable.ic_focused_hdmi
                    unfocusedDrawable = R.drawable.ic_unfocused_hdmi
                    tvTitle.setText(data.nameResource)
                }
            }

            ivIcon.setImageResource(if (container.hasFocus()) focusedDrawable else unfocusedDrawable)
            ivIndicator.visibility = if (data.isConnected) View.VISIBLE else View.GONE

            container.setOnClickListener {
                inputsHelper.changeInput(data, container.context)
                //TODO: refresh, diffutils
                //notifyDataSetChanged()
            }

            container.setOnKeyListener { _, _, event ->
                return@setOnKeyListener when {
                    event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK -> {
                        onBackClick()
                        true
                    }
                    else -> (event.keyCode == KeyEvent.KEYCODE_DPAD_UP || event.keyCode == KeyEvent.KEYCODE_BACK)
                }
            }

        }

        fun onFocusChanged(hasFocus: Boolean) {
            ivIcon.setImageResource(if (hasFocus) focusedDrawable else unfocusedDrawable)
            tvTitle.setTextColor(ContextCompat.getColor(itemView.context, if (hasFocus) R.color.colorWhite else R.color.sirocco))
            ivIcon.animate()
                    .scaleY(if (hasFocus) 1.1f else 1f)
                    .scaleX(if (hasFocus) 1.1f else 1f)
                    .setDuration(100)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()
        }
    }
}