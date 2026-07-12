package com.kalazacare.app.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.ui.ConfigViewModel
import com.kalazacare.app.ui.components.KalazaTopBar
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.SurfaceVariant
import com.kalazacare.app.ui.theme.White

@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel = viewModel(),
    onBack: () -> Unit
) {
    val staffList by viewModel.staffList.collectAsState()
    val utilItems by viewModel.utilItems.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Staff Management", "Utility Items")

    Scaffold(
        topBar = {
            KalazaTopBar(
                title = "Admin Configuration",
                showBack = false // It's a bottom nav root
            )
        },
        containerColor = SurfaceVariant
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = White,
                contentColor = KalazaRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = KalazaRed
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> StaffEditor(
                    staffList = staffList,
                    onAddStaff = { viewModel.addStaff(it) },
                    onRevokeStaff = { viewModel.revokeStaff(it) }
                )
                1 -> UtilItemsEditor(
                    items = utilItems,
                    onAddItem = { viewModel.addUtilityItem(it) },
                    onDeleteItem = { viewModel.deleteUtilityItem(it) }
                )
            }
        }
    }
}
