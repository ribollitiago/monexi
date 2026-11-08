package com.moduxi.monexi.ui.theme

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }

        if (digits.isBlank()) {
            return TransformedText(
                text = AnnotatedString(""),
                offsetMapping = OffsetMapping.Identity
            )
        }

        val cents = digits.toLongOrNull() ?: 0L
        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        val formatted = formatter.format(cents / 100.0)

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return formatted.length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return digits.length
                }
            }
        )
    }
}
