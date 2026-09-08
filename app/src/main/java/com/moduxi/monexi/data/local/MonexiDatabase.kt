package com.moduxi.monexi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moduxi.monexi.data.local.dao.CategoryDao
import com.moduxi.monexi.data.local.entity.CategoryEntity

@Database(
    entities = [
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MonexiDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MonexiDatabase? = null

        fun getInstance(context: Context): MonexiDatabase{
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonexiDatabase::class.java,
                    "monexi.db"
                ).build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}