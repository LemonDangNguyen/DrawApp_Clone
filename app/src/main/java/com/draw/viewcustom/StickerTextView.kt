package com.draw.viewcustom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.GestureDetector
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.draw.R
import com.draw.activity.DrawActivity
import com.draw.callback.ICallBackCheck
import kotlin.math.atan2
import kotlin.math.hypot

class StickerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private var text: String = "Sticker"
    private lateinit var stickerTextView: TextView
    private lateinit var borderView: RelativeLayout
    private lateinit var deleteButton: AppCompatImageView
    private lateinit var flipButton: AppCompatImageView
    private lateinit var transformButton: AppCompatImageView

    private var isTouchingSticker = false
    private val hideBorderHandler = Handler(Looper.getMainLooper())
    private val hideBorderRunnable = Runnable { borderView.isVisible = false }


    private var initialDistance: Float = 0f
    private var currentScale: Float = 1f
    private var initialRotation: Float = 0f
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private var isHandleCheck: ICallBackCheck? = null

    init {
        setupGestureDetector()
        // Initialize the border view
        borderView = RelativeLayout(context).apply {
            background = createBorderDrawable()
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
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
            paint.strokeWidth = 5f //  độ dày đường viền
            paint.style = Paint.Style.STROKE
        }
    }

    private fun initStickerView() {
        stickerTextView = TextView(context).apply {
            text = this@StickerTextView.text // Sử dụng giá trị của thuộc tính text
            textSize = 24f // Kích thước chữ
            setTextColor(Color.BLACK) // Màu chữ
            setBackgroundColor(Color.TRANSPARENT) // Nền trong suốt
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            gravity = Gravity.CENTER
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
            layoutParams = LayoutParams(100, 100).apply { // Tăng kích thước
                addRule(ALIGN_PARENT_BOTTOM, TRUE)
                addRule(ALIGN_PARENT_END, TRUE)
            }
            setOnTouchListener { _, event -> handleTransform(event) }
        }
        borderView.addView(transformButton)
    }

    fun updateText(newText: String) {
        text = newText
        stickerTextView.text = newText
        updateBorderSize() // Cập nhật kích thước khung viền khi thay đổi nội dung
    }
    fun setTextColor(color: Int) {
        stickerTextView.setTextColor(color) // Cập nhật màu chữ của TextView
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

        // Cập nhật vị trí của các nút điều chỉnh
        updateButtonPositions()
        applyTransformToBorder()
    }

    private fun updateButtonPositions() {
        val buttonSize = 50
        val borderPadding = -3 // Khoảng cách âm từ viền

        deleteButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, borderPadding, borderPadding, borderPadding) // Đặt margin âm để nút nằm trên viền
        }

        flipButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(CENTER_HORIZONTAL, TRUE)
            setMargins(0, borderPadding, 0, borderPadding) // Đặt margin âm để nút nằm trên viền
        }

        transformButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_BOTTOM, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, 0, borderPadding, borderPadding) // Đặt margin âm để nút nằm trên viền
        }
    }

    private fun applyTransformToBorder() {
        borderView.pivotX = stickerTextView.width * stickerTextView.scaleX / 2
        borderView.pivotY = stickerTextView.height * stickerTextView.scaleY / 2
        borderView.rotation = stickerTextView.rotation
        borderView.scaleX = stickerTextView.scaleX
        borderView.scaleY = stickerTextView.scaleY
    }

    private fun flipSticker() {
        stickerTextView.scaleX *= -1

    }

    private fun removeSticker() {
        this.visibility = View.GONE
        //(parent as? ViewGroup)?.removeView(this)
    }

    private fun handleTransform(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Khởi tạo các biến cần thiết
                lastX = event.rawX
                lastY = event.rawY
                initialDistance = getDistance(event)
                initialRotation = getAngle(event.rawX, event.rawY)
                borderView.isVisible = true
                hideBorderHandler.removeCallbacks(hideBorderRunnable)
            }
            MotionEvent.ACTION_MOVE -> {
                // Tính toán khoảng cách mới
                val newDistance = getDistance(event)
                val scaleFactor = newDistance / initialDistance
                if (scaleFactor > 0.5f && scaleFactor < 2f) {
                    currentScale = scaleFactor
                    stickerTextView.scaleX = currentScale
                    stickerTextView.scaleY = currentScale
                    updateBorderSize() // Cập nhật kích thước khung viền
                }

                // Tính toán góc mới
                val newAngle = getAngle(event.rawX, event.rawY)
                val rotationDelta = newAngle - initialRotation
                stickerTextView.rotation += rotationDelta
                initialRotation = newAngle

                // Lưu lại vị trí cuối
                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                // Đặt lại trạng thái khi thả ra
                hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
            }
        }
        return true
    }


    private fun getDistance(event: MotionEvent): Float {
        val dx = event.rawX - stickerTextView.x - (stickerTextView.width * stickerTextView.scaleX / 2)
        val dy = event.rawY - stickerTextView.y - (stickerTextView.height * stickerTextView.scaleY / 2)
        return hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }

    private fun getAngle(x: Float, y: Float): Float {
        val dx = x - stickerTextView.x
        val dy = y - stickerTextView.y
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchWithinTransformButton(event)) {
                    // Bắt đầu sự kiện chạm vào nút transformButton
                    lastX = event.rawX
                    lastY = event.rawY
                    initialDistance = getDistance(event)
                    initialRotation = getAngle(event.rawX, event.rawY)
                    return true
                } else if (isTouchWithinSticker(event)) {
                    // Xử lý tương tác chạm với sticker
                    isTouchingSticker = true
                    lastX = event.rawX
                    lastY = event.rawY
                    borderView.isVisible = true
                    hideBorderHandler.removeCallbacks(hideBorderRunnable)
                    return true
                } else {
                    isTouchingSticker = false
                    return false
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouchWithinTransformButton(event)) {
                    // Thực hiện logic biến đổi khi kéo nút transformButton
                    val newDistance = getDistance(event)
                    val scaleFactor = newDistance / initialDistance

                    // Giới hạn scale factor để tránh sticker quá lớn hoặc quá nhỏ
                    if (scaleFactor > 0.5f && scaleFactor < 2f) {
                        currentScale = scaleFactor
                        stickerTextView.scaleX = currentScale
                        stickerTextView.scaleY = currentScale
                        updateBorderSize() // Cập nhật kích thước border
                    }

                    // Xoay sticker theo góc mới
                    val newAngle = getAngle(event.rawX, event.rawY)
                    stickerTextView.rotation += newAngle - initialRotation
                    initialRotation = newAngle

                    // Cập nhật vị trí cuối
                    lastX = event.rawX
                    lastY = event.rawY
                    return true
                }

                if (isTouchingSticker) {
                    // Di chuyển sticker
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY
                    stickerTextView.translationX += dx
                    stickerTextView.translationY += dy
                    borderView.translationX += dx
                    borderView.translationY += dy

                    lastX = event.rawX
                    lastY = event.rawY
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isTouchWithinTransformButton(event)) {
                    // Dừng thao tác với transformButton
                    hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
                    return true
                }

                if (isTouchingSticker) {
                    hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
                }

                isTouchingSticker = false
            }
        }
        return true
    }




    private fun isTouchWithinTransformButton(event: MotionEvent): Boolean {
        // Lấy tọa độ tâm của nút transformButton
        val buttonCenterX = transformButton.x + transformButton.width / 2
        val buttonCenterY = transformButton.y + transformButton.height / 2

        // Tính khoảng cách giữa điểm chạm và tâm nút transform
        val dx = event.x - buttonCenterX
        val dy = event.y - buttonCenterY
        val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()

        // Kiểm tra nếu điểm chạm nằm trong bán kính 75 pixels từ tâm nút
        return distance <= 15f
    }


    private fun isTouchWithinSticker(event: MotionEvent): Boolean {
        val stickerRect = Rect()
        val borderRect = Rect()

        // Lấy vùng của stickerTextView
        stickerTextView.getHitRect(stickerRect)

        // Lấy vùng của borderView
        borderView.getHitRect(borderRect)


        // Kiểm tra xem sự kiện chạm có nằm trong vùng của stickerTextView hoặc borderView không
        return stickerRect.contains(event.x.toInt(), event.y.toInt()) || borderRect.contains(event.x.toInt(), event.y.toInt())
    }
//
//
//
    fun getStickerBitmap(): Bitmap {
        // Vẽ StickerTextView lên bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
    private fun setupGestureDetector() {
        val gestureListener = GestureListener(this)
        setOnTouchListener { view, event ->
            gestureListener.onTouch(view, event)
        }
    }

    private inner class GestureListener(private val stickerTextView: StickerTextView) : GestureDetector.SimpleOnGestureListener() {
        private val gestureDetector = GestureDetector(stickerTextView.context, this)

        // Phương thức xử lý sự kiện chạm
        fun onTouch(view: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Mở dialog khi bấm 2 lần
            val context = stickerTextView.context
            if (context is DrawActivity) {
                context.showStickerTextDialog(stickerTextView)
            }
            return true
        }
    }
}