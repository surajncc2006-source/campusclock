package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ClassStudent

@Composable
fun AddStudentDialog(
    classId: Long,
    studentToEdit: ClassStudent? = null,
    onDismiss: () -> Unit,
    onAddSingle: (name: String, rollNo: String, email: String) -> Unit,
    onAddBulk: (List<Pair<String, String>>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Single, 1: Batch paste

    // Single student state
    var name by remember { mutableStateOf(studentToEdit?.studentName ?: "") }
    var rollNo by remember { mutableStateOf(studentToEdit?.rollNo ?: "") }
    var email by remember { mutableStateOf(studentToEdit?.email ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var rollError by remember { mutableStateOf(false) }

    // Batch import state
    var bulkText by remember {
        mutableStateOf(
            if (studentToEdit != null) "" else "01, Aarav Sharma\n02, Aditi Verma\n03, Ananya Gupta"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (selectedTab == 0) Icons.Default.PersonAdd else Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (studentToEdit == null) "Add Students to Class" else "Edit Student",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (studentToEdit == null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Single") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Paste List") }
                        )
                    }
                }

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = {
                            rollNo = it
                            rollError = false
                        },
                        label = { Text("Roll Number *") },
                        placeholder = { Text("e.g. SLC/CS/24/01") },
                        isError = rollError,
                        supportingText = { if (rollError) Text("Roll number is required") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_roll_input")
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        label = { Text("Student Full Name *") },
                        placeholder = { Text("e.g. Aarav Sharma") },
                        isError = nameError,
                        supportingText = { if (nameError) Text("Student name is required") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email / Contact (Optional)") },
                        placeholder = { Text("aarav@slc.du.ac.in") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Paste student names & roll numbers (one student per line):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Format: Roll No, Full Name",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = bulkText,
                            onValueChange = { bulkText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("bulk_student_input"),
                            placeholder = { Text("SLC/01, Aarav\nSLC/02, Aditi\nSLC/03, Bhavya") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTab == 0) {
                        if (rollNo.isBlank()) rollError = true
                        if (name.isBlank()) nameError = true
                        if (rollNo.isNotBlank() && name.isNotBlank()) {
                            onAddSingle(name.trim(), rollNo.trim(), email.trim())
                        }
                    } else {
                        // Parse lines
                        val parsed = bulkText.lines()
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .mapNotNull { line ->
                                val parts = if (line.contains(",")) {
                                    line.split(",", limit = 2)
                                } else if (line.contains("\t")) {
                                    line.split("\t", limit = 2)
                                } else {
                                    line.split(" ", limit = 2)
                                }
                                if (parts.size >= 2) {
                                    Pair(parts[0].trim(), parts[1].trim())
                                } else if (parts.size == 1 && parts[0].isNotBlank()) {
                                    Pair(parts[0].trim(), parts[0].trim())
                                } else null
                            }

                        if (parsed.isNotEmpty()) {
                            onAddBulk(parsed)
                        }
                    }
                },
                modifier = Modifier.testTag("confirm_student_button")
            ) {
                Text(if (selectedTab == 0) "Save Student" else "Import Students")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
