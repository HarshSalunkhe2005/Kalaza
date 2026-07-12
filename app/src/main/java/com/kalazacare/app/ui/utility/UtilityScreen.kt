package com.kalazacare.app.ui.utility

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.data.model.UtilityRecord
import com.kalazacare.app.ui.UtilityViewModel
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.White

@Composable
fun UtilityScreen(
    patientId: String,
    viewModel: UtilityViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val items by viewModel.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.load(patientId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = White,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Medical Utility Chart",
                        style = MaterialTheme.typography.titleLarge,
                        color = KalazaRed
                    )
                }
            }

            UtilityTable(
                records = records,
                modifier = Modifier.weight(1f)
            )
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = KalazaRed,
            contentColor = White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Utility Record")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Utility Record") },
                text = { Text("Form fields for Face Mask, Diapers, Gloves, etc. would go here.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addRecord(
                                UtilityRecord(
                                    patientId = patientId,
                                    faceMask = 2,
                                    diaperPant = 1,
                                    diaperStitch = 0,
                                    handGloves = 4,
                                    tinaBed = 1,
                                    wetWipes = 5,
                                    issuedToCaregiver = "Jane",
                                    issuedBySupervisor = "Admin"
                                )
                            )
                            showAddDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KalazaRed)
                    ) {
                        Text("Save")
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
