package com.moduxi.monexi

import android.app.Application
import com.moduxi.monexi.data.local.MonexiDatabase
import com.moduxi.monexi.data.repository.RoomCategoryRepository

class MonexiApplication : Application() {
    val database: MonexiDatabase by lazy { MonexiDatabase.getInstance(this) }
    val categoryRepository: RoomCategoryRepository by lazy { RoomCategoryRepository(database.categoryDao()) }
}