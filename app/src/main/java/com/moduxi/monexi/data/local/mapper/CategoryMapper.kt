package com.moduxi.monexi.data.local.mapper

import com.moduxi.monexi.data.local.entity.CategoryEntity
import com.moduxi.monexi.domain.model.Category

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        isDefault = isDefault
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        isDefault = isDefault
    )
}