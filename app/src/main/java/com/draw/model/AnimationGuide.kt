package com.draw.model

data class AnimationGuide(
    val name: Int,
    val img: Int,
    val listFrame: List<Int>,
    var animationSpeed: Int = 5
)
