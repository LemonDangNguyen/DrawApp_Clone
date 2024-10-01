package com.draw.adapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.draw.R

class CategoryMemeAdapter(
    private val categories: List<Int>,
    private val onCategoryClick: (Int) -> Unit
) : RecyclerView.Adapter<CategoryMemeAdapter.CategoryViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION // Vị trí được chọn

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.categoryImage)

        fun bind(category: Int, position: Int) {
            imageView.setImageResource(category)

            // Đặt viền bo góc nếu vị trí được chọn
            if (position == selectedPosition) {
                itemView.setBackgroundResource(R.drawable.rounded_border) // Viền bo góc khi được chọn
            } else {
                itemView.setBackgroundResource(android.R.color.transparent) // Không viền khi không chọn
            }

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // Cập nhật lại viền của item cũ và item mới
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // Gọi hàm xử lý sự kiện click
                onCategoryClick(adapterPosition)
            }
        }
    }
}


