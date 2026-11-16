package com.moduxi.monexi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moduxi.monexi.data.local.dao.AccountDao
import com.moduxi.monexi.data.local.dao.CategoryDao
import com.moduxi.monexi.data.local.dao.CreditCardDao
import com.moduxi.monexi.data.local.dao.PaymentMethodDao
import com.moduxi.monexi.data.local.dao.RecurringTransactionDao
import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.entity.AccountEntity
import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.data.local.entity.CreditCardEntity
import com.moduxi.monexi.data.local.entity.PaymentMethodEntity
import com.moduxi.monexi.data.local.entity.RecurringTransactionEntity
import com.moduxi.monexi.data.local.entity.TransactionEntity
import com.moduxi.monexi.domain.model.transaction.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        PaymentMethodEntity::class,
        TransactionEntity::class,
        AccountEntity::class,
        CreditCardEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class MonexiDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: MonexiDatabase? = null


        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        // Ids fixos (ver DefaultData): os mesmos em qualquer instalação
                        database.categoryDao().insertCategories(DefaultData.categories)
                        database.paymentMethodDao().insertPaymentMethods(DefaultData.paymentMethods)
                        // A conta "Carteira" é criada por usuário em AccountRepository.ensureDefaultAccount()
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
                    .fallbackToDestructiveMigration(true) //ALTERAR PARA MIGRATIONS NO FUTURO
                    .addCallback(databaseCallback)
                    .build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}
