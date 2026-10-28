package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.TransactionEntity
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = type.name,
        categoryId = category.id,
        paymentMethodId = paymentMethod.id,
        date = date,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = syncStatus.name,
        remoteId = remoteId
    )
}

fun TransactionEntity.toDomain(
    categories: List<Category>,
    paymentMethods: List<PaymentMethod>
): Transaction? {
    val category = categories.firstOrNull { it.id == categoryId }
    val paymentMethod = paymentMethods.firstOrNull { it.id == paymentMethodId }

    if (category == null || paymentMethod == null) {
        return null
    }

    val transactionType = try {
        TransactionType.valueOf(type)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    val status = try {
        SyncStatus.valueOf(syncStatus)
    } catch (e: Exception) {
        SyncStatus.SYNCED
    }

    return Transaction(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = transactionType,
        category = category,
        paymentMethod = paymentMethod,
        date = date,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = status,
        remoteId = remoteId
    )
}