package com.draw.viewcustom

import android.view.View

class StickerHistory {
    private val undoStack = mutableListOf<StickerAction>() // Lịch sử Undo
    val redoStack = mutableListOf<StickerAction>() // Lịch sử Redo

    fun addSticker(sticker: View, position: Pair<Float, Float>) {
        undoStack.add(StickerAction(sticker, position, ActionType.ADD))
        redoStack.clear() // Xóa redo stack khi có hành động mới
    }

    fun removeSticker(sticker: View, position: Pair<Float, Float>) {
        undoStack.add(StickerAction(sticker, position, ActionType.REMOVE))
        redoStack.clear() // Xóa redo stack khi có hành động mới
    }

    fun undo(): StickerAction? {
        if (undoStack.isNotEmpty()) {
            val action = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(action)
            return action
        }
        return null
    }

    fun redo(): StickerAction? {
        if (redoStack.isNotEmpty()) {
            val action = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(action)
            return action
        }
        return null
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
