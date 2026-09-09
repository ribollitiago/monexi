package com.moduxi.monexi

import android.app.Application
import com.moduxi.monexi.data.local.MonexiDatabase
import com.moduxi.monexi.data.repository.RoomCategoryRepository
import com.moduxi.monexi.data.repository.RoomPaymentMethodRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository

class MonexiApplication : Application() {
    val database: MonexiDatabase by lazy { MonexiDatabase.getInstance(this) }
    val categoryRepository: RoomCategoryRepository by lazy { RoomCategoryRepository(database.categoryDao()) }
    val paymentMethodRepository: PaymentMethodRepository by lazy { RoomPaymentMethodRepository(database.paymentMethodDao()) }
}