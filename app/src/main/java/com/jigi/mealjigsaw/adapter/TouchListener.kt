package com.jigi.mealjigsaw.adapter

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.jigi.mealjigsaw.model.PuzzlePiece
import com.jigi.mealjigsaw.view.PuzzleActivity
import kotlin.math.pow
import kotlin.math.sqrt

class TouchListener (private val activity: PuzzleActivity) : OnTouchListener {

    private var xDelta = 0f
    private var yDelta = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val x = motionEvent.rawX
        val y = motionEvent.rawY

        val tolerance = sqrt(view.width.toDouble().pow(2.0) + view.height.toDouble().pow(2.0)) / 10
        val piece = view as PuzzlePiece

        if (!piece.canMove) {
            return true
        }

        val lParams = view.getLayoutParams() as RelativeLayout.LayoutParams
        when (motionEvent.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                xDelta = x - lParams.leftMargin
                yDelta = y - lParams.topMargin
                piece.bringToFront()
            }

            MotionEvent.ACTION_MOVE -> {
                lParams.leftMargin = (x - xDelta).toInt()
                lParams.topMargin = (y - yDelta).toInt()
                view.setLayoutParams(lParams)
            }

            MotionEvent.ACTION_UP -> {
                val xDiff = StrictMath.abs(piece.xCoordination - lParams.leftMargin)
                val yDiff = StrictMath.abs(piece.yCoordination - lParams.topMargin)
                if (xDiff <= tolerance && yDiff <= tolerance) {
                    lParams.leftMargin = piece.xCoordination
                    lParams.topMargin = piece.yCoordination
                    piece.layoutParams = lParams
                    piece.canMove = false
                    sendViewToBack(piece)
                    activity.checkGameOver()
                }
            }
        }
        return true
    }

    private fun sendViewToBack(child: View) {
        val parent = child.parent as ViewGroup
        parent.removeView(child)
        parent.addView(child, 0)
    }
}