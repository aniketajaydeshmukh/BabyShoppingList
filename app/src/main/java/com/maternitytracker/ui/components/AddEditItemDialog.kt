package com.maternitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem
import com.maternitytracker.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    item: ShoppingItem?,
    viewModel: ShoppingViewModel,
    onDismiss: () -> Unit,
    onSave: (ShoppingItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "1") }
    var estimatedPrice by remember { mutableStateOf(item?.estimatedPrice?.toString() ?: "") }
    var actualPrice by remember { mutableStateOf(item?.actualPrice?.toString() ?: "") }
    var isPurchased by remember { mutableStateOf(item?.isPurchased ?: false) }
    var selectedLabels by remember { mutableStateOf(
        item?.labels?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    ) }

    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle(initialValue = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item != null) "Edit Item" else "Add Item") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quantity and Estimated Price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = estimatedPrice,
                        onValueChange = { estimatedPrice = it },
                        label = { Text("Est. Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                }

                // Purchase status and actual price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = isPurchased,
                            onCheckedChange = { isPurchased = it }
                        )
                        Text("Purchased", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    if (isPurchased) {
                        OutlinedTextField(
                            value = actualPrice,
                            onValueChange = { actualPrice = it },
                            label = { Text("Actual Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )
                    }
                }

                // Labels section
                Text(
                    "Labels:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )

                if (allLabels.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allLabels) { label ->
                            LabelChip(
                                label = label,
                                isSelected = selectedLabels.contains(label.name),
                                onClick = {
                                    selectedLabels = if (selectedLabels.contains(label.name)) {
                                        selectedLabels - label.name
                                    } else {
                                        selectedLabels + label.name
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        "No labels available. Create labels in settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantityInt = quantity.toIntOrNull() ?: 1
                    val estimatedPriceDouble = estimatedPrice.toDoubleOrNull() ?: 0.0
                    val actualPriceDouble = if (isPurchased && actualPrice.isNotBlank()) {
                        actualPrice.toDoubleOrNull()
                    } else null

                    val newItem = if (item != null) {
                        item.copy(
                            name = name.trim(),
                            quantity = quantityInt,
                            estimatedPrice = estimatedPriceDouble,
                            actualPrice = actualPriceDouble,
                            isPurchased = isPurchased,
                            purchasedAt = if (isPurchased && !item.isPurchased) java.util.Date() else item.purchasedAt,
                            labels = selectedLabels.joinToString(", ")
                        )
                    } else {
                        ShoppingItem(
                            name = name.trim(),
                            quantity = quantityInt,
                            estimatedPrice = estimatedPriceDouble,
                            actualPrice = actualPriceDouble,
                            isPurchased = isPurchased,
                            purchasedAt = if (isPurchased) java.util.Date() else null,
                            labels = selectedLabels.joinToString(", ")
                        )
                    }
                    onSave(newItem)
                },
                enabled = name.isNotBlank() && quantity.toIntOrNull() != null && estimatedPrice.toDoubleOrNull() != null
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

@Composable
private fun LabelChip(
    label: Label,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(android.graphics.Color.parseColor(label.color))
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            ),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.8f)
                        else Color(android.graphics.Color.parseColor(label.color))
                    )
            )
            Text(
                text = label.name,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}