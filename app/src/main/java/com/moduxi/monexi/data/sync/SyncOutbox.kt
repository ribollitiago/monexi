package com.moduxi.monexi.data.sync

import android.content.Context
import org.json.JSONObject

/** Uma gravação que ainda não foi confirmada pelo Firestore. */
data class PendingOp(
    val collection: String,
    val id: String,
    val type: String, // TYPE_SET ou TYPE_DELETE
    val data: Map<String, Any?> = emptyMap()
) {
    companion object {
        const val TYPE_SET = "set"
        const val TYPE_DELETE = "delete"
    }
}

/**
 * Fila persistente de envios pendentes (uma entrada por documento; a mais recente vence).
 * Fica em SharedPreferences para sobreviver ao fechamento do app e não exigir mudança
 * no schema do Room.
 */
class SyncOutbox(context: Context) {

    private val prefs = context.getSharedPreferences("monexi_sync_outbox", Context.MODE_PRIVATE)
    private val flags = context.getSharedPreferences("monexi_sync_flags", Context.MODE_PRIVATE)
    private val legacyIds = context.getSharedPreferences("monexi_legacy_ids", Context.MODE_PRIVATE)

    private fun key(userId: String, collection: String, id: String) = "$userId|$collection|$id"

    /** Guarda a operação e devolve o token (conteúdo serializado) usado para confirmar o envio. */
    fun put(userId: String, op: PendingOp): String {
        val payload = JSONObject()
        payload.put("t", op.type)
        val data = JSONObject()
        op.data.forEach { (field, value) -> data.put(field, value ?: JSONObject.NULL) }
        payload.put("d", data)

        val token = payload.toString()
        prefs.edit().putString(key(userId, op.collection, op.id), token).apply()
        return token
    }

    /** Remove a entrada, mas só se ela ainda for a mesma que foi enviada. */
    fun remove(userId: String, collection: String, id: String, token: String) {
        val entryKey = key(userId, collection, id)
        if (prefs.getString(entryKey, null) == token) {
            prefs.edit().remove(entryKey).apply()
        }
    }

    fun isPending(userId: String, collection: String, id: String): Boolean =
        prefs.contains(key(userId, collection, id))

    /** Operações pendentes do usuário, com o token de cada uma. */
    fun pending(userId: String): List<Pair<PendingOp, String>> {
        val prefix = "$userId|"
        return prefs.all.mapNotNull { (entryKey, value) ->
            if (!entryKey.startsWith(prefix) || value !is String) return@mapNotNull null
            val parts = entryKey.split("|", limit = 3)
            if (parts.size != 3) return@mapNotNull null

            try {
                val payload = JSONObject(value)
                val dataJson = payload.optJSONObject("d") ?: JSONObject()
                val data = HashMap<String, Any?>()
                dataJson.keys().forEach { field ->
                    val fieldValue = dataJson.get(field)
                    data[field] = if (fieldValue == JSONObject.NULL) null else fieldValue
                }
                PendingOp(parts[1], parts[2], payload.optString("t", PendingOp.TYPE_SET), data) to value
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Guarda "id antigo -> id fixo" das categorias/formas padrão desta instalação. Os documentos
     * que ainda estão na nuvem com o id antigo são convertidos ao serem baixados.
     */
    fun putLegacyId(oldId: String, newId: String) {
        legacyIds.edit().putString(oldId, newId).apply()
    }

    fun resolveLegacyId(id: String): String = legacyIds.getString(id, null) ?: id

    /** Envio único de todos os dados locais já foi feito para este usuário nesta instalação? */
    fun isFullPushDone(userId: String): Boolean = flags.getBoolean("full_push_done_$userId", false)

    fun markFullPushDone(userId: String) {
        flags.edit().putBoolean("full_push_done_$userId", true).apply()
    }
}
