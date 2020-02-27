package com.wezom.kiviremoteserver.common

import android.util.LruCache


class KiviCache : LruCache<String, String>(150)