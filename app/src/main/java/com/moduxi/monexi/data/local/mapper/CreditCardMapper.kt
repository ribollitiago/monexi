package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.CreditCardEntity
import com.moduxi.monexi.domain.model.CreditCard

fun CreditCardEntity.toDomain(): CreditCard {
    return CreditCard(
        id = id,
        userId = userId,
        name = name,
        limitAmount = limitAmount,
        closingDay = closingDay,
        dueDay = dueDay,
        colorHex = colorHex,
        iconName = iconName,
        isArchived = isArchived
    )
}

fun CreditCard.toEntity(): CreditCardEntity {
    return CreditCardEntity(
        id = id,
        userId = userId,
        name = name,
        limitAmount = limitAmount,
        closingDay = closingDay,
        dueDay = dueDay,
        colorHex = colorHex,
        iconName = iconName,
        isArchived = isArchived
    )
}
