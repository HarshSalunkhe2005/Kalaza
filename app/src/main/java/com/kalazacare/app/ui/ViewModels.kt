package com.kalazacare.app.ui

import androidx.lifecycle.ViewModel
import com.kalazacare.app.data.model.*
import com.kalazacare.app.data.repository.*
import com.kalazacare.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// Login
// ─────────────────────────────────────────────────────────────────────────────

class LoginViewModel(
    private val authRepo: AuthRepository = MockAuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please fill in all fields")
            return
        }
        val staff = authRepo.login(email, password)
        if (staff != null) {
            SessionManager.setCurrentStaff(staff)
            _loginState.value = LoginState.Success(staff)
        } else {
            _loginState.value = LoginState.Error("Invalid credentials or account inactive")
        }
    }

    fun resetState() { _loginState.value = LoginState.Idle }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val staff: Staff) : LoginState()
    data class Error(val message: String) : LoginState()
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard
// ─────────────────────────────────────────────────────────────────────────────

class DashboardViewModel(
    private val patientRepo: PatientRepository = MockPatientRepository(),
    private val medRepo: MedicationRepository  = MockMedicationRepository(),
    private val approvalRepo: ApprovalRepository = MockApprovalRepository(),
) : ViewModel() {

    private val _patients      = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _searchQuery   = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pendingMeds   = MutableStateFlow(0)
    val pendingMeds: StateFlow<Int> = _pendingMeds.asStateFlow()

    private val _pendingApprovals = MutableStateFlow(0)
    val pendingApprovals: StateFlow<Int> = _pendingApprovals.asStateFlow()

    init { load() }

    fun load() {
        _patients.value      = patientRepo.getAllPatients()
        _pendingApprovals.value = approvalRepo.getPendingRequests().size
    }

    fun search(query: String) {
        _searchQuery.value = query
        _patients.value = if (query.isBlank()) patientRepo.getAllPatients()
                          else patientRepo.searchPatients(query)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Patient Profile
// ─────────────────────────────────────────────────────────────────────────────

class PatientViewModel(
    private val patientRepo: PatientRepository = MockPatientRepository(),
    private val approvalRepo: ApprovalRepository = MockApprovalRepository(),
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    fun load(patientId: String) {
        _patient.value = patientRepo.getPatientById(patientId)
    }

    fun submitEditRequest(patientId: String, patientName: String,
                          field: String, oldVal: String, newVal: String) {
        approvalRepo.submitRequest(
            ApprovalRequest(
                id = "ar_${System.currentTimeMillis()}",
                patientId = patientId, patientName = patientName,
                requestedById = SessionManager.getCurrentStaffId(),
                requestedByName = SessionManager.getCurrentStaffName(),
                fieldChanged = field, oldValue = oldVal, newValue = newVal
            )
        )
    }

    fun savePatient(patient: Patient) {
        if (patient.id.isEmpty()) {
            patientRepo.addPatient(patient.copy(id = "p_${System.currentTimeMillis()}"))
        } else {
            patientRepo.updatePatient(patient)
        }
        _patient.value = patient
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vitals
// ─────────────────────────────────────────────────────────────────────────────

class VitalsViewModel(
    private val repo: VitalsRepository = MockVitalsRepository()
) : ViewModel() {

    private val _vitals = MutableStateFlow<List<VitalRecord>>(emptyList())
    val vitals: StateFlow<List<VitalRecord>> = _vitals.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun load(patientId: String) {
        _vitals.value = repo.getVitalsForPatient(patientId)
    }

    fun addVital(record: VitalRecord) {
        repo.addVital(record)
        load(record.patientId)
    }

    fun updateVital(record: VitalRecord) {
        repo.updateVital(record)
        load(record.patientId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAR (Medication)
// ─────────────────────────────────────────────────────────────────────────────

class MarViewModel(
    private val repo: MedicationRepository = MockMedicationRepository()
) : ViewModel() {

    private val _medications = MutableStateFlow<List<MedicationEntry>>(emptyList())
    val medications: StateFlow<List<MedicationEntry>> = _medications.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun load(patientId: String, date: LocalDate = LocalDate.now()) {
        _selectedDate.value  = date
        _medications.value   = repo.getMedicationsForPatient(patientId, date)
    }

    fun markAdministered(id: String) {
        repo.markAdministered(id, SessionManager.getCurrentStaffName())
        _medications.value = _medications.value.map {
            if (it.id == id) it.copy(
                status = MedStatus.ADMINISTERED,
                administeredBy = SessionManager.getCurrentStaffName(),
                administeredAt = java.time.LocalDateTime.now()
            ) else it
        }
    }

    fun addMedication(entry: MedicationEntry) {
        repo.addMedication(entry.copy(id = "m_${System.currentTimeMillis()}"))
        load(entry.patientId, entry.scheduledDate)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────────────────────

class UtilityViewModel(
    private val repo: UtilityRepository = MockUtilityRepository()
) : ViewModel() {

    private val _records = MutableStateFlow<List<UtilityRecord>>(emptyList())
    val records: StateFlow<List<UtilityRecord>> = _records.asStateFlow()

    private val _items = MutableStateFlow<List<UtilityItem>>(emptyList())
    val items: StateFlow<List<UtilityItem>> = _items.asStateFlow()

    fun load(patientId: String) {
        _records.value = repo.getUtilityForPatient(patientId)
        _items.value   = repo.getUtilityItems()
    }

    fun addRecord(record: UtilityRecord) {
        repo.addUtilityRecord(record.copy(id = "u_${System.currentTimeMillis()}"))
        load(record.patientId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Doctor Visits
// ─────────────────────────────────────────────────────────────────────────────

class DoctorVisitViewModel(
    private val repo: DoctorVisitRepository = MockDoctorVisitRepository()
) : ViewModel() {

    private val _visits = MutableStateFlow<List<DoctorVisit>>(emptyList())
    val visits: StateFlow<List<DoctorVisit>> = _visits.asStateFlow()

    fun load(patientId: String) { _visits.value = repo.getVisitsForPatient(patientId) }

    fun addVisit(visit: DoctorVisit) {
        repo.addVisit(visit.copy(id = "dv_${System.currentTimeMillis()}"))
        load(visit.patientId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Care Notes
// ─────────────────────────────────────────────────────────────────────────────

class CareNoteViewModel(
    private val repo: CareNoteRepository = MockCareNoteRepository()
) : ViewModel() {

    private val _notes = MutableStateFlow<List<CareNote>>(emptyList())
    val notes: StateFlow<List<CareNote>> = _notes.asStateFlow()

    fun load(patientId: String) { _notes.value = repo.getNotesForPatient(patientId) }

    fun addNote(patientId: String, noteText: String) {
        repo.addNote(CareNote(
            id = "cn_${System.currentTimeMillis()}",
            patientId = patientId,
            staffId   = SessionManager.getCurrentStaffId(),
            staffName = SessionManager.getCurrentStaffName(),
            note      = noteText
        ))
        load(patientId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Approval Queue
// ─────────────────────────────────────────────────────────────────────────────

class ApprovalViewModel(
    private val repo: ApprovalRepository = MockApprovalRepository()
) : ViewModel() {

    private val _requests = MutableStateFlow<List<ApprovalRequest>>(emptyList())
    val requests: StateFlow<List<ApprovalRequest>> = _requests.asStateFlow()

    init { load() }

    fun load() { _requests.value = repo.getAllRequests() }

    fun approve(id: String) {
        repo.approve(id, SessionManager.getCurrentStaffName())
        load()
    }

    fun reject(id: String, reason: String) {
        repo.reject(id, SessionManager.getCurrentStaffName(), reason)
        load()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Audit Log
// ─────────────────────────────────────────────────────────────────────────────

class AuditLogViewModel(
    private val repo: AuditRepository = MockAuditRepository()
) : ViewModel() {

    private val _logs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val logs: StateFlow<List<AuditLogEntry>> = _logs.asStateFlow()

    init { load() }

    fun load() { _logs.value = repo.getAllLogs() }
}

// ─────────────────────────────────────────────────────────────────────────────
// Config / Admin
// ─────────────────────────────────────────────────────────────────────────────

class ConfigViewModel(
    private val staffRepo: StaffRepository     = MockStaffRepository(),
    private val utilityRepo: UtilityRepository = MockUtilityRepository(),
) : ViewModel() {

    private val _staffList = MutableStateFlow<List<Staff>>(emptyList())
    val staffList: StateFlow<List<Staff>> = _staffList.asStateFlow()

    private val _utilItems = MutableStateFlow<List<UtilityItem>>(emptyList())
    val utilItems: StateFlow<List<UtilityItem>> = _utilItems.asStateFlow()

    init { load() }

    fun load() {
        _staffList.value = staffRepo.getAllStaff()
        _utilItems.value = utilityRepo.getUtilityItems()
    }

    fun addStaff(staff: Staff) {
        staffRepo.addStaff(staff.copy(id = "staff_${System.currentTimeMillis()}"))
        _staffList.value = staffRepo.getAllStaff()
    }

    fun revokeStaff(id: String) {
        staffRepo.revokeStaff(id)
        _staffList.value = staffRepo.getAllStaff()
    }

    fun addUtilityItem(item: UtilityItem) {
        utilityRepo.addUtilityItem(item.copy(id = "ui_${System.currentTimeMillis()}"))
        _utilItems.value = utilityRepo.getUtilityItems()
    }

    fun deleteUtilityItem(id: String) {
        utilityRepo.deleteUtilityItem(id)
        _utilItems.value = utilityRepo.getUtilityItems()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary
// ─────────────────────────────────────────────────────────────────────────────

data class SummaryStats(
    val vitalsRecorded: Int = 0,
    val medsAdministered: Int = 0,
    val medsPending: Int = 0,
    val utilityLogs: Int = 0,
    val pendingApprovals: Int = 0,
)

class SummaryViewModel(
    private val medRepo: MedicationRepository  = MockMedicationRepository(),
    private val vitalsRepo: VitalsRepository   = MockVitalsRepository(),
    private val approvalRepo: ApprovalRepository = MockApprovalRepository(),
    private val patientRepo: PatientRepository = MockPatientRepository(),
) : ViewModel() {

    private val _stats = MutableStateFlow(SummaryStats())
    val stats: StateFlow<SummaryStats> = _stats.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    init { load(LocalDate.now()) }

    fun load(date: LocalDate) {
        _selectedDate.value = date
        val pts = patientRepo.getAllPatients()
        _patients.value = pts

        val allMeds = pts.flatMap { medRepo.getMedicationsForPatient(it.id, date) }
        val allVitals = pts.flatMap { vitalsRepo.getVitalsForDate(it.id, date) }

        _stats.value = SummaryStats(
            vitalsRecorded   = allVitals.size,
            medsAdministered = allMeds.count { it.status == MedStatus.ADMINISTERED },
            medsPending      = allMeds.count { it.status == MedStatus.PENDING },
            utilityLogs      = 0,
            pendingApprovals = approvalRepo.getPendingRequests().size
        )
    }
}
