package com.draw.viewcustom

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.draw.R
import com.draw.adapter.ImageAdapter
import com.draw.extensions.checkPer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StickerPhotoDialog(
    private var stickerPhotoView: StickerPhotoView,
    private val context: Context
) : BottomSheetDialogFragment() {

    private lateinit var rvImages: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private var imagePaths: MutableList<String> = mutableListOf()
    private var selectedImagePath: String? = null // Để lưu đường dẫn ảnh được chọn

    // Kiểm tra quyền truy cập lưu trữ
    val storagePer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    // Định nghĩa hằng số REQUEST_PERMISSION
    companion object {
        private const val REQUEST_PERMISSION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialogView = inflater.inflate(R.layout.bottom_sheet_insert_photo, container, false)

        // Thiết lập RecyclerView
        rvImages = dialogView.findViewById(R.id.rvImages)
        rvImages.layoutManager = GridLayoutManager(context, 3)

        checkPermission.launch(storagePer)

        val ivCheck = dialogView.findViewById<ImageView>(R.id.ivCheck)
        ivCheck.setOnClickListener {
            // Cập nhật StickerPhotoView với ảnh đã chọn
            selectedImagePath?.let { path ->
                val bitmap = getBitmapFromPath(path)
                bitmap?.let {
                    stickerPhotoView.setImage(it) // Sử dụng Bitmap thay vì String
                }
            }
            stickerPhotoView.visibility = View.VISIBLE // Hiển thị StickerPhotoView
            dismiss() // Đóng BottomSheetDialog sau khi cập nhật
        }


        return dialogView
    }
    private fun getBitmapFromPath(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }

    private var checkPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (context.checkPer(storagePer)) loadImages()
        }

    private fun loadImages() {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val imagePath = it.getString(columnIndex)
                imagePaths.add(imagePath)
            }

            // Thiết lập adapter cho RecyclerView
            imageAdapter = ImageAdapter(requireContext(), imagePaths) { imagePath ->
                selectedImagePath = imagePath // Cập nhật đường dẫn ảnh được chọn
            }
            rvImages.adapter = imageAdapter
        } ?: run {
            Toast.makeText(context, "No images found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages() // Tải ảnh khi được cấp quyền
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}