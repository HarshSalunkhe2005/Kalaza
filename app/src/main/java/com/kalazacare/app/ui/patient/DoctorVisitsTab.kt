package com.kalazacare.app.ui.patient

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
import com.kalazacare.app.data.model.DoctorVisit
import com.kalazacare.app.ui.DoctorVisitViewModel
import com.kalazacare.app.ui.components.EmptyState
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.util.SessionManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DoctorVisitsTab(
    visits: List<DoctorVisit>,
    patientId: String,
    viewModel: DoctorVisitViewModel,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (visits.isEmpty()) {
            EmptyState(
                title = "No Visits",
                message = "No doctor visits recorded yet.",
                icon = Icons.Default.Add
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visits) { visit ->
                    DoctorVisitCard(visit)
                }
            }
        }

        if (SessionManager.isAdmin()) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = KalazaRed,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add Doctor Visit")
            }
        }
    }

    if (showAddDialog) {
        AddDoctorVisitDialog(
            patientId = patientId,
            onDismiss = { showAddDialog = false },
            onSave = { visit ->
                viewModel.addVisit(visit)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DoctorVisitCard(visit: DoctorVisit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dr. ${visit.doctorName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KalazaRed
                )
                Text(
                    text = visit.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = visit.specialty,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Notes: ${visit.notes}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (visit.prescriptionChanges.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Prescription Changes: ${visit.prescriptionChanges}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (visit.nextVisitDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Next Visit: ${visit.nextVisitDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AddDoctorVisitDialog(
    patientId: String,
    onDismiss: () -> Unit,
    onSave: (DoctorVisit) -> Unit,
) {
    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var prescriptionChanges by remember { mutableStateOf("") }
    var nextVisitDays by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Doctor Visit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Specialty") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prescriptionChanges,
                    onValueChange = { prescriptionChanges = it },
                    label = { Text("Prescription Changes") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextVisitDays,
                    onValueChange = { nextVisitDays = it },
                    label = { Text("Next visit in (days)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = nextVisitDays.toLongOrNull()
                    val nextDate = if (days != null) LocalDate.now().plusDays(days) else null
                    onSave(
                        DoctorVisit(
                            patientId = patientId,
                            doctorName = doctorName,
                            specialty = specialty,
                            notes = notes,
                            prescriptionChanges = prescriptionChanges,
                            nextVisitDate = nextDate
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
                enabled = doctorName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
