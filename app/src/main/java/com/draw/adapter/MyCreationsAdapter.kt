package com.draw.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.draw.R
import com.draw.databinding.ItemAnimationGuildeBinding
import com.draw.model.Animation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MyCreationsAdapter(
    private val context: Context,
    private val listAnimation: MutableList<Animation>,
    private val onClickItem: (item: Animation) -> Unit,
) :
    RecyclerView.Adapter<MyCreationsAdapter.AnimationViewHolder>() {

    inner class AnimationViewHolder(val binding: ItemAnimationGuildeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(pos: Int) {
            val animation = listAnimation[pos]

            binding.name.isSelected = true

            Glide.with(context).load(animation.link).placeholder(R.drawable.placeholder_loading)
                .into(binding.img)

            binding.name.text = animation.name
            val sdf = SimpleDateFormat("EEE, dd MMMM", Locale.getDefault())
            val formattedDate = sdf.format(Date(animation.createdAt))
            binding.frame.text = formattedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimationViewHolder {
        val bindingItem =
            ItemAnimationGuildeBinding.inflate(LayoutInflater.from(context), parent, false)
        return AnimationViewHolder(bindingItem)
    }

    override fun onBindViewHolder(holder: AnimationViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener {
            onClickItem.invoke(listAnimation[position])
        }
    }

    override fun getItemCount(): Int {
        return listAnimation.size
    }

    fun setList(list: MutableList<Animation>){
        listAnimation.clear()
        listAnimation.addAll(list)
        notifyDataSetChanged()
    }
}
