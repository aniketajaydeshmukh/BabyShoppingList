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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem
import com.maternitytracker.ui.components.AddEditItemDialog
import com.maternitytracker.ui.components.PurchaseAnalyticsChart
import com.maternitytracker.ui.components.QuickPurchaseDialog
import com.maternitytracker.viewmodel.ShoppingViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShoppingViewModel,
    onNavigateToLabelManagement: () -> Unit
) {
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedLabels by viewModel.selectedLabels.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()
    val showPurchased by viewModel.showPurchased.collectAsStateWithLifecycle()
    val budgetInfo by viewModel.budgetInfo.collectAsStateWithLifecycle(
        initialValue = ShoppingViewModel.BudgetInfo(0.0, 0.0, 0.0, 0, 0, 0f)
    )

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ShoppingItem?>(null) }
    var showQuickPurchaseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with title and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Maternity Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = { showQuickPurchaseDialog = true }) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Quick Purchase",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNavigateToLabelManagement) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Manage Labels",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analytics Chart
        PurchaseAnalyticsChart(
            budgetInfo = budgetInfo,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Budget Summary Card
        BudgetSummaryCard(budgetInfo = budgetInfo)

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Controls
        FilterControls(
            allLabels = allLabels,
            selectedLabels = selectedLabels,
            filterMode = filterMode,
            showPurchased = showPurchased,
            onLabelToggle = viewModel::toggleLabelFilter,
            onClearFilters = viewModel::clearLabelFilters,
            onFilterModeChange = viewModel::setFilterMode,
            onToggleShowPurchased = viewModel::toggleShowPurchased
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Items List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Items (${filteredItems.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Items List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No items found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Add your first item or adjust filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { item ->
                    ShoppingItemCard(
                        item = item,
                        onEdit = { editingItem = item },
                        onDelete = { showDeleteDialog = item },
                        onTogglePurchased = { 
                            viewModel.updateItem(item.copy(
                                isPurchased = !item.isPurchased,
                                purchasedAt = if (!item.isPurchased) Date() else null
                            ))
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog || editingItem != null) {
        AddEditItemDialog(
            item = editingItem,
            viewModel = viewModel,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onSave = { item ->
                if (editingItem != null) {
                    viewModel.updateItem(item)
                } else {
                    viewModel.addItem(item)
                }
                showAddDialog = false
                editingItem = null
            }
        )
    }

    if (showQuickPurchaseDialog) {
        QuickPurchaseDialog(
            viewModel = viewModel,
            onDismiss = { showQuickPurchaseDialog = false }
        )
    }

    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(item)
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
private fun BudgetSummaryCard(budgetInfo: ShoppingViewModel.BudgetInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BudgetItem(
                label = "Estimated",
                amount = budgetInfo.totalEstimated,
                color = MaterialTheme.colorScheme.primary
            )
            if (budgetInfo.totalActual > 0) {
                BudgetItem(
                    label = "Actual",
                    amount = budgetInfo.totalActual,
                    color = MaterialTheme.colorScheme.secondary
                )
                BudgetItem(
                    label = "Remaining",
                    amount = budgetInfo.remaining,
                    color = if (budgetInfo.remaining >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun BudgetItem(
    label: String,
    amount: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = NumberFormat.getCurrencyInstance().format(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun FilterControls(
    allLabels: List<Label>,
    selectedLabels: Set<String>,
    filterMode: ShoppingViewModel.FilterMode,
    showPurchased: Boolean,
    onLabelToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onFilterModeChange: (ShoppingViewModel.FilterMode) -> Unit,
    onToggleShowPurchased: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Filter mode and show purchased toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Filter Mode:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = filterMode == ShoppingViewModel.FilterMode.AND,
                        onClick = { onFilterModeChange(ShoppingViewModel.FilterMode.AND) },
                        label = { Text("AND") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = filterMode == ShoppingViewModel.FilterMode.OR,
                        onClick = { onFilterModeChange(ShoppingViewModel.FilterMode.OR) },
                        label = { Text("OR") }
                    )
                }
                
                FilterChip(
                    selected = showPurchased,
                    onClick = onToggleShowPurchased,
                    label = { Text("Show Purchased") }
                )
            }

            if (allLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Labels:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (selectedLabels.isNotEmpty()) {
                        TextButton(onClick = onClearFilters) {
                            Text("Clear")
                        }
                    }
                }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allLabels) { label ->
                        LabelFilterChip(
                            label = label,
                            isSelected = selectedLabels.contains(label.name),
                            onClick = { onLabelToggle(label.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelFilterChip(
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

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePurchased: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Qty: ${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Est: ${NumberFormat.getCurrencyInstance().format(item.estimatedPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (item.isPurchased && item.actualPrice != null) {
                            Text(
                                text = "Actual: ${NumberFormat.getCurrencyInstance().format(item.actualPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (item.labels.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Labels: ${item.labels}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    if (item.isPurchased && item.purchasedAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Purchased: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(item.purchasedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onTogglePurchased) {
                        Icon(
                            if (item.isPurchased) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (item.isPurchased) "Mark as unpurchased" else "Mark as purchased",
                            tint = if (item.isPurchased) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                        )
                    }
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
    }
}