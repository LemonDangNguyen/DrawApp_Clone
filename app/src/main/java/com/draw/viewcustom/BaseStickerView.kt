package com.draw.viewcustom

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.draw.R
import com.draw.callback.ICallBackCheck
import kotlin.math.atan2
import kotlin.math.hypot

// Base class chứa các chức năng chung của StickerView
abstract class BaseStickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    protected lateinit var borderView: RelativeLayout
    protected lateinit var deleteButton: AppCompatImageView
    protected lateinit var flipButton: AppCompatImageView
    protected lateinit var transformButton: AppCompatImageView

    protected var isTouchingSticker = false
    protected val hideBorderHandler = Handler(Looper.getMainLooper())
    protected val hideBorderRunnable = Runnable { borderView.isVisible = false }

    protected var initialDistance: Float = 0f
    protected var currentScale: Float = 1f
    protected var initialRotation: Float = 0f
    protected var lastX: Float = 0f
    protected var lastY: Float = 0f
    protected var isMoving: Boolean = false

    protected var isHandleCheck: ICallBackCheck? = null

    init {
        setupBorderView()
        initControlButtons()
    }

    // Hàm này cần được lớp con triển khai để tính kích thước nội dung cụ thể
    protected abstract fun calculateContentSize(): Pair<Float, Float>

    // Cập nhật kích thước khung viền cho các sticker view
    protected fun updateBorderSize() {
        val (contentWidth, contentHeight) = calculateContentSize()
        val padding = 50f

        val newWidth = (contentWidth * scaleX + padding).toInt()
        val newHeight = (contentHeight * scaleY + padding).toInt()

        borderView.layoutParams = LayoutParams(newWidth, newHeight).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }

        // Cập nhật vị trí của các nút điều chỉnh
        updateButtonPositions()
        applyTransformToBorder() // Cập nhật các thuộc tính scale, rotation cho viền
    }

    private fun setupBorderView() {
        // Khởi tạo borderView với viền
        borderView = RelativeLayout(context).apply {
            background = createBorderDrawable()
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            isVisible = false
        }
        addView(borderView)
    }

    private fun createBorderDrawable(): ShapeDrawable {
        // Tạo viền xung quanh sticker
        return ShapeDrawable(RectShape()).apply {
            paint.color = Color.DKGRAY
            paint.strokeWidth = 5f // Độ dày viền
            paint.style = Paint.Style.STROKE
        }
    }

    // Khởi tạo các nút điều khiển
    private fun initControlButtons() {
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
    }

    // Cập nhật vị trí các nút điều khiển
    protected fun updateButtonPositions() {
        val buttonSize = 50
        val borderPadding = -3 // Khoảng cách âm từ viền

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
    }

    // Xử lý xóa sticker
    protected fun removeSticker() {
        this.visibility = View.GONE
    }

    // Xử lý lật sticker
    protected fun flipSticker() {
        // Lật sticker mà không thay đổi vị trí các nút
        this.scaleX *= -1
    }

    // Xử lý biến đổi (scale và rotate) sticker
    protected fun handleTransform(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchWithinTransformButton(event)) {
                    lastX = event.rawX
                    lastY = event.rawY
                    initialDistance = getDistance(event)
                    initialRotation = getAngle(event.rawX, event.rawY)
                    borderView.isVisible = true
                    hideBorderHandler.removeCallbacks(hideBorderRunnable)
                } else {
                    isMoving = true
                    lastX = event.rawX
                    lastY = event.rawY
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMoving) {
                    // Di chuyển sticker
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY
                    x += dx
                    y += dy
                    lastX = event.rawX
                    lastY = event.rawY
                } else {
                    // Xử lý scale và rotate nếu không phải di chuyển
                    val newDistance = getDistance(event)
                    val scaleFactor = newDistance / initialDistance
                    if (scaleFactor > 0.5f && scaleFactor < 2f) {
                        currentScale = scaleFactor
                        scaleX = currentScale
                        scaleY = currentScale
                    }

                    val newAngle = getAngle(event.rawX, event.rawY)
                    val rotationDelta = newAngle - initialRotation
                    rotation += rotationDelta
                    initialRotation = newAngle
                }
            }
            MotionEvent.ACTION_UP -> {
                isMoving = false
                hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
            }
        }
        return true
    }

    // Tính toán khoảng cách giữa 2 điểm chạm
    private fun getDistance(event: MotionEvent): Float {
        val dx = event.rawX - x - (width * scaleX / 2)
        val dy = event.rawY - y - (height * scaleY / 2)
        return hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }

    // Tính toán góc giữa 2 điểm chạm
    private fun getAngle(x: Float, y: Float): Float {
        val dx = x - this.x
        val dy = y - this.y
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    // Kiểm tra sự kiện chạm trong nút transformButton
    protected fun isTouchWithinTransformButton(event: MotionEvent): Boolean {
        val buttonCenterX = transformButton.x + transformButton.width / 2
        val buttonCenterY = transformButton.y + transformButton.height / 2
        val dx = event.x - buttonCenterX
        val dy = event.y - buttonCenterY
        return hypot(dx.toDouble(), dy.toDouble()).toFloat() <= 15f
    }

    // Kiểm tra sự kiện chạm trong sticker
    protected fun isTouchWithinSticker(event: MotionEvent): Boolean {
        val stickerRect = Rect()
        val borderRect = Rect()
        getHitRect(stickerRect)
        borderView.getHitRect(borderRect)
        return stickerRect.contains(event.x.toInt(), event.y.toInt()) || borderRect.contains(event.x.toInt(), event.y.toInt())
    }

    // Chức năng ẩn viền sau một thời gian nhất định
    protected fun hideBorderWithDelay() {
        hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
    }

    // Áp dụng các biến đổi như scale, rotation cho viền
    protected fun applyTransformToBorder() {
        borderView.pivotX = width / 2f
        borderView.pivotY = height / 2f
        borderView.rotation = rotation
        borderView.scaleX = scaleX
        borderView.scaleY = scaleY
    }

    // Thay đổi phương thức onTouchEvent để không ẩn sticker
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Hiển thị viền khi bắt đầu chạm vào sticker
                borderView.isVisible = true
                hideBorderHandler.removeCallbacks(hideBorderRunnable) // Ngừng hẹn giờ ẩn viền
            }
            MotionEvent.ACTION_UP -> {
                // Ẩn viền sau khi chạm xong
                hideBorderWithDelay()
            }
        }
        return super.onTouchEvent(event) // Kế thừa xử lý chạm từ BaseStickerView
    }
}