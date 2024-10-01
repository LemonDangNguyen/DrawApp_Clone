package com.draw.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.draw.R
import com.draw.databinding.ItemAnimationGuildeHorizontalBinding
import com.draw.model.AnimationGuide

class AnimationGuideHorizontalAdapter(
    private val context: Context,
    private val listGuide: MutableList<AnimationGuide>,
    private val onClickItem: (pos: Int) -> Unit
) :
    RecyclerView.Adapter<AnimationGuideHorizontalAdapter.AnimationGuideViewHolder>() {

    inner class AnimationGuideViewHolder(val binding: ItemAnimationGuildeHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(pos: Int) {
            val animationGuide = listGuide[pos]
            binding.img.setImageResource(animationGuide.img)
            binding.tvNumberFrame.text =
                "${animationGuide.listFrame.size} ${context.getString(R.string.frame)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimationGuideViewHolder {
        val bindingItem =
            ItemAnimationGuildeHorizontalBinding.inflate(LayoutInflater.from(context), parent, false)
        return AnimationGuideViewHolder(bindingItem)
    }

    override fun onBindViewHolder(holder: AnimationGuideViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener {
            onClickItem.invoke(position)
        }
    }


    override fun getItemCount(): Int {
        return listGuide.size
    }
}
