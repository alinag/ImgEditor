package com.alinag.imgeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.epam.beacons.Coordinate
import com.epam.beacons.Pivot
import com.epam.beacons.trilateration.TrilaterationSolver
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import kotlinx.android.synthetic.main.activity_main.bw_filter_button as bwFilterButton
import kotlinx.android.synthetic.main.activity_main.choose_image_button as chooseButton
import android.content.ContentResolver
import java.io.InputStream


class MainActivity : AppCompatActivity() {


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_edit)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val t = TrilaterationSolver()
        val c = t.solve(listOf(Pivot(Coordinate(0.0, 0.0), 1.5), Pivot(Coordinate(1.0, 1.0), 3.5)))
        println(c)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        chooseButton.setOnClickListener { onChooseImageButtonClicked() }
        bwFilterButton.setOnClickListener { onBwFilterButtonClick() }
    }

    private fun onChooseImageButtonClicked() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        startActivityForResult(Intent.createChooser(intent, "котики"), SELECTED_PICTURE)
        startActivityForResult(intent, SELECTED_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SELECTED_PICTURE -> {
                val uri = data?.data
                val projection = arrayOf(MediaStore.Images.Media.DATA)

                val cursor = contentResolver.query(uri, projection, null, null, null)
                cursor.moveToFirst()

                val columnIndex = cursor.getColumnIndex(projection[0])
                val filePath = cursor.getString(columnIndex)
                cursor.close()

                imageView.apply {
                    setImageURI(uri)
                    colorFilter = null
                }
                print(uri?.lastPathSegment.toString())
//                resizeImage(uri.toString())

//                val newuri = getPathFromFile(uri!!.path)

//                resizeImage(getPathFromFile(filePath).toString())
                resize2(Uri.parse(filePath))
//                resizeImage(uri!!.path)
            }
        }
    }

    private fun onBwFilterButtonClick() = imageView.apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) }

    private fun save() {

        val bitmap = imageView.getDrawingCache()
//        resizeImage(file)
    }

    fun resizeImage(/*file: File, */path: String, scaleTo: Int = 1024) {
        val newFile = File(Environment.getExternalStorageDirectory().absolutePath /*filesDir*/, "qwe.jpeg")
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(/*file.absolutePath*/ path, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = Math.min(photoW / scaleTo, photoH / scaleTo)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        val oldFile = BitmapFactory.decodeFile(path, bmOptions) ?: return

        val stream = FileOutputStream(File(Environment.getExternalStorageDirectory().absolutePath + path))
        stream.flush()
        stream.close()

        newFile.outputStream().use {
            oldFile.compress(Bitmap.CompressFormat.JPEG, 75, it)
            oldFile.recycle()
        }
    }

    fun resize2(uri: Uri){
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        val cr = contentResolver
        var input: InputStream? = null
        var input1: InputStream? = null
        try {
            input = cr.openInputStream(uri)
            val oldFile = BitmapFactory.decodeStream(input, null, bmOptions)

            val newFile = File(Environment.getExternalStorageDirectory().absolutePath /*filesDir*/, "qwe.jpeg")
            newFile.outputStream().use {
                oldFile.compress(Bitmap.CompressFormat.JPEG, 75, it)
                oldFile.recycle()
            }
            if (input != null) {
                input!!.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


//        val photoW = bmOptions.outWidth
//        val photoH = bmOptions.outHeight
//        try {
//            input1 = cr.openInputStream(uri)
//            val takenImage = BitmapFactory.decodeStream(input1)
//            if (input1 != null) {
//                input1!!.close()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

    }

    fun Uri.getExternalPath() = if (lastPathSegment.split(COLON_CHAR)[0] == PRIMARY_PREFIX) {
        externalStoragePath + lastPathSegment.substring(lastPathSegment.indexOf(COLON_CHAR) + 1)
    } else {
        externalStoragePath + lastPathSegment
    }

    fun getPathFromFile(filePath: String): Uri {

        return FileProvider.getUriForFile(this, AUTHORITY, File(filePath))
    }

    companion object {
        private const val PRIMARY_PREFIX = "primary"
        private const val COLON_CHAR = ":"
        private const val SLASH = "/"
        private val externalStoragePath = Environment.getExternalStorageDirectory().toString() + SLASH
        private const val AUTHORITY = "com.alinag.imgeditor.fileprovider"
        private const val SELECTED_PICTURE = 1
    }
}
