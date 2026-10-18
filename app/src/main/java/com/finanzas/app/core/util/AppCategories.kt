package com.finanzas.app.core.util

/**
 * Identificadores de categorias reservadas por el sistema.
 *
 * Vive en core/util (no en data/) para que la capa de dominio pueda usarla sin
 * depender de la capa de datos.
 */
object AppCategories {

    /**
     * Categoria de los egresos que genera la app al amortizar una deuda.
     * Amortizar NO es un gasto de consumo: baja la caja y baja el pasivo en la
     * misma cuantia. Los agregados la excluyen para no duplicarla con la compra
     * original a credito, pero el movimiento sigue visible en la linea de tiempo.
     */
    const val DEBT_PAYMENT: String = "cat_exp_debt_payment"
}
