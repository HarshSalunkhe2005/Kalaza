package com.kalazacare.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.R
import com.kalazacare.app.ui.LoginState
import com.kalazacare.app.ui.LoginViewModel
import com.kalazacare.app.ui.components.KalazaTextField
import com.kalazacare.app.ui.theme.KalazaDarkMaroon
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.OnSurfaceVariant
import com.kalazacare.app.ui.theme.White

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo Placeholder
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Using default icon as placeholder
            contentDescription = "Kalaza Care Logo",
            modifier = Modifier.size(120.dp),
            tint = KalazaRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Kalaza Care",
            style = MaterialTheme.typography.displayMedium,
            color = KalazaDarkMaroon
        )
        Text(
            text = "A Place Like Home",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        KalazaTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = loginState is LoginState.Error
        )

        Spacer(modifier = Modifier.height(16.dp))

        KalazaTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            isError = loginState is LoginState.Error
        )

        if (loginState is LoginState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (loginState as LoginState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KalazaRed,
                contentColor = White
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(
                    color = White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
