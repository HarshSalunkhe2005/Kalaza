package com.kalazacare.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

/**
 * Master switch for the whole Wi-Fi gate — set to false to skip the check entirely and let
 * anyone log in regardless of network, for quick testing of everything else in the app while
 * the SSID-detection issue on newer Android is still being tracked down.
 * MUST be back to true before this ships to Kalaza Care.
 */
const val WIFI_GATE_ENABLED = true

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

/**
 * Beyond the app's own runtime permission, Android also silently refuses to reveal the real
 * SSID (returning "<unknown ssid>") unless the device's system-wide Location Services toggle is
 * on -- a classic gotcha since this has nothing to do with the app actually reading location.
 * Checked separately so the UI can tell "wrong network" apart from "can't check yet".
 */
fun isLocationServicesEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

/** Temporary diagnostic dump — shows exactly what each permission/API call returns on this device. */
fun wifiDebugDump(context: Context): String {
    val sb = StringBuilder()
    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val wifiStateGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) ==
        PackageManager.PERMISSION_GRANTED
    val nearbyWifiGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
    } else null
    sb.appendLine("SDK: ${Build.VERSION.SDK_INT}, Manufacturer: ${Build.MANUFACTURER} ${Build.MODEL}")
    sb.appendLine("ACCESS_FINE_LOCATION granted: $fineGranted")
    sb.appendLine("ACCESS_WIFI_STATE granted: $wifiStateGranted")
    sb.appendLine("NEARBY_WIFI_DEVICES granted: ${nearbyWifiGranted ?: "n/a (below API 33)"}")
    sb.appendLine("Location services enabled: ${isLocationServicesEnabled(context)}")
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val networkProviderEnabled = try { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
    sb.appendLine("Network location provider enabled: ${networkProviderEnabled ?: "unknown"}")
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networks = connectivityManager.allNetworks
            sb.appendLine("Networks found: ${networks.size}")
            networks.forEachIndexed { i, network ->
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                val transportInfo = capabilities?.transportInfo
                val ssid = (transportInfo as? WifiInfo)?.ssid
                sb.appendLine("  [$i] wifi=$isWifi transportInfo=${transportInfo?.javaClass?.simpleName} ssid=$ssid")
            }
        } else {
            @Suppress("DEPRECATION")
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            sb.appendLine("legacy WifiManager ssid=${wifiManager.connectionInfo.ssid}")
        }
    } catch (e: Exception) {
        sb.appendLine("Exception: $e")
    }
    return sb.toString()
}
