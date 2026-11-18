package com.moduxi.monexi.presentation.settings.report

/**
 * Tipo do report. [apiValue] é o valor enviado no campo `type` da API
 * (a API mapeia para as labels do GitHub).
 */
enum class ReportType(val label: String, val apiValue: String) {
    BUG("Bug", "bug"),
    IDEA("Ideia / Sugestão", "idea"),
    FIX("Correção", "fix"),
    QUESTION("Dúvida", "question"),
    OTHER("Outro", "other")
}
