package com.kalazacare.app.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalazacare.app.data.model.Staff
import com.kalazacare.app.data.model.UserRole
import com.kalazacare.app.ui.components.RoleBadge
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.OnSurface
import com.kalazacare.app.ui.theme.OnSurfaceVariant
import com.kalazacare.app.ui.theme.White
import com.kalazacare.app.util.DateUtils

@Composable
fun StaffEditor(
    staffList: List<Staff>,
    onAddStaff: (Staff) -> Unit,
    onRevokeStaff: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(staffList) { staff ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = staff.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                RoleBadge(isAdmin = staff.role == UserRole.ADMIN)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = staff.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Joined: ${DateUtils.formatDate(staff.joinedDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }

                        if (staff.isActive) {
                            TextButton(onClick = { onRevokeStaff(staff.id) }) {
                                Text("Revoke", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text(
                                text = "Revoked",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = KalazaRed,
            contentColor = White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Staff")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Staff") },
                text = { Text("Form for Name, Email, Password, and Role selection would go here.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onAddStaff(
                                Staff(
                                    name = "New Staff",
                                    email = "newstaff@kalazacare.com",
                                    role = UserRole.STAFF,
                                    phone = "+91 99999 00000"
                                )
                            )
                            showAddDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KalazaRed)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
