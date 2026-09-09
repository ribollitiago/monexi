package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.PaymentMethodEntity
import com.moduxi.monexi.domain.model.PaymentMethod

fun PaymentMethodEntity.toDomain(): PaymentMethod {
    return PaymentMethod(
        id = id,
        name = name,
        isDefault = isDefault
    )
}

fun PaymentMethod.toEntity(): PaymentMethodEntity{
    return PaymentMethodEntity(
        id = id,
        name = name,
        isDefault = isDefault
    )
}