package com.alinag.imgeditor

import android.app.AlertDialog
import android.content.Context

class DialogHelper private constructor() {
    var permissionCallback: PermissionCallback? = null

    fun getRequestPermissionDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Grant permission")
                .setPositiveButton("Yes") { _, _ -> permissionCallback?.onPositiveButtonClicked(ACTION_PERMISSION_REQUEST) }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        return builder.create()
    }

    fun getOpenSettingsDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Open settings to grant permission")
                .setPositiveButton("Yes") { _, _ -> permissionCallback?.onPositiveButtonClicked(ACTION_OPEN_OPTIONS) }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        return builder.create()
    }

    interface PermissionCallback {
        fun onPositiveButtonClicked(action: Int)
    }

    private object Holder {
        val INSTANCE = DialogHelper()
    }

    companion object {
        const val ACTION_PERMISSION_REQUEST = 1
        const val ACTION_OPEN_OPTIONS = 2
        val instance: DialogHelper by lazy { Holder.INSTANCE }
    }
}