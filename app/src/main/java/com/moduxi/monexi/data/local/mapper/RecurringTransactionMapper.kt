package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.RecurringTransactionEntity
import com.moduxi.monexi.domain.model.recurring.Frequency
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

fun RecurringTransaction.toEntity(): RecurringTransactionEntity {
    return RecurringTransactionEntity(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = type.name,
        categoryId = categoryId,
        accountId = accountId,
        creditCardId = creditCardId,
        dayOfMonth = dayOfMonth,
        frequency = frequency.name,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive
    )
}

fun RecurringTransactionEntity.toDomain(): RecurringTransaction {
    val transactionType = try {
        TransactionType.valueOf(type)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    val freq = try {
        Frequency.valueOf(frequency)
    } catch (e: Exception) {
        Frequency.MONTHLY
    }

    return RecurringTransaction(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = transactionType,
        categoryId = categoryId,
        accountId = accountId,
        creditCardId = creditCardId,
        dayOfMonth = dayOfMonth,
        frequency = freq,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive
    )
}
