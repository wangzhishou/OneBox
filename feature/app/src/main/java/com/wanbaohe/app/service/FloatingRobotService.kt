package com.wanbaohe.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.t8rin.logger.Logger.makeLog
import kotlin.math.abs

class FloatingRobotService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var robotView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isMoving = false
    private var lastAction = 0

    companion object {
        private var isShowing = false

        // In toggleFloatingRobot method, add logging before starting service
        @RequiresApi(Build.VERSION_CODES.M)
        fun toggleFloatingRobot(context: Context) {
            makeLog { "Attempting to toggle floating robot, current state: $isShowing" }

            // Check permission explicitly
            val hasPermission = Settings.canDrawOverlays(context)
            makeLog { "Has overlay permission: $hasPermission" }

            val intent = Intent(context, FloatingRobotService::class.java)
            if (isShowing) {
                makeLog { "Stopping service" }
                context.stopService(intent)
                isShowing = false
            } else {
                if (!hasPermission) {
                    makeLog { "Missing overlay permission, cannot start service" }
                    // Prompt for permission
                    return
                }
                makeLog { "Starting service" }
                context.startService(intent)
                isShowing = true
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        makeLog { "FloatingRobotService onStartCommand" }
        if (intent == null) return START_NOT_STICKY

        // Create layout parameters for the floating view
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        // Create and add the floating view
        val container = FrameLayout(this)

        // Create lifecycle and state registry owners
        val lifecycleOwner = object : LifecycleOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)

            init {
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }

            override val lifecycle: Lifecycle get() = lifecycleRegistry
        }

        val stateRegistryOwner = object : SavedStateRegistryOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)
            private val controller = SavedStateRegistryController.create(this)

            override val savedStateRegistry: SavedStateRegistry = controller.savedStateRegistry
            override val lifecycle: Lifecycle = lifecycleRegistry

            init {
                controller.performRestore(null)
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }
        }

        // Set owners on the container first
        container.setViewTreeLifecycleOwner(lifecycleOwner)
        container.setViewTreeSavedStateRegistryOwner(stateRegistryOwner)

        val composeView = ComposeView(this).apply {
            setContent {
                makeLog{ "FloatingRobotService setContent" }
//                FloatingRobot()
            }
        }

        container.addView(composeView)
        container.setOnTouchListener(createTouchListener())
        robotView = container

        // Add the view to window manager with animation
        windowManager.addView(robotView, layoutParams)
        animateEntry()

        return START_STICKY
    }

    private fun createTouchListener(): View.OnTouchListener = View.OnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                lastAction = event.action
                isMoving = false
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY

                // Determine if user is actually moving or just tapping
                if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                    isMoving = true
                }

                if (isMoving) {
                    layoutParams.x = initialX + deltaX.toInt()
                    layoutParams.y = initialY + deltaY.toInt()
                    windowManager.updateViewLayout(robotView, layoutParams)
                }
                lastAction = event.action
            }

            MotionEvent.ACTION_UP -> {
                if (isMoving) {
                    snapToEdge()
                } else if (lastAction == MotionEvent.ACTION_DOWN) {
                    // Handle click if needed
                    view.performClick()
                }
                lastAction = event.action
            }
        }
        true
    }

    private fun snapToEdge() {
        val width = resources.displayMetrics.widthPixels

        // Calculate which edge is closest
        val toRight = width - (layoutParams.x + robotView.width)
        val toLeft = layoutParams.x

        // Animate to edge
        if (toRight < toLeft) {
            animateToPosition(width - robotView.width, layoutParams.y)
        } else {
            animateToPosition(0, layoutParams.y)
        }
    }

    private fun animateToPosition(x: Int, y: Int) {
        robotView.animate()
            .x(x.toFloat())
            .setDuration(300)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                layoutParams.x = x
                layoutParams.y = y
                windowManager.updateViewLayout(robotView, layoutParams)
            }
            .start()
    }

    private fun animateEntry() {
        robotView.alpha = 0f
        robotView.scaleX = 0.5f
        robotView.scaleY = 0.5f

        robotView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(1.5f))
            .start()
    }

    override fun onDestroy() {
        if (::robotView.isInitialized) {
            windowManager.removeView(robotView)
        }
        super.onDestroy()
    }
}