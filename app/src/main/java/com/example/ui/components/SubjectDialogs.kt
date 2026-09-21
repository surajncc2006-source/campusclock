package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Subject
import com.example.ui.UserProfile
import kotlin.math.roundToInt

val AVAILABLE_COLORS = listOf(
    "#1E3A8A", // Deep Navy
    "#880E4F", // DU Maroon
    "#0D9488", // Teal
    "#D97706", // Gold
    "#4338CA", // Indigo
    "#059669", // Emerald
    "#DC2626", // Crimson
    "#7C3AED"  // Violet
)

@Composable
fun AddEditSubjectDialog(
    subjectToEdit: Subject? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, teacher: String, room: String, target: Double, color: String) -> Unit
) {
    var name by remember { mutableStateOf(subjectToEdit?.name ?: "") }
    var code by remember { mutableStateOf(subjectToEdit?.code ?: "") }
    var teacher by remember { mutableStateOf(subjectToEdit?.teacher ?: "") }
    var room by remember { mutableStateOf(subjectToEdit?.room ?: "") }
    var targetPercentage by remember { mutableDoubleStateOf(subjectToEdit?.targetPercentage ?: 75.0) }
    var selectedColor by remember { mutableStateOf(subjectToEdit?.colorHex ?: AVAILABLE_COLORS.first()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subjectToEdit == null) "Add New Subject" else "Edit Subject",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Data Structures") },
                    isError = isError,
                    supportingText = if (isError) { { Text("Subject name is required") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input")
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code (Optional)") },
                    placeholder = { Text("e.g. CS-DSC-101") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_code_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { teacher = it },
                        label = { Text("Teacher") },
                        placeholder = { Text("Dr. Sharma") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_teacher_input")
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room / Lab") },
                        placeholder = { Text("Room 204") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_room_input")
                    )
                }

                Text(
                    text = "Target Attendance: ${targetPercentage.roundToInt()}% (DU Standard: 67% - 75%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = targetPercentage.toFloat(),
                    onValueChange = { targetPercentage = it.toDouble() },
                    valueRange = 50f..95f,
                    steps = 8,
                    modifier = Modifier.testTag("target_slider")
                )

                Text(
                    text = "Color Tag",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AVAILABLE_COLORS.forEach { colorHex ->
                        val isSelected = selectedColor.equals(colorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(
                            name.trim(),
                            code.trim(),
                            teacher.trim(),
                            room.trim(),
                            targetPercentage.roundToInt().toDouble(),
                            selectedColor
                        )
                    }
                },
                modifier = Modifier.testTag("save_subject_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, roll: String, course: String, semester: String, target: Double) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.studentName) }
    var roll by remember { mutableStateOf(currentProfile.rollNo) }
    var course by remember { mutableStateOf(currentProfile.course) }
    var semester by remember { mutableStateOf(currentProfile.semester) }
    var target by remember { mutableDoubleStateOf(currentProfile.targetCriteria) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Student Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )
                OutlinedTextField(
                    value = roll,
                    onValueChange = { roll = it },
                    label = { Text("College Roll Number") },
                    placeholder = { Text("SLC/2024/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_roll_input")
                )
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Degree / Course") },
                    placeholder = { Text("e.g. B.Com (Hons), B.A., B.Sc") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_course_input")
                )
                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it },
                    label = { Text("Semester / Year") },
                    placeholder = { Text("e.g. Semester 3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_sem_input")
                )

                Text(
                    text = "Target Criteria: ${target.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = target.toFloat(),
                    onValueChange = { target = it.toDouble() },
                    valueRange = 50f..95f,
                    steps = 8
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name.trim(), roll.trim(), course.trim(), semester.trim(), target.roundToInt().toDouble())
                },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
