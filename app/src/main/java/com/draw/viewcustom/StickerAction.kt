package com.draw.viewcustom

import android.view.View

data class StickerAction(
    val sticker: View, // Sử dụng View cho sticker
    val position: Pair<Float, Float>,
    val actionType: ActionType // Thêm loại hành động
)

enum class ActionType {
    ADD, REMOVE // Các hành động có thể
}
