package com.kalazacare.app.ui.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kalazacare.app.R
import com.kalazacare.app.ui.LoginState
import com.kalazacare.app.ui.LoginViewModel
import com.kalazacare.app.ui.components.KalazaTextField
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.util.ALLOWED_WIFI_SSID
import com.kalazacare.app.util.currentWifiSsid
import com.kalazacare.app.util.isLocationServicesEnabled
import com.kalazacare.app.util.wifiDebugDump
import kotlinx.coroutines.delay

private enum class WifiGateState {
    CHECKING, ALLOWED, WRONG_NETWORK, NEED_PERMISSION_EXPLANATION, PERMISSION_DENIED, LOCATION_SERVICES_OFF
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var gateState by remember { mutableStateOf(WifiGateState.CHECKING) }
    var detectedSsid by remember { mutableStateOf<String?>(null) }

    fun checkWifiNow() {
        detectedSsid = currentWifiSsid(context)
        gateState = when {
            detectedSsid == ALLOWED_WIFI_SSID -> WifiGateState.ALLOWED
            !isLocationServicesEnabled(context) -> WifiGateState.LOCATION_SERVICES_OFF
            else -> WifiGateState.WRONG_NETWORK
        }
    }

    fun requiredWifiPermissionsGranted(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val nearbyGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) ==
                PackageManager.PERMISSION_GRANTED
        } else true
        return fineGranted && nearbyGranted
    }

    val requestWifiPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { if (requiredWifiPermissionsGranted()) checkWifiNow() else gateState = WifiGateState.PERMISSION_DENIED }

    fun launchWifiPermissionRequest() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        requestWifiPermissions.launch(permissions)
    }

    LaunchedEffect(Unit) {
        if (requiredWifiPermissionsGranted()) {
            delay(500) // brief, deliberate pause so the "checking" spinner is actually visible
            checkWifiNow()
        } else {
            gateState = WifiGateState.NEED_PERMISSION_EXPLANATION
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .blur(if (gateState == WifiGateState.ALLOWED) 0.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo area
            Image(
                painter = painterResource(id = R.drawable.logo_kalaza),
                contentDescription = "Kalaza Care Logo",
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)),
            )
            
            Text(
                text = "Kalaza Care",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = KalazaRed
            )
            Text(
                text = "A Place Like Home",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Error message
            if (loginState is LoginState.Error) {
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Input fields
            KalazaTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            KalazaTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login button
            Button(
                onClick = { viewModel.login(name, password) },
                enabled = gateState == WifiGateState.ALLOWED,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (loginState is LoginState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "LOGIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (gateState) {
            WifiGateState.CHECKING -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = KalazaRed)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Checking Wi-Fi network…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            WifiGateState.NEED_PERMISSION_EXPLANATION -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Wi-Fi Check Needed") },
                    text = {
                        Text(
                            "Kalaza Care only works on the facility's Wi-Fi network. Android " +
                                "requires location permission just to read which Wi-Fi network " +
                                "you're connected to — the app never tracks or stores your location."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { launchWifiPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
                        ) { Text("Continue") }
                    },
                )
            }
            WifiGateState.PERMISSION_DENIED -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Permission Required") },
                    text = {
                        Text(
                            "Without this permission the app can't verify you're on the right " +
                                "Wi-Fi network. Grant it in App Settings, then retry."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
                        ) { Text("Open App Settings") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            if (requiredWifiPermissionsGranted()) checkWifiNow() else gateState = WifiGateState.NEED_PERMISSION_EXPLANATION
                        }) { Text("Retry") }
                    },
                )
            }
            WifiGateState.WRONG_NETWORK -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Wrong Wi-Fi Network") },
                    text = {
                        Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                            Text("Please connect to the \"$ALLOWED_WIFI_SSID\" Wi-Fi network to continue.")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Debug info:\n" + wifiDebugDump(context),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                            colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
                        ) { Text("Open Wi-Fi Settings") }
                    },
                    dismissButton = {
                        TextButton(onClick = { checkWifiNow() }) { Text("Retry") }
                    },
                )
            }
            WifiGateState.LOCATION_SERVICES_OFF -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Location Services Needed") },
                    text = {
                        Text(
                            "Android won't reveal the connected Wi-Fi network's name unless " +
                                "the device's Location Services toggle is on — this is separate " +
                                "from the permission you already granted. Turn it on, then retry."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                            colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
                        ) { Text("Open Location Settings") }
                    },
                    dismissButton = {
                        TextButton(onClick = { checkWifiNow() }) { Text("Retry") }
                    },
                )
            }
            WifiGateState.ALLOWED -> {}
        }
    }
}
