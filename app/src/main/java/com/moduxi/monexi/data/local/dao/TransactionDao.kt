package com.moduxi.monexi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moduxi.monexi.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY date DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND deletedAt IS NULL ORDER BY date DESC")
    fun observeTransactionsByUser(userId: String): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun clearTransactionsByUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    //Extrato Conta
    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
            AND accountId = :accountId
            AND type = 'INCOME'
            AND deletedAt IS NULL
        ORDER BY date DESC
    """)
    fun observeTransactionsByAccount(userId: String, accountId: String): Flow<List<TransactionEntity>>

    //Soma RECEITAS de uma conta
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE userId = :userId
            AND accountId = :accountId
            AND type = 'INCOME'
            AND deletedAt IS NULL
    """)
    fun observeTotalIncomeByAccount(userId: String, accountId: String): Flow<Double>

    //Soma DESPESAS de uma conta
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE userId = :userId
            AND accountId = :accountId
            AND type = 'EXPENSE'
            AND deletedAt IS NULL
    """)
    fun observeTotalExpenseByAccount(userId: String, accountId: String): Flow<Double>

    //Extrato Cartao
    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
            AND creditCardId = :creditCardId
            AND deletedAt IS NULL
        ORDER BY date DESC
    """)
    fun observeTransactionsByCreditCard(userId: String, creditCardId: String): Flow<List<TransactionEntity>>

    //Transferencias fatura
    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
            AND creditCardBillId = :billId
            AND deletedAt IS NULL
        ORDER BY date DESC
    """)
    fun observeTransactionsByBill(userId: String, billId: String): Flow<List<TransactionEntity>>

    //Total gasto ainda em aberto (calcular limite)
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE userId = :userId
            AND creditCardId = :creditCardId
            AND type = 'EXPENSE'
            AND deletedAt IS NULL
    """)
    fun observeTotalSpendByCreditCard(userId: String, creditCardId: String): Flow<Double>

    //Buscar todas as parcelas de uma compra
    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
            AND installmentGroupId = :groupId
            AND deletedAt IS NULL
        ORDER BY installmentNumber ASC
    """)
    suspend fun getInstallmentsByGroup(userId: String, groupId: String): List<TransactionEntity>

    //inserir varias parcelas de uma vez só
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    //Verificar se uma transação recorrente já gerou lançamento no mês
    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
            AND recurringTransactionId = :recurringId
            AND date BETWEEN :startDate AND :endDate
            AND deletedAt IS NULL
        LIMIT 1
    """)
    suspend fun getRecurringTransactionInPeriod(
        userId: String,
        recurringId: String,
        startDate: Long,
        endDate: Long
    ): TransactionEntity?
}
