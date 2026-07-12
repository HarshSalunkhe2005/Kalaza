package com.kalazacare.app.ui.approval

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.data.model.ApprovalRequest
import com.kalazacare.app.data.model.ApprovalStatus
import com.kalazacare.app.ui.ApprovalViewModel
import com.kalazacare.app.ui.components.ApprovalStatusBadge
import com.kalazacare.app.ui.components.ConfirmDialog
import com.kalazacare.app.ui.components.EmptyState
import com.kalazacare.app.ui.components.KalazaTopBar
import com.kalazacare.app.ui.theme.*
import com.kalazacare.app.util.DateUtils

@Composable
fun ApprovalQueueScreen(
    viewModel: ApprovalViewModel = viewModel(),
    onBack: () -> Unit
) {
    val requests by viewModel.requests.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Approved", "Rejected")

    var approveDialogRequest by remember { mutableStateOf<ApprovalRequest?>(null) }
    var rejectDialogRequest by remember { mutableStateOf<ApprovalRequest?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            KalazaTopBar(
                title = "Approval Queue",
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
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = White,
                contentColor = KalazaRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = KalazaRed
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            val filteredRequests = when (selectedTabIndex) {
                0 -> requests.filter { it.status == ApprovalStatus.PENDING }
                1 -> requests.filter { it.status == ApprovalStatus.APPROVED }
                else -> requests.filter { it.status == ApprovalStatus.REJECTED }
            }

            if (filteredRequests.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Check,
                    title = "All Caught Up",
                    message = "No ${tabs[selectedTabIndex].lowercase()} requests to show."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRequests) { request ->
                        ApprovalRequestCard(
                            request = request,
                            onApprove = { approveDialogRequest = request },
                            onReject = { rejectDialogRequest = request }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Approve Dialog
        approveDialogRequest?.let { request ->
            ConfirmDialog(
                title = "Approve Change?",
                message = "Are you sure you want to approve this change for ${request.patientName}?",
                confirmText = "Approve",
                onConfirm = {
                    viewModel.approve(request.id)
                    approveDialogRequest = null
                },
                onDismiss = { approveDialogRequest = null }
            )
        }

        // Reject Dialog
        rejectDialogRequest?.let { request ->
            ConfirmDialog(
                title = "Reject Change?",
                message = "Are you sure you want to reject this change for ${request.patientName}?",
                confirmText = "Reject",
                isDestructive = true,
                onConfirm = {
                    viewModel.reject(request.id, "Rejected by Admin")
                    rejectDialogRequest = null
                },
                onDismiss = { rejectDialogRequest = null }
            )
        }
    }
}

@Composable
fun ApprovalRequestCard(
    request: ApprovalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.patientName,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )
                ApprovalStatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Field: ${request.fieldChanged}",
                style = MaterialTheme.typography.bodyMedium,
                color = KalazaRed
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Surface(
                color = SurfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Old: ${request.oldValue}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("New: ${request.newValue}", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Requested by ${request.requestedByName} • ${DateUtils.timeAgo(request.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )

                if (request.status == ApprovalStatus.PENDING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onReject) {
                            Icon(Icons.Filled.Close, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = onApprove) {
                            Icon(Icons.Filled.Check, contentDescription = "Approve", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
