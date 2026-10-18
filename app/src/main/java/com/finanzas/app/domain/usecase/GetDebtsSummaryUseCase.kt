package com.finanzas.app.domain.usecase

import com.finanzas.app.core.util.MoneyConverter
import com.finanzas.app.domain.model.DebtAnalyticsSummary
import com.finanzas.app.domain.model.DebtStatus
import com.finanzas.app.domain.repository.CurrencyRepository
import com.finanzas.app.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Resumen analitico de deudas, expresado en USD.
 *
 * A-07: antes este caso de uso calculaba `activeDebts` pero luego iteraba sobre
 * TODAS las deudas (incluidas las ya pagadas) para acumular el saldo pendiente,
 * mientras GetFinancialSummaryUseCase filtraba ACTIVE/OVERDUE. Resultado: la
 * pantalla de Deudas y el Dashboard mostraban dos totales distintos.
 * Ahora ambos usan el mismo conjunto: deudas ACTIVE u OVERDUE.
 *
 * Los historicos (interes pagado, mora pagada, capital amortizado) si se
 * acumulan sobre TODAS las deudas: son hechos pasados que no desaparecen
 * porque la deuda se haya saldado.
 */
class GetDebtsSummaryUseCase(
    private val debtRepository: DebtRepository,
    private val currencyRepository: CurrencyRepository
) {
    operator fun invoke(): Flow<DebtAnalyticsSummary> {
        return combine(
            debtRepository.getAllDebtsWithInstallmentsFlow(),
            currencyRepository.getExchangeRateFlow()
        ) { debtsWithInstallments, rateInfo ->
            val trm = rateInfo.officialTrm

            val activeDebts = debtsWithInstallments.filter {
                it.debt.status == DebtStatus.ACTIVE || it.debt.status == DebtStatus.OVERDUE
            }

            var remainingDebt = 0.0
            var pendingInstallments = 0
            for (item in activeDebts) {
                remainingDebt += MoneyConverter.toBase(item.remainingTotalDebt, item.debt.currency, trm)
                pendingInstallments += item.pendingInstallmentsCount
            }

            var originalPrincipal = 0.0
            var paidPrincipal = 0.0
            var paidInterest = 0.0
            var paidLateFees = 0.0
            var paidOverall = 0.0
            for (item in debtsWithInstallments) {
                val moneda = item.debt.currency
                originalPrincipal += MoneyConverter.toBase(item.debt.originalPrincipal, moneda, trm)
                paidPrincipal += MoneyConverter.toBase(item.totalPaidPrincipal, moneda, trm)
                paidInterest += MoneyConverter.toBase(item.totalPaidInterest, moneda, trm)
                paidLateFees += MoneyConverter.toBase(item.totalPaidLateFees, moneda, trm)
                paidOverall += MoneyConverter.toBase(item.totalPaidOverall, moneda, trm)
            }

            DebtAnalyticsSummary(
                totalActiveDebtsCount = activeDebts.size,
                totalRemainingDebt = remainingDebt,
                totalOriginalPrincipal = originalPrincipal,
                totalPaidPrincipal = paidPrincipal,
                totalPaidInterest = paidInterest,
                totalPaidLateFees = paidLateFees,
                totalPaidOverall = paidOverall,
                pendingInstallmentsCount = pendingInstallments
            )
        }
    }
}
