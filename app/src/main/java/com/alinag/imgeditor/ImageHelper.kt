package com.epam.test

import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileOutputStream

class ImageHelper private constructor() {
    private var resized: Bitmap? = null

    fun writeToFile() {
        val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/imgEditor"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()

        val fileName = "edited pic" + System.currentTimeMillis() + ".jpeg"
        val file = File(dir, fileName)

        var fOut: FileOutputStream? = null
        try {
            fOut = FileOutputStream(file)
            resized?.compress(Bitmap.CompressFormat.JPEG, 75, fOut)
            fOut.flush()

        } catch (e: Exception) {

        } finally {
            fOut?.close()
        }
    }

    fun getScaledImage(selectedImage: Uri, context: Context): Bitmap? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val whereArgs = arrayOf("image/jpg", "image/jpeg", "image/png")

        val where = (MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?")

        try {
            val cursor = context.contentResolver.query(selectedImage, filePathColumn, where, whereArgs, null) ?: return null
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            if (columnIndex < 0) return null

            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            val resource = BitmapFactory.decodeFile(picturePath) ?: return null
            val originalWidth = resource.width.toFloat()
            val originalHeight = resource.height.toFloat()

            val currentMaxSideSize = Math.max(originalWidth, originalHeight)

            val scale = Math.min(1000 / currentMaxSideSize, 1f)

            resized = Bitmap.createScaledBitmap(resource, (originalWidth * scale).toInt(), (originalHeight * scale).toInt(), true)
            if (resource !== resized) resource.recycle()
        } catch (e: CursorIndexOutOfBoundsException) {
            launch(UI) {
                Toast.makeText(context, "Crashed", Toast.LENGTH_SHORT).show()
            }
        }

        return resized
    }

    fun clear() {
        resized?.recycle()
    }

    private object Holder {
        val INSTANCE = ImageHelper()
    }

    companion object {
        val instance: ImageHelper by lazy { Holder.INSTANCE }
    }
}