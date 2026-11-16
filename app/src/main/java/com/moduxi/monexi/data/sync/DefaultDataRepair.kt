package com.moduxi.monexi.data.sync

import androidx.room.withTransaction
import com.moduxi.monexi.data.local.DefaultData
import com.moduxi.monexi.data.local.MonexiDatabase

/**
 * Garante as categorias e formas de pagamento padrão com ids fixos e conserta
 * instalações antigas, em que elas tinham ids aleatórios: os lançamentos e
 * recorrências passam a apontar para o id fixo e a linha antiga é removida.
 * Pode rodar em toda abertura do app: quando não há nada a consertar, não faz nada.
 */
class DefaultDataRepair(
    private val database: MonexiDatabase,
    private val outbox: SyncOutbox
) {

    suspend fun ensureDefaultsAndRepair() {
        val categoryDao = database.categoryDao()
        val paymentMethodDao = database.paymentMethodDao()
        val transactionDao = database.transactionDao()
        val recurringDao = database.recurringTransactionDao()

        database.withTransaction {
            val now = System.currentTimeMillis()

            val localCategories = categoryDao.getCategories()
            DefaultData.categories.forEach { default ->
                if (localCategories.none { it.id == default.id }) {
                    categoryDao.insertCategory(default)
                }
                localCategories
                    .filter { it.isDefault && it.id != default.id && it.name == default.name && it.type == default.type }
                    .forEach { legacy ->
                        // Guardado antes de apagar a linha: lançamentos que ainda estão só na nuvem
                        // com o id antigo são convertidos no download
                        outbox.putLegacyId(legacy.id, default.id)
                        transactionDao.remapCategory(oldId = legacy.id, newId = default.id, now = now)
                        recurringDao.remapCategory(oldId = legacy.id, newId = default.id)
                        categoryDao.deleteCategory(legacy)
                    }
            }

            val localPaymentMethods = paymentMethodDao.getPaymentMethods()
            DefaultData.paymentMethods.forEach { default ->
                if (localPaymentMethods.none { it.id == default.id }) {
                    paymentMethodDao.insertPaymentMethod(default)
                }
                localPaymentMethods
                    .filter { it.isDefault && it.id != default.id && it.name == default.name }
                    .forEach { legacy ->
                        outbox.putLegacyId(legacy.id, default.id)
                        transactionDao.remapPaymentMethod(oldId = legacy.id, newId = default.id, now = now)
                        recurringDao.remapPaymentMethod(oldId = legacy.id, newId = default.id)
                        paymentMethodDao.deletePaymentMethod(legacy)
                    }
            }
        }
    }
}
