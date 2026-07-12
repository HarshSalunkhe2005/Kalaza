package com.kalazacare.app.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalazacare.app.data.model.UtilityItem
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.OnSurface
import com.kalazacare.app.ui.theme.OnSurfaceVariant
import com.kalazacare.app.ui.theme.White

@Composable
fun UtilItemsEditor(
    items: List<UtilityItem>,
    onAddItem: (UtilityItem) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
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
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Unit: ${item.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
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
            contentColor = White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Utility Item")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Utility Item") },
                text = { Text("Form for Item Name and Unit would go here.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onAddItem(
                                UtilityItem(
                                    name = "New Item",
                                    unit = "pcs",
                                    displayOrder = 99
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
