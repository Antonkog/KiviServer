package com.wezom.kiviremoteserver.service.aspect;

import android.content.Context;

import com.wezom.kiviremoteserver.interfaces.DriverValue;

import java.util.List;

public interface AvailableValues {
      int [] getIds();
      List<DriverValue> getAsDriverList(Context context);
}
