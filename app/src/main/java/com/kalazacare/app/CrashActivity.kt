package com.kalazacare.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kalazacare.app.ui.MainActivity
import com.kalazacare.app.ui.theme.KalazaTheme

/**
 * Shown instead of the OS crash dialog. Displays the complete exception + stack trace with
 * no line/length limit, so the user never sees a message cut off with "…".
 */
class CrashActivity : ComponentActivity() {
    companion object {
        const val EXTRA_STACK_TRACE = "stack_trace"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: "Unknown error"
        setContent {
            KalazaTheme {
                CrashScreen(stackTrace) {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
            }
        }
    }
}

@Composable
private fun CrashScreen(stackTrace: String, onRestart: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("Kalaza Care ran into a problem", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stackTrace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                    Text("Restart App")
                }
            }
        }
    }
}
