package com.wezom.kiviremoteserver.interfaces


data class DriverValue(val enumValueName: String,
                       val currentName: String,
                       val valPrimaryKey: String,
                       val intCondition: Int,
                       val active: Boolean)