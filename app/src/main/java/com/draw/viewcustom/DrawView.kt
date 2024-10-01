package com.draw.viewcustom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.draw.R
import com.draw.model.DrawInfo
import com.draw.model.PaintPath
import com.draw.ultis.Common
import kotlin.math.abs
import kotlin.math.sqrt

class DrawView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mPath: Path? = null
    private var pathListHistory = mutableListOf<PaintPath?>()
    private var pathListUndo = mutableListOf<PaintPath?>()
    private var currentX: Float? = null
    private var currentY: Float? = null
    private var touchTolerance = 4f
    private var color = Color.BLACK
    private var penWidth = 10f
    private var mediaPlayer = MediaPlayer()
    private var isPlay = false
    private var isEraserMode = false

    private val savedMatrix = Matrix()
    private val zoomMatrix = Matrix()
    private val zoomMatrixInverse = Matrix()

    private var mode = NONE
    private var startPoint = PointF()
    private var oldDist = 1f

    private var isFirstDraw = true
    private var onDrawChange: OnDrawChange? = null

    var backgroundBitmap: Bitmap? = null

    companion object {
        private const val NONE = 0
        private const val ZOOM = 2
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DrawView,
            0, 0
        ).apply {
            try {
                color = getColor(R.styleable.DrawView_penColor, Color.BLACK)
                penWidth = getFloat(R.styleable.DrawView_penWidth, 10f)
                setBackgroundColor(Color.WHITE)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(zoomMatrix)

        canvas.drawColor(Color.WHITE)

        backgroundBitmap?.let {
            val paint = Paint().apply {
                alpha = (0.3f * 255).toInt()
            }
            val srcRect = Rect(0, 0, it.width, it.height)
            val destRect = Rect(0, 0, width, height)
            canvas.drawBitmap(it, srcRect, destRect, paint)
        }

        if (pathListHistory.size > 0) {
            Common.getBitmapWithoutWhite(pathListHistory, width, height)?.let {
                val paint = Paint().apply {
                    alpha = (1f * 255).toInt()
                }
                val srcRect = Rect(0, 0, it.width, it.height)
                val destRect = Rect(0, 0, width, height)
                canvas.drawBitmap(it, srcRect, destRect, paint)
            }
        }

        canvas.restore()
    }
    interface OnDrawChange {
        fun onDrawChange()
    }

    fun setOnDrawChange(listener: OnDrawChange) {
        this.onDrawChange = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                zoomMatrixInverse.reset()
                zoomMatrix.invert(zoomMatrixInverse)
                val transformedPoint = floatArrayOf(event.x, event.y)
                zoomMatrixInverse.mapPoints(transformedPoint)
                touchStart(transformedPoint[0], transformedPoint[1])
                invalidate()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(zoomMatrix)
                    midPoint(startPoint, event)
                    mode = ZOOM
                }
                if (event.pointerCount > 1) {
                    touchUp()
                    if (pathListHistory.size > 0) {
                        pathListHistory.removeAt(pathListHistory.size - 1)
                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                mode = NONE
                touchUp()
                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == ZOOM && event.pointerCount > 1) {
                    val newDist = spacing(event)
                    if (newDist > 10f) {
                        zoomMatrix.set(savedMatrix)

                        val endPoint = PointF()
                        midPoint(endPoint, event)

                        val scale = newDist / oldDist
                        zoomMatrix.postScale(scale, scale, startPoint.x, startPoint.y)
                        zoomMatrix.postTranslate(
                            endPoint.x - startPoint.x,
                            endPoint.y - startPoint.y
                        )
                    }
                } else {
                    zoomMatrixInverse.reset()
                    zoomMatrix.invert(zoomMatrixInverse)
                    val transformedPoint = floatArrayOf(event.x, event.y)
                    zoomMatrixInverse.mapPoints(transformedPoint)
                    touchMove(transformedPoint[0], transformedPoint[1])
                }
                invalidate()
            }
        }
        return true
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    private fun touchStart(xPos: Float, yPos: Float) {
        mPath = Path()
        val paintPath = PaintPath(mPath!!, getPaint())
        pathListHistory.add(paintPath)
        pathListUndo.clear()
        mPath!!.reset()
        mPath!!.moveTo(xPos, yPos)
        currentX = xPos
        currentY = yPos

        playSoundLooping()
    }

    private fun touchMove(xPos: Float, yPos: Float) {
        val dx = abs(xPos - currentX!!)
        val dy = abs(yPos - currentY!!)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            mPath!!.quadTo(currentX!!, currentY!!, (xPos + currentX!!) / 2, (yPos + currentY!!) / 2)
            currentX = xPos
            currentY = yPos
        }
    }

    private fun touchUp() {
        mPath!!.lineTo(currentX!!, currentY!!)
        onDrawChange?.onDrawChange()
        if (pathListHistory.size == 1 && isFirstDraw) {
            isFirstDraw = false
        }
        stopSound()
    }

    fun setUndo() {
        val size = pathListHistory.size
        if (size > 0) {
            pathListUndo.add(pathListHistory[size - 1])
            pathListHistory.removeAt(size - 1)
            onDrawChange?.onDrawChange()
            invalidate()
        }
    }

    fun setRedo() {
        val size = pathListUndo.size
        if (size > 0) {
            pathListHistory.add(pathListUndo[size - 1])
            pathListUndo.removeAt(size - 1)
            onDrawChange?.onDrawChange()
            invalidate()
        }
    }

    fun setPenColor(color: Int) {
        this.color = color
    }

    fun setPenWidth(width: Float) {
        penWidth = width
    }

    private fun getPaint(): Paint {
        val paint = Paint()
        if (isEraserMode) {
            paint.color = -1
        } else {
            paint.color = color
        }
        paint.strokeWidth = penWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true
        return paint
    }

    private fun flipPath(path: Path, canvasWidth: Float): Path {
        val matrix = Matrix()
        matrix.setScale(-1f, 1f, canvasWidth / 2, 0f)
        val mirroredPath = Path(path)
        mirroredPath.transform(matrix)
        return mirroredPath
    }

    fun flip() {
        for (paintPath in pathListHistory) {
            if (paintPath != null) {
                paintPath.path = flipPath(paintPath.path, width.toFloat())
            }
        }
        invalidate()
    }

    fun setEraserMode(isEraserMode: Boolean) {
        this.isEraserMode = isEraserMode
    }
    fun getEraserMode():Boolean {
        return isEraserMode
    }

    fun clearDraw() {
        pathListHistory.add(null)
        onDrawChange?.onDrawChange()
        invalidate()
    }

    fun getHistoryPaint(): MutableList<PaintPath?> {
        return pathListHistory
    }

    fun getHistoryUndo(): MutableList<PaintPath?> {
        return pathListUndo
    }

    fun setHistory(drawInfo: DrawInfo) {
        pathListHistory.clear()
        pathListHistory.addAll(drawInfo.listHistory)
        pathListUndo.clear()
        pathListUndo.addAll(drawInfo.listUndo)
        onDrawChange?.onDrawChange()
        invalidate()
    }

    fun createNewDraw() {
        pathListHistory.clear()
        pathListUndo.clear()
        onDrawChange?.onDrawChange()
        invalidate()
        zoomMatrix.reset()
        savedMatrix.reset()
        zoomMatrixInverse.reset()
        oldDist = 1f
    }

    fun resetPos() {
        zoomMatrix.reset()
        savedMatrix.reset()
        zoomMatrixInverse.reset()
        oldDist = 1f
    }

    private fun playSoundLooping() {
        stopSound()
        if (context == null) {
            return
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.draw_sound)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        isPlay = true
    }

    private fun stopSound() {
        if (isPlay) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
            }
        }
        isPlay = false
    }
}
