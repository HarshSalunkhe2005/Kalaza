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
 * The facility Wi-Fi router(s)' gateway/local IP address (e.g. "192.168.1.1") -- the ONLY way
 * this app tells "am I on the right network". SSID matching was dropped entirely: several
 * Android 13+ devices (a test Vivo included) kept the SSID redacted as "<unknown ssid>" no
 * matter what -- every documented permission granted (ACCESS_FINE_LOCATION, NEARBY_WIFI_DEVICES,
 * Location Services, the network location provider), still unresolved. Gateway/routing info was
 * never part of that same privacy carve-out, since it's not considered location-identifying the
 * way a Wi-Fi network name is, so it's proven to actually work where SSID didn't.
 *
 * To find this on a phone already connected to the target network: Settings -> Wi-Fi -> tap the
 * connected network -> look for "Gateway" or "Router" under its IP details (may be under an
 * "Advanced" section depending on the phone). Add that value to this set.
 */
val ALLOWED_GATEWAY_IPS = setOf("10.24.64.1", "192.168.0.1")

/**
 * The connected Wi-Fi network's gateway/router IP address, or null if not connected to Wi-Fi or
 * it couldn't be determined. Unlike SSID, this isn't gated behind location permission -- it's
 * ordinary IP routing info, available with just ACCESS_NETWORK_STATE.
 */
fun currentWifiGatewayIp(context: Context): String? {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiNetwork = connectivityManager.allNetworks.firstOrNull { network ->
        connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    } ?: return null
    val linkProperties = connectivityManager.getLinkProperties(wifiNetwork) ?: return null
    return linkProperties.routes.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress
}

fun isOnAllowedWifi(context: Context): Boolean {
    val gateway = currentWifiGatewayIp(context)
    return gateway != null && ALLOWED_GATEWAY_IPS.contains(gateway)
}

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
    sb.appendLine("Detected gateway IP: ${currentWifiGatewayIp(context) ?: "none"} (allowed: $ALLOWED_GATEWAY_IPS)")
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
