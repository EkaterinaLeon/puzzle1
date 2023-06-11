package com.jigi.mealjigsaw.model

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView

class PuzzlePiece(context: Context?) : AppCompatImageView(context!!) {
    var xCoordination = 0
    var yCoordination = 0
    var pieceWidth = 0
    var pieceHeight = 0
    var canMove = true
}