package com.draw.viewcustom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import com.draw.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import android.os.Handler
import android.os.Looper

class StickerMemeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private lateinit var memeImageView: AppCompatImageView
    private lateinit var deleteButton: AppCompatImageView
    private lateinit var flipButton: AppCompatImageView
    private lateinit var transformButton: AppCompatImageView
    private lateinit var borderView: View
    private var isDragging = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var isTransforming = false
    private var initialDistance = 0f
    private var initialRotation = 0f

    private val handler = Handler(Looper.getMainLooper())
    private var hideButtonsRunnable: Runnable? = null

    init {
        initMemeView()
    }

    private fun initMemeView() {
        memeImageView = createImageView()
        borderView = createBorderView()
        deleteButton = createControlButton(R.drawable.ic_sticker_delete) { removeMeme() }
        flipButton = createControlButton(R.drawable.ic_sticker_flip) { flipMeme() }
        transformButton = createTransformButton()

        addView(memeImageView)
        addView(borderView)
        addView(deleteButton)
        addView(flipButton)
        addView(transformButton)
    }

    private fun createImageView(): AppCompatImageView {
        return AppCompatImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setOnTouchListener { _, event -> handleTouch(event) }
            gravity = Gravity.CENTER
        }
    }

    private fun createBorderView(): View {
        return View(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            background = createBorderDrawable()
        }
    }

    private fun createBorderDrawable(): ShapeDrawable {
        return ShapeDrawable(RectShape()).apply {
            paint.color = Color.DKGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
        }
    }

    private fun createControlButton(resId: Int, onClick: () -> Unit): AppCompatImageView {
        return AppCompatImageView(context).apply {
            setImageResource(resId)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setOnClickListener { onClick() }
        }
    }

    private fun createTransformButton(): AppCompatImageView {
        return AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_resize)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setOnTouchListener { _, event -> handleTransform(event) }
        }
    }

    fun setImage(bitmap: Bitmap) {
        memeImageView.setImageBitmap(bitmap)
        updateControlButtonPositions()
        updateBorderSize()
    }

    fun setImageResource(resourceId: Int) {
        memeImageView.setImageResource(resourceId)
        updateControlButtonPositions()
        updateBorderSize()
    }

    private fun updateControlButtonPositions() {
        val borderPadding = -3  // Đặt khoảng cách từ nút đến viền là -3
        val borderWidth = borderView.width
        val borderHeight = borderView.height
        val centerX = borderView.x + borderWidth / 2
        val centerY = borderView.y + borderHeight / 2
        val angle = Math.toRadians(memeImageView.rotation.toDouble())

        val buttonSize = (borderWidth * 0.2).toInt()
        updateButtonSize(deleteButton, buttonSize)
        updateButtonSize(flipButton, buttonSize)
        updateButtonSize(transformButton, buttonSize)

        // Điều chỉnh offset với borderPadding
        setButtonPosition(deleteButton, centerX, centerY, (borderWidth / 2 + borderPadding).toFloat(), (-borderHeight / 2 + borderPadding).toFloat(), angle)
        setButtonPosition(flipButton, centerX, centerY, (0 + borderPadding).toFloat(), (-borderHeight / 2 + borderPadding).toFloat(), angle)
        setButtonPosition(transformButton, centerX, centerY, (borderWidth / 2 + borderPadding).toFloat(), (borderHeight / 2 + borderPadding).toFloat(), angle)

        updateBorderSize()
    }

    private fun updateBorderSize() {
        val padding = 10
        val imageWidth = memeImageView.width
        val imageHeight = memeImageView.height
        borderView.layoutParams = LayoutParams(
            imageWidth + padding,
            imageHeight + padding
        )
        borderView.x = memeImageView.x - padding / 2
        borderView.y = memeImageView.y - padding / 2
    }

    private fun setButtonPosition(button: View, centerX: Float, centerY: Float, offsetX: Float, offsetY: Float, angle: Double) {
        val rotatedX = centerX + (offsetX * cos(angle) - offsetY * sin(angle))
        val rotatedY = centerY + (offsetX * sin(angle) + offsetY * cos(angle))
        button.x = (rotatedX - button.width / 2).toFloat()
        button.y = (rotatedY - button.height / 2).toFloat()
    }

    private fun updateButtonSize(button: View, size: Int) {
        button.layoutParams = button.layoutParams.apply {
            width = size
            height = size
        }
    }

    private fun flipMeme() {
        memeImageView.scaleX *= -1
        updateBorderSize()
    }

    private fun removeMeme() {
        this.visibility = View.GONE
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                isDragging = true
                showControlButtons()
                scheduleHideControlButtons()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = event.rawX - lastTouchX
                    val deltaY = event.rawY - lastTouchY

                    memeImageView.translationX += deltaX
                    memeImageView.translationY += deltaY
                    borderView.translationX += deltaX
                    borderView.translationY += deltaY

                    lastTouchX = event.rawX
                    lastTouchY = event.rawY

                    updateControlButtonPositions()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return true
    }

    private fun handleTransform(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.rawY
                initialDistance = memeImageView.scaleX
                initialRotation = getAngle(event.rawX - memeImageView.x, event.rawY - memeImageView.y)
                isTransforming = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTransforming) {
                    val deltaY = event.rawY - lastTouchY

                    // Thay đổi kích thước ảnh
                    val scaleFactor = initialDistance + deltaY / 500f
                    if (scaleFactor in 0.5f..2f) {
                        memeImageView.scaleX = scaleFactor
                        memeImageView.scaleY = scaleFactor
                        borderView.scaleX = scaleFactor
                        borderView.scaleY = scaleFactor
                        updateControlButtonPositions()

                    }

                    // Thay đổi góc xoay của ảnh
                    val newRotation = getAngle(event.rawX - memeImageView.x, event.rawY - memeImageView.y)
                    memeImageView.rotation += newRotation - initialRotation
                    borderView.rotation += newRotation - initialRotation
                    initialRotation = newRotation

                    // Gọi hàm cập nhật vị trí của các nút điều chỉnh
                    updateControlButtonPositions()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTransforming = false
            }
        }
        return true
    }


    private fun getAngle(x: Float, y: Float): Float {
        return Math.toDegrees(atan2(y, x).toDouble()).toFloat()
    }

    private fun showControlButtons() {
        deleteButton.visibility = View.VISIBLE
        flipButton.visibility = View.VISIBLE
        transformButton.visibility = View.VISIBLE
        borderView.visibility = View.VISIBLE
    }

    private fun hideControlButtons() {
        deleteButton.visibility = View.GONE
        flipButton.visibility = View.GONE
        transformButton.visibility = View.GONE
        borderView.visibility = View.GONE
    }

    private fun scheduleHideControlButtons() {
        hideButtonsRunnable?.let { handler.removeCallbacks(it) }

        hideButtonsRunnable = Runnable {
            hideControlButtons()
        }

        handler.postDelayed(hideButtonsRunnable!!, 2000)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateControlButtonPositions()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateControlButtonPositions()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hideButtonsRunnable?.let { handler.removeCallbacks(it) }
    }
}