package com.moduxi.monexi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moduxi.monexi.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM paymentMethods ORDER BY name ASC")
    fun observePaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM paymentMethods")
    suspend fun getPaymentMethods(): List<PaymentMethodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>)

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity)
}