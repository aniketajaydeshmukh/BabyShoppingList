package com.maternitytracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.maternitytracker.data.dao.LabelDao
import com.maternitytracker.data.dao.ShoppingItemDao
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem

@Database(
    entities = [ShoppingItem::class, Label::class],
    version = 2, // Updated version for new features
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun labelDao(): LabelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to shopping_items table
                database.execSQL("ALTER TABLE shopping_items ADD COLUMN actualPrice REAL")
                database.execSQL("ALTER TABLE shopping_items ADD COLUMN purchasedAt INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "maternity_tracker_database"
                )
                .addMigrations(MIGRATION_1_2) // Add migration support
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}