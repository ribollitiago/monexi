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
import com.moduxi.monexi.data.local.dao.TransactionDao
import com.moduxi.monexi.data.local.entity.AccountEntity
import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.data.local.entity.CreditCardEntity
import com.moduxi.monexi.data.local.entity.PaymentMethodEntity
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
        CreditCardEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class MonexiDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun creditCardDao(): CreditCardDao

    companion object {
        @Volatile
        private var INSTANCE: MonexiDatabase? = null

        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        val categoryDao = database.categoryDao()
                        categoryDao.insertCategories(
                            listOf(
                                CategoryEntity(name = "Alimentação", type = TransactionType.EXPENSE.name, isDefault = true, iconName = "Restaurant", colorHex = "#E53935"),
                                CategoryEntity(name = "Transporte", type = TransactionType.EXPENSE.name, isDefault = true, iconName = "DirectionsCar", colorHex = "#1E88E5"),
                                CategoryEntity(name = "Casa", type = TransactionType.EXPENSE.name, isDefault = true, iconName = "Home", colorHex = "#8E24AA"),
                                CategoryEntity(name = "Saúde", type = TransactionType.EXPENSE.name, isDefault = true, iconName = "MedicalServices", colorHex = "#00ACC1"),
                                CategoryEntity(name = "Salário", type = TransactionType.INCOME.name, isDefault = true, iconName = "Work", colorHex = "#43A047"),
                                CategoryEntity(name = "Freelance", type = TransactionType.INCOME.name, isDefault = true, iconName = "AttachMoney", colorHex = "#FB8C00"),
                                CategoryEntity(name = "Investimentos", type = TransactionType.INCOME.name, isDefault = true, iconName = "AttachMoney", colorHex = "#3949AB")
                            )
                        )
                        val paymentMethodDao = database.paymentMethodDao()
                        paymentMethodDao.insertPaymentMethods(
                            listOf(
                                PaymentMethodEntity(name = "Dinheiro", isDefault = true),
                                PaymentMethodEntity(name = "Cartão de Crédito", isDefault = true),
                                PaymentMethodEntity(name = "Cartão de Débito", isDefault = true),
                                PaymentMethodEntity(name = "Pix", isDefault = true)
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
                    .fallbackToDestructiveMigration(true) //ALTERAR PARA MIGRATIONS NO FUTURO
                    .addCallback(databaseCallback)
                    .build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}