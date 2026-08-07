package com.shifenmiao.marquee.activity

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.retainedComponent
import com.shifenmiao.marquee.screen.FullScreenSubtitles
import com.shifenmiao.marquee.screenLogic.MarqueeComponent
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiState
import com.t8rin.imagetoolbox.core.ui.utils.ComposeActivity
import com.t8rin.imagetoolbox.core.ui.utils.provider.ImageToolboxCompositionLocals
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MarqueeActivity : ComposeActivity() {

    @Inject
    lateinit var componentFactory: MarqueeComponent.Factory

    private val component: MarqueeComponent by lazy {
        retainedComponent { componentContext ->
            componentFactory(componentContext)
        }
    }

    @Composable
    override fun Content() {
        ImageToolboxCompositionLocals(
            settingsState = component.settingsState.toUiState(),
            settingsManager = component.settingsManager
        ) {
            FullScreenSubtitles(
                onGoBack = {
                    finish()
                },
                component = component
            )
        }

    }
}
