package com.draw.viewcustom

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
    private var mDefaultColor: Int = 0
) : BottomSheetDialogFragment() {


    private lateinit var mColorPreview: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout tùy chỉnh
        val dialogView = inflater.inflate(R.layout.bottom_sheet_dialog_textsticker, container, false)

        // Tìm các view trong layout
        val etInput = dialogView.findViewById<EditText>(R.id.etInput)
        mColorPreview  = dialogView.findViewById(R.id.preview_selected_color)
        mDefaultColor = 0
        var pickColorButton = dialogView.findViewById<TextView>(R.id.pick_color_button)
        //var setcolorButton = dialogView.findViewById<TextView>(R.id.set_color_button)
//        setcolorButton.setOnClickListener {
//            stickerTextView.setTextColor(mDefaultColor)
//        }
        // Thiết lập nút chọn màu
        pickColorButton.setOnClickListener {
            showColorPicker(dialogView)
        }

        val ivCheck = dialogView.findViewById<ImageView>(R.id.ivCheck)
        ivCheck.setOnClickListener {
            val newText = etInput.text.toString()
            stickerTextView.updateText(newText.trim())
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
                    // Không cần thực hiện gì khi hủy dialog
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    // Đổi màu mDefaultColor theo màu được chọn
                    mDefaultColor = color
                    // Đặt màu cho xem trước và `stickerTextView`
                    mColorPreview.setBackgroundColor(mDefaultColor)
                    stickerTextView.setTextColor(mDefaultColor)

                    // Đặt màu cho `etInput`
                    val etInput: EditText = dialogView.findViewById(R.id.etInput)
                    etInput.setTextColor(mDefaultColor) // sử dụng màu đã chọn
                }
            })
        colorPickerDialogue.show()
    }




}