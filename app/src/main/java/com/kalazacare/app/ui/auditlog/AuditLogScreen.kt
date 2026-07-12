package com.kalazacare.app.ui.auditlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.data.model.AuditLogEntry
import com.kalazacare.app.ui.AuditLogViewModel
import com.kalazacare.app.ui.components.EmptyState
import com.kalazacare.app.ui.components.KalazaTopBar
import com.kalazacare.app.ui.theme.*
import com.kalazacare.app.util.DateUtils

@Composable
fun AuditLogScreen(
    viewModel: AuditLogViewModel = viewModel(),
    onBack: () -> Unit
) {
    val logs by viewModel.logs.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            KalazaTopBar(
                title = "Audit Log",
                showBack = false
            )
        },
        containerColor = SurfaceVariant
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (logs.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Info,
                    title = "No Logs Found",
                    message = "System audit logs will appear here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(logs.size) { index ->
                        AuditLogItem(
                            entry = logs[index],
                            isLast = index == logs.lastIndex
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(entry: AuditLogEntry, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(KalazaRed)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(Outline)
                        .padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Content Card
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.action,
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.timeAgo(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${entry.performedByName} on ${entry.targetPatientName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface
                )
                
                if (entry.details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}
