package com.draw.model

import android.graphics.Bitmap

data class DrawInfo(
    var bitmap: Bitmap?,
    val listHistory: MutableList<PaintPath?>,
    val listUndo: MutableList<PaintPath?>
) {
    constructor(
        listHistory: MutableList<PaintPath?>,
        listUndo: MutableList<PaintPath?>
    ) : this(null, listHistory, listUndo)

    fun setInfo(drawInfo: DrawInfo) {
        this.bitmap = drawInfo.bitmap
        this.listHistory.clear()
        this.listHistory.addAll(drawInfo.listHistory)
        this.listUndo.clear()
        this.listUndo.addAll(drawInfo.listUndo)
    }
}
