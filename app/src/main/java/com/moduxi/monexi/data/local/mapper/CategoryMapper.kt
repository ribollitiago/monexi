package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.domain.model.Category
import com.moduxi.monexi.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        type = TransactionType.valueOf(type),
        isDefault = isDefault
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.name,
        isDefault = isDefault
    )
}