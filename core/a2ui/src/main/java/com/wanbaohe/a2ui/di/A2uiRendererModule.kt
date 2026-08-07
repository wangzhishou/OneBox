package com.wanbaohe.a2ui.di

import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.AudioPlayerRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.BadgeRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.DividerRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.IconRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.ImageRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.TabsRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.TextRenderer
import com.wanbaohe.a2ui.catalog.builtin.display.VideoRenderer
import com.wanbaohe.a2ui.catalog.builtin.feedback.ModalRenderer
import com.wanbaohe.a2ui.catalog.builtin.feedback.ProgressRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.ButtonRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.CheckboxRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.ChoicePickerRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.ColorPickerRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.ColumnSelectorRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.DateInputRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.GridSelectorRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.TimeInputRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.ListSelectorRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.LocationPickerRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.RadioGroupRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.RowSelectorRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.SliderRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.StepperRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.SwitchRenderer
import com.wanbaohe.a2ui.catalog.builtin.input.TextFieldRenderer
import com.wanbaohe.a2ui.catalog.builtin.layout.CardRenderer
import com.wanbaohe.a2ui.catalog.builtin.layout.ColumnRenderer
import com.wanbaohe.a2ui.catalog.builtin.layout.ListRenderer
import com.wanbaohe.a2ui.catalog.builtin.layout.RowRenderer
import com.wanbaohe.a2ui.catalog.builtin.layout.SpacerRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class A2uiRendererModule {

    @Binds @IntoSet
    abstract fun bindColumnRenderer(impl: ColumnRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindRowRenderer(impl: RowRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindCardRenderer(impl: CardRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindListRenderer(impl: ListRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindButtonRenderer(impl: ButtonRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindTextFieldRenderer(impl: TextFieldRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindCheckboxRenderer(impl: CheckboxRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindChoicePickerRenderer(impl: ChoicePickerRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindSliderRenderer(impl: SliderRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindDateInputRenderer(impl: DateInputRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindTimeInputRenderer(impl: TimeInputRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindTextRenderer(impl: TextRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindImageRenderer(impl: ImageRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindIconRenderer(impl: IconRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindDividerRenderer(impl: DividerRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindVideoRenderer(impl: VideoRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindAudioPlayerRenderer(impl: AudioPlayerRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindTabsRenderer(impl: TabsRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindProgressRenderer(impl: ProgressRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindModalRenderer(impl: ModalRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindSpacerRenderer(impl: SpacerRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindSwitchRenderer(impl: SwitchRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindStepperRenderer(impl: StepperRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindRadioGroupRenderer(impl: RadioGroupRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindRowSelectorRenderer(impl: RowSelectorRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindColumnSelectorRenderer(impl: ColumnSelectorRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindGridSelectorRenderer(impl: GridSelectorRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindListSelectorRenderer(impl: ListSelectorRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindColorPickerRenderer(impl: ColorPickerRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindLocationPickerRenderer(impl: LocationPickerRenderer): A2uiComponentRenderer

    @Binds @IntoSet
    abstract fun bindBadgeRenderer(impl: BadgeRenderer): A2uiComponentRenderer
}
