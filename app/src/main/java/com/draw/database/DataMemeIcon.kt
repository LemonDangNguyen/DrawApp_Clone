package com.draw.database

import com.draw.R

object DataMemeIcon {

    // Danh sách các danh mục
    val categories = listOf(
        R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3, R.drawable.icon_4, // Thêm các hình ảnh danh mục
    )

    // Danh sách các meme cho mỗi danh mục
    val memesByCategory = mapOf(
        0 to listOf(R.drawable.icon_1, R.drawable.icon_1,  R.drawable.icon_1 , R.drawable.icon_1),
        1 to listOf(R.drawable.icon_2, R.drawable.icon_2,  R.drawable.icon_2 , R.drawable.icon_2),
        2 to listOf(R.drawable.icon_3, R.drawable.icon_3,  R.drawable.icon_3 , R.drawable.icon_3),
        3 to listOf(R.drawable.icon_4, R.drawable.icon_4,  R.drawable.icon_4 , R.drawable.icon_4)
    )
}