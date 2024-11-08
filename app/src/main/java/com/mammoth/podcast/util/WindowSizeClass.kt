package com.mammoth.podcast.util

import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * Returns true if the width or height size classes are compact.
 */
val WindowSizeClass.isCompact: Boolean
    get() = windowWidthSizeClass == WindowWidthSizeClass.COMPACT ||
        windowHeightSizeClass == WindowHeightSizeClass.COMPACT
