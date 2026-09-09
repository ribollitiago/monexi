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
): Transaction? {
    val category = categories.firstOrNull { it.id == categoryId }
    val paymentMethod = paymentMethods.firstOrNull { it.id == paymentMethodId }

    if (category == null || paymentMethod == null) {
        return null
    }

    return Transaction(
        id = id,
        title = title,
        amount = amount,
        type = TransactionType.valueOf(type),
        category = category,
        paymentMethod = paymentMethod,
        date = date
    )
}