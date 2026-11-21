package com.wanbaohe.a2ui.workbench

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.serialization.json.JsonPrimitive

internal enum class WorkbenchSample(val title: Int) {
    Form(R.string.a2wb_form),
    Layout(R.string.a2wb_layout),
    Media(R.string.a2wb_media),
    Binding(R.string.a2wb_binding),
    Lifecycle(R.string.a2wb_lifecycle),
    Invalid(R.string.a2wb_invalid),
}

internal class WorkbenchSamples @Inject constructor(@ApplicationContext private val context: Context) {
    fun source(sample: WorkbenchSample, catalogId: String): String {
        val create = frame("createSurface", """"catalogId":${JsonPrimitive(catalogId)},"sendDataModel":true""")
        return when (sample) {
            WorkbenchSample.Form -> array(create, components(form()), frame("updateDataModel",
                """"path":"/","value":{"name":"","choice":["a"],"agree":false,"level":3,"date":"2026-11-21"}"""))
            WorkbenchSample.Layout -> array(create, components(layout()))
            WorkbenchSample.Media -> array(create, components("""
                {"id":"root","component":"Column","children":["image","video","audio"]},
                {"id":"image","component":"Image","url":"${WorkbenchMedia.IMAGE}"},
                {"id":"video","component":"Video","url":"${WorkbenchMedia.CLIP}"},
                {"id":"audio","component":"AudioPlayer","url":"${WorkbenchMedia.CLIP}","description":${label(R.string.a2wb_play)}}
            """.trimIndent()))
            WorkbenchSample.Binding -> array(
                create,
                components("""
                    {"id":"root","component":"Column","children":["summary","list"]},
                    {"id":"summary","component":"Text","text":{"call":"formatNumber","args":{"value":{"path":"/total"},"decimals":2}}},
                    {"id":"list","component":"List","children":{"componentId":"item","path":"/items"}},
                    {"id":"item","component":"Text","text":{"path":"title"}}
                """.trimIndent()),
                frame("updateDataModel", """"path":"/","value":{"total":1234.5,"items":[{"title":"A"},{"title":"B"}]}"""),
            )
            WorkbenchSample.Lifecycle -> array(
                create,
                components("""{"id":"root","component":"Text","text":{"path":"/title"}}"""),
                frame("updateDataModel", """"path":"/title","value":${label(R.string.a2wb_first_update)}"""),
                frame("updateDataModel", """"path":"/title","value":${label(R.string.a2wb_second_update)}"""),
                frame("deleteSurface"),
            )
            WorkbenchSample.Invalid -> array(create, components("""{"id":"root","component":"NotInCatalog"}"""))
        }
    }

    private fun form() = """
        {"id":"root","component":"Card","child":"form"},
        {"id":"form","component":"Column","children":["heading","name","choice","agree","level","date","submit"]},
        {"id":"heading","component":"Text","text":${label(R.string.a2wb_form)},"variant":"h3"},
        {"id":"name","component":"TextField","label":${label(R.string.a2wb_name)},"value":{"path":"/name"},"checks":[{"condition":{"call":"required","args":{"value":{"path":"/name"}}},"message":${label(R.string.a2wb_required)}}]},
        {"id":"choice","component":"ChoicePicker","label":${label(R.string.a2wb_choice)},"options":[{"label":"A","value":"a"},{"label":"B","value":"b"}],"value":{"path":"/choice"},"variant":"mutuallyExclusive","displayStyle":"chips"},
        {"id":"agree","component":"CheckBox","label":${label(R.string.a2wb_agree)},"value":{"path":"/agree"}},
        {"id":"level","component":"Slider","label":${label(R.string.a2wb_level)},"min":0,"max":10,"value":{"path":"/level"}},
        {"id":"date","component":"DateTimeInput","label":${label(R.string.a2wb_date)},"enableDate":true,"value":{"path":"/date"}},
        {"id":"submit","component":"Button","child":"submitLabel","variant":"primary","action":{"event":{"name":"submit","context":{"name":{"path":"/name"},"choice":{"path":"/choice"}}}}},
        {"id":"submitLabel","component":"Text","text":${label(R.string.a2wb_submit)}}
    """.trimIndent()

    private fun layout() = """
        {"id":"root","component":"Column","children":["row","divider","tabs","modal"]},
        {"id":"row","component":"Row","children":["icon","title"]},
        {"id":"icon","component":"Icon","name":"info"},
        {"id":"title","component":"Text","text":${label(R.string.a2wb_layout)}},
        {"id":"divider","component":"Divider"},
        {"id":"tabs","component":"Tabs","tabs":[{"title":"List","child":"list"},{"title":"Card","child":"card"}]},
        {"id":"list","component":"List","children":["title","icon"]},
        {"id":"card","component":"Card","child":"title"},
        {"id":"modal","component":"Modal","trigger":"trigger","content":"card"},
        {"id":"trigger","component":"Text","text":${label(R.string.a2wb_open_modal)}}
    """.trimIndent()

    private fun label(id: Int) = JsonPrimitive(context.getString(id))
    private fun array(vararg frames: String) = frames.joinToString(",\n", "[\n", "\n]")
    private fun components(items: String) = frame("updateComponents", """"components":[$items]""")

    private fun frame(type: String, properties: String? = null): String {
        val extra = properties?.let { ",$it" }.orEmpty()
        return """{"version":"v0.9.1","$type":{"surfaceId":"demo"$extra}}"""
    }
}
