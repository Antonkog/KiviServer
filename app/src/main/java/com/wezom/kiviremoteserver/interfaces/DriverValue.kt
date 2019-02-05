package com.wezom.kiviremoteserver.interfaces


data class DriverValue(var enumValueName: String,
                       var currentName: String,
                       val valPrimaryKey: String,
                       val intCondition: Int,
                       val active: Boolean)