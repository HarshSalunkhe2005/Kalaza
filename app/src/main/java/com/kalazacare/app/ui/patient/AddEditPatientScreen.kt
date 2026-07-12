package com.kalazacare.app.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalazacare.app.data.model.Gender
import com.kalazacare.app.data.model.Patient
import com.kalazacare.app.ui.PatientViewModel
import com.kalazacare.app.ui.components.KalazaTextField
import com.kalazacare.app.ui.components.KalazaTopBar
import com.kalazacare.app.ui.theme.KalazaRed
import com.kalazacare.app.ui.theme.SurfaceVariant
import com.kalazacare.app.ui.theme.White

@Composable
fun AddEditPatientScreen(
    patientId: String?,
    viewModel: PatientViewModel = viewModel(),
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val isEditing = !patientId.isNullOrBlank()
    val patient by viewModel.patient.collectAsState()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var roomNo by remember { mutableStateOf("") }
    var primaryDiagnosis by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf("") }
    var currentIssues by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }

    LaunchedEffect(patientId) {
        if (isEditing && patientId != null) {
            viewModel.load(patientId)
        }
    }

    LaunchedEffect(patient) {
        if (isEditing && patient != null) {
            val p = patient!!
            name = p.name
            age = p.age.toString()
            roomNo = p.roomNo
            primaryDiagnosis = p.primaryDiagnosis
            medicalHistory = p.medicalHistory
            currentIssues = p.currentIssues
            allergies = p.allergies
            emergencyContact = p.emergencyContact
            emergencyPhone = p.emergencyPhone
            selectedGender = p.gender
        }
    }

    Scaffold(
        topBar = {
            KalazaTopBar(
                title = if (isEditing) "Edit Patient" else "Add Patient",
                showBack = true,
                onBack = onBack
            )
        },
        containerColor = SurfaceVariant
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Basic Info", style = MaterialTheme.typography.titleMedium, color = KalazaRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    KalazaTextField(value = name, onValueChange = { name = it }, label = "Full Name")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row {
                        KalazaTextField(
                            value = age, 
                            onValueChange = { age = it }, 
                            label = "Age", 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        KalazaTextField(
                            value = roomNo, 
                            onValueChange = { roomNo = it }, 
                            label = "Room No",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Gender", style = MaterialTheme.typography.bodyMedium)
                    Row {
                        Gender.values().forEach { gender ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedGender == gender,
                                    onClick = { selectedGender = gender },
                                    colors = RadioButtonDefaults.colors(selectedColor = KalazaRed)
                                )
                                Text(gender.name.lowercase().replaceFirstChar { it.uppercase() })
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Medical Details", style = MaterialTheme.typography.titleMedium, color = KalazaRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    KalazaTextField(value = primaryDiagnosis, onValueChange = { primaryDiagnosis = it }, label = "Primary Diagnosis")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    KalazaTextField(value = medicalHistory, onValueChange = { medicalHistory = it }, label = "Medical History", singleLine = false, maxLines = 4)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    KalazaTextField(value = currentIssues, onValueChange = { currentIssues = it }, label = "Current Issues", singleLine = false, maxLines = 4)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    KalazaTextField(value = allergies, onValueChange = { allergies = it }, label = "Allergies")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Emergency Contact", style = MaterialTheme.typography.titleMedium, color = KalazaRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    KalazaTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = "Contact Name")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    KalazaTextField(
                        value = emergencyPhone, 
                        onValueChange = { emergencyPhone = it }, 
                        label = "Phone Number",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val p = Patient(
                        id = if (isEditing) patientId!! else "",
                        name = name,
                        age = age.toIntOrNull() ?: 0,
                        gender = selectedGender,
                        roomNo = roomNo,
                        primaryDiagnosis = primaryDiagnosis,
                        medicalHistory = medicalHistory,
                        currentIssues = currentIssues,
                        allergies = allergies,
                        emergencyContact = emergencyContact,
                        emergencyPhone = emergencyPhone
                    )
                    viewModel.savePatient(p)
                    onSaved()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KalazaRed, contentColor = White)
            ) {
                Text(if (isEditing) "Save Changes" else "Add Patient")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
