package com.alinag.imgeditor

import android.Manifest
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
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import kotlinx.android.synthetic.main.activity_main.bw_filter_button as bwFilterButton
import kotlinx.android.synthetic.main.activity_main.choose_image_button as chooseButton
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.epam.test.DialogHelper
import com.epam.test.DialogHelper.Companion.ACTION_OPEN_OPTIONS
import com.epam.test.DialogHelper.Companion.ACTION_PERMISSION_REQUEST
import com.epam.test.ImageHelper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.InputStream


class MainActivity : AppCompatActivity(), DialogHelper.PermissionCallback, ImageHelper.ErrorCallback {
    private val rootParent = Job()
    private lateinit var imageHelper: ImageHelper
    private lateinit var dialogHelper: DialogHelper

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

        imageHelper = ImageHelper.instance.apply {
            errorCallback = this@MainActivity
        }
        dialogHelper = DialogHelper.instance.apply {
            permissionCallback = this@MainActivity
        }


        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        chooseButton.setOnClickListener { onChooseImageButtonClicked() }
        bwFilterButton.setOnClickListener { onBwFilterButtonClick() }
    }

    override fun onPositiveButtonClicked(action: Int) {
        when (action) {
            ACTION_PERMISSION_REQUEST -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MainActivity.REQUEST_WRITE_STORAGE)
            ACTION_OPEN_OPTIONS -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
        }
    }

    private fun onChooseImageButtonClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            dialogHelper.getRequestPermissionDialog(this).show()
        } else {
            openGallery()
        }
    }
    private fun onSaveImageButtonClicked() {
        inner@ launch(CommonPool, parent = rootParent) {
            launch(UI, parent = rootParent) {
                Toast.makeText(this@MainActivity, "Start", Toast.LENGTH_SHORT).show()
            }
            imageHelper.writeToFile()

            launch(UI, parent = rootParent) {
                Toast.makeText(this@MainActivity, "Finish", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun onBwFilterButtonClick() = imageView.apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) }

    private fun openGallery() {
        val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    dialogHelper.getRequestPermissionDialog(this).show()
                } else {
                    dialogHelper.getOpenSettingsDialog(this).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {

            inner@ launch(CommonPool, parent = rootParent) {

                launch(UI, parent = rootParent) {
                    Toast.makeText(this@MainActivity, "Start", Toast.LENGTH_SHORT).show()
                }

                val resized = imageHelper.getScaledImage(data.data, this@MainActivity)

                launch(UI, parent = rootParent) {
                    if (resized != null) imageView.setImageBitmap(resized)
                    Toast.makeText(this@MainActivity, "Finish", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onImageLoadedError() {
        launch(UI, parent = rootParent) {
            Toast.makeText(this@MainActivity, "Choose another image format, bitch", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        rootParent.cancel()
        imageHelper.clear()

        super.onDestroy()
    }

    companion object {
        private const val REQUEST_CODE = 1
        private const val REQUEST_WRITE_STORAGE = 2
        private const val PRIMARY_PREFIX = "primary"
        private const val COLON_CHAR = ":"
        private const val SLASH = "/"
        private val externalStoragePath = Environment.getExternalStorageDirectory().toString() + SLASH
        private const val AUTHORITY = "com.alinag.imgeditor.fileprovider"
        private const val SELECTED_PICTURE = 1
    }
}
