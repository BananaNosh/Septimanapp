package com.nobodysapps.septimanapp.view

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Pads [this] view by the requested system-bar insets, preserving the padding already
 * declared in XML. Needed since targetSdk 35, where Android 15 forces edge-to-edge and
 * the status/navigation bars no longer reserve space for app content. On pre-15 devices
 * the insets are zero, so this is a no-op there.
 */
fun View.applySystemBarInsetsAsPadding(
    top: Boolean = false,
    bottom: Boolean = true,
    horizontal: Boolean = true
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialLeft + if (horizontal) bars.left else 0,
            top = initialTop + if (top) bars.top else 0,
            right = initialRight + if (horizontal) bars.right else 0,
            bottom = initialBottom + if (bottom) bars.bottom else 0
        )
        windowInsets
    }
    ViewCompat.requestApplyInsets(this)
}
