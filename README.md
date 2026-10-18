# Finanzas Express - Botón Flotante Overlay & Multicuenta (Android Nativo)

Aplicación Android nativa desarrollada en **Kotlin** con arquitectura **MVVM + Clean Architecture** y **Jetpack Compose / Material 3**, diseñada para la captura ultra rápida de movimientos financieros mediante un **Botón Flotante tipo AssistiveTouch (Overlay)** con acople magnético a los bordes (`snap to edge`).

---

## 🚀 Características Principales

1. **Botón Flotante AssistiveTouch (SYSTEM_ALERT_WINDOW):**
   - Servicio en primer plano (`Foreground Service`) persistente y de bajo consumo.
   - Arrastre fluido con detección de toques vs arrastre y acople automático a los bordes (`snap to edge`).
   - Al tocar el botón, despliega un modal flotante modal en Jetpack Compose sin abrir la aplicación principal.

2. **Lógica Financiera Multicuenta:**
   - **Tipos de Cuentas (`AccountType`):** Efectivo, Cuentas Corrientes, Cuentas de Ahorros, Tarjetas de Crédito y Billeteras Digitales.
   - **Movimientos Soportados (`TransactionType`):**
     - `EXPENSE`: Gasto/Compra (debitado de cuenta origen).
     - `CREDIT_PURCHASE`: Consumo con Tarjeta de Crédito (aumenta saldo utilizado/deuda con selección de cuotas diferidas).
     - `TRANSFER`: Transferencia atómica entre cuentas propias.
     - `REMITTANCE`: Giros al exterior (cálculo en vivo de monto a recibir en moneda destino ej. COP, tasa de cambio y fee/comisión).
     - `INCOME`: Ingreso/Salario/Nómina (sumado a cuenta destino).

3. **Room Database con Transacciones Atómicas (`@Transaction`):**
   - Modificación coordinada de la tabla `transactions` y actualización simultánea del `currentBalance` en `accounts`.
   - Soporte de reversión atómica al eliminar un movimiento.
   - Precarga inicial (`RoomDatabase.Callback`) con cuentas y categorías estándar listas para usar.

4. **Offline-First & Preparación para Backend:**
   - Estructura `RemoteSyncDataSource` con implementación `FakeRemoteSyncDataSource` que simula la sincronización en la nube y marcas `isSynced` / `syncTimestamp`.

5. **Dashboard Jetpack Compose:**
   - Tarjetas de Patrimonio Neto (Activos Líquidos vs Deuda en Tarjetas).
   - Switch de activación/desactivación del Overlay con validación de permisos.
   - Carrusel de Cuentas con saldos en tiempo real.
   - Timeline de Movimientos agrupados por fecha (Hoy, Ayer, Fecha).
   - Exportación de respaldos completos en formatos **JSON** y **CSV** con `FileProvider`.

---

## 🏛 Estructura del Proyecto (Clean Architecture)

```
com.finanzas.app
├── FinanzasApp.kt                     # Application Class y Service Locator / DI
├── core
│   ├── overlay
│   │   └── OverlayLifecycleOwner.kt   # Contexto Lifecycle/SavedState para Compose en WindowManager
│   └── util
│       ├── BackupExporter.kt          # Exportador JSON y CSV + FileProvider share
│       ├── CurrencyFormatter.kt       # Formateador de divisas (USD, COP, etc.)
│       ├── DateFormatter.kt           # Formateador de fechas para headers y timeline
│       └── HapticHelper.kt            # Feedback háptico vibratorio
├── domain
│   ├── model
│   │   ├── Account.kt & AccountType.kt
│   │   ├── Transaction.kt & TransactionType.kt
│   │   ├── Category.kt
│   │   ├── TransactionWithDetails.kt
│   │   └── FinancialSummary.kt
│   ├── repository
│   │   ├── AccountRepository.kt
│   │   ├── CategoryRepository.kt
│   │   └── TransactionRepository.kt
│   └── usecase
│       ├── RegisterMovementUseCase.kt # Lógica atómica de negocio
│       ├── GetFinancialSummaryUseCase.kt
│       ├── ExportDataUseCase.kt
│       └── SyncDataUseCase.kt
├── data
│   ├── local
│   │   ├── converters/Converters.kt
│   │   ├── dao/AccountDao.kt, CategoryDao.kt, TransactionDao.kt
│   │   ├── database/AppDatabase.kt & InitialDataCallback.kt
│   │   └── entity/AccountEntity.kt, CategoryEntity.kt, TransactionEntity.kt
│   ├── mapper/EntityMappers.kt
│   ├── remote/RemoteSyncDataSource.kt & FakeRemoteSyncDataSource.kt
│   └── repository/AccountRepositoryImpl.kt, CategoryRepositoryImpl.kt, TransactionRepositoryImpl.kt
├── service
│   └── FloatingMovementService.kt     # Foreground Service + WindowManager + Gestos táctiles
└── ui
    ├── theme/ (Color.kt, Theme.kt, Type.kt)
    ├── overlay/
    │   ├── QuickMovementOverlayScreen.kt
    │   ├── QuickMovementViewModel.kt
    │   └── components/ (NumericKeypad.kt, MovementTypeChips.kt, AccountPicker.kt, CategoryPicker.kt, RemittanceForm.kt, InstallmentsPicker.kt)
    └── dashboard/
        ├── MainActivity.kt
        ├── MainViewModel.kt
        └── components/ (BalanceSummaryCard.kt, AccountsSection.kt, TransactionTimelineSection.kt, OverlayPermissionBanner.kt, ExportBackupDialog.kt)
```

---

## 🛠 Requisitos y Configuración

- **Android Studio:** Ladybug / Iguana / Koala o superior
- **JDK:** 17
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 35 (Android 15)
- **Kotlin:** 2.0.20+ con Compose Compiler integrado

### Permiso de Superposición en Android:
Al iniciar la aplicación por primera vez, se mostrará un banner solicitando conceder el permiso `Permitir mostrar sobre otras aplicaciones` (`SYSTEM_ALERT_WINDOW`) necesario para que el botón AssistiveTouch pueda funcionar mientras utilizas WhatsApp, el navegador o cualquier otra app.
