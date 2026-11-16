package com.moduxi.monexi.data.local

import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.data.local.entity.PaymentMethodEntity
import com.moduxi.monexi.domain.model.transaction.TransactionType

/**
 * Dados padrão do app com ids FIXOS.
 *
 * Antes cada instalação gerava ids aleatórios para estas linhas; como elas não vão
 * para o Firestore, um lançamento sincronizado apontava para uma categoria que não
 * existia em outra instalação e sumia. Com ids fixos a referência vale em qualquer aparelho.
 * Nunca altere um id já publicado.
 */
object DefaultData {

    val categories: List<CategoryEntity> = listOf(
        category("default_cat_alimentacao", "Alimentação", TransactionType.EXPENSE, "Restaurant", "#E53935"),
        category("default_cat_transporte", "Transporte", TransactionType.EXPENSE, "DirectionsCar", "#1E88E5"),
        category("default_cat_casa", "Casa", TransactionType.EXPENSE, "Home", "#8E24AA"),
        category("default_cat_saude", "Saúde", TransactionType.EXPENSE, "MedicalServices", "#00ACC1"),
        category("default_cat_fatura_cartao", "Fatura de Cartão", TransactionType.EXPENSE, "AttachMoney", "#FB8C00"),
        category("default_cat_salario", "Salário", TransactionType.INCOME, "Work", "#43A047"),
        category("default_cat_freelance", "Freelance", TransactionType.INCOME, "AttachMoney", "#FB8C00"),
        category("default_cat_investimentos", "Investimentos", TransactionType.INCOME, "AttachMoney", "#3949AB")
    )

    val benefitsVoucher = PaymentMethodEntity(
        id = "default_benefits_voucher",
        name = "Vale Refeição / Alimentação",
        isDefault = true
    )

    val paymentMethods: List<PaymentMethodEntity> = listOf(
        PaymentMethodEntity(id = "default_pm_pix", name = "Pix", isDefault = true),
        PaymentMethodEntity(id = "default_pm_dinheiro", name = "Dinheiro", isDefault = true),
        PaymentMethodEntity(id = "default_pm_transferencia", name = "Transferência Bancária", isDefault = true),
        PaymentMethodEntity(id = "default_pm_debito", name = "Cartão de Débito", isDefault = true),
        PaymentMethodEntity(id = "default_pm_credito", name = "Cartão de Crédito", isDefault = true),
        benefitsVoucher
    )

    /** Id da conta "Carteira" padrão: fixo por usuário, para ser a mesma em qualquer aparelho. */
    fun walletAccountId(userId: String): String = "default_wallet_$userId"

    private fun category(id: String, name: String, type: TransactionType, iconName: String, colorHex: String) =
        CategoryEntity(
            id = id,
            name = name,
            type = type.name,
            isDefault = true,
            iconName = iconName,
            colorHex = colorHex
        )
}
