package com.draw.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.draw.R
import com.draw.databinding.ItemDrawBinding
import com.draw.model.AnimationGuide
import com.draw.model.DrawInfo
import com.draw.model.PaintPath
import com.draw.ultis.Common.getBitmapWithoutWhite
import com.draw.ultis.ViewControl.gone
import com.draw.ultis.ViewControl.visible

@SuppressLint("NotifyDataSetChanged")
class DrawViewAdapter(
    private val context: Context,
    private val isGuide: Boolean,
    private val onClickItem: (newPosition: Int, oldPosition: Int) -> Unit
) :
    RecyclerView.Adapter<DrawViewAdapter.DrawViewHolder>() {
    private var selectItem = 0
    val listDraw = mutableListOf(
        DrawInfo(mutableListOf(), mutableListOf())
    )
    private var animationGuide: AnimationGuide? = null

    inner class DrawViewHolder(val binding: ItemDrawBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {
            if (isGuide && animationGuide != null) {
                binding.img.setImageResource(animationGuide!!.listFrame[pos])
                binding.tvPosition.text = (1 + pos).toString()
                if (pos != selectItem) {
                    binding.bg.setBackgroundResource(R.drawable.bg_radius_12dp_unselect)
                    binding.tvPosition.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#C5C9CC"))
                } else {
                    binding.bg.setBackgroundResource(R.drawable.bg_radius_12dp_select)
                    binding.tvPosition.backgroundTintList = null
                }
            } else {
                if (pos == listDraw.size) {
                    binding.img.gone()
                    binding.tvPosition.gone()
                    binding.bg.setBackgroundResource(R.drawable.img_add_frame)
                } else {
                    binding.img.visible()
                    binding.tvPosition.visible()
                    binding.tvPosition.text = (1 + pos).toString()
                    val drawInfo = listDraw[pos]
                    if (drawInfo.bitmap != null) {
                        binding.img.setImageBitmap(drawInfo.bitmap)
                    }
                    if (pos != selectItem) {
                        binding.bg.setBackgroundResource(R.drawable.bg_radius_12dp_unselect)
                        binding.tvPosition.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#C5C9CC"))
                    } else {
                        binding.bg.setBackgroundResource(R.drawable.bg_radius_12dp_select)
                        binding.tvPosition.backgroundTintList = null
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawViewHolder {
        val bindingItem = ItemDrawBinding.inflate(LayoutInflater.from(context), parent, false)
        return DrawViewHolder(bindingItem)
    }

    override fun onBindViewHolder(holder: DrawViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener {
            if (!isGuide && position == listDraw.size) {
                listDraw.add(DrawInfo(mutableListOf(), mutableListOf()))
            }
            onClickItem.invoke(position, selectItem)
            setSelectItem(position)
        }
    }

    fun setNextorPrevios(isNext: Boolean) {
        if (isNext) {
            val newPos = selectItem + 1
            if (!isGuide && newPos == listDraw.size) {
                listDraw.add(DrawInfo(mutableListOf(), mutableListOf()))
            }
            onClickItem.invoke(newPos, selectItem)
            setSelectItem(newPos)
        } else {
            val newPos = selectItem - 1
            onClickItem.invoke(newPos, selectItem)
            setSelectItem(newPos)
        }
    }

    fun makeCopy(width: Int, height: Int) {
        if (!isGuide) {
            val listHistory =
                mutableListOf<PaintPath?>().apply { addAll(listDraw[selectItem].listHistory) }
            val listUndo =
                mutableListOf<PaintPath?>().apply { addAll(listDraw[selectItem].listUndo) }

            listDraw.add(
                selectItem + 1,
                DrawInfo(
                    getBitmapWithoutWhite(listDraw[selectItem].listHistory, width, height),
                    listHistory,
                    listUndo
                )
            )

            setSelectItem(selectItem + 1)
            notifyDataSetChanged()
        } else if (selectItem < listDraw.size - 1) {
            val listHistory =
                mutableListOf<PaintPath?>().apply { addAll(listDraw[selectItem].listHistory) }
            val listUndo =
                mutableListOf<PaintPath?>().apply { addAll(listDraw[selectItem].listUndo) }

            listDraw[selectItem + 1] = DrawInfo(listHistory, listUndo)
            setSelectItem(selectItem + 1)
            notifyDataSetChanged()
        }
    }

    private fun setSelectItem(pos: Int) {
        val oldPos = selectItem
        selectItem = pos
        notifyItemChanged(oldPos)
        notifyItemChanged(selectItem)
    }

    fun getSelectItem(): Int {
        return selectItem
    }

    fun setAnimationGuide(animationGuide: AnimationGuide) {
        this.animationGuide = animationGuide
        listDraw.clear()
        for (i in 1..animationGuide.listFrame.size) {
            listDraw.add(DrawInfo(mutableListOf(), mutableListOf()))
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (!isGuide) {
            listDraw.size + 1
        } else if (animationGuide != null) {
            animationGuide!!.listFrame.size
        } else {
            0
        }
    }

}