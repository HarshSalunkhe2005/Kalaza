package com.kalazacare.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

/**
 * The one Wi-Fi network staff are allowed to log in from (Tier 1 network gate — checked once
 * at login, not enforced continuously). Currently set to the test network; swap this to Kalaza
 * Care's real SSID before rolling out there. This is a UX-level deterrent, not a hard security
 * boundary — it's checked in app code, so it doesn't replace RLS as the real access control.
 */
const val ALLOWED_WIFI_SSID = "CHANGE_ME_TEST_SSID"

/**
 * The currently-connected Wi-Fi network's name, or null if not connected to Wi-Fi at all, or if
 * it couldn't be read (e.g. permission not granted, or device location services are off).
 */
fun currentWifiSsid(context: Context): String? {
    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        (capabilities.transportInfo as? WifiInfo)?.ssid
    } else {
        @Suppress("DEPRECATION")
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        wifiManager.connectionInfo.ssid
    }
    val ssid = raw?.trim('"')
    return if (ssid.isNullOrBlank() || ssid == "<unknown ssid>") null else ssid
}

fun isOnAllowedWifi(context: Context): Boolean = currentWifiSsid(context) == ALLOWED_WIFI_SSID
