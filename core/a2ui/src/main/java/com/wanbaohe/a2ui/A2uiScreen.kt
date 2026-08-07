package com.wanbaohe.a2ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.glass.MeshGradientBackground
import com.wanbaohe.a2ui.ui.A2uiSurfaceView
import com.wanbaohe.a2ui.viewModel.A2uiComponent
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun A2uiScreen(
    component: A2uiComponent,
) {
    DisposableEffect(Unit) {
        component.connect()
        onDispose {
            component.disconnect()
        }
    }

    MeshGradientBackground {
        Scaffold(
            topBar = {
                GlassTopAppBar(
                    title = { Text(stringResource(R.string.a2ui_screen)) },
                    navigationIcon = {
                        EnhancedIconButton(onClick = { component.onGoBack() }) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
        ) { padding ->
            A2uiSurfaceView(
                surfaceId = component.surfaceId,
                viewerContext = component.viewerContext,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding).imePadding(),
            )
        }
    }
}
