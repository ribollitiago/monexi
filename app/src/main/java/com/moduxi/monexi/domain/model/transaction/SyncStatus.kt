package com.moduxi.monexi.domain.model.transaction

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}