package com.moduxi.monexi.domain.model.account

enum class AccountType(val label: String) {
    CHECKING("Conta Corrente"),
    SAVINGS("Conta Poupança"),
    BENEFITS("Vale Alimentação / Refeição"),
    INVESTIMENT("Investimentos"),
    CASH("Dinheiro / Carteira"),
    DIGITAL_WALLET("Carteira Digital")
}