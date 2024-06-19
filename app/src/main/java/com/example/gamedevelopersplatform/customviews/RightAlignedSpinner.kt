package com.example.gamedevelopersplatform.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.widget.ListPopupWindow
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner

class RightAlignedSpinner(context: Context, attrs: AttributeSet?) : AppCompatSpinner(context, attrs) {

    @SuppressLint("DiscouragedPrivateApi")
    override fun performClick(): Boolean {
        val screenSize = Point()
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
        display.getSize(screenSize)

        val spinnerPosition = IntArray(2)
        getLocationOnScreen(spinnerPosition)

        // Calculate the xOffset to align the dropdown to the right
        val xOffset = screenSize.x - spinnerPosition[0] + width

        // Adjust the popup window offset before showing it
        try {
            val popupField = Spinner::class.java.getDeclaredField("mPopup")
            popupField.isAccessible = true
            val popupWindow = popupField.get(this) as ListPopupWindow
            popupWindow.horizontalOffset = xOffset
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.performClick()
    }
}
