package com.draw.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.draw.R

class ImageAdapter(
    private val context: Context,
    private val imagePaths: List<String>,
    private val onImageSelected: (String) -> Unit // Callback để thông báo ảnh đã chọn
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION // Vị trí được chọn

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePath = imagePaths[position]

        // Load hình ảnh vào ImageView bằng Glide
        Glide.with(context)
            .load(imagePath)
            .placeholder(R.drawable.frame_a_creator_10) // Hình ảnh placeholder
            .error(R.drawable.frame_an_angry_gift_7)   // Hình ảnh lỗi
            .into(holder.imageView)

        // Đặt màu nền dựa trên vị trí được chọn
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY) // Màu khi được chọn
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT) // Màu mặc định
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            // Gọi callback với đường dẫn ảnh được chọn
            onImageSelected(imagePath)

            // Cập nhật màu nền của hình cũ và hình mới
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }
}
