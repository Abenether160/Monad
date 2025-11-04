package com.example.burglaralert

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AlertOverlay(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayLayout: LinearLayout? = null

    // Show overlay for up to 1 minute (60000ms)
    fun showOverlay() {
        if (overlayLayout != null) return // Already showing

        overlayLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.RED)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            // Alert text
            val alertText = TextView(context).apply {
                text = "⚠ ALERT ⚠"
                setTextColor(Color.WHITE)
                textSize = 48f
                gravity = Gravity.CENTER
            }
            addView(alertText)

            // Turn Off button
            val btnTurnOff = Button(context).apply {
                text = "Turn Off"
                setOnClickListener { removeOverlay() }
            }
            addView(btnTurnOff)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayLayout, params)

        // Auto remove after 1 minute
        overlayLayout?.postDelayed({ removeOverlay() }, 60000)
    }

    fun removeOverlay() {
        overlayLayout?.let {
            windowManager.removeView(it)
        }
        overlayLayout = null
    }
}