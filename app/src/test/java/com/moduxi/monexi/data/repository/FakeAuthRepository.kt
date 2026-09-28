package com.moduxi.monexi.data.repository

import com.google.firebase.auth.FirebaseUser
import com.moduxi.monexi.domain.repository.AuthRepository

class FakeAuthRepository(
    override val currentUser: FirebaseUser? = null
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUp(email: String, password: String): Result<Unit> = Result.success(Unit)
    override fun signOut() {}
}
