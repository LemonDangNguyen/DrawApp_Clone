package com.draw.viewcustom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.draw.R
import com.draw.callback.ICallBackCheck
import kotlin.math.atan2
import kotlin.math.hypot

class StickerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private var text: String = "Sticker"
    private lateinit var stickerTextView: TextView
    lateinit var borderView: RelativeLayout
    private lateinit var deleteButton: AppCompatImageView
    private lateinit var flipButton: AppCompatImageView
    private lateinit var transformButton: AppCompatImageView
    private lateinit var rotateButton: AppCompatImageView

    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var isResizing = false
    private var isMoving = false
    private var initialRotation: Float = 0f
    private var midPoint = FloatArray(2)
    private var currentRotation: Float = 0f

    private val hideBorderHandler = Handler(Looper.getMainLooper())
    private val hideBorderRunnable = Runnable { borderView.isVisible = false }

    private var isHandleCheck: ICallBackCheck? = null

    init {
        setupView()
    }

    private fun setupView() {
        borderView = RelativeLayout(context).apply {
            background = createBorderDrawable()
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    addRule(CENTER_IN_PARENT, TRUE)
                }
            isVisible = false
        }
        addView(borderView)

        initStickerView()
    }

    private fun createBorderDrawable(): ShapeDrawable {
        return ShapeDrawable(RectShape()).apply {
            paint.color = Color.DKGRAY
            paint.strokeWidth = 5f
            paint.style = Paint.Style.STROKE
        }
    }

    private fun initStickerView() {
        stickerTextView = TextView(context).apply {
            text = this@StickerTextView.text
            textSize = 24f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    addRule(CENTER_IN_PARENT, TRUE)
                }
        }
        addView(stickerTextView)

        deleteButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_delete)
            layoutParams = LayoutParams(30, 30).apply {
                addRule(ALIGN_PARENT_TOP, TRUE)
                addRule(ALIGN_PARENT_END, TRUE)
            }
            setOnClickListener { removeSticker() }
        }
        borderView.addView(deleteButton)

        flipButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_flip)
            layoutParams = LayoutParams(30, 30).apply {
                addRule(ALIGN_PARENT_TOP, TRUE)
                addRule(CENTER_HORIZONTAL, TRUE)
            }
            setOnClickListener { flipSticker() }
        }
        borderView.addView(flipButton)

        transformButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_resize)
            layoutParams = LayoutParams(100, 100).apply {
                addRule(ALIGN_PARENT_BOTTOM, TRUE)
                addRule(ALIGN_PARENT_END, TRUE)
            }
            setOnTouchListener { _, event -> handleTransform(event) }
        }
        borderView.addView(transformButton)

        rotateButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_rotate)
            layoutParams = LayoutParams(30, 30).apply {
                addRule(ALIGN_PARENT_BOTTOM, TRUE)
                addRule(ALIGN_PARENT_START, TRUE)
            }
            setOnTouchListener { _, event -> handleRotate(event) }
        }
        borderView.addView(rotateButton)

        updateBorderSize()
    }

    fun updateText(newText: String) {
        text = newText
        stickerTextView.text = newText
        updateBorderSize()
    }

    private fun updateBorderSize() {
        val textWidth = stickerTextView.paint.measureText(stickerTextView.text.toString())
        val textHeight = stickerTextView.paint.fontMetrics.run { bottom - top }
        val padding = 50f

        val newWidth = (textWidth * stickerTextView.scaleX + padding).toInt()
        val newHeight = (textHeight * stickerTextView.scaleY + padding).toInt()

        borderView.layoutParams = LayoutParams(newWidth, newHeight).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }
        borderView.requestLayout()

        updateButtonPositions()
    }
    private fun updateButtonPositions() {
        val buttonSize = 50
        val borderPadding = -3

        deleteButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, borderPadding, borderPadding, borderPadding)
        }
        flipButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(CENTER_HORIZONTAL, TRUE)
            setMargins(0, borderPadding, 0, borderPadding)
        }
        transformButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_BOTTOM, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, 0, borderPadding, borderPadding)
        }
        rotateButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_BOTTOM, TRUE)
            addRule(ALIGN_PARENT_START, TRUE)
            setMargins(borderPadding, 0, borderPadding, borderPadding)
        }
    }
    private fun flipSticker() {
        stickerTextView.scaleX *= -1

    }
    private fun removeSticker() {
        this.visibility = View.GONE
    }
    private fun handleTransform(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isResizing = true
                lastX = event.rawX
                lastY = event.rawY
                borderView.isVisible = true
                hideBorderHandler.removeCallbacks(hideBorderRunnable)
            }
            MotionEvent.ACTION_MOVE -> {
                if (isResizing) {
                    val deltaX = event.rawX - lastX
                    val deltaY = event.rawY - lastY
                    val newScaleX = stickerTextView.scaleX + deltaX / 200
                    val newScaleY = stickerTextView.scaleY + deltaY / 200

                    if (newScaleX > 0.1f && newScaleY > 0.1f) {
                        stickerTextView.scaleX = newScaleX
                        stickerTextView.scaleY = newScaleY
                        updateBorderSize()
                    }
                    lastX = event.rawX
                    lastY = event.rawY
                }
            }
            MotionEvent.ACTION_UP -> {
                isResizing = false
                hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
            }
        }
        return true
    }
    private fun handleRotate(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialRotation = getAngle(event.rawX, event.rawY)
                midPoint[0] =
                    stickerTextView.x + (stickerTextView.width * stickerTextView.scaleX) / 2
                midPoint[1] =
                    stickerTextView.y + (stickerTextView.height * stickerTextView.scaleY) / 2
                borderView.isVisible = true
                hideBorderHandler.removeCallbacks(hideBorderRunnable)
            }

            MotionEvent.ACTION_MOVE -> {
                val newAngle = getAngle(event.rawX, event.rawY)
                val deltaAngle = newAngle - initialRotation
                currentRotation += deltaAngle
                stickerTextView.rotation = currentRotation // Cập nhật xoay
                borderView.rotation = currentRotation // Cập nhật xoay cho khung viền
                initialRotation = newAngle
                updateBorderSize() // Cập nhật lại khung viền
            }

            MotionEvent.ACTION_UP -> {
                hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
            }
        }
        return true
    }
    private fun getAngle(x: Float, y: Float): Float {
        val dx = x - midPoint[0]
        val dy = y - midPoint[1]
        return (atan2(dy, dx) * (180 / Math.PI)).toFloat()
    }
    fun setTextColor(color: Int) {
        stickerTextView.setTextColor(color)

    }
    fun setICallBackCheck(callback: ICallBackCheck) {
        isHandleCheck = callback
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            // Lấy tọa độ chạm
            val touchX = it.x
            val touchY = it.y

            // Lấy vùng (hit rect) của stickerTextView (vùng chứa nội dung chính)
            val stickerRect = stickerTextView.run {
                val rect = android.graphics.Rect()
                getHitRect(rect)
                rect
            }
            // Kiểm tra xem điểm chạm có nằm trong vùng stickerTextView hay không
            if (!stickerRect.contains(touchX.toInt(), touchY.toInt())) {
                // Nếu không chạm vào stickerTextView thì không di chuyển sticker
                return false
            }
            // Nếu chạm vào stickerTextView thì xử lý di chuyển
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isResizing) {
                        isMoving = true
                        lastX = it.rawX
                        lastY = it.rawY
                        borderView.isVisible = true
                        hideBorderHandler.removeCallbacks(hideBorderRunnable)
                    } else {

                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isMoving) {
                        val deltaX = it.rawX - lastX
                        val deltaY = it.rawY - lastY
                        this.x += deltaX
                        this.y += deltaY

                        lastX = it.rawX
                        lastY = it.rawY
                    } else {

                    }
                }
                MotionEvent.ACTION_UP -> {
                    isMoving = false
                    hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
                }
                else -> {}
            }
        }
        return true
    }
    fun getStickerBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    interface OnStickerDeleteListener {
        fun onStickerDelete(sticker: View)
    }
}