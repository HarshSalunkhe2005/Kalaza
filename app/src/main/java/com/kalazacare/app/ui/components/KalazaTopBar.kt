package com.kalazacare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalazacare.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalazaTopBar(
    title: String,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column {
        // Dark maroon status bar stripe at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(KalazaDarkMaroon)
        )
        TopAppBar(
            title = {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface,
                )
            },
            navigationIcon = {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface,
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor    = White,
                scrolledContainerColor = White,
                titleContentColor = OnSurface,
            ),
        )
        HorizontalDivider(color = Outline, thickness = 0.5.dp)
    }
}

@Composable
fun NotificationBell(badgeCount: Int = 0, onClick: () -> Unit = {}) {
    BadgedBox(
        badge = {
            if (badgeCount > 0) Badge(
                containerColor = KalazaRed,
                contentColor   = White,
            ) { Text("$badgeCount", style = MaterialTheme.typography.labelSmall) }
        }
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = OnSurfaceVariant,
            )
        }
    }
}
