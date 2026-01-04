package com.maternitytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem
import com.maternitytracker.data.repository.ShoppingRepository

class ShoppingViewModel(private val repository: ShoppingRepository) : ViewModel() {

    // State flows
    private val _selectedLabels = MutableStateFlow<Set<String>>(emptySet())
    val selectedLabels: StateFlow<Set<String>> = _selectedLabels.asStateFlow()

    private val _filterMode = MutableStateFlow(FilterMode.AND)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    private val _showPurchased = MutableStateFlow(false)
    val showPurchased: StateFlow<Boolean> = _showPurchased.asStateFlow()

    // NEW: Search functionality
    private val _searchResults = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val searchResults: StateFlow<List<ShoppingItem>> = _searchResults.asStateFlow()

    // Data flows
    val allItems: Flow<List<ShoppingItem>> = repository.getAllItems()
    val allLabels: Flow<List<Label>> = repository.getAllLabels()

    // Filtered items based on current filters
    val filteredItems: Flow<List<ShoppingItem>> = combine(
        allItems,
        selectedLabels,
        filterMode,
        showPurchased
    ) { items, labels, mode, purchased ->
        items.filter { item ->
            val purchaseFilter = if (purchased) true else !item.isPurchased
            
            if (labels.isEmpty()) {
                purchaseFilter
            } else {
                val itemLabels = item.labels.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val matchesLabels = when (mode) {
                    FilterMode.AND -> labels.all { label -> itemLabels.contains(label) }
                    FilterMode.OR -> labels.any { label -> itemLabels.contains(label) }
                }
                purchaseFilter && matchesLabels
            }
        }
    }

    // Budget calculations
    val budgetInfo: Flow<BudgetInfo> = filteredItems.map { items ->
        val totalEstimated = items.sumOf { it.estimatedPrice * it.quantity }
        val totalActual = items.filter { it.isPurchased && it.actualPrice != null }
            .sumOf { (it.actualPrice ?: 0.0) * it.quantity }
        val purchasedCount = items.count { it.isPurchased }
        val totalCount = items.size
        
        BudgetInfo(
            totalEstimated = totalEstimated,
            totalActual = totalActual,
            remaining = totalEstimated - totalActual,
            purchasedCount = purchasedCount,
            totalCount = totalCount,
            progressPercentage = if (totalCount > 0) (purchasedCount.toFloat() / totalCount * 100) else 0f
        )
    }

    // Shopping item operations
    fun addItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.insertItem(item)
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            repository.deleteAllItems()
        }
    }

    // NEW: Quick purchase functionality
    fun searchItems(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                _searchResults.value = repository.searchUnpurchasedItems(query)
            }
        }
    }

    fun markItemPurchased(itemId: Long, actualPrice: Double) {
        viewModelScope.launch {
            repository.markItemPurchased(itemId, actualPrice)
        }
    }

    // Label operations
    fun addLabel(label: Label) {
        viewModelScope.launch {
            repository.insertLabel(label)
        }
    }

    fun updateLabel(label: Label) {
        viewModelScope.launch {
            repository.updateLabel(label)
        }
    }

    fun deleteLabel(label: Label) {
        viewModelScope.launch {
            repository.deleteLabel(label)
        }
    }

    // NEW: Enhanced label management
    suspend fun getLabelCountByName(name: String): Int {
        return repository.getLabelCountByName(name)
    }

    fun renameLabel(oldLabel: Label, newName: String) {
        viewModelScope.launch {
            repository.updateItemsWithNewLabelName(oldLabel.name, newName)
            repository.updateLabel(oldLabel.copy(name = newName))
        }
    }

    fun deleteLabelAndReferences(label: Label) {
        viewModelScope.launch {
            repository.deleteLabelAndReferences(label)
        }
    }

    // Filter operations
    fun toggleLabelFilter(label: String) {
        val current = _selectedLabels.value.toMutableSet()
        if (current.contains(label)) {
            current.remove(label)
        } else {
            current.add(label)
        }
        _selectedLabels.value = current
    }

    fun clearLabelFilters() {
        _selectedLabels.value = emptySet()
    }

    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun toggleShowPurchased() {
        _showPurchased.value = !_showPurchased.value
    }

    enum class FilterMode {
        AND, OR
    }

    data class BudgetInfo(
        val totalEstimated: Double,
        val totalActual: Double,
        val remaining: Double,
        val purchasedCount: Int,
        val totalCount: Int,
        val progressPercentage: Float
    )
}

class ShoppingViewModelFactory(private val repository: ShoppingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}