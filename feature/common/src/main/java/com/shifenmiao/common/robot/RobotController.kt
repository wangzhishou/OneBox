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
     * 搜索按钮左/右缘，坐标系与 [updateRobotPosition] 里的 positionInRoot 一致。
     * 未测量（横屏没有搜索按钮）时为 NaN，X 退回固定右边距。
     */
    private var searchAnchorLeftPx = Float.NaN
    private var searchAnchorRightPx = Float.NaN

    /** 机器人可点击区域与搜索按钮之间的间距，避免挡住搜索。 */
    private val searchButtonGap = 16.dp

    /**
     * 默认 Y 在标题栏视觉中心之上再抬一段。
     * 机器人主体偏下，居中时下缘会盖住标题栏下面的分类导航。
     */
    private val robotTopLift = 28.dp

    fun updateSearchAnchor(leftPx: Float, rightPx: Float) {
        searchAnchorLeftPx = leftPx
        searchAnchorRightPx = rightPx
    }

    fun clearSearchAnchor() {
        searchAnchorLeftPx = Float.NaN
        searchAnchorRightPx = Float.NaN
    }

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

            val targetTopLeftY = appBarCenterY - visualRobotSize / 2f + topOffsetPx - robotTopLift.toPx()

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

            // X 跟搜索按钮走，不跟屏幕宽度百分比走：可点击区域贴在按钮朝标题的一侧，并留出间距。
            val anchorLeft = searchAnchorLeftPx
            val anchorRight = searchAnchorRightPx
            val appBarLeft = positionInRoot.x
            val targetTopLeftX = if (
                !anchorLeft.isNaN() &&
                !anchorRight.isNaN() &&
                anchorRight > anchorLeft &&
                anchorLeft >= appBarLeft - 1f &&
                anchorRight <= appBarRight + 8.dp.toPx()
            ) {
                val gapPx = searchButtonGap.toPx()
                val searchCenterX = (anchorLeft + anchorRight) / 2f
                val appBarCenterX = (appBarLeft + appBarRight) / 2f
                if (searchCenterX >= appBarCenterX) {
                    (anchorLeft - gapPx) - robotLayoutSize
                } else {
                    anchorRight + gapPx
                }
            } else {
                (appBarRight - 80.dp.toPx()) - robotLayoutSize
            }

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
