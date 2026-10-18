package com.finanzas.app.domain.usecase

import com.finanzas.app.domain.model.Transaction
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.TransactionRepository

/**
 * Registra de forma atomica cualquier movimiento financiero.
 *
 * REGLA DE MONEDA (INV-3 en el punto de entrada):
 *   La moneda de un movimiento es la de su cuenta. Una cuenta = una moneda.
 *   Un movimiento entre cuentas de monedas distintas SOLO existe como giro
 *   (REMITTANCE), porque es el unico tipo que lleva tasa de cambio y monto destino.
 *   Una transferencia entre monedas distintas sumaria dolares con pesos.
 */
class RegisterMovementUseCase(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        if (transaction.amount <= 0.0) {
            return Result.failure(IllegalArgumentException("El monto debe ser mayor a 0.00"))
        }
        if (transaction.feeAmount < 0.0) {
            return Result.failure(IllegalArgumentException("La comisión no puede ser negativa"))
        }

        when (transaction.type) {
            TransactionType.EXPENSE,
            TransactionType.CREDIT_PURCHASE -> {
                if (transaction.sourceAccountId.isNullOrBlank()) {
                    return Result.failure(IllegalArgumentException("Debes seleccionar una cuenta origen"))
                }
            }

            TransactionType.INCOME -> {
                if (transaction.targetAccountId.isNullOrBlank() && transaction.sourceAccountId.isNullOrBlank()) {
                    return Result.failure(IllegalArgumentException("Debes seleccionar una cuenta de destino"))
                }
            }

            TransactionType.TRANSFER -> {
                val sourceId = transaction.sourceAccountId
                val targetId = transaction.targetAccountId
                if (sourceId.isNullOrBlank()) {
                    return Result.failure(IllegalArgumentException("Debes seleccionar una cuenta de origen"))
                }
                if (targetId.isNullOrBlank()) {
                    return Result.failure(IllegalArgumentException("Debes seleccionar una cuenta de destino"))
                }
                if (sourceId == targetId) {
                    return Result.failure(IllegalArgumentException("Las cuentas origen y destino deben ser distintas"))
                }

                val source = accountRepository.getAccountById(sourceId)
                val target = accountRepository.getAccountById(targetId)
                if (source != null && target != null && source.currency != target.currency) {
                    return Result.failure(
                        IllegalArgumentException(
                            "No se puede transferir de ${source.currency} a ${target.currency}. " +
                                    "Para mover dinero entre monedas registra un Giro."
                        )
                    )
                }
            }

            TransactionType.REMITTANCE -> {
                val sourceId = transaction.sourceAccountId
                if (sourceId.isNullOrBlank()) {
                    return Result.failure(IllegalArgumentException("Debes seleccionar una cuenta origen"))
                }
                if (transaction.amount <= transaction.feeAmount - 0.005) {
                    return Result.failure(IllegalArgumentException("El monto debe superar la comisión"))
                }

                val target = transaction.targetAccountId?.let { accountRepository.getAccountById(it) }
                if (target != null) {
                    val destino = transaction.targetCurrency ?: target.currency
                    if (destino != target.currency) {
                        return Result.failure(
                            IllegalArgumentException(
                                "La cuenta destino está en ${target.currency} y el giro llega en $destino"
                            )
                        )
                    }
                    if (transaction.targetAmount == null || transaction.targetAmount <= 0.0) {
                        return Result.failure(
                            IllegalArgumentException("Falta el monto que llega en ${target.currency}")
                        )
                    }
                }
            }
        }

        return transactionRepository.registerMovement(transaction)
    }
}
