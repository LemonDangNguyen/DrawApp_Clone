package com.draw.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animations")
data class Animation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    val link: String,
    val isGuide: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor(name: String, link: String, isGuide: Boolean) : this(0, name, link, isGuide)
}

