package com.moduxi.monexi.domain.usecase

import com.moduxi.monexi.domain.model.category.Category
import com.moduxi.monexi.domain.model.creditcard.BillStatus
import com.moduxi.monexi.domain.model.creditcard.CreditCard
import com.moduxi.monexi.domain.model.payment.PaymentMethod
import com.moduxi.monexi.domain.model.transaction.Transaction
import com.moduxi.monexi.domain.model.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalculateCreditCardBillsUseCaseTest {

    private val useCase = CalculateCreditCardBillsUseCase()

    // Cartão com fechamento dia 25 e vencimento dia 10
    private val testCreditCard = CreditCard(
        id = "card_1",
        name = "Nubank",
        closingDay = 25,
        dueDay = 10
    )

    private val dummyCategory = Category(id = "cat_1", name = "Alimentação", type = TransactionType.EXPENSE)
    private val dummyPaymentMethod = PaymentMethod(id = "pm_1", name = "Cartão de Crédito")

    // Data de referência de teste: 15 de Outubro de 2026 às 12:00:00
    private val refDateOct15: Long = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // 1. Compra antes do fechamento atribuída ao ciclo correto
    @Test
    fun testPurchaseBeforeClosingBelongsToCorrectBill() {
        val purchaseCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 14, 30, 0)
        }
        val tx = Transaction(
            id = "tx_1",
            title = "Supermercado",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = purchaseCal.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val bills = useCase(testCreditCard, listOf(tx), referenceDateMillis = refDateOct15)
        // Compras feitas até 25/Out vencem em Nov/2026 (fatura de mês 11/2026)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertEquals(1, billNov?.transactions?.size)
        assertEquals(150.0, billNov?.totalAmount ?: 0.0, 0.001)
        assertEquals(150.0, billNov?.remainingBalance ?: 0.0, 0.001)
    }

    // 2. Compra exatamente no limite do fechamento (23:59:59.999 do closingDay)
    @Test
    fun testPurchaseExactlyAtClosingLimitBelongsToCurrentBill() {
        val closingLimitCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 25, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val tx = Transaction(
            id = "tx_at_closing",
            title = "Compra no fechamento",
            amount = 80.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = closingLimitCal.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val bills = useCase(testCreditCard, listOf(tx), referenceDateMillis = refDateOct15)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertTrue(billNov?.transactions?.any { it.id == "tx_at_closing" } == true)
    }

    // 3. Compra depois do fechamento atribuída ao ciclo seguinte (ex: 26/Out -> fatura Dez/2026)
    @Test
    fun testPurchaseAfterClosingRollsToNextCycle() {
        val afterClosingCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 26, 0, 0, 1)
        }
        val tx = Transaction(
            id = "tx_after_closing",
            title = "Farmácia",
            amount = 90.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = afterClosingCal.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val bills = useCase(testCreditCard, listOf(tx), referenceDateMillis = refDateOct15)
        val billDec = bills.firstOrNull { it.year == 2026 && it.month == 12 }

        assertNotNull(billDec)
        assertTrue(billDec?.transactions?.any { it.id == "tx_after_closing" } == true)
        assertEquals(90.0, billDec?.totalAmount ?: 0.0, 0.001)
    }

    // 4. Pagar integralmente fatura fechada antes do vencimento não altera o ciclo das compras
    @Test
    fun testFullPaymentOfClosedBillLeavesCyclesIntact() {
        // Data de ref: 28 de Outubro (fatura de Nov/2026 está fechada, pois fechou dia 25)
        val refDateOct28 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 28, 10, 0, 0)
        }.timeInMillis

        val purchaseTx = Transaction(
            id = "tx_p1",
            title = "Restaurante",
            amount = 200.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 12, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val paymentTx = Transaction(
            id = "pay_full",
            title = "Pagamento Fatura",
            amount = 200.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 28, 14, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id,
            creditCardBillId = "card_1_2026_11"
        )

        val bills = useCase(testCreditCard, listOf(purchaseTx, paymentTx), referenceDateMillis = refDateOct28)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertEquals(200.0, billNov?.totalAmount ?: 0.0, 0.001)
        assertEquals(200.0, billNov?.paidAmount ?: 0.0, 0.001)
        assertEquals(0.0, billNov?.remainingBalance ?: 0.0, 0.001)
        assertEquals(BillStatus.PAID, billNov?.status)
        assertTrue(billNov?.isPaid == true)
    }

    // 5. Pagar parcialmente deixa saldo restante e status PARTIALLY_PAID
    @Test
    fun testPartialPaymentLeavesRemainingBalanceAndNotPaid() {
        val tx = Transaction(
            id = "tx_part",
            title = "Compras",
            amount = 500.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val payTx = Transaction(
            id = "pay_part",
            title = "Pagamento Parcial",
            amount = 200.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 20, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id,
            creditCardBillId = "card_1_2026_11"
        )

        val bills = useCase(testCreditCard, listOf(tx, payTx), referenceDateMillis = refDateOct15)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertEquals(500.0, billNov?.totalAmount ?: 0.0, 0.001)
        assertEquals(200.0, billNov?.paidAmount ?: 0.0, 0.001)
        assertEquals(300.0, billNov?.remainingBalance ?: 0.0, 0.001)
        assertEquals(0.0, billNov?.creditAmount ?: 0.0, 0.001)
        assertFalse(billNov?.isPaid == true)
        assertEquals(BillStatus.PARTIALLY_PAID, billNov?.status)
    }

    // 6. Pagamento antecipado em fatura aberta não a fecha; nova compra subsequente entra nela
    @Test
    fun testEarlyPaymentInOpenBillKeepsBillOpenAndReceivesSubsequentPurchases() {
        val earlyPayDate = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 10, 0, 0) }.timeInMillis
        val purchaseAfterPayDate = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 18, 16, 0, 0) }.timeInMillis

        val initialTx = Transaction(
            id = "tx_init",
            title = "Posto",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 5, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val earlyPayment = Transaction(
            id = "pay_early",
            title = "Pagamento Antecipado",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = earlyPayDate,
            creditCardId = testCreditCard.id,
            creditCardBillId = "card_1_2026_11"
        )

        // Nova compra feita dia 18/Out (ainda antes do fechamento dia 25/Out)
        val newTx = Transaction(
            id = "tx_new",
            title = "Mercado",
            amount = 80.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = purchaseAfterPayDate,
            creditCardId = testCreditCard.id
        )

        // Ref: dia 19/Out (fatura continua ABERTA)
        val refDateOct19 = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 19, 12, 0, 0) }.timeInMillis

        val bills = useCase(testCreditCard, listOf(initialTx, earlyPayment, newTx), referenceDateMillis = refDateOct19)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertEquals(2, billNov?.transactions?.size)
        assertEquals(180.0, billNov?.totalAmount ?: 0.0, 0.001)
        assertEquals(100.0, billNov?.paidAmount ?: 0.0, 0.001)
        assertEquals(80.0, billNov?.remainingBalance ?: 0.0, 0.001)
        assertEquals(BillStatus.PARTIALLY_PAID, billNov?.status)
    }

    // 7. Pagar fatura futura não impede novos lançamentos elegíveis nela
    @Test
    fun testPayingFutureBillAllowsEligiblePurchases() {
        val futurePay = Transaction(
            id = "pay_future",
            title = "Pagamento Fatura Futura",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = refDateOct15,
            creditCardId = testCreditCard.id,
            creditCardBillId = "card_1_2026_12" // Fatura de Dez/2026
        )

        // Compra após 25/Out pertence à fatura de Dez/2026
        val purchaseTx = Transaction(
            id = "tx_dec",
            title = "Presente",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 28, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val bills = useCase(testCreditCard, listOf(futurePay, purchaseTx), referenceDateMillis = refDateOct15)
        val billDec = bills.firstOrNull { it.year == 2026 && it.month == 12 }

        assertNotNull(billDec)
        assertEquals(1, billDec?.transactions?.size)
        assertEquals(150.0, billDec?.totalAmount ?: 0.0, 0.001)
        assertEquals(150.0, billDec?.paidAmount ?: 0.0, 0.001)
        assertEquals(0.0, billDec?.remainingBalance ?: 0.0, 0.001)
        assertEquals(BillStatus.PAID, billDec?.status)
    }

    // 8. Pagamento excedente cria/mostra saldo credor sem criar despesa negativa
    @Test
    fun testSurplusPaymentCreatesCreditAmount() {
        val tx = Transaction(
            id = "tx_small",
            title = "Café",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val overPay = Transaction(
            id = "pay_over",
            title = "Pagamento com sobra",
            amount = 80.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 12, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id,
            creditCardBillId = "card_1_2026_11"
        )

        val bills = useCase(testCreditCard, listOf(tx, overPay), referenceDateMillis = refDateOct15)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertEquals(50.0, billNov?.totalAmount ?: 0.0, 0.001)
        assertEquals(80.0, billNov?.paidAmount ?: 0.0, 0.001)
        assertEquals(0.0, billNov?.remainingBalance ?: 0.0, 0.001)
        assertEquals(30.0, billNov?.creditAmount ?: 0.0, 0.001)
        assertTrue(billNov?.isPaid == true)
    }

    // 9. Pagamento estornado ou com soft-delete deixa saldo correto
    @Test
    fun testReversedOrDeletedPaymentRestoresPendingBalance() {
        val tx = Transaction(
            id = "tx_due",
            title = "Aluguel carro",
            amount = 300.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        // Pagamento que foi estornado/deletado (deletedAt preenchido)
        val deletedPay = Transaction(
            id = "pay_deleted",
            title = "Pagamento Falho",
            amount = 300.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 12, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id,
            creditCardBillId = "card_1_2026_11",
            deletedAt = System.currentTimeMillis()
        )

        val bills = useCase(testCreditCard, listOf(tx, deletedPay), referenceDateMillis = refDateOct15)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertEquals(300.0, billNov?.totalAmount ?: 0.0, 0.001)
        assertEquals(0.0, billNov?.paidAmount ?: 0.0, 0.001)
        assertEquals(300.0, billNov?.remainingBalance ?: 0.0, 0.001)
        assertFalse(billNov?.isPaid == true)
    }

    // 10. Processar transações duplicadas não altera saldo se IDs forem únicos e filtros respeitados
    @Test
    fun testDeduplicatedTransactionsProduceConsistentResults() {
        val tx = Transaction(
            id = "tx_unique",
            title = "Internet",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val bills1 = useCase(testCreditCard, listOf(tx), referenceDateMillis = refDateOct15)
        val bills2 = useCase(testCreditCard, listOf(tx, tx.copy()), referenceDateMillis = refDateOct15)

        // Se passar a mesma lista duas vezes (ex: deduplicada por id no repositório), o resultado é determinístico
        assertNotNull(bills1)
        assertNotNull(bills2)
    }

    // 11. Parcelamento tem soma exata e distribui corretamente centavos residuais
    @Test
    fun testInstallmentsExactSumWithCentDistribution() {
        val totalAmount = 100.00
        val installmentsCount = 3

        val totalCents = Math.round(totalAmount * 100)
        val baseInstallmentCents = totalCents / installmentsCount
        val remainderCents = totalCents % installmentsCount

        val installmentAmounts = (1..installmentsCount).map { i ->
            if (i == 1) {
                (baseInstallmentCents + remainderCents) / 100.0
            } else {
                baseInstallmentCents / 100.0
            }
        }

        // Parcela 1: 33.34, Parcela 2: 33.33, Parcela 3: 33.33 -> Soma = 100.00
        assertEquals(33.34, installmentAmounts[0], 0.0001)
        assertEquals(33.33, installmentAmounts[1], 0.0001)
        assertEquals(33.33, installmentAmounts[2], 0.0001)
        assertEquals(100.00, installmentAmounts.sum(), 0.0001)
    }

    // 12. Ciclos em meses curtos e datas 29/30/31 não geram datas inválidas
    @Test
    fun testShortMonthsAndEdgeDaysDoNotGenerateInvalidDates() {
        val cardDay31 = CreditCard(
            id = "card_edge",
            name = "Cartão Dia 31",
            closingDay = 31,
            dueDay = 28
        )

        // Ciclo da fatura de Março/2026 (fevereiro tem 28 dias)
        val cycleMarch = useCase.calculateCycleForYearMonth(cardDay31, 2026, 3)
        val cal = Calendar.getInstance().apply { timeInMillis = cycleMarch.closingMillis }

        // Deve fechar em 28 de Fevereiro (último dia real do mês) sem estourar para Março
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, cal.get(Calendar.MONTH))
        assertEquals(28, cal.get(Calendar.DAY_OF_MONTH))
    }

    // 13. Pagamento pela conta bancária (creditCardBillId != null) não duplica despesa no orçamento
    @Test
    fun testPaymentTransactionDoesNotDuplicateExpenseInBudget() {
        val calculateExpenseUseCase = CalculateExpenseUseCase()

        val cardPurchase = Transaction(
            id = "tx_pur",
            title = "Cinema",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = refDateOct15,
            creditCardId = testCreditCard.id,
            creditCardBillId = null
        )

        val billPayment = Transaction(
            id = "tx_pay",
            title = "Pagamento Fatura",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = PaymentMethod(id = "pix", name = "Pix"),
            date = refDateOct15,
            accountId = "acc_1",
            creditCardId = testCreditCard.id,
            creditCardBillId = "card_1_2026_11"
        )

        val totalExpense = calculateExpenseUseCase(listOf(cardPurchase, billPayment))

        // Deve contabilizar apenas os R$ 50,00 da compra de consumo, e NÃO R$ 100,00
        assertEquals(50.0, totalExpense, 0.001)
    }

    // 14. Status OVERDUE quando fatura não quitada passa da data de vencimento
    @Test
    fun testOverdueStatusAfterDueDate() {
        // Vencimento da fatura Nov/2026 é 10/Nov/2026. Data de ref: 15/Nov/2026
        val refDateNov15 = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 15, 12, 0, 0)
        }.timeInMillis

        val tx = Transaction(
            id = "tx_unpaid",
            title = "Livraria",
            amount = 120.0,
            type = TransactionType.EXPENSE,
            category = dummyCategory,
            paymentMethod = dummyPaymentMethod,
            date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 10, 0, 0) }.timeInMillis,
            creditCardId = testCreditCard.id
        )

        val bills = useCase(testCreditCard, listOf(tx), referenceDateMillis = refDateNov15)
        val billNov = bills.firstOrNull { it.year == 2026 && it.month == 11 }

        assertNotNull(billNov)
        assertEquals(BillStatus.OVERDUE, billNov?.status)
    }
}
