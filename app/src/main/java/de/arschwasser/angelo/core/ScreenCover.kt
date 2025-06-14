package de.arschwasser.angelo.core

import android.content.Context
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.toColorInt

object ScreenCover {
    fun show(context: Context): View? {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        val v = View(context)
        v.setBackgroundColor("#F3B5B1".toColorInt())
        try {
            windowManager.addView(v, params)
            return v // return the view so caller can remove it later
        } catch (_: Exception) {
            // Overlay permission not granted
            return null
        }
    }

    fun hide(context: Context, view: View?) {
        if (view == null) return
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            windowManager.removeView(view)
        } catch (_: Exception) {
            // Already removed or not attached
        }
    }
}
