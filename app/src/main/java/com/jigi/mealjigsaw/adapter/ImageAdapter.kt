package com.jigi.mealjigsaw.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.jigi.mealjigsaw.R
import kotlinx.coroutines.*
import java.io.IOException

class ImageAdapter(private val mContext: Context) : BaseAdapter() {

    private var files: Array<String>? = null

    override fun getCount(): Int { return files!!.size }
    override fun getItem(position: Int): Any? { return null }
    override fun getItemId(position: Int): Long { return 0 }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val v = LayoutInflater.from(mContext).inflate(R.layout.grid_element,null)
        val imageView = v.findViewById<ImageView>(R.id.gridImageview)
        imageView.setImageBitmap(null)
        imageView.post {
            var bitmap: Bitmap?
            runBlocking {
                bitmap = getPicFromAsset(imageView, files!![position])
                return@runBlocking
            }
            imageView.setImageBitmap(bitmap)
        }
        return v
    }

    private fun getPicFromAsset(imageView: ImageView, assetName: String): Bitmap? {
        val targetW = imageView.width
        val targetH = imageView.height

        return if (targetW == 0 || targetH == 0) {
            null
        } else try {
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true

            BitmapFactory.decodeStream(
                mContext.assets.open("img/$assetName"),
                Rect(-1, -1, -1, -1), bmOptions
            )

            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight

            val scaleFactor = (photoW / targetW).coerceAtMost(photoH / targetH)
            mContext.assets.open("img/$assetName").reset()

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            BitmapFactory.decodeStream(
                mContext.assets.open("img/$assetName"),
                Rect(-1, -1, -1, -1), bmOptions
            )

        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    init {
        try {
            files = mContext.assets.list("img")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}