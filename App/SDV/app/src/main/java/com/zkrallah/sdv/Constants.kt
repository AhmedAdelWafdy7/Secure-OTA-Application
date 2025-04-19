package com.zkrallah.sdv

import android.content.Context
import android.widget.Toast
import com.zkrallah.sdv.domain.models.BottomNavItem

const val CLIENT_ID = "android_client"
const val BROKER_URL = BuildConfig.MQTT_BROKER_URL

val SCREENS = listOf(
    BottomNavItem(
        "Home",
        "home",
        selectedIcon = R.drawable.ic_home_filled,
        unSelectedIcon = R.drawable.ic_home_outlined
    ),
    BottomNavItem(
        "History",
        "history",
        selectedIcon = R.drawable.ic_history_filled,
        unSelectedIcon = R.drawable.ic_history_filled
    ),
    BottomNavItem(
        "Account",
        "account",
        selectedIcon = R.drawable.ic_person_filled,
        unSelectedIcon = R.drawable.ic_person_outlined
    ),
)

val ROUTES = listOf(
    "home"
)

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}