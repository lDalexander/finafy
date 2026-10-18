package com.finanzas.app.service

import android.animation.ValueAnimator
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.finanzas.app.FinanzasApp
import com.finanzas.app.R
import com.finanzas.app.core.overlay.OverlayLifecycleOwner
import com.finanzas.app.ui.dashboard.MainActivity
import com.finanzas.app.ui.overlay.QuickMovementOverlayScreen
import com.finanzas.app.ui.overlay.QuickMovementViewModel
import kotlin.math.abs

/**
 * Servicio en primer plano (Foreground Service) que gestiona el botón flotante (AssistiveTouch)
 * y el modal ultra rápido de captura de movimientos financieros.
 */
class FloatingMovementService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 9001
        var isRunning = false
            private set
    }

    private lateinit var windowManager: WindowManager
    private var floatingBubbleView: View? = null
    private var bubbleLayoutParams: WindowManager.LayoutParams? = null

    // Modal Compose Overlay
    private var modalComposeView: ComposeView? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    private var isModalShowing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startAsForegroundService()
        createFloatingBubble()
    }

    private fun startAsForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, FinanzasApp.OVERLAY_CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_service_notification_title))
            .setContentText(getString(R.string.overlay_service_notification_text))
            .setSmallIcon(android.R.drawable.ic_input_add)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createFloatingBubble() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val sizePx = (56 * density).toInt()

        bubbleLayoutParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (resources.displayMetrics.widthPixels - sizePx - (16 * density)).toInt()
            y = (resources.displayMetrics.heightPixels * 0.4).toInt()
        }

        // Crear la vista nativa del botón flotante tipo AssistiveTouch
        val frameLayout = FrameLayout(this).apply {
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#10B981")) // Emerald
                setStroke((2 * density).toInt(), Color.parseColor("#34D399"))
            }
            background = backgroundDrawable
            elevation = 12 * density

            val icon = ImageView(context).apply {
                setImageResource(android.R.drawable.ic_input_add)
                setColorFilter(Color.parseColor("#0D1117"))
                val pad = (14 * density).toInt()
                setPadding(pad, pad, pad, pad)
            }
            addView(icon)
        }

        // Listener de gestos táctiles (Drag and Drop + Snap to edge + Click)
        frameLayout.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var startClickTime: Long = 0

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val params = bubbleLayoutParams ?: return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        startClickTime = System.currentTimeMillis()
                        v?.animate()?.scaleX(0.9f)?.scaleY(0.9f)?.setDuration(100)?.start()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(floatingBubbleView, params)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        v?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(150)?.start()
                        val duration = System.currentTimeMillis() - startClickTime
                        val diffX = abs(event.rawX - initialTouchX)
                        val diffY = abs(event.rawY - initialTouchY)

                        if (diffX < 15 && diffY < 15 && duration < 350) {
                            // Click detectado -> Abrir modal rápido
                            showQuickModalOverlay()
                        } else {
                            // Arrastre finalizado -> Snap to edge magnético
                            snapToNearestEdge()
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingBubbleView = frameLayout
        windowManager.addView(floatingBubbleView, bubbleLayoutParams)
    }

    private fun snapToNearestEdge() {
        val params = bubbleLayoutParams ?: return
        val bubble = floatingBubbleView ?: return

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val bubbleWidth = bubble.width.takeIf { it > 0 } ?: (56 * displayMetrics.density).toInt()
        val margin = (8 * displayMetrics.density).toInt()

        val currentX = params.x
        val targetX = if (currentX + bubbleWidth / 2 < screenWidth / 2) {
            margin // Borde izquierdo
        } else {
            screenWidth - bubbleWidth - margin // Borde derecho
        }

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                params.x = animation.animatedValue as Int
                windowManager.updateViewLayout(floatingBubbleView, params)
            }
        }
        animator.start()
    }

    private fun showQuickModalOverlay() {
        if (isModalShowing) return
        isModalShowing = true

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // La ventana ocupa la pantalla completa y es el contenido el que se
        // desplaza dentro. Con WRAP_CONTENT, al elegir "Giro / Remesa" el formulario
        // crecia, la ventana crecia con el y, centrada, se desbordaba por arriba y
        // por abajo hasta taparse con la barra de navegacion del sistema. Ademas el
        // scroll interno nunca se activaba, porque la ventana nunca se quedaba sin
        // espacio. Los margenes seguros los aplica el Compose con safeDrawingPadding.
        val modalParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            dimAmount = 0.65f
            gravity = Gravity.CENTER
            // Con campos de texto (COP de ventanilla, comision, nota) el teclado
            // debe encoger la ventana, no taparlos.
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.onCreate()
        overlayLifecycleOwner = lifecycleOwner

        val app = application as FinanzasApp
        val viewModel = QuickMovementViewModel(
            accountRepository = app.accountRepository,
            categoryRepository = app.categoryRepository,
            currencyRepository = app.currencyRepository,
            registerMovementUseCase = app.registerMovementUseCase
        )

        val composeView = ComposeView(this).apply {
            lifecycleOwner.attachTo(this)
            setContent {
                QuickMovementOverlayScreen(
                    viewModel = viewModel,
                    onDismiss = { dismissQuickModal() }
                )
            }
        }

        lifecycleOwner.onResume()
        modalComposeView = composeView
        windowManager.addView(composeView, modalParams)
    }

    private fun dismissQuickModal() {
        if (!isModalShowing) return
        isModalShowing = false

        modalComposeView?.let { view ->
            overlayLifecycleOwner?.onPause()
            overlayLifecycleOwner?.onDestroy()
            overlayLifecycleOwner = null
            windowManager.removeView(view)
            modalComposeView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        dismissQuickModal()
        floatingBubbleView?.let {
            windowManager.removeView(it)
            floatingBubbleView = null
        }
    }
}
