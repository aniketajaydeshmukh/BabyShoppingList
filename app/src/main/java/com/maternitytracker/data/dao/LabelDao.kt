package com.maternitytracker.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.maternitytracker.data.entities.Label

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAllLabels(): Flow<List<Label>>

    @Query("SELECT * FROM labels WHERE id = :id")
    suspend fun getLabelById(id: Long): Label?

    @Insert
    suspend fun insertLabel(label: Label): Long

    @Update
    suspend fun updateLabel(label: Label)

    @Delete
    suspend fun deleteLabel(label: Label)

    @Query("DELETE FROM labels")
    suspend fun deleteAllLabels()

    @Query("SELECT COUNT(*) FROM labels WHERE name = :name")
    suspend fun getLabelCountByName(name: String): Int

    @Query("UPDATE shopping_items SET labels = REPLACE(labels, :oldLabel, :newLabel) WHERE labels LIKE '%' || :oldLabel || '%'")
    suspend fun updateItemsWithNewLabelName(oldLabel: String, newLabel: String)

    @Query("DELETE FROM shopping_items WHERE labels = :labelName OR labels LIKE :labelName || ',%' OR labels LIKE '%,' || :labelName || ',%' OR labels LIKE '%,' || :labelName")
    suspend fun deleteAllItemsForLabel(labelName: String)

    @Transaction
    suspend fun deleteLabelAndReferences(label: Label) {
        deleteAllItemsForLabel(label.name)
        deleteLabel(label)
    }
}