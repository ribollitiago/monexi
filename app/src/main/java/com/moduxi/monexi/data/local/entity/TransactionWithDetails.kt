package com.moduxi.monexi.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.moduxi.monexi.domain.model.Category

data class TransactionWithDetails (
    @Embedded val transaction: TransactionEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,

    @Relation(
        parentColumn = "paymentMethodId",
        entityColumn = "id"
    )
    val paymentMethod: PaymentMethodEntity
)