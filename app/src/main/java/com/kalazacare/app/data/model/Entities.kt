package com.kalazacare.app.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// ─────────────────────────────────────────────────────────────────────────────
// Enumerations
// ─────────────────────────────────────────────────────────────────────────────

enum class UserRole { ADMIN, STAFF }

enum class Gender { MALE, FEMALE, OTHER }

enum class ApprovalStatus { PENDING, APPROVED, REJECTED }

enum class MedStatus { PENDING, ADMINISTERED, OVERDUE }

// ─────────────────────────────────────────────────────────────────────────────
// Core Entities
// ─────────────────────────────────────────────────────────────────────────────

data class Patient(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.MALE,
    val roomNo: String = "",
    val photoUrl: String = "",
    val medicalHistory: String = "",
    val currentIssues: String = "",
    val allergies: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val admissionDate: LocalDate = LocalDate.now(),
    val isArchived: Boolean = false,
    val primaryDiagnosis: String = "",
)

data class Staff(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STAFF,
    val phone: String = "",
    val isActive: Boolean = true,
    val joinedDate: LocalDate = LocalDate.now(),
)

// ─────────────────────────────────────────────────────────────────────────────
// Clinical Records
// ─────────────────────────────────────────────────────────────────────────────

data class VitalRecord(
    val id: String = "",
    val patientId: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val pulse: String = "",          // bpm
    val bp: String = "",             // e.g. "120/80"
    val spo2: String = "",           // %
    val temperature: String = "",    // °F
    val sugarFasting: String = "",   // mg/dL
    val sugarPP: String = "",        // mg/dL
    val signedBy: String = "",       // Staff name
)

data class MedicationEntry(
    val id: String = "",
    val patientId: String = "",
    val medicineName: String = "",
    val dose: String = "",           // e.g. "500mg"
    val quantity: String = "",       // e.g. "1 tablet"
    val scheduleTime: LocalTime = LocalTime.now(),
    val scheduledDate: LocalDate = LocalDate.now(),
    val status: MedStatus = MedStatus.PENDING,
    val administeredBy: String = "",
    val administeredAt: LocalDateTime? = null,
    val notes: String = "",
)

data class UtilityRecord(
    val id: String = "",
    val patientId: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val faceMask: Int = 0,
    val diaperPant: Int = 0,
    val diaperStitch: Int = 0,
    val handGloves: Int = 0,
    val tinaBed: Int = 0,
    val wetWipes: Int = 0,
    val issuedToCaregiver: String = "",
    val issuedBySupervisor: String = "",
    val checkedBy: String = "",
)

data class DoctorVisit(
    val id: String = "",
    val patientId: String = "",
    val doctorName: String = "",
    val specialty: String = "",
    val date: LocalDate = LocalDate.now(),
    val notes: String = "",
    val nextVisitDate: LocalDate? = null,
    val prescriptionChanges: String = "",
)

data class CareNote(
    val id: String = "",
    val patientId: String = "",
    val staffId: String = "",
    val staffName: String = "",
    val date: LocalDate = LocalDate.now(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val note: String = "",
)

// ─────────────────────────────────────────────────────────────────────────────
// Admin / Workflow Entities
// ─────────────────────────────────────────────────────────────────────────────

data class ApprovalRequest(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val requestedById: String = "",
    val requestedByName: String = "",
    val fieldChanged: String = "",
    val oldValue: String = "",
    val newValue: String = "",
    val status: ApprovalStatus = ApprovalStatus.PENDING,
    val reviewedById: String = "",
    val reviewedByName: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val reviewedAt: LocalDateTime? = null,
    val rejectionReason: String = "",
)

data class AuditLogEntry(
    val id: String = "",
    val action: String = "",
    val performedById: String = "",
    val performedByName: String = "",
    val targetPatientId: String = "",
    val targetPatientName: String = "",
    val details: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val iconName: String = "edit",   // Material icon name for display
)

// ─────────────────────────────────────────────────────────────────────────────
// Config
// ─────────────────────────────────────────────────────────────────────────────

data class UtilityItem(
    val id: String = "",
    val name: String = "",
    val unit: String = "pcs",
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
)
