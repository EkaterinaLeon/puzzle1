package com.jigi.mealjigsaw.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.BitmapFactory.Options
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jigi.mealjigsaw.R
import com.jigi.mealjigsaw.model.PuzzlePiece
import com.jigi.mealjigsaw.adapter.TouchListener
import com.jigi.mealjigsaw.model.RotateImage.rotateImage
import java.io.IOException
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt


class PuzzleActivity : AppCompatActivity() {

    private var pieces: ArrayList<PuzzlePiece?>? = null
    private var mCurrentPhotoPath: String? = null
    private var mCurrentPhotoUri: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        val layout = findViewById<RelativeLayout>(R.id.layout)
        val imageView = findViewById<ImageView>(R.id.imageView)

        val intent = intent
        val assetName = intent.getStringExtra("assetName")
        mCurrentPhotoPath = intent.getStringExtra("mCurrentPhotoPath")
        mCurrentPhotoUri = intent.getStringExtra("mCurrentPhotoUri")

        val btnBack = findViewById<FloatingActionButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        imageView.post {
            if (assetName != null) {
                setPicFromAsset(assetName, imageView)
            } else if (mCurrentPhotoPath != null) {
                setPicFromPath(mCurrentPhotoPath!!, imageView)
            } else if (mCurrentPhotoUri != null) {
                imageView.setImageURI(Uri.parse(mCurrentPhotoUri))
            }
            pieces = splitImage()
            val touchListener = TouchListener(this@PuzzleActivity)

            pieces!!.shuffle()
            for (piece in pieces!!) {
                piece!!.setOnTouchListener(touchListener)
                layout.addView(piece)

                // randomize position
                val lParams = piece.layoutParams as RelativeLayout.LayoutParams
                lParams.leftMargin = Random().nextInt(layout.width - piece.pieceWidth)
                lParams.topMargin = layout.height - piece.pieceHeight
                piece.layoutParams = lParams
            }
        }
    }

    private fun setPicFromAsset(assetName: String, imageView: ImageView) {
        // Get the dimensions
        val targetW = imageView.width
        val targetH = imageView.height
        val am = assets

        try {
            val `is` = am.open("img/$assetName")
            val bmOptions = Options()

            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`is`, Rect(-1, -1, -1, -1), bmOptions)

            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight
            val scaleFactor = min(photoW / targetW, photoH / targetH)
            `is`.reset()

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            val bitmap = BitmapFactory.decodeStream(`is`, Rect(-1, -1, -1, -1), bmOptions)
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }


    private fun splitImage(): ArrayList<PuzzlePiece?> {
        val piecesNumber = intent.getIntExtra("piecesNumber", 0)
        val rows = intent.getIntExtra("rows", 0)
        val cols = intent.getIntExtra("cols", 0)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val pieces = ArrayList<PuzzlePiece?>(piecesNumber)

        val drawable = imageView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val dimensions = getBitmapPositionInsideImageView(imageView)

        val scaledBitmapLeft = dimensions[0]
        val scaledBitmapTop = dimensions[1]
        val scaledBitmapWidth = dimensions[2]
        val scaledBitmapHeight = dimensions[3]

        val croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft)
        val croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop)

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, scaledBitmapWidth, scaledBitmapHeight, true
        )

        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            abs(scaledBitmapLeft),
            abs(scaledBitmapTop),
            croppedImageWidth,
            croppedImageHeight
        )

        val pieceWidth = croppedImageWidth / cols
        val pieceHeight = croppedImageHeight / rows


        var yCoordination = 0
        for (row in 0 until rows) {
            var xCoordination = 0
            for (col in 0 until cols) {
                var offsetX = 0
                var offsetY = 0
                if (col > 0) {
                    offsetX = pieceWidth / 3
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3
                }

                val pieceBitmap = Bitmap.createBitmap(
                    croppedBitmap,
                    xCoordination - offsetX,
                    yCoordination - offsetY,
                    pieceWidth + offsetX,
                    pieceHeight + offsetY
                )

                val piece = PuzzlePiece(applicationContext)
                piece.setImageBitmap(pieceBitmap)
                piece.xCoordination = xCoordination - offsetX + imageView.left
                piece.yCoordination = yCoordination - offsetY + imageView.top
                piece.pieceWidth = pieceWidth + offsetX
                piece.pieceHeight = pieceHeight + offsetY

                val puzzlePiece = Bitmap.createBitmap(
                    pieceWidth + offsetX,
                    pieceHeight + offsetY,
                    Bitmap.Config.ARGB_8888
                )

                val bumpSize = pieceHeight / 4
                val canvas = Canvas(puzzlePiece)
                val path = Path()
                path.moveTo(offsetX.toFloat(), offsetY.toFloat())
                if (row == 0) {
                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                } else {
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        offsetY.toFloat()
                    )
                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 6).toFloat(),
                        (offsetY - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        (offsetY - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        offsetY.toFloat()
                    )
                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                }
                if (col == cols - 1) {
                    path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
                } else {
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )
                    path.cubicTo(
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        (pieceBitmap.width - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat()
                    )
                    path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())
                }
                if (row == rows - 1) {
                    path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
                } else {
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                    path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
                }
                if (col == 0) {
                    path.close()
                } else {
                    path.lineTo(
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat()
                    )
                    path.cubicTo(
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        (offsetX - bumpSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )
                    path.close()
                }

                val paint = Paint()
                paint.color = -0x1000000
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(pieceBitmap, 0f, 0f, paint)

                var border = Paint()
                border.color = -0x7f000001
                border.style = Paint.Style.STROKE
                border.strokeWidth = 8.0f
                canvas.drawPath(path, border)

                border = Paint()
                border.color = -0x80000000
                border.style = Paint.Style.STROKE
                border.strokeWidth = 3.0f
                canvas.drawPath(path, border)

                piece.setImageBitmap(puzzlePiece)
                pieces.add(piece)
                xCoordination += pieceWidth
            }
            yCoordination += pieceHeight
        }
        return pieces
    }

    private fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray {
        val ret = IntArray(4)
        if (imageView == null || imageView.drawable == null) return ret

        val f = FloatArray(9)
        imageView.imageMatrix.getValues(f)

        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]

        val d = imageView.drawable
        val origW = d.intrinsicWidth
        val origH = d.intrinsicHeight

        val actW = (origW * scaleX).roundToInt()
        val actH = (origH * scaleY).roundToInt()
        ret[2] = actW
        ret[3] = actH

        val imgViewW = imageView.width
        val imgViewH = imageView.height
        val top = (imgViewH - actH) / 2
        val left = (imgViewW - actW) / 2
        ret[0] = left
        ret[1] = top
        return ret
    }

    fun checkGameOver()  {
        if (isGameOver) {
            AlertDialog.Builder(this@PuzzleActivity)
                .setTitle("Вы победили .. !!")
                .setIcon(R.drawable.ic_celebration)
                .setMessage("Вы победили...\nВы хотите новую игру ..?")
                .setPositiveButton("Да") {
                    dialog,_->
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") {
                    dialog, _->
                    finish()
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private val isGameOver:Boolean
        get() {
            for (piece in pieces!!) {
                if (piece!!.canMove) {
                    return false
                }
            }
            return true
        }

    private fun setPicFromPath(mCurrentPhotoPath: String, imageView: ImageView) {
        val targetW = imageView.width
        val targetH = imageView.height

        val bmOptions = Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        val scaleFactor = min(photoW / targetW, photoH / targetH)
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        var rotatedBitmap = bitmap

        try {
            val ei = ExifInterface(mCurrentPhotoPath)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(bitmap, 270f)
            }
        } catch (e: IOException) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
        imageView.setImageBitmap(rotatedBitmap)
    }


}