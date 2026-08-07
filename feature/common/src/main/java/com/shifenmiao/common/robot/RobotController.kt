package com.shifenmiao.common.robot

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

object RobotController {
    /**
     * Updates the robot's position based on the app bar coordinates
     */
    fun updateRobotPosition(
        coordinates: LayoutCoordinates,
        localDensity: Density,
        collapsedFraction: Float,
        topOffsetPx: Float = 0f,
        appComponent: AppComponent,
        screenName: String = "",
        currentScreen: String = ""
    ) {
        if (currentScreen != screenName) {
            return
        }
        if (!coordinates.isAttached) return

        val robotState = appComponent.robotState.value

        // Auto visibility follows app bar collapse only before the user drags the robot.
        if (!robotState.hasBeenDragged) {
            val shouldShow = !robotState.disabled && collapsedFraction < 0.95f
            if (robotState.visible != shouldShow) {
                appComponent.toggleRobotVisibility(shouldShow)
            }
        }

        val rootCoordinates = coordinates.findRootCoordinates()
        val positionInRoot = coordinates.positionInRoot()

        with(localDensity) {
            val screenWidth = rootCoordinates.size.width.toFloat()
            val screenHeight = rootCoordinates.size.height.toFloat()

            // We approximate the app bar *visual* height during collapse/expand.
            // If the current measured height is larger than the expanded default,
            // we prefer the measured height as expandedHeightPx.
            val measuredAppBarHeightPx = coordinates.size.height.toFloat()
            val collapsedHeightPx = if (currentScreen == Screen.NewApp().simpleName) {
                TopAppBarDefaults.LargeAppBarCollapsedHeight.toPx()
            } else {
                // In this material3 version regular TopAppBar uses a single height.
                // Use expandedHeight as a stable approximation for collapsed height.
                TopAppBarDefaults.TopAppBarExpandedHeight.toPx()
            }
            val expandedDefaultPx = if (currentScreen == Screen.NewApp().simpleName) {
                TopAppBarDefaults.LargeAppBarExpandedHeight.toPx()
            } else {
                TopAppBarDefaults.TopAppBarExpandedHeight.toPx()
            }
            val expandedHeightPx = maxOf(measuredAppBarHeightPx, expandedDefaultPx)

            val currentAppBarHeightPx = expandedHeightPx +
                    (collapsedHeightPx - expandedHeightPx) * collapsedFraction.coerceIn(0f, 1f)

            val appBarTop = positionInRoot.y
            val appBarRight = positionInRoot.x + coordinates.size.width

            // Robot's *layout* size (unscaled)
            val robotLayoutSize = appComponent.robotState.value.size.toPx()

            // Scale: expanded 100%, collapsed 60%
            val targetScale = 1.0f - (collapsedFraction * 0.4f)

            // Visual size (scaled)
            val visualRobotSize = robotLayoutSize * targetScale

            // AppBar vertical center uses the approximated visual height.
            val appBarCenterY = appBarTop + currentAppBarHeightPx / 2f

            val targetTopLeftY = appBarCenterY - visualRobotSize / 2f + topOffsetPx - 10.dp.toPx()

            // IMPORTANT:
            // FloatingRobotDragController maps percent to an extended range:
            //   min = -robotSize/2; max = container - robotSize/2
            // RobotController must compute percent using the same contract,
            // otherwise the robot will not align with the intended center.
            val minX = -robotLayoutSize / 2f
            val maxX = screenWidth - robotLayoutSize / 2f
            val xRange = (maxX - minX).takeIf { it > 0f } ?: 1f

            val minY = -robotLayoutSize / 2f
            val maxY = screenHeight - robotLayoutSize / 2f
            val yRange = (maxY - minY).takeIf { it > 0f } ?: 1f

            val finalY = targetTopLeftY.coerceIn(minY, maxY)
            val percentY = ((finalY - minY) / yRange).coerceIn(0f, 1f)

            // X: keep existing strategy: lock robot's *layout right edge* to AppBarRight - fixed distance.
            val targetOffsetFromRight = 80.dp.toPx()
            val targetTopLeftX = (appBarRight - targetOffsetFromRight) - robotLayoutSize

            val finalX = targetTopLeftX.coerceIn(minX, maxX)
            val percentX = ((finalX - minX) / xRange).coerceIn(0f, 1f)

            // Mark initialized only after we computed a real position (prevents (0,0) flash)
            if (!appComponent.robotState.value.isInitialized) {
                appComponent.setIsInitialized(true)
            }
            if (!appComponent.robotState.value.hasComputedInitialPosition) {
                appComponent.setRobotHasComputedInitialPosition(true)
            }

            if (!appComponent.robotState.value.hasBeenDragged) {
                appComponent.setRobotOffsetPercent(percentX, percentY)
            }
            appComponent.setRobotScale(targetScale)
        }
    }
}
