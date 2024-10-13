package com.draw.viewcustom

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.draw.R
import com.draw.callback.ICallBackCheck
import kotlin.math.atan2

class StickerMemeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private lateinit var stickerImageView: ImageView
    private lateinit var borderView: RelativeLayout
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
        // Tạo view chứa viền và các nút điều khiển
        borderView = RelativeLayout(context).apply {
            background = createBorderDrawable()
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            isVisible = false
        }
        addView(borderView)

        // Tạo stickerImageView để chứa hình ảnh sticker
        stickerImageView = ImageView(context).apply {
            setImageResource(R.drawable.anhtest)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
        }
        addView(stickerImageView)

        // Tạo các nút chức năng
        setupControlButtons()

        updateBorderSize() // Cập nhật kích thước viền ban đầu
    }

    private fun setupControlButtons() {
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
    }

    private fun createBorderDrawable(): ShapeDrawable {
        return ShapeDrawable(RectShape()).apply {
            paint.color = Color.DKGRAY
            paint.strokeWidth = 5f
            paint.style = Paint.Style.STROKE
        }
    }

    private fun updateBorderSize() {
        val imageWidth = stickerImageView.width
        val imageHeight = stickerImageView.height
        val padding = 70f

        val newWidth = (imageWidth * stickerImageView.scaleX + padding).toInt()
        val newHeight = (imageHeight * stickerImageView.scaleY + padding).toInt()

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
        stickerImageView.scaleX *= -1
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
                showBorder()
            }

            MotionEvent.ACTION_MOVE -> {
                if (isResizing) {
                    val deltaX = event.rawX - lastX
                    val deltaY = event.rawY - lastY
                    val newScaleX = stickerImageView.scaleX + deltaX / 200
                    val newScaleY = stickerImageView.scaleY + deltaY / 200

                    if (newScaleX > 0.1f && newScaleY > 0.1f) {
                        stickerImageView.scaleX = newScaleX
                        stickerImageView.scaleY = newScaleY
                        updateBorderSize()
                    }

                    lastX = event.rawX
                    lastY = event.rawY
                }
            }

            MotionEvent.ACTION_UP -> {
                isResizing = false
                hideBorderAfterDelay()
            }
        }
        return true
    }

    private fun handleRotate(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialRotation = getAngle(event.rawX, event.rawY)
                midPoint[0] = stickerImageView.x + (stickerImageView.width * stickerImageView.scaleX) / 2
                midPoint[1] = stickerImageView.y + (stickerImageView.height * stickerImageView.scaleY) / 2
                showBorder()
            }

            MotionEvent.ACTION_MOVE -> {
                val newAngle = getAngle(event.rawX, event.rawY)
                val deltaAngle = newAngle - initialRotation
                currentRotation += deltaAngle
                stickerImageView.rotation = currentRotation
                borderView.rotation = currentRotation
                initialRotation = newAngle
                updateBorderSize()
            }

            MotionEvent.ACTION_UP -> {
                hideBorderAfterDelay()
            }
        }
        return true
    }

    private fun getAngle(x: Float, y: Float): Float {
        val dx = x - midPoint[0]
        val dy = y - midPoint[1]
        return (atan2(dy, dx) * (180 / Math.PI)).toFloat()
    }

    fun setICallBackCheck(callback: ICallBackCheck) {
        isHandleCheck = callback
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val touchX = it.x
            val touchY = it.y

            val stickerRect = stickerImageView.run {
                val rect = android.graphics.Rect()
                getHitRect(rect)
                rect
            }

            if (!stickerRect.contains(touchX.toInt(), touchY.toInt())) {
                return false
            }

            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMoving = true
                    lastX = it.rawX
                    lastY = it.rawY
                    showBorder()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isMoving) {
                        val deltaX = it.rawX - lastX
                        val deltaY = it.rawY - lastY

                        this.x += deltaX
                        this.y += deltaY

                        lastX = it.rawX
                        lastY = it.rawY

                        updateBorderSize()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    isMoving = false
                    hideBorderAfterDelay()
                }
            }
        }
        return true
    }

    private fun showBorder() {
        borderView.isVisible = true
        hideBorderHandler.removeCallbacks(hideBorderRunnable)
    }

    private fun hideBorderAfterDelay() {
        hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
    }

    fun setImageResource(resId: Int) {
        if (resId != 0) { // Kiểm tra ID hình ảnh hợp lệ
            stickerImageView.setImageResource(resId)
            updateButtonPositions()
            updateBorderSize() // Cập nhật viền ngay lập tức
        }
    }
}
