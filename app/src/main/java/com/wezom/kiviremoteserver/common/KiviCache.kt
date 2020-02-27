package com.wezom.kiviremoteserver.common

import android.support.v4.util.LruCache

class KiviCache : LruCache<String, String>(150)