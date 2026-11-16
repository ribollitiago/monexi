package com.moduxi.monexi

import android.app.Application
import com.moduxi.monexi.data.local.MonexiDatabase
import com.moduxi.monexi.data.repository.FirebaseAuthRepository
import com.moduxi.monexi.data.repository.RoomAccountRepository
import com.moduxi.monexi.data.repository.RoomCategoryRepository
import com.moduxi.monexi.data.repository.RoomCreditCardRepository
import com.moduxi.monexi.data.repository.RoomPaymentMethodRepository
import com.moduxi.monexi.data.repository.RoomRecurringTransactionRepository
import com.moduxi.monexi.data.repository.RoomTransactionRepository
import com.moduxi.monexi.data.repository.local.OnboardingPrefs
import com.moduxi.monexi.data.repository.local.ThemeManager
import com.moduxi.monexi.data.sync.DefaultDataRepair
import com.moduxi.monexi.data.sync.RemoteStore
import com.moduxi.monexi.data.sync.SyncManager
import com.moduxi.monexi.data.sync.SyncOutbox
import com.moduxi.monexi.domain.repository.AccountRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.CreditCardRepository
import com.moduxi.monexi.domain.repository.PaymentMethodRepository
import com.moduxi.monexi.domain.repository.RecurringTransactionRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import com.moduxi.monexi.domain.usecase.GenerateRecurringTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MonexiApplication : Application() {
    val themeManager by lazy { ThemeManager(this) }
    val onboardingPrefs by lazy { OnboardingPrefs(this) }
    val database: MonexiDatabase by lazy { MonexiDatabase.getInstance(this) }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }

    // Fila de envios pendentes e escrita no Firestore sem bloquear (offline-first)
    val syncOutbox by lazy { SyncOutbox(this) }
    val remoteStore by lazy { RemoteStore(authRepository, syncOutbox) }

    // Implementações concretas: o SyncManager precisa delas para o envio completo
    private val roomCategoryRepository by lazy {
        RoomCategoryRepository(
            categoryDao = database.categoryDao(),
            authRepository = authRepository,
            remoteStore = remoteStore
        )
    }

    private val roomPaymentMethodRepository by lazy {
        RoomPaymentMethodRepository(
            paymentMethodDao = database.paymentMethodDao(),
            authRepository = authRepository,
            remoteStore = remoteStore
        )
    }

    private val roomAccountRepository by lazy {
        RoomAccountRepository(
            accountDao = database.accountDao(),
            authRepository = authRepository,
            remoteStore = remoteStore
        )
    }

    private val roomCreditCardRepository by lazy {
        RoomCreditCardRepository(
            creditCardDao = database.creditCardDao(),
            transactionDao = database.transactionDao(),
            authRepository = authRepository,
            remoteStore = remoteStore
        )
    }

    private val roomTransactionRepository by lazy {
        RoomTransactionRepository(
            transactionDao = database.transactionDao(),
            categoryRepository = roomCategoryRepository,
            paymentMethodRepository = roomPaymentMethodRepository,
            authRepository = authRepository,
            remoteStore = remoteStore
        )
    }

    private val roomRecurringTransactionRepository by lazy {
        RoomRecurringTransactionRepository(
            recurringTransactionDao = database.recurringTransactionDao(),
            transactionDao = database.transactionDao(),
            authRepository = authRepository,
            remoteStore = remoteStore
        )
    }

    val categoryRepository: CategoryRepository get() = roomCategoryRepository
    val paymentMethodRepository: PaymentMethodRepository get() = roomPaymentMethodRepository
    val accountRepository: AccountRepository get() = roomAccountRepository
    val creditCardRepository: CreditCardRepository get() = roomCreditCardRepository
    val transactionRepository: TransactionRepository get() = roomTransactionRepository
    val recurringTransactionRepository: RecurringTransactionRepository get() = roomRecurringTransactionRepository

    val syncManager by lazy {
        SyncManager(
            database = database,
            authRepository = authRepository,
            defaultDataRepair = DefaultDataRepair(database, syncOutbox),
            outbox = syncOutbox,
            remoteStore = remoteStore,
            categoryRepository = roomCategoryRepository,
            paymentMethodRepository = roomPaymentMethodRepository,
            accountRepository = roomAccountRepository,
            creditCardRepository = roomCreditCardRepository,
            recurringTransactionRepository = roomRecurringTransactionRepository,
            transactionRepository = roomTransactionRepository,
            generateRecurringTransactions = GenerateRecurringTransactionsUseCase(
                recurringTransactionRepository = roomRecurringTransactionRepository,
                transactionRepository = roomTransactionRepository,
                categoryRepository = roomCategoryRepository,
                paymentMethodRepository = roomPaymentMethodRepository
            )
        )
    }

    override fun onCreate() {
        super.onCreate()

        // Dados padrão com id fixo + reparo de instalações antigas (local, funciona sem internet e sem login).
        // A sincronização com a nuvem e a geração dos recorrentes ficam no SyncManager,
        // chamado no login e na abertura da Home.
        CoroutineScope(Dispatchers.IO).launch {
            syncManager.ensureLocalDefaults()
        }
    }
}
