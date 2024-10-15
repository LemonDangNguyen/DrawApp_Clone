package com.draw.viewcustom

import android.graphics.Bitmap
import android.view.View
import android.widget.FrameLayout
import kotlin.collections.mutableListOf
class StickerManager(private val container: FrameLayout) {
    private val stickers = mutableListOf<Pair<View, Pair<Float, Float>>>()
    private val stickerHistory = StickerHistory() // Đối tượng quản lý lịch sử

    // Thêm một sticker văn bản
    fun addStickerText(text: String, x: Float, y: Float) {
        val stickerTextView = StickerTextView(container.context)
        stickerTextView.updateText(text)
        stickerTextView.x = x
        stickerTextView.y = y
        stickers.add(Pair(stickerTextView, Pair(x, y)))
        container.addView(stickerTextView)

        stickerHistory.addSticker(stickerTextView, Pair(x, y))
        stickerHistory.redoStack.clear() // Xóa redo stack khi có hành động mới
    }

    // Thêm một sticker ảnh
    fun addStickerPhoto(photo: Bitmap, x: Float, y: Float) {
        val stickerPhotoView = StickerPhotoView(container.context)
        stickerPhotoView.setImageBitmap(photo)
        stickerPhotoView.x = x
        stickerPhotoView.y = y
        stickers.add(Pair(stickerPhotoView, Pair(x, y)))
        container.addView(stickerPhotoView)

        stickerHistory.addSticker(stickerPhotoView, Pair(x, y))
        stickerHistory.redoStack.clear()
    }

    // Thêm một sticker meme
    fun addStickerMeme(resId: Int, x: Float, y: Float) {
        val stickerMemeView = StickerMemeView(container.context)
        stickerMemeView.setImageResource(resId)
        stickerMemeView.x = x
        stickerMemeView.y = y
        stickers.add(Pair(stickerMemeView, Pair(x, y)))
        container.addView(stickerMemeView)

        stickerHistory.addSticker(stickerMemeView, Pair(x, y))
        stickerHistory.redoStack.clear()
    }

    // Lấy danh sách các sticker
    fun getStickers(): List<Pair<View, Pair<Float, Float>>> {
        return stickers
    }

    // Xóa tất cả các sticker
    fun removeAllStickers() {
        container.removeAllViews()
        stickers.clear()
        stickerHistory.clear() // Xóa lịch sử khi xóa tất cả
    }

    // Xóa sticker cụ thể
    fun removeSticker(sticker: View) {
        container.removeView(sticker)
        stickers.removeAll { it.first == sticker }
        stickerHistory.removeSticker(sticker, Pair(sticker.x, sticker.y))
    }

    // Thực hiện hành động undo
    fun undo(): StickerAction? {
        val action = stickerHistory.undo()
        action?.let {
            when (it.actionType) {
                ActionType.ADD -> removeSticker(it.sticker)
                ActionType.REMOVE -> addSticker(it.sticker, it.position.first, it.position.second)
            }
        }
        return action
    }

    // Thực hiện hành động redo
    fun redo(): StickerAction? {
        val action = stickerHistory.redo()
        action?.let {
            when (it.actionType) {
                ActionType.ADD -> addSticker(it.sticker, it.position.first, it.position.second)
                ActionType.REMOVE -> removeSticker(it.sticker)
            }
        }
        return action
    }

    // Thêm một sticker với vị trí cụ thể (để hỗ trợ undo/redo)
    private fun addSticker(sticker: View, x: Float, y: Float) {
        container.addView(sticker)
        stickers.add(Pair(sticker, Pair(x, y)))
    }
}
