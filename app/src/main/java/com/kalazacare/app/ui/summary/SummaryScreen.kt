package com.kalazacare.app.ui.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.ui.SummaryViewModel
import com.kalazacare.app.ui.components.KalazaTopBar
import com.kalazacare.app.ui.theme.*
import com.kalazacare.app.util.DateUtils
import java.time.LocalDate

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel = viewModel(),
    onBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val patients by viewModel.patients.collectAsState()

    Scaffold(
        topBar = {
            KalazaTopBar(
                title = "Daily Summary",
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
            // Header with Date
            Surface(
                color = White,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = DateUtils.formatDateLong(selectedDate),
                        style = MaterialTheme.typography.titleLarge,
                        color = KalazaRed
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Facility Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Vitals Logged",
                            value = stats.vitalsRecorded.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Meds Given",
                            value = stats.medsAdministered.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Meds Pending",
                            value = stats.medsPending.toString(),
                            valueColor = if (stats.medsPending > 0) MaterialTheme.colorScheme.error else OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Approvals",
                            value = stats.pendingApprovals.toString(),
                            valueColor = if (stats.pendingApprovals > 0) MaterialTheme.colorScheme.error else OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Patient Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(patients) { patient ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = patient.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Room ${patient.roomNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = KalazaRed
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
