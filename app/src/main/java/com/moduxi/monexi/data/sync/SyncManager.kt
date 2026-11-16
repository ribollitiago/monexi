package com.moduxi.monexi.data.sync

import android.util.Log
import com.moduxi.monexi.data.local.MonexiDatabase
import com.moduxi.monexi.data.repository.RoomAccountRepository
import com.moduxi.monexi.data.repository.RoomCategoryRepository
import com.moduxi.monexi.data.repository.RoomCreditCardRepository
import com.moduxi.monexi.data.repository.RoomPaymentMethodRepository
import com.moduxi.monexi.data.repository.RoomRecurringTransactionRepository
import com.moduxi.monexi.data.repository.RoomTransactionRepository
import com.moduxi.monexi.domain.repository.AuthRepository
import com.moduxi.monexi.domain.usecase.GenerateRecurringTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Ponto único de sincronização (login e abertura do app).
 *
 * Ordem:
 * 1. garante os dados padrão com id fixo e conserta instalações antigas (local);
 * 2. reenvia o que ainda não foi confirmado pela nuvem;
 * 3. baixa tudo na ordem das dependências (categorias e formas antes dos lançamentos);
 * 4. uma única vez por usuário/instalação, envia todos os dados locais (conserta o que
 *    as versões antigas nunca enviaram: carteira padrão, campos faltantes, ids corrigidos);
 * 5. gera os lançamentos recorrentes do mês, já com os dados da nuvem.
 *
 * Sem internet nada trava: as etapas de rede têm tempo limite e o app segue com o Room.
 */
class SyncManager(
    private val database: MonexiDatabase,
    private val authRepository: AuthRepository,
    private val defaultDataRepair: DefaultDataRepair,
    private val outbox: SyncOutbox,
    private val remoteStore: RemoteStore,
    private val categoryRepository: RoomCategoryRepository,
    private val paymentMethodRepository: RoomPaymentMethodRepository,
    private val accountRepository: RoomAccountRepository,
    private val creditCardRepository: RoomCreditCardRepository,
    private val recurringTransactionRepository: RoomRecurringTransactionRepository,
    private val transactionRepository: RoomTransactionRepository,
    private val generateRecurringTransactions: GenerateRecurringTransactionsUseCase
) {
    private val mutex = Mutex()

    @Volatile
    private var lastSyncAt = 0L

    @Volatile
    private var lastSyncUserId: String? = null

    /** Só a parte local (dados padrão + reparo); não depende de login nem de internet. */
    suspend fun ensureLocalDefaults() = withContext(Dispatchers.IO) {
        try {
            defaultDataRepair.ensureDefaultsAndRepair()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao preparar os dados padrão: ${e.message}", e)
        }
    }

    /**
     * @param force ignora o intervalo mínimo entre sincronizações (usado no login).
     * @return true se o download da nuvem foi concluído.
     */
    suspend fun syncAll(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureLocalDefaults()

            val userId = authRepository.currentUser?.uid?.takeIf { it.isNotEmpty() }
                ?: return@withLock false

            val now = System.currentTimeMillis()
            if (!force && userId == lastSyncUserId && now - lastSyncAt < MIN_INTERVAL_MS) {
                return@withLock true
            }

            // Instalação nova (sem dados locais deste usuário): não há nada antigo para consertar
            val hadLocalData = try {
                database.accountDao().observeAllAccountsByUser(userId).first().isNotEmpty() ||
                        database.transactionDao().getAllTransactionsByUser(userId).isNotEmpty()
            } catch (e: Exception) {
                true
            }
            if (!hadLocalData) outbox.markFullPushDone(userId)

            val pulled = withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
                try {
                    remoteStore.pushPending()

                    val results = listOf(
                        categoryRepository.syncFromRemote(),
                        paymentMethodRepository.syncFromRemote(),
                        accountRepository.syncFromRemote(),
                        creditCardRepository.syncFromRemote(),
                        recurringTransactionRepository.syncFromRemote(),
                        transactionRepository.syncFromRemote()
                    )
                    results.forEach { result ->
                        result.exceptionOrNull()?.let { Log.e(TAG, "Falha ao baixar dados: ${it.message}", it) }
                    }
                    results.all { it.isSuccess }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro na sincronização: ${e.message}", e)
                    false
                }
            } ?: false

            if (pulled && !outbox.isFullPushDone(userId)) {
                try {
                    categoryRepository.pushAllLocal()
                    paymentMethodRepository.pushAllLocal()
                    accountRepository.pushAllLocal()
                    creditCardRepository.pushAllLocal()
                    recurringTransactionRepository.pushAllLocal()
                    transactionRepository.pushAllLocal()
                    outbox.markFullPushDone(userId)
                } catch (e: Exception) {
                    Log.e(TAG, "Erro no envio completo: ${e.message}", e)
                }
            }

            // Roda mesmo sem internet: o que estiver agendado para hoje precisa aparecer
            try {
                generateRecurringTransactions(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao gerar lançamentos recorrentes: ${e.message}", e)
            }

            if (pulled) {
                lastSyncAt = now
                lastSyncUserId = userId
            }
            pulled
        }
    }

    private companion object {
        const val TAG = "FirestoreSync"
        const val MIN_INTERVAL_MS = 30_000L
        const val NETWORK_TIMEOUT_MS = 30_000L
    }
}
