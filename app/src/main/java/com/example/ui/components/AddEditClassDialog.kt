package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Class
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.TeacherClass

val TEACHER_CLASS_COLORS = listOf(
    "#1E3A8A", // Deep Navy
    "#880E4F", // DU Maroon
    "#0D9488", // Teal
    "#D97706", // Gold
    "#4338CA", // Indigo
    "#059669", // Emerald
    "#DC2626", // Crimson
    "#7C3AED"  // Violet
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditClassDialog(
    classToEdit: TeacherClass? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, dept: String, sem: String, sec: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(classToEdit?.className ?: "") }
    var code by remember { mutableStateOf(classToEdit?.courseCode ?: "") }
    var department by remember { mutableStateOf(classToEdit?.department ?: "Computer Science") }
    var semester by remember { mutableStateOf(classToEdit?.semester ?: "Semester 3") }
    var section by remember { mutableStateOf(classToEdit?.section ?: "A") }
    var selectedColor by remember { mutableStateOf(classToEdit?.colorHex ?: TEACHER_CLASS_COLORS[0]) }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Class,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (classToEdit == null) "Create New Class" else "Edit Class Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Class / Subject Name *") },
                    placeholder = { Text("e.g. Data Structures, Macroeconomics") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Class name is required") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Paper Code") },
                        placeholder = { Text("e.g. CS-301") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        placeholder = { Text("e.g. A, B") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        placeholder = { Text("e.g. Comp. Science") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Semester") },
                        placeholder = { Text("e.g. Sem 3") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Class Color Tag",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TEACHER_CLASS_COLORS.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) onSurfaceColor else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(
                            name.trim(),
                            code.trim(),
                            department.trim(),
                            semester.trim(),
                            section.trim(),
                            selectedColor
                        )
                    }
                },
                modifier = Modifier.testTag("save_class_button")
            ) {
                Text(if (classToEdit == null) "Create Class" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
