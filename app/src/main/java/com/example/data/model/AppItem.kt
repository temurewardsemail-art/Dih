package com.example.data.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val activityName: String? = null,
    val label: String,
    val iconDrawable: Drawable? = null,
    val bannerDrawable: Drawable? = null,
    val isPriority: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isSystemApp: Boolean = false,
    val orderIndex: Int = 0,
    val accentColor: Long? = null,
    val fallbackTag: String? = null
)
