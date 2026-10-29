package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.AccountEntity
import com.moduxi.monexi.domain.model.account.Account

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        userId = userId,
        name = name,
        initialBalance = initialBalance,
        colorHex = colorHex,
        iconName = iconName,
        isArchived = isArchived,
        type = type,
        excludeFromTotal = excludeFromTotal
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        userId = userId,
        name = name,
        initialBalance = initialBalance,
        colorHex = colorHex,
        iconName = iconName,
        isArchived = isArchived,
        type = type,
        excludeFromTotal = excludeFromTotal
    )
}
