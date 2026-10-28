package com.moduxi.monexi.domain.model.transaction

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val paymentMethod: PaymentMethod,
    val date: Long,
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val remoteId: String? = null,

    val accountId: String? = null,
    val creditCardId: String? = null,
    val creditCardBillId: String? = null,

    val installmentNumber: Int? = null,
    val totalInstallments: Int? = null,
    val installmentGroupId: String? = null,

    val recurringTransactionId: String? = null
)
