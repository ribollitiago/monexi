package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.domain.repository.CategoryRepository
import com.moduxi.monexi.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class PayCreditCardBillUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(
        bill: CreditCardBill,
        creditCard: CreditCard,
        accountId: String,
        paymentMethod: PaymentMethod,
        userId: String,
        amountToPay: Double = bill.remainingBalance.takeIf { it > 0.0 } ?: bill.totalAmount,
        paymentDateMillis: Long = System.currentTimeMillis()
    ): Transaction {
        val categories = categoryRepository.categories.firstOrNull() ?: emptyList()
        val category = categories.firstOrNull { it.name.contains("Fatura", ignoreCase = true) }
            ?: categories.firstOrNull { it.type == TransactionType.EXPENSE }
            ?: Category(
                id = "bill_payment",
                name = "Fatura de Cartão",
                type = TransactionType.EXPENSE,
                isDefault = true
            )

        val paymentTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = "Pagamento Fatura - ${creditCard.name} (${bill.month}/${bill.year})",
            amount = amountToPay,
            type = TransactionType.EXPENSE,
            category = category,
            paymentMethod = paymentMethod,
            date = paymentDateMillis,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_CREATE,
            accountId = accountId,
            creditCardId = creditCard.id,
            creditCardBillId = bill.id
        )

        transactionRepository.addTransaction(paymentTransaction)
        return paymentTransaction
    }
}
