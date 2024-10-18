package com.draw.model

import android.graphics.Color

data class TextModel (
    val id: Int,
    val text: String = "StickerTextView",
    val textcolor: Int = Color.BLACK,
    val textSize: Int = 24,
    val font: String = "",
    val background: Int = 0

)