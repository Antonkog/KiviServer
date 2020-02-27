package com.wezom.kiviremoteserver.net.server.model

import android.content.Context

//
// Created by Antonio on 2019-12-17.
// email: akogan777@gmail.com
//

data class LongTapAction(val actionId: String,
                         val name: Int,
                         val imgResId: Int,
                         val imgUrl: String?){
    fun getStringName(context :  Context) : String {
        return context.getString(name)
    }

    override fun toString(): String {
        return "LongTapAction(actionId='$actionId', name=$name, imgResId=$imgResId, imgUrl=$imgUrl)"
    }
}
