@file:Suppress("DEPRECATION")

package com.draw.ultis

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import com.draw.model.DrawInfo
import com.draw.model.PaintPath

object Common {
    const val KEY_PROJECT_NAME = "KEY_PROJECT_NAME"
    const val KEY_ANIM_GUIDE = "KEY_ANIM_GUIDE"
    const val KEY_POSITION_ANIM_GUIDE = "KEY_POSITION_ANIM_GUIDE"
    const val KEY_IS_GUIDE = "KEY_IS_GUIDE"
    const val KEY_FROM_MY_CREATORS = "KEY_FROM_MY_CREATORS"

    val listDrawInfo = mutableListOf<DrawInfo>()
    var time_frame = 0L

    var back_next = false


    fun getBitmapFromPathListHistory(
        list: MutableList<PaintPath?>,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        for (path in list) {
            if (path != null) {
                if (path.paint.color == -1) {
                    Log.d("TAG123", "getBitmapFromPathListHistory: asd")
                    path.paint.color = Color.WHITE
                    path.paint.xfermode = null
                }
                canvas.drawPath(path.path, path.paint)


            } else {
                canvas.drawColor(Color.WHITE)
            }
        }
        return bitmap
    }

    fun getBitmapWithoutWhite(list: MutableList<PaintPath?>, width: Int, height: Int): Bitmap? {
        if (width <= 0 && height <= 0) {
            return null
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        for (path in list) {
            if (path != null) {
                if (path.paint.color == -1) {
                    path.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                }else{
                    path.paint.xfermode = null
                }
                canvas.drawPath(path.path, path.paint)
            } else {
                val clearPaint = Paint().apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), clearPaint)
            }
        }
        return bitmap
    }
}