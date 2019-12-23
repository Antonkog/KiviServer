package com.wezom.kiviremoteserver.service.aspect.aspect_v2

import android.graphics.Color
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.PageIndicatorView
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.CardData

class CardsMainAdapter(var items: List<CardData>, val action1: (data: CardData) -> Unit, val action2: () -> Unit) : RecyclerView.Adapter<CardsMainAdapter.CardMainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardMainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        val holder = CardMainViewHolder(view)
        holder.card.setOnFocusChangeListener { _, hasFocus -> holder.scaleCard(hasFocus) }
        return holder
    }

    override fun onBindViewHolder(holder: CardMainViewHolder, position: Int) {
        if (position == 0) { holder.card.requestFocus() }
        holder.bindData(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CardMainViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.card)
        private val actionsView: View = view.findViewById(R.id.actions_view)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val mainText: TextView = view.findViewById(R.id.mainText)
        private val secondText: TextView = view.findViewById(R.id.secondText)
        private val progressIndicator: PageIndicatorView = view.findViewById(R.id.progressIndicator)
        private var indicatorCurrentIndex = 0
        private var showActionsView = false

        fun bindData(data: CardData) {
            mainText.setText(data.title)
            imageView.setImageResource(data.image)
            showActionsView = data.values.isNotEmpty()
            if (showActionsView) {
                card.setOnKeyListener { _, _, event ->
                    return@setOnKeyListener when {
                        event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_DPAD_UP -> { action1(data); true; }
                        event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK -> { action2(); true; }
                        else -> event.keyCode == KeyEvent.KEYCODE_DPAD_UP || event.keyCode == KeyEvent.KEYCODE_BACK
                    }
                }
            }

            actionsView.visibility = if (card.isFocused && showActionsView) View.VISIBLE else View.INVISIBLE

            if (data.values.isNotEmpty()) {
                secondText.setText(data.values[0].stringRes)
                progressIndicator.visibility = View.VISIBLE
                progressIndicator.count = data.values.size
                progressIndicator.selection = 0

                card.setOnClickListener {
                    indicatorCurrentIndex++
                    progressIndicator.selection = indicatorCurrentIndex % data.values.size
                    secondText.setText(data.values[indicatorCurrentIndex % data.values.size].stringRes)
                }
            } else {
                secondText.setText(data.subTitle)
                card.setOnClickListener(null)
                progressIndicator.visibility = View.GONE
            }
        }

        fun scaleCard(hasFocus: Boolean) {
            val color = Color.parseColor(if (hasFocus) "#444444" else "#333333")
            card.setCardBackgroundColor(color)
            card.animate()
                    .scaleY(if (hasFocus) 1.1f else 1f)
                    .scaleX(if (hasFocus) 1.1f else 1f)
                    .setDuration(100)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()

            actionsView.visibility = if (hasFocus && showActionsView) View.VISIBLE else View.INVISIBLE
            itemView.translationZ = (if (hasFocus) 5 else 0).toFloat()
        }
    }

}