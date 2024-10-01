package com.draw.model

data class PaintPathEntry(
    val id: Long = 0,
    val idDrawing: Long,
    val pathString: String,
    val paintString: String
){
    constructor(idDrawing: Long, pathString: String, paintString: String): this(0, idDrawing, pathString, paintString)
}