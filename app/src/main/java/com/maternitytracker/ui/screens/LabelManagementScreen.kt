package com.maternitytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maternitytracker.data.entities.Label
import com.maternitytracker.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelManagementScreen(
    viewModel: ShoppingViewModel,
    onNavigateBack: () -> Unit
) {
    val labels by viewModel.allLabels.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<Label?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Label?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Manage Labels",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Label")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Labels list
        if (labels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Label,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No labels yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Tap + to create your first label",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(labels) { label ->
                    LabelItem(
                        label = label,
                        onEdit = { editingLabel = label },
                        onDelete = { showDeleteDialog = label }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingLabel != null) {
        LabelDialog(
            label = editingLabel,
            onDismiss = {
                showAddDialog = false
                editingLabel = null
            },
            onSave = { name, color ->
                if (editingLabel != null) {
                    viewModel.renameLabel(editingLabel!!, name)
                    viewModel.updateLabel(editingLabel!!.copy(name = name, color = color))
                } else {
                    viewModel.addLabel(Label(name = name, color = color))
                }
                showAddDialog = false
                editingLabel = null
            },
            viewModel = viewModel
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { label ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Label") },
            text = { Text("Are you sure you want to delete \"${label.name}\"? This will also delete all items with this label.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLabelAndReferences(label)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LabelItem(
    label: Label,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(label.color)))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Label name
            Text(
                text = label.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            // Action buttons
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelDialog(
    label: Label?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    viewModel: ShoppingViewModel
) {
    var name by remember { mutableStateOf(label?.name ?: "") }
    var selectedColor by remember { mutableStateOf(label?.color ?: "#E1BEE7") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val predefinedColors = listOf(
        "#E1BEE7", // Lavender
        "#FFB6C1", // Light Pink
        "#98FB98", // Pale Green
        "#87CEEB", // Sky Blue
        "#DDA0DD", // Plum
        "#F0E68C", // Khaki
        "#FFA07A", // Light Salmon
        "#20B2AA"  // Light Sea Green
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (label != null) "Edit Label" else "Add Label") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = null
                    },
                    label = { Text("Label Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Choose Color:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(predefinedColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(
                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                    color = if (selectedColor == color) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        name.isBlank() -> nameError = "Name cannot be empty"
                        else -> {
                            // Check for duplicates (only for new labels or when name changed)
                            if (label == null || label.name != name) {
                                // For now, we'll allow the save and let the repository handle duplicates
                                // In a real app, you might want to check this asynchronously
                            }
                            onSave(name.trim(), selectedColor)
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}