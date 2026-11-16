package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.recurring.RecurringTransaction
import com.moduxi.monexi.domain.model.transaction.TransactionType

/**
 * Forma de pagamento de uma recorrência. Recorrências antigas foram salvas sem
 * paymentMethodId; nesse caso escolhe uma forma coerente com a origem
 * (crédito se for no cartão; senão "Dinheiro" para a carteira ou "Pix" para
 * conta bancária, sempre respeitando o que é válido para receita/despesa).
 */
fun RecurringTransaction.resolvePaymentMethod(
    paymentMethods: List<PaymentMethod>,
    isCashAccount: Boolean = false
): PaymentMethod? {
    paymentMethods.firstOrNull { it.id == paymentMethodId }?.let { return it }

    return if (creditCardId != null) {
        paymentMethods.firstOrNull { it.isCredit() } ?: paymentMethods.firstOrNull()
    } else {
        paymentMethods.firstNonCredit(isCashAccount, type)
    }
}

fun PaymentMethod.isCredit(): Boolean = name.contains("Crédito", ignoreCase = true)

/** Receita não pode ser recebida em cartão (crédito/débito). */
fun PaymentMethod.isValidFor(type: TransactionType): Boolean {
    if (type != TransactionType.INCOME) return true
    val lower = name.lowercase()
    return !lower.contains("crédito") && !lower.contains("débito") &&
            !lower.contains("debito") && !lower.contains("cartão")
}

/**
 * Forma de pagamento padrão que não é crédito: "Dinheiro" para a carteira,
 * "Pix" para conta bancária, senão a primeira válida para o tipo.
 */
fun List<PaymentMethod>.firstNonCredit(
    isCashAccount: Boolean = false,
    type: TransactionType = TransactionType.EXPENSE
): PaymentMethod? {
    val candidates = filter { !it.isCredit() && it.isValidFor(type) }
    val preferredName = if (isCashAccount) "Dinheiro" else "Pix"
    return candidates.firstOrNull { it.name.contains(preferredName, ignoreCase = true) }
        ?: candidates.firstOrNull()
        ?: firstOrNull()
}
