package com.draw.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.draw.databinding.ItemTypeAnimationBinding
import com.draw.model.TypeAnimationGuide

class TypeAnimationAdapter(
    private val context: Context,
    private val listTypeAnimationGuide: MutableList<TypeAnimationGuide>,
    private val onClickSeeAll: (pos: Int) -> Unit,
    private val onClickGuide: (posType: Int, posGuide: Int) -> Unit
) : RecyclerView.Adapter<TypeAnimationAdapter.AnimationGuideViewHolder>() {

    inner class AnimationGuideViewHolder(val binding: ItemTypeAnimationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(pos: Int) {
            val animationGuide = listTypeAnimationGuide[pos]
            binding.tvName.text = context.getText(animationGuide.name)
            binding.rcv.adapter = AnimationGuideHorizontalAdapter(context, animationGuide.listAnimationGuide){
                onClickGuide.invoke(pos, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimationGuideViewHolder {
        val bindingItem =
            ItemTypeAnimationBinding.inflate(LayoutInflater.from(context), parent, false)
        return AnimationGuideViewHolder(bindingItem)
    }

    override fun onBindViewHolder(holder: AnimationGuideViewHolder, position: Int) {
        holder.bind(position)
        holder.binding.btnSeeAll.setOnClickListener {
            onClickSeeAll.invoke(position)
        }
    }


    override fun getItemCount(): Int {
        return listTypeAnimationGuide.size
    }
}
