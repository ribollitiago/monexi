package com.moduxi.monexi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moduxi.monexi.data.local.dao.CategoryDao
import com.moduxi.monexi.data.local.entity.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        val dao = database.categoryDao()
                        dao.insertCategories(
                            listOf(
                                CategoryEntity(name = "Alimentação", isDefault = true),
                                CategoryEntity(name = "Transporte", isDefault = true),
                                CategoryEntity(name = "Casa", isDefault = true),
                                CategoryEntity(name = "Trabalho", isDefault = true)
                            )
                        )
                    }
                }
            }
        }

        fun getInstance(context: Context): MonexiDatabase{
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonexiDatabase::class.java,
                    "monexi.db"
                )
                    .addCallback(databaseCallback)
                    .build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}