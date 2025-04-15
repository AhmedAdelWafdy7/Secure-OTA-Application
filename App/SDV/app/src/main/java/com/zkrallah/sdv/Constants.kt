package com.zkrallah.sdv

import android.content.Context
import android.widget.Toast

const val CLIENT_ID = "android_client"

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}