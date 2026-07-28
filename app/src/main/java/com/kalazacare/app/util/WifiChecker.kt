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
const val ALLOWED_WIFI_SSID = "LHBC_Students"

/**
 * The currently-connected Wi-Fi network's name, or null if not connected to Wi-Fi at all, or if
 * it couldn't be read (e.g. permission not granted, or device location services are off).
 *
 * Deliberately scans every network rather than just `activeNetwork`: when a VPN is running,
 * Android reports the VPN tunnel as the "active" network (since that's what carries app
 * traffic), which would otherwise make this look like there's no Wi-Fi connection at all even
 * though the device is still physically on the right Wi-Fi underneath the VPN.
 */
fun currentWifiSsid(context: Context): String? {
    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.allNetworks.firstNotNullOfOrNull { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                (capabilities.transportInfo as? WifiInfo)?.ssid
            } else null
        }
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
