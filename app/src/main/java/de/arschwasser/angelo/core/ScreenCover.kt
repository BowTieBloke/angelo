package de.arschwasser.angelo.core

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager

object ScreenCover {
    private var view: View? = null
    private var wm: WindowManager? = null

    fun show(context: Context) {
        if (view != null) return
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        val v = View(context)
        v.setBackgroundColor(android.graphics.Color.BLACK)
        try {
            windowManager.addView(v, params)
            view = v
            wm = windowManager
        } catch (_: Exception) {
            // Overlay permission not granted
        }
    }

    fun hide() {
        view?.let { v ->
            wm?.removeView(v)
        }
        view = null
        wm = null
    }
}
