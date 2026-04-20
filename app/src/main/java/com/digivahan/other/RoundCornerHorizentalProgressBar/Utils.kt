package com.digivahan.other.RoundCornerHorizentalProgressBar

import android.content.Context
import android.util.TypedValue

object Utils {
    @JvmStatic
    fun dp2px(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
}