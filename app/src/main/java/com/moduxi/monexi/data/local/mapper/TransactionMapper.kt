package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.TransactionEntity
import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

const val MISSING_CATEGORY_NAME = "Sem categoria"
const val MISSING_PAYMENT_METHOD_NAME = "Não informado"

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
        remoteId = remoteId,
        accountId = accountId,
        creditCardId = creditCardId,
        creditCardBillId = creditCardBillId,
        installmentNumber = installmentNumber,
        totalInstallments = totalInstallments,
        installmentGroupId = installmentGroupId,
        recurringTransactionId = recurringTransactionId,
    )
}

fun TransactionEntity.toDomain(
    categories: List<Category>,
    paymentMethods: List<PaymentMethod>
): Transaction? {
    val transactionType = try {
        TransactionType.valueOf(type)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    // Categoria ou forma de pagamento que não existe neste aparelho: o lançamento continua
    // aparecendo (antes era descartado e o saldo ficava errado); o id original é preservado.
    val category = categories.firstOrNull { it.id == categoryId }
        ?: Category(id = categoryId, name = MISSING_CATEGORY_NAME, type = transactionType, colorHex = "#9E9E9E")
    val paymentMethod = paymentMethods.firstOrNull { it.id == paymentMethodId }
        ?: PaymentMethod(id = paymentMethodId, name = MISSING_PAYMENT_METHOD_NAME)

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
        remoteId = remoteId,
        accountId = accountId,
        creditCardId = creditCardId,
        creditCardBillId = creditCardBillId,
        installmentNumber = installmentNumber,
        totalInstallments = totalInstallments,
        installmentGroupId = installmentGroupId,
        recurringTransactionId = recurringTransactionId
    )
}