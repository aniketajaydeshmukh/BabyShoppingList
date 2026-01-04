package com.maternitytracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maternitytracker.data.entities.ShoppingItem
import com.maternitytracker.viewmodel.ShoppingViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPurchaseDialog(
    viewModel: ShoppingViewModel,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var actualPrice by remember { mutableStateOf("") }
    
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    
    // Trigger search when query changes
    LaunchedEffect(searchQuery) {
        viewModel.searchItems(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Text("Quick Purchase")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedItem == null) {
                    // Search phase
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search items to purchase") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (searchQuery.isNotBlank()) {
                        if (searchResults.isEmpty()) {
                            Text(
                                "No unpurchased items found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            Text(
                                "Select item to purchase:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(searchResults) { item ->
                                    SearchResultItem(
                                        item = item,
                                        onClick = { selectedItem = item }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            "Start typing to search for items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    // Purchase phase
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Purchasing:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                selectedItem!!.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Quantity: ${selectedItem!!.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Estimated: ${NumberFormat.getCurrencyInstance().format(selectedItem!!.estimatedPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = actualPrice,
                        onValueChange = { actualPrice = it },
                        label = { Text("Actual Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Enter the actual price you paid") }
                    )
                }
            }
        },
        confirmButton = {
            if (selectedItem != null) {
                TextButton(
                    onClick = {
                        actualPrice.toDoubleOrNull()?.let { price ->
                            viewModel.markItemPurchased(selectedItem!!.id, price)
                            onDismiss()
                        }
                    },
                    enabled = actualPrice.toDoubleOrNull() != null && actualPrice.toDoubleOrNull()!! > 0
                ) {
                    Text("Purchase")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (selectedItem != null) {
                        selectedItem = null
                        actualPrice = ""
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (selectedItem != null) "Back" else "Cancel")
            }
        }
    )
}

@Composable
private fun SearchResultItem(
    item: ShoppingItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Qty: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = NumberFormat.getCurrencyInstance().format(item.estimatedPrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (item.labels.isNotBlank()) {
                Text(
                    text = "Labels: ${item.labels}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}