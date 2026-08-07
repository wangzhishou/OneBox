package com.t8rin.imagetoolbox.library_details.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.text.HtmlText
import com.t8rin.imagetoolbox.library_details.presentation.screenLogic.LibraryDetailsComponent

@Composable
fun LibraryDetailsContent(
    component: LibraryDetailsComponent
) {
    BaseScreen(
        title = component.libraryName,
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        SelectionContainer {
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 7.dp),
                shape = RoundedCornerShape(16.dp),
                containerAlpha = 0.24f,
                borderWidth = 0.8.dp,
            ) {
                HtmlText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(
                            WindowInsets.displayCutout
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        )
                        .padding(16.dp),
                    html = component.libraryDescription
                )
            }
        }
    }
}