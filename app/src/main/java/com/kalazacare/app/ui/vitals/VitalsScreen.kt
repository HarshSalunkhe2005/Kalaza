package com.kalazacare.app.ui.vitals

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.data.model.VitalRecord
import com.kalazacare.app.ui.VitalsViewModel
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.White

@Composable
fun VitalsScreen(
    patientId: String,
    viewModel: VitalsViewModel = viewModel()
) {
    val vitals by viewModel.vitals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.load(patientId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header for Vitals section
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
                        text = "Vitals Log",
                        style = MaterialTheme.typography.titleLarge,
                        color = KalazaRed
                    )
                }
            }

            // Vitals Table
            VitalsTable(
                vitals = vitals,
                modifier = Modifier.weight(1f)
            )
        }

        // Add FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = KalazaRed,
            contentColor = White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Vitals")
        }

        if (showAddDialog) {
            // A simple placeholder for the add dialog/bottom sheet
            // In a real app this would be a full form or bottom sheet
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Vitals") },
                text = { Text("Form fields for pulse, BP, SpO2, Temp, Sugar would go here.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addVital(
                                VitalRecord(
                                    id = "v_${System.currentTimeMillis()}",
                                    patientId = patientId,
                                    pulse = "80",
                                    bp = "120/80",
                                    spo2 = "98",
                                    temperature = "98.6"
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
