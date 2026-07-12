package com.kalazacare.app.ui.mar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.data.model.MedStatus
import com.kalazacare.app.data.model.MedicationEntry
import com.kalazacare.app.ui.MarViewModel
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.White
import com.kalazacare.app.util.SessionManager
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun MarScreen(
    patientId: String,
    viewModel: MarViewModel = viewModel()
) {
    val medications by viewModel.medications.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.load(patientId, LocalDate.now())
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
                        text = "Medication Record (MAR)",
                        style = MaterialTheme.typography.titleLarge,
                        color = KalazaRed
                    )
                }
            }

            MarTable(
                medications = medications,
                onMarkAdministered = { id -> viewModel.markAdministered(id) },
                modifier = Modifier.weight(1f)
            )
        }

        if (SessionManager.isAdmin()) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = KalazaRed,
                contentColor = White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Medication")
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Medication") },
                text = { Text("Form fields for medicine name, dose, quantity, schedule time would go here.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addMedication(
                                MedicationEntry(
                                    patientId = patientId,
                                    medicineName = "New Med",
                                    dose = "10mg",
                                    quantity = "1 tablet",
                                    scheduleTime = LocalTime.now(),
                                    scheduledDate = LocalDate.now(),
                                    status = MedStatus.PENDING
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
