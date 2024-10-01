package com.draw.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.draw.R
import com.draw.databinding.ItemAnimationGuildeBinding
import com.draw.model.AnimationGuide

class AnimationGuideAdapter(
    private val context: Context,
    private val listGuide: MutableList<AnimationGuide>,
    private val onClickItem: (pos: Int) -> Unit
) :
    RecyclerView.Adapter<AnimationGuideAdapter.AnimationGuideViewHolder>() {

    inner class AnimationGuideViewHolder(val binding: ItemAnimationGuildeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(pos: Int) {
            val animationGuide = listGuide[pos]
            binding.name.isSelected = true
            binding.frame.isSelected = true
            binding.img.setImageResource(animationGuide.img)
            binding.name.text = context.getText(animationGuide.name)
            binding.frame.text =
                "${animationGuide.listFrame.size} ${context.getString(R.string.frame)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimationGuideViewHolder {
        val bindingItem =
            ItemAnimationGuildeBinding.inflate(LayoutInflater.from(context), parent, false)
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
