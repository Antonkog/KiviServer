package com.wezom.kiviremoteserver.service.aspect.values;

import androidx.annotation.StringRes;

public interface TextTypedValues {
    @StringRes  int getStringResourceID();
    int getID();
}
