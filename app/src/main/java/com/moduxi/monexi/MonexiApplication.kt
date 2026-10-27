package com.moduxi.monexi

import android.app.Application
import com.moduxi.monexi.data.local.MonexiDatabase
import com.moduxi.monexi.data.repository.FirebaseAuthRepository
import com.moduxi.monexi.data.repository.RoomAccountRepository
import com.moduxi.monexi.data.repository.RoomCategoryRepository
import com.moduxi.monexi.data.repository.RoomCreditCardRepository
import com.moduxi.monexi.data.repository.RoomPaymentMethodRepository
import com.moduxi.monexi.data.repository.RoomTransactionRepository
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.TransactionRepository

class MonexiApplication : Application() {
    val themeManager by lazy { ThemeManager(this) }
    val database: MonexiDatabase by lazy { MonexiDatabase.getInstance(this) }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }

    val categoryRepository: RoomCategoryRepository by lazy {
        RoomCategoryRepository(
            categoryDao = database.categoryDao(),
            authRepository = authRepository
        )
    }

    val paymentMethodRepository: PaymentMethodRepository by lazy {
        RoomPaymentMethodRepository(
            paymentMethodDao = database.paymentMethodDao(),
            authRepository = authRepository
        )
    }

    val accountRepository: AccountRepository by lazy {
        RoomAccountRepository(
            accountDao = database.accountDao(),
            authRepository = authRepository
        )
    }

    val creditCardRepository: CreditCardRepository by lazy {
        RoomCreditCardRepository(
            creditCardDao = database.creditCardDao(),
            authRepository = authRepository
        )
    }

    val transactionRepository: TransactionRepository by lazy {
        RoomTransactionRepository(
            transactionDao = database.transactionDao(),
            categoryRepository = categoryRepository,
            paymentMethodRepository = paymentMethodRepository,
            authRepository = authRepository
        )
    }
}
