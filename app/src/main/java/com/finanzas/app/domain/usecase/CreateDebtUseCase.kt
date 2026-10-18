package com.finanzas.app.domain.usecase

import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.domain.model.DebtStatus
import com.finanzas.app.domain.repository.DebtRepository
import java.util.Calendar
import java.util.UUID
import kotlin.math.roundToLong

/**
 * Genera la deuda y su calendario de cuotas.
 *
 * MODELO DE INTERES: recargo PLANO sobre el capital
 *     interes_total = capital * (porcentaje / 100)
 * No es una tasa anual ni hay capitalizacion. Un 10% a 3 cuotas equivale a una
 * TEA real cercana al 78%; a 12 cuotas, ~19,5%. Ver RateMath.effectiveAnnualRate,
 * que el dialogo muestra al usuario para que la cifra no induzca a error.
 *
 * M-11 (reparto sin residuo): las cuotas se reparten en CENTAVOS ENTEROS y el
 * residuo se distribuye de a un centavo entre las primeras cuotas, de modo que
 *     suma(cuotas mostradas) == total financiado, exactamente.
 * Antes 1000/3 producia tres cuotas de 333,33 que sumaban 999,99.
 */
class CreateDebtUseCase(
    private val debtRepository: DebtRepository
) {
    suspend operator fun invoke(
        title: String,
        entityName: String,
        originalPrincipal: Double,
        interestRatePercentage: Double,
        totalInstallments: Int,
        customInstallmentAmount: Double? = null,
        dueDayOfMonth: Int = 15,
        startDate: Long = System.currentTimeMillis(),
        currency: String = "USD",
        accountId: String? = null,
        notes: String? = null
    ): Result<Unit> {
        if (originalPrincipal <= 0.0) {
            return Result.failure(IllegalArgumentException("El monto de la deuda debe ser mayor a 0.00"))
        }
        if (interestRatePercentage < 0.0) {
            return Result.failure(IllegalArgumentException("El recargo no puede ser negativo"))
        }

        val isFlexible = totalInstallments <= 0

        val principalRedondeado = CurrencyFormatter.roundMoney(originalPrincipal)
        val totalCalculado = CurrencyFormatter.roundMoney(
            principalRedondeado * (1.0 + interestRatePercentage / 100.0)
        )

        val totalWithInterest = if (isFlexible) {
            totalCalculado
        } else {
            customInstallmentAmount
                ?.let { CurrencyFormatter.roundMoney(it * totalInstallments) }
                ?: totalCalculado
        }

        val installmentAmount = if (isFlexible) 0.0
        else CurrencyFormatter.roundMoney(totalWithInterest / totalInstallments)

        val debtId = UUID.randomUUID().toString()

        val debt = Debt(
            id = debtId,
            title = title.ifBlank { "Deuda / Crédito" },
            entityName = entityName.ifBlank { "Comercio / Entidad" },
            originalPrincipal = principalRedondeado,
            interestRatePercentage = interestRatePercentage,
            totalInstallments = if (isFlexible) 0 else totalInstallments,
            installmentAmount = installmentAmount,
            totalWithInterest = totalWithInterest,
            startDate = startDate,
            dueDayOfMonth = dueDayOfMonth,
            currency = currency,
            accountId = accountId,
            notes = notes,
            status = DebtStatus.ACTIVE
        )

        val installments = mutableListOf<DebtInstallment>()
        if (!isFlexible) {
            // Todo el reparto se hace en centavos enteros: aritmetica exacta.
            val totalCents = (totalWithInterest * 100).roundToLong()
            var principalCents = (principalRedondeado * 100).roundToLong()
            var interestCents = totalCents - principalCents
            if (interestCents < 0L) {
                // El total pactado es menor al capital (descuento): no hay interes.
                interestCents = 0L
                principalCents = totalCents
            }

            val n = totalInstallments.toLong()
            val basePrincipal = principalCents / n
            val restoPrincipal = principalCents % n
            val baseInterest = interestCents / n
            val restoInterest = interestCents % n

            for (i in 1..totalInstallments) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = startDate
                    add(Calendar.MONTH, i)
                    val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                    set(Calendar.DAY_OF_MONTH, dueDayOfMonth.coerceIn(1, maxDay))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 0)
                }

                // El residuo se reparte de a un centavo entre las primeras cuotas.
                val pCents = basePrincipal + (if (i <= restoPrincipal) 1L else 0L)
                val iCents = baseInterest + (if (i <= restoInterest) 1L else 0L)

                installments.add(
                    DebtInstallment(
                        id = UUID.randomUUID().toString(),
                        debtId = debtId,
                        installmentNumber = i,
                        dueDate = calendar.timeInMillis,
                        principalAmount = pCents / 100.0,
                        interestAmount = iCents / 100.0,
                        lateFeeAmount = 0.0,
                        isPaid = false,
                        paidDate = null,
                        actualPaidAmount = null,
                        paymentAccountId = null,
                        notes = null
                    )
                )
            }
        }

        return debtRepository.createDebtWithInstallments(debt, installments)
    }
}
