package com.kalazacare.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.kalazacare.app.ui.*
import com.kalazacare.app.ui.approval.ApprovalQueueScreen
import com.kalazacare.app.ui.auditlog.AuditLogScreen
import com.kalazacare.app.ui.config.ConfigScreen
import com.kalazacare.app.ui.dashboard.DashboardScreen
import com.kalazacare.app.ui.login.LoginScreen
import com.kalazacare.app.ui.patient.AddEditPatientScreen
import com.kalazacare.app.ui.patient.PatientProfileScreen
import com.kalazacare.app.ui.summary.SummaryScreen
import com.kalazacare.app.util.SessionManager

object Routes {
    const val LOGIN           = "login"
    const val DASHBOARD       = "dashboard"
    const val PATIENT_PROFILE = "patient/{patientId}"
    const val PATIENT_NEW     = "patient/new"
    const val PATIENT_EDIT    = "patient/{patientId}/edit"
    const val APPROVAL_QUEUE  = "approval"
    const val AUDIT_LOG       = "auditlog"
    const val CONFIG          = "config"
    const val SUMMARY         = "summary"

    fun patientProfile(id: String) = "patient/$id"
    fun patientEdit(id: String)    = "patient/$id/edit"
}

@Composable
fun KalazaNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Routes where bottom nav should be visible
    val bottomNavRoutes = setOf(
        Routes.DASHBOARD, Routes.APPROVAL_QUEUE,
        Routes.AUDIT_LOG, Routes.CONFIG, Routes.SUMMARY
    )
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                KalazaBottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController   = navController,
            startDestination = Routes.LOGIN,
            modifier         = Modifier.padding(innerPadding),
        ) {

            // ── Login ──────────────────────────────────────────────────────────
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // ── Dashboard ──────────────────────────────────────────────────────
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onPatientClick = { patientId ->
                        navController.navigate(Routes.patientProfile(patientId))
                    },
                    onAddPatient = {
                        navController.navigate(Routes.PATIENT_NEW)
                    }
                )
            }

            // ── Patient Profile ────────────────────────────────────────────────
            composable(
                route = Routes.PATIENT_PROFILE,
                arguments = listOf(navArgument("patientId") { type = NavType.StringType })
            ) { backStack ->
                val patientId = backStack.arguments?.getString("patientId") ?: ""
                PatientProfileScreen(
                    patientId = patientId,
                    onBack = { navController.popBackStack() },
                    onEditPatient = { navController.navigate(Routes.patientEdit(patientId)) }
                )
            }

            // ── Add/Edit Patient ───────────────────────────────────────────────
            composable(Routes.PATIENT_NEW) {
                AddEditPatientScreen(
                    patientId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.PATIENT_EDIT,
                arguments = listOf(navArgument("patientId") { type = NavType.StringType })
            ) { backStack ->
                val patientId = backStack.arguments?.getString("patientId") ?: ""
                AddEditPatientScreen(
                    patientId = patientId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // ── Approval Queue ─────────────────────────────────────────────────
            composable(Routes.APPROVAL_QUEUE) {
                ApprovalQueueScreen(onBack = { navController.popBackStack() })
            }

            // ── Audit Log ──────────────────────────────────────────────────────
            composable(Routes.AUDIT_LOG) {
                AuditLogScreen(onBack = { navController.popBackStack() })
            }

            // ── Config ─────────────────────────────────────────────────────────
            composable(Routes.CONFIG) {
                ConfigScreen(onBack = { navController.popBackStack() })
            }

            // ── Summary ────────────────────────────────────────────────────────
            composable(Routes.SUMMARY) {
                SummaryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
