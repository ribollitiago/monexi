package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.TransactionEntity
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.PaymentMethod
import com.moduxi.monexi.domain.model.Transaction
import com.moduxi.monexi.domain.model.TransactionType

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type.name,
        categoryId = category.id,
        paymentMethodId = paymentMethod.id,
        date = date
    )
}

fun TransactionEntity.toDomain(
    categories: List<Category>,
    paymentMethods: List<PaymentMethod>
): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        type = TransactionType.valueOf(type),
        category = categories.first { it.id == categoryId },
        paymentMethod = paymentMethods.first { it.id == paymentMethodId },
        date = date
    )
}