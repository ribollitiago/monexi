package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.account.AccountType
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
    accountType: AccountType? = null
): PaymentMethod? {
    paymentMethods.firstOrNull { it.id == paymentMethodId }?.let { return it }

    return if (creditCardId != null) {
        paymentMethods.firstOrNull { it.isCredit() } ?: paymentMethods.firstOrNull()
    } else {
        paymentMethods.firstNonCredit(accountType, type)
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

/** Vale Refeição / Alimentação (forma de pagamento das contas de vale-benefício). */
fun PaymentMethod.isBenefitsVoucher(): Boolean {
    val lower = name.lowercase()
    return lower.contains("vale") && (lower.contains("refei") || lower.contains("aliment"))
}

/**
 * A forma de pagamento faz sentido para este tipo de conta?
 * - Vale Refeição / Alimentação: só contas de vale-benefício, e elas só aceitam o vale
 * - Dinheiro: só carteira (dinheiro físico)
 * - Cartão de Débito, Pix e Transferência: contas que não sejam a carteira
 * - Crédito usa cartão, não conta; formas personalizadas valem para qualquer conta comum
 */
fun PaymentMethod.isCompatibleWith(accountType: AccountType): Boolean {
    if (isCredit()) return false
    if (accountType == AccountType.BENEFITS || isBenefitsVoucher()) {
        return accountType == AccountType.BENEFITS && isBenefitsVoucher()
    }

    val lower = name.lowercase()
    return when {
        lower.contains("dinheiro") -> accountType == AccountType.CASH
        lower.contains("débito") || lower.contains("debito") ||
                lower.contains("pix") ||
                lower.contains("transferência") || lower.contains("transferencia") ->
            accountType != AccountType.CASH
        else -> true
    }
}

/**
 * Forma de pagamento padrão que não é crédito, válida para o tipo de lançamento
 * e compatível com a conta: "Dinheiro" para a carteira, o vale para contas de
 * vale-benefício, "Pix" para as demais quando possível, senão a primeira compatível.
 */
fun List<PaymentMethod>.firstNonCredit(
    accountType: AccountType? = null,
    type: TransactionType = TransactionType.EXPENSE
): PaymentMethod? {
    val candidates = filter {
        !it.isCredit() && it.isValidFor(type) && (accountType == null || it.isCompatibleWith(accountType))
    }
    val preferredName = if (accountType == AccountType.CASH) "Dinheiro" else "Pix"
    return candidates.firstOrNull { it.name.contains(preferredName, ignoreCase = true) }
        ?: candidates.firstOrNull()
        ?: firstOrNull { !it.isCredit() && it.isValidFor(type) }
        ?: firstOrNull()
}
