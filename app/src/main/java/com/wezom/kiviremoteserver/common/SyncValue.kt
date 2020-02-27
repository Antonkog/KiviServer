package com.wezom.kiviremoteserver.common

import android.content.Context

interface SyncValue {
    fun getSyncFrequency(): Long
    fun init(context : Context)
}