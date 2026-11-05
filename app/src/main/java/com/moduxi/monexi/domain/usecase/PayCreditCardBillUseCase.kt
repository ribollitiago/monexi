package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.creditcard.CreditCardBill
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.SyncStatus
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import com.moduxi.monexi.domain.repository.TransactionRepository
import java.util.UUID

class PayCreditCardBillUseCase(
    private val transactionRepository: TransactionRepository
) {

    suspend operator fun invoke(
        bill: CreditCardBill,
        creditCard: CreditCard,
        accountId: String,
        paymentMethod: PaymentMethod,
        userId: String
    ) {
        val paymentTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = "Pagamento Fatura - ${creditCard.name} (${bill.month}/${bill.year})",
            amount = bill.totalAmount,
            type = TransactionType.EXPENSE,
            category = Category(
                id = "bill_payment",
                name = "Fatura de Cartão",
                type = TransactionType.EXPENSE,
                isDefault = true
            ),
            paymentMethod = paymentMethod,
            date = bill.dueDateMillis,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_CREATE,
            accountId = accountId,
            creditCardId = creditCard.id,
            creditCardBillId = bill.id
        )

        transactionRepository.addTransaction(paymentTransaction)
    }
}
