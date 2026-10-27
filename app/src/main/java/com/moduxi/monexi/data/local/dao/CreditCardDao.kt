package com.moduxi.monexi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moduxi.monexi.data.local.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM creditCards WHERE id = :id")
    suspend fun getCreditCardById(id: String): CreditCardEntity?

    @Query("SELECT * FROM creditCards WHERE userId = :userId AND isArchived = 0 ORDER BY name ASC")
    fun observeCreditCardsByUser(userId: String): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM creditCards WHERE userId = :userId ORDER BY name ASC")
    fun observeAllCreditCardsByUser(userId: String): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM creditCards WHERE userId = :userId AND isArchived = 1 ORDER BY name ASC")
    fun observeArchivedCreditCardsByUser(userId: String): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(creditCard: CreditCardEntity): Long

    @Update
    suspend fun updateCreditCard(creditCard: CreditCardEntity)

    @Delete
    suspend fun deleteCreditCard(creditCard: CreditCardEntity)
}