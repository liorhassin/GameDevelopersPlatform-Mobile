package com.example.gamedevelopersplatform.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.widget.ListPopupWindow
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner

class RightAlignedSpinner(context: Context, attrs: AttributeSet?) : AppCompatSpinner(context, attrs) {

    @SuppressLint("DiscouragedPrivateApi", "ClickableViewAccessibility")
    override fun performClick(): Boolean {

        val screenSize = Point()
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
        display.getSize(screenSize)

        val spinnerPosition = IntArray(2)
        getLocationOnScreen(spinnerPosition)

        val xOffset = screenSize.x - spinnerPosition[0] - width

        try {
            val popupField = Spinner::class.java.getDeclaredField("mPopup")
            popupField.isAccessible = true
            val popupWindow = popupField.get(this) as ListPopupWindow

            popupWindow.horizontalOffset = xOffset
            popupWindow.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }
}
