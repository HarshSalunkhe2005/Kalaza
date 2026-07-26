package com.kalazacare.app.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalazacare.app.data.model.UtilityItem
import com.kalazacare.app.ui.theme.KalazaRed

@Composable
fun UtilItemsEditor(
    items: List<UtilityItem>,
    onAddItem: (UtilityItem) -> Unit,
    onUpdateItem: (UtilityItem) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<UtilityItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Unit: ${item.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { editingItem = item }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Item",
                                tint = KalazaRed
                            )
                        }
                        IconButton(onClick = { onDeleteItem(item.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Item",
                                tint = MaterialTheme.colorScheme.error
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
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Utility Item")
        }

        if (showAddDialog) {
            UtilItemDialog(
                title = "Add Utility Item",
                confirmLabel = "Add",
                initialName = "",
                initialUnit = "pcs",
                onDismiss = { showAddDialog = false },
                onConfirm = { name, unit ->
                    onAddItem(UtilityItem(name = name, unit = unit, displayOrder = 99))
                    showAddDialog = false
                }
            )
        }

        editingItem?.let { item ->
            UtilItemDialog(
                title = "Edit Utility Item",
                confirmLabel = "Save",
                initialName = item.name,
                initialUnit = item.unit,
                onDismiss = { editingItem = null },
                onConfirm = { name, unit ->
                    onUpdateItem(item.copy(name = name, unit = unit))
                    editingItem = null
                }
            )
        }
    }
}

@Composable
private fun UtilItemDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    initialUnit: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, unit: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var unit by remember { mutableStateOf(initialUnit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g. pcs, pack)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, unit) },
                colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
                enabled = name.isNotBlank() && unit.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
