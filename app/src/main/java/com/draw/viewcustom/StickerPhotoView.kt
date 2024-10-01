package com.draw.viewcustom

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import com.draw.R
import kotlin.math.atan2
import kotlin.math.hypot
import android.os.Handler
import android.os.Looper
import android.view.Gravity

class StickerPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private lateinit var stickerImageView: AppCompatImageView
    private lateinit var deleteButton: AppCompatImageView
    private lateinit var flipButton: AppCompatImageView
    private lateinit var transformButton: AppCompatImageView

    private var isDragging = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var isTransforming = false
    private var initialDistance = 0f
    private var initialRotation = 0f

    private val handler = Handler(Looper.getMainLooper())
    private var hideButtonsRunnable: Runnable? = null

    init {
        initStickerView()
        gravity = Gravity.CENTER
    }

    private fun initStickerView() {
        // Initialize ImageView
        stickerImageView = AppCompatImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            setOnTouchListener { _, event -> handleTouch(event) }
        }
        addView(stickerImageView)

        // Initialize buttons
        deleteButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_delete)
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { removeSticker() }
        }
        addView(deleteButton)

        flipButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_flip)
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { flipSticker() }
        }
        addView(flipButton)

        transformButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_resize)
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            setOnTouchListener { _, event -> handleTransform(event) }
        }
        addView(transformButton)
    }

    fun setImage(bitmap: Bitmap) {
        stickerImageView.setImageBitmap(bitmap)
        updateControlButtonPositions()
    }

    private fun updateControlButtonPositions() {
        val width = stickerImageView.width * stickerImageView.scaleX
        val height = stickerImageView.height * stickerImageView.scaleY

        // Tọa độ trung tâm của stickerImageView
        val centerX = stickerImageView.x + stickerImageView.pivotX
        val centerY = stickerImageView.y + stickerImageView.pivotY

        // Tính toán các điểm góc dựa trên phép xoay
        val points = arrayOf(
            floatArrayOf(-width / 2, -height / 2),  // Top-left
            floatArrayOf(width / 2, -height / 2),   // Top-right
            floatArrayOf(width / 2, height / 2),    // Bottom-right
            floatArrayOf(-width / 2, height / 2)    // Bottom-left
        )

        val rotationMatrix = android.graphics.Matrix()
        rotationMatrix.setRotate(stickerImageView.rotation, 0f, 0f)

        points.forEach {
            rotationMatrix.mapPoints(it)
        }

        // Cập nhật vị trí của các nút dựa trên vị trí các góc sau khi xoay
        deleteButton.x = centerX + points[1][0] - deleteButton.width / 2 // Top-right
        deleteButton.y = centerY + points[1][1] - deleteButton.height / 2

        flipButton.x = centerX + points[0][0] + (points[1][0] - points[0][0]) / 2 - flipButton.width / 2 // Middle-top
        flipButton.y = centerY + points[0][1] + (points[1][1] - points[0][1]) / 2 - flipButton.height / 2

        transformButton.x = centerX + points[2][0] - transformButton.width / 2 // Bottom-right
        transformButton.y = centerY + points[2][1] - transformButton.height / 2
    }



    private fun flipSticker() {
        stickerImageView.scaleX *= -1
    }

    private fun removeSticker() {
        this.visibility = View.GONE
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                isDragging = true
                // Hiển thị các nút điều khiển tại vị trí hiện tại của nhãn dán
                showControlButtons()
                // Lập lịch ẩn các nút sau 2 giây
                scheduleHideControlButtons()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = event.rawX - lastTouchX
                    val deltaY = event.rawY - lastTouchY

                    // Di chuyển nhãn dán
                    stickerImageView.translationX += deltaX
                    stickerImageView.translationY += deltaY

                    // Di chuyển các nút cùng với nhãn dán
                    updateControlButtonPositions()

                    // Cập nhật vị trí chạm cuối
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
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
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                initialDistance = 0f // Đặt khoảng cách ban đầu bằng 0 cho ngón tay đơn
                initialRotation = getAngle(event.rawX - stickerImageView.x, event.rawY - stickerImageView.y)
                isTransforming = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTransforming) {
                    // Tính toán di chuyển để thay đổi kích thước
                    val deltaX = event.rawX - lastTouchX
                    val deltaY = event.rawY - lastTouchY

                    // Tính toán tỷ lệ thay đổi theo khoảng cách di chuyển của ngón tay
                    val scaleFactor = 1 + (deltaY / height) // Điều chỉnh tỷ lệ theo di chuyển Y
                    if (scaleFactor > 0.5f && scaleFactor < 2f) { // Giới hạn tỷ lệ
                        stickerImageView.scaleX *= scaleFactor
                        stickerImageView.scaleY *= scaleFactor
                    }

                    // Tính góc xoay mới
                    val newRotation = getAngle(event.rawX - stickerImageView.x, event.rawY - stickerImageView.y)
                    val rotationDelta = newRotation - initialRotation
                    stickerImageView.rotation += rotationDelta
                    initialRotation = newRotation

                    // Cập nhật lại tọa độ cuối cùng cho lần di chuyển tiếp theo
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY

                    // Cập nhật vị trí của các nút sau khi thay đổi kích thước hoặc xoay
                    updateControlButtonPositions()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTransforming = false
            }
        }
        return true
    }



    // Hàm tính khoảng cách giữa hai ngón tay
    private fun getDistance(event: MotionEvent): Float {
        return if (event.pointerCount == 2) {
            val dx = event.getX(0) - event.getX(1)
            val dy = event.getY(0) - event.getY(1)
            hypot(dx, dy)
        } else {
            0f
        }
    }

    // Hàm tính góc giữa hai điểm
    private fun getAngle(x: Float, y: Float): Float {
        return Math.toDegrees(atan2(y, x).toDouble()).toFloat()
    }

    private fun showControlButtons() {
        deleteButton.visibility = View.VISIBLE
        flipButton.visibility = View.VISIBLE
        transformButton.visibility = View.VISIBLE
    }

    private fun hideControlButtons() {
        deleteButton.visibility = View.GONE
        flipButton.visibility = View.GONE
        transformButton.visibility = View.GONE
    }

    private fun scheduleHideControlButtons() {
        // Hủy bất kỳ runnable nào đang chờ
        hideButtonsRunnable?.let { handler.removeCallbacks(it) }

        // Tạo một runnable để ẩn các nút sau 2 giây
        hideButtonsRunnable = Runnable {
            hideControlButtons()
        }

        // Đặt runnable để chạy sau 2 giây
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
}