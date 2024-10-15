package com.draw.viewcustom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.draw.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class StickerTextDialog(
    private val stickerTextView: StickerTextView,

    // This is the default color of the preview box
    private var mDefaultColor: Int
) : BottomSheetDialogFragment() {
    private var onStickerTextEntered: ((String) -> Unit)? = null

    // Phương thức này sẽ được gọi để thiết lập callback
    fun setOnStickerTextEntered(listener: (String) -> Unit) {
        onStickerTextEntered = listener
    }


    private lateinit var mColorPreview: View
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout tùy chỉnh
        val dialogView = inflater.inflate(R.layout.bottom_sheet_dialog_textsticker, container, false)

        // Tìm các view trong layout
        val etInput = dialogView.findViewById<EditText>(R.id.etInput)
        mColorPreview  = dialogView.findViewById(R.id.preview_selected_color)
        var pickColorButton = dialogView.findViewById<TextView>(R.id.pick_color_button)
        // Thiết lập nút chọn màu
        pickColorButton.setOnClickListener {
            showColorPicker(dialogView)
        }

        val ivCheck = dialogView.findViewById<ImageView>(R.id.ivCheck)
        ivCheck.setOnClickListener {
            val newText = etInput.text.toString()
            stickerTextView.updateText(newText.trim())
            onStickerTextEntered?.invoke(newText.trim())
           // onStickerTextEntered?.invoke(stickerTextView.setTextColor(mDefaultColor).toString())
            stickerTextView.visibility = View.VISIBLE // Hiển thị StickerTextView
            dismiss() // Đóng BottomSheetDialog sau khi cập nhật
        }

        return dialogView
    }

    private fun showColorPicker(dialogView: View) {
        // Tạo AmbilWarnaDialog để chọn màu
        val colorPickerDialogue = AmbilWarnaDialog(this.activity, mDefaultColor,
            object : OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    mDefaultColor = color // Lưu màu sắc đã chọn

                    // Cập nhật màu sắc cho stickerTextView
                    stickerTextView.setTextColor(mDefaultColor) // Đặt màu cho văn bản trong stickerTextView

                    // Cập nhật màu sắc cho preview và EditText
                    mColorPreview.setBackgroundColor(mDefaultColor)
                    val etInput: EditText = dialogView.findViewById(R.id.etInput)
                    etInput.setTextColor(mDefaultColor) // Đặt màu cho EditText
                }
            })
        colorPickerDialogue.show()
    }

}