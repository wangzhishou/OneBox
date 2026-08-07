package com.shifenmiao.ai.agent.tool.builtin.pdf.di

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.builtin.pdf.ConvertImagesToPdfTool
import com.shifenmiao.ai.agent.tool.builtin.pdf.ConvertPdfToImagesTool
import com.shifenmiao.ai.agent.tool.builtin.pdf.OpenPdfPreviewTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(SingletonComponent::class)
object PdfAgentToolModule {

    @Provides
    @IntoMap
    @StringKey("open_pdf_preview")
    fun provideOpenPdfPreviewTool(tool: OpenPdfPreviewTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("convert_pdf_to_images")
    fun provideConvertPdfToImagesTool(tool: ConvertPdfToImagesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("convert_images_to_pdf")
    fun provideConvertImagesToPdfTool(tool: ConvertImagesToPdfTool): AgentTool = tool
}
