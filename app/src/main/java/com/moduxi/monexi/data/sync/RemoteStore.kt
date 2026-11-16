package com.moduxi.monexi.data.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.moduxi.monexi.domain.repository.AuthRepository
import java.util.concurrent.ConcurrentHashMap

/**
 * Escrita no Firestore sem bloquear quem chamou (offline-first).
 *
 * O dado já está salvo no Room; aqui a gravação entra na fila ([SyncOutbox]) e é
 * disparada sem esperar o servidor. Quando o servidor confirma, sai da fila. O que
 * falhar continua na fila e é reenviado em [pushPending].
 */
class RemoteStore(
    private val authRepository: AuthRepository,
    private val outbox: SyncOutbox
) {
    // Envios já entregues ao SDK neste processo (o SDK mantém a própria fila offline),
    // para não empilhar a mesma gravação várias vezes enquanto não há internet.
    private val inFlight: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private val currentUserId: String?
        get() = authRepository.currentUser?.uid?.takeIf { it.isNotEmpty() }

    fun set(collection: String, id: String, data: Map<String, Any?>) {
        enqueue(PendingOp(collection, id, PendingOp.TYPE_SET, data))
    }

    fun delete(collection: String, id: String) {
        enqueue(PendingOp(collection, id, PendingOp.TYPE_DELETE))
    }

    /** Há uma alteração local deste documento ainda não confirmada? (no download, o local vence) */
    fun isPending(collection: String, id: String): Boolean {
        val userId = currentUserId ?: return false
        return outbox.isPending(userId, collection, id)
    }

    /** Converte o id antigo (aleatório) de uma categoria/forma padrão para o id fixo, se houver. */
    fun resolveLegacyId(id: String): String = outbox.resolveLegacyId(id)

    fun isFullPushDone(): Boolean {
        val userId = currentUserId ?: return false
        return outbox.isFullPushDone(userId)
    }

    /** Reenvia tudo que ainda não foi confirmado. */
    fun pushPending() {
        val userId = currentUserId ?: return
        outbox.pending(userId).forEach { (op, token) -> send(userId, op, token) }
    }

    private fun enqueue(op: PendingOp) {
        val userId = currentUserId
        if (userId == null) {
            Log.e(TAG, "Sem usuário autenticado: ${op.collection}/${op.id} não foi enfileirado")
            return
        }
        val token = outbox.put(userId, op)
        send(userId, op, token)
    }

    private fun send(userId: String, op: PendingOp, token: String) {
        val flightKey = "$userId|${op.collection}|${op.id}|$token"
        if (!inFlight.add(flightKey)) return

        try {
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection(op.collection)
                .document(op.id)

            val task = if (op.type == PendingOp.TYPE_DELETE) document.delete() else document.set(op.data)

            task
                .addOnSuccessListener {
                    outbox.remove(userId, op.collection, op.id, token)
                    inFlight.remove(flightKey)
                }
                .addOnFailureListener { error ->
                    // Continua na fila; será reenviado na próxima sincronização
                    inFlight.remove(flightKey)
                    Log.e(TAG, "Falha ao enviar ${op.collection}/${op.id}: ${error.message}", error)
                }
        } catch (e: Exception) {
            inFlight.remove(flightKey)
            Log.e(TAG, "Erro ao enviar ${op.collection}/${op.id}: ${e.message}", e)
        }
    }

    private companion object {
        const val TAG = "FirestoreSync"
    }
}
