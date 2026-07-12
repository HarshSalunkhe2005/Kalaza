package com.kalazacare.app.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.ui.PatientViewModel
import com.kalazacare.app.ui.components.KalazaTopBar
import com.kalazacare.app.ui.theme.*
import com.kalazacare.app.util.DateUtils
import com.kalazacare.app.util.toInitials

@Composable
fun PatientProfileScreen(
    patientId: String,
    viewModel: PatientViewModel = viewModel(),
    onBack: () -> Unit,
    onEditPatient: () -> Unit
) {
    val patient by viewModel.patient.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.load(patientId)
    }

    Scaffold(
        topBar = {
            KalazaTopBar(
                title = "Patient Profile",
                showBack = true,
                onBack = onBack,
                actions = {
                    IconButton(onClick = onEditPatient) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Patient")
                    }
                }
            )
        },
        containerColor = SurfaceVariant
    ) { innerPadding ->
        if (patient == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KalazaRed)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header section
                Surface(
                    color = White,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            Text(
                                text = patient!!.name.toInitials(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = KalazaDarkMaroon,
                                    fontWeight = FontWeight.Bold,
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = patient!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${patient!!.age} yrs • ${patient!!.gender.name} • Room ${patient!!.roomNo}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (patient!!.primaryDiagnosis.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50.dp),
                            ) {
                                Text(
                                    text = patient!!.primaryDiagnosis,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = KalazaDarkMaroon,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Details Sections
                PatientSection(title = "Medical History", content = patient!!.medicalHistory)
                PatientSection(title = "Current Issues", content = patient!!.currentIssues)
                PatientSection(title = "Allergies", content = patient!!.allergies)
                
                PatientSection(title = "Emergency Contact") {
                    Column {
                        Text(text = patient!!.emergencyContact, style = MaterialTheme.typography.bodyLarge)
                        Text(text = patient!!.emergencyPhone, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                }

                PatientSection(title = "Admission Info") {
                    Text(
                        text = "Admitted on: ${DateUtils.formatDateLong(patient!!.admissionDate)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PatientSection(
    title: String,
    content: String? = null,
    contentComposable: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = KalazaRed,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (content != null) {
                Text(
                    text = content.ifBlank { "None reported" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurface
                )
            }
            if (contentComposable != null) {
                contentComposable()
            }
        }
    }
}
