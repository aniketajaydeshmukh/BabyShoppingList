package com.maternitytracker.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.maternitytracker.data.entities.ShoppingItem
import java.util.Date

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getItemById(id: Long): ShoppingItem?

    @Insert
    suspend fun insertItem(item: ShoppingItem): Long

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM shopping_items WHERE isPurchased = 0 AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchUnpurchasedItems(searchQuery: String): List<ShoppingItem>

    @Query("UPDATE shopping_items SET isPurchased = 1, actualPrice = :actualPrice, purchasedAt = :purchasedAt WHERE id = :itemId")
    suspend fun markItemPurchased(itemId: Long, actualPrice: Double, purchasedAt: Date)
}