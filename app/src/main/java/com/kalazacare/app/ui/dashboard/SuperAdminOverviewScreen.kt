package com.kalazacare.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalazacare.app.data.model.MedStatus
import com.kalazacare.app.ui.ConfigViewModel
import com.kalazacare.app.ui.DailySummaryViewModel
import com.kalazacare.app.ui.DashboardViewModel
import com.kalazacare.app.ui.PatientDaySummary
import com.kalazacare.app.ui.SummaryViewModel
import com.kalazacare.app.ui.components.KalazaTopBar
import com.kalazacare.app.ui.components.MedStatusBadge
import com.kalazacare.app.ui.components.PatientCard
import com.kalazacare.app.ui.config.UtilItemsEditor
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.util.DateUtils
import java.time.LocalDate

/**
 * Super Admin's landing screen after login — a tabbed overview consolidating what used to
 * require jumping between separate screens: today's key numbers, a 7-day rollup (with a link
 * into the full date-range report), utility items, and the patient list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminOverviewScreen(
    dashboardViewModel: DashboardViewModel,
    summaryViewModel: SummaryViewModel,
    configViewModel: ConfigViewModel,
    dailySummaryViewModel: DailySummaryViewModel,
    onPatientClick: (String) -> Unit,
    onOpenFullReport: () -> Unit,
    onLogout: () -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Today's Update", "Weekly Report", "Utilities", "Patient Details")

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            summaryViewModel.load(LocalDate.now().minusDays(6), LocalDate.now())
        }
    }

    Scaffold(
        topBar = {
            KalazaTopBar(
                title = "Super Admin Overview",
                onLogout = onLogout,
                onRefresh = {
                    dashboardViewModel.load()
                    if (selectedTabIndex == 0) dailySummaryViewModel.load()
                    if (selectedTabIndex == 1) summaryViewModel.load(summaryViewModel.startDate.value, summaryViewModel.endDate.value)
                    if (selectedTabIndex == 2) configViewModel.load()
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = KalazaRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = KalazaRed,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = KalazaRed,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> TodayUpdateTab(dashboardViewModel, dailySummaryViewModel, onPatientClick)
                1 -> WeeklyReportTab(summaryViewModel, onOpenFullReport)
                2 -> {
                    val utilItems by configViewModel.utilItems.collectAsState()
                    UtilItemsEditor(
                        items = utilItems,
                        onAddItem = { configViewModel.addUtilityItem(it) },
                        onUpdateItem = { configViewModel.updateUtilityItem(it) },
                        onDeleteItem = { configViewModel.deleteUtilityItem(it) }
                    )
                }
                3 -> PatientDetailsTab(dashboardViewModel, onPatientClick)
            }
        }
    }
}

@Composable
private fun TodayUpdateTab(
    dashboardViewModel: DashboardViewModel,
    summaryViewModel: DailySummaryViewModel,
    onPatientClick: (String) -> Unit,
) {
    val totalPatients by dashboardViewModel.totalPatients.collectAsState()
    val pendingMeds by dashboardViewModel.pendingMeds.collectAsState()
    val pendingApprovals by dashboardViewModel.pendingApprovals.collectAsState()
    val patientSummaries by summaryViewModel.patientSummaries.collectAsState()
    val isLoading by summaryViewModel.isLoading.collectAsState()

    var groupByPatient by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OverviewStatCard(modifier = Modifier.weight(1f), title = "Total Patients", value = totalPatients.toString())
                OverviewStatCard(
                    modifier = Modifier.weight(1f), title = "Pending Meds", value = pendingMeds.toString(),
                    isAlert = pendingMeds > 0,
                )
                OverviewStatCard(
                    modifier = Modifier.weight(1f), title = "Pending Approvals", value = pendingApprovals.toString(),
                    isAlert = pendingApprovals > 0,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Daily Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = !groupByPatient,
                    onClick = { groupByPatient = false },
                    label = { Text("By Category") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KalazaRed, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                )
                FilterChip(
                    selected = groupByPatient,
                    onClick = { groupByPatient = true },
                    label = { Text("By Patient") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KalazaRed, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KalazaRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!groupByPatient) {
                    val allMeds = patientSummaries.flatMap { s -> s.meds.map { it to s.patient.name } }
                    item {
                        CategorySection(
                            title = "Medications",
                            done = allMeds.count { (m, _) -> m.status == MedStatus.ADMINISTERED },
                            total = allMeds.size,
                        ) {
                            allMeds.filter { (m, _) -> m.status != MedStatus.ADMINISTERED }
                                .sortedBy { (m, _) -> m.scheduleTime }
                                .forEach { (m, patientName) ->
                                    RemainingRow("${DateUtils.formatTime(m.scheduleTime)} — ${m.medicineName} for $patientName")
                                }
                        }
                    }
                    item {
                        CategorySection(
                            title = "Vitals",
                            done = patientSummaries.count { it.vitalsRecordedToday },
                            total = patientSummaries.size,
                        ) {
                            patientSummaries.filter { !it.vitalsRecordedToday }
                                .forEach { RemainingRow(it.patient.name) }
                        }
                    }
                    item {
                        CategorySection(
                            title = "Utilities",
                            done = patientSummaries.count { it.utilityLoggedToday },
                            total = patientSummaries.size,
                        ) {
                            patientSummaries.filter { !it.utilityLoggedToday }
                                .forEach { RemainingRow(it.patient.name) }
                        }
                    }
                } else {
                    items(patientSummaries) { summary ->
                        PatientSummarySection(summary, onClick = { onPatientClick(summary.patient.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySection(title: String, done: Int, total: Int, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "$done of $total done",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (done == total) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                if (done == total) {
                    Text("Nothing remaining.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
                }
            }
        }
    }
}

@Composable
private fun RemainingRow(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun PatientSummarySection(summary: PatientDaySummary, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val medsDone = summary.meds.count { it.status == MedStatus.ADMINISTERED }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(summary.patient.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "$medsDone of ${summary.meds.size} meds done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                if (summary.meds.isEmpty()) {
                    Text("No medications scheduled today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        summary.meds.sortedBy { it.scheduleTime }.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "${DateUtils.formatTime(m.scheduleTime)} — ${m.medicineName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                )
                                MedStatusBadge(m.status)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Vitals recorded today: ${if (summary.vitalsRecordedToday) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (summary.vitalsRecordedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
                Text(
                    "Utility logged today: ${if (summary.utilityLoggedToday) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (summary.utilityLoggedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClick) { Text("Open Patient", color = KalazaRed) }
            }
        }
    }
}

@Composable
private fun WeeklyReportTab(viewModel: SummaryViewModel, onOpenFullReport: () -> Unit) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Last 7 Days",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KalazaRed)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewStatCard(modifier = Modifier.weight(1f), title = "Vitals Recorded", value = stats.vitalsRecorded.toString())
                    OverviewStatCard(modifier = Modifier.weight(1f), title = "Meds Administered", value = stats.medsAdministered.toString())
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewStatCard(modifier = Modifier.weight(1f), title = "Meds Pending", value = stats.medsPending.toString(), isAlert = stats.medsPending > 0)
                    OverviewStatCard(modifier = Modifier.weight(1f), title = "Utility Logs", value = stats.utilityLogs.toString())
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onOpenFullReport,
                colors = ButtonDefaults.buttonColors(containerColor = KalazaRed),
            ) {
                Text("Open Full Report (date range + export)")
            }
        }
    }
}

@Composable
private fun PatientDetailsTab(viewModel: DashboardViewModel, onPatientClick: (String) -> Unit) {
    val patients by viewModel.patients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.search(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search patients...") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(patients) { patient ->
                PatientCard(patient = patient, onClick = { onPatientClick(patient.id) })
            }
        }
    }
}

@Composable
private fun OverviewStatCard(title: String, value: String, modifier: Modifier = Modifier, isAlert: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
