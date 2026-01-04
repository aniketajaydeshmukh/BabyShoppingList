package com.maternitytracker.data.repository

import kotlinx.coroutines.flow.Flow
import com.maternitytracker.data.dao.LabelDao
import com.maternitytracker.data.dao.ShoppingItemDao
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem
import java.util.Date

class ShoppingRepository(
    private val shoppingItemDao: ShoppingItemDao,
    private val labelDao: LabelDao
) {
    // Shopping Items
    fun getAllItems(): Flow<List<ShoppingItem>> = shoppingItemDao.getAllItems()

    suspend fun getItemById(id: Long): ShoppingItem? = shoppingItemDao.getItemById(id)

    suspend fun insertItem(item: ShoppingItem): Long = shoppingItemDao.insertItem(item)

    suspend fun updateItem(item: ShoppingItem) = shoppingItemDao.updateItem(item)

    suspend fun deleteItem(item: ShoppingItem) = shoppingItemDao.deleteItem(item)

    suspend fun deleteAllItems() = shoppingItemDao.deleteAllItems()

    // NEW: Search functionality for quick purchase
    suspend fun searchUnpurchasedItems(searchQuery: String): List<ShoppingItem> = 
        shoppingItemDao.searchUnpurchasedItems(searchQuery)

    // NEW: Mark item as purchased with actual price
    suspend fun markItemPurchased(itemId: Long, actualPrice: Double) = 
        shoppingItemDao.markItemPurchased(itemId, actualPrice, Date())

    // Labels
    fun getAllLabels(): Flow<List<Label>> = labelDao.getAllLabels()

    suspend fun getLabelById(id: Long): Label? = labelDao.getLabelById(id)

    suspend fun insertLabel(label: Label): Long = labelDao.insertLabel(label)

    suspend fun updateLabel(label: Label) = labelDao.updateLabel(label)

    suspend fun deleteLabel(label: Label) = labelDao.deleteLabel(label)

    suspend fun deleteAllLabels() = labelDao.deleteAllLabels()

    // NEW: Enhanced label management
    suspend fun getLabelCountByName(name: String): Int = labelDao.getLabelCountByName(name)

    suspend fun updateItemsWithNewLabelName(oldLabel: String, newLabel: String) = 
        labelDao.updateItemsWithNewLabelName(oldLabel, newLabel)

    suspend fun deleteLabelAndReferences(label: Label) = labelDao.deleteLabelAndReferences(label)
}