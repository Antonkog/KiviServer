package com.wezom.kiviremoteserver.service.aspect;

import android.content.Context;
import android.support.annotation.StringRes;

import com.wezom.kiviremoteserver.interfaces.DriverValue;

import java.util.List;

public interface AvailableValues {
      int [] getIds();
      List<DriverValue> getAsDriverList(Context context);

}
