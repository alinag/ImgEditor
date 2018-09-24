package com.alinag.imgeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

class ImageHelper private constructor() {
    private var resized: Bitmap? = null
    private var filePath: String? = null

    fun writeToFile(context: Context) {
        val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/imgEditor"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()

        val fileName = "edited pic" + System.currentTimeMillis() + ".jpg"
        val file = File(dir, fileName)
        filePath = file.path

        var fOut: FileOutputStream? = null
        try {
            fOut = FileOutputStream(file)
            resized?.compress(Bitmap.CompressFormat.JPEG, 70, fOut)
            fOut.flush()
            MediaScannerConnection.scanFile(context, arrayOf(filePath), arrayOf("image/jpg"), null)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fOut?.close()
        }
    }

    fun getScaledImage(selectedImage: Uri, context: Context): Bitmap? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = context.contentResolver.query(selectedImage, filePathColumn, null, null, null) ?: return null

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            if (columnIndex < 0) return null

            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            BitmapFactory.Options().apply {
                inJustDecodeBounds = true

                BitmapFactory.decodeFile(picturePath, this)

                inSampleSize = calculateInSampleSize(this, REQ_SIDE_SIZE)

                inJustDecodeBounds = false

                resized = BitmapFactory.decodeFile(picturePath, this) ?: return null
            }
        }

        return resized
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqSideSize: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        val heightCoef = height.toDouble() / reqSideSize.toDouble()
        val widthCoef = width.toDouble() / reqSideSize.toDouble()

        if (heightCoef >= MAX_COEF || widthCoef >= MAX_COEF) {
            inSampleSize = Math.ceil(Math.max(heightCoef, widthCoef)).toInt()
        }

        return inSampleSize
    }

    fun clear() {
        resized?.recycle()
        resized = null
        filePath = null
    }

    private object Holder {
        val INSTANCE = ImageHelper()
    }

    companion object {
        val instance: ImageHelper by lazy { Holder.INSTANCE }
        private const val MAX_COEF = 1.25
        private const val REQ_SIDE_SIZE = 1000
    }
}