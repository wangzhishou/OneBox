/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.t8rin.imagetoolbox.core.domain.model

import com.t8rin.imagetoolbox.core.domain.model.MimeType.Multiple

sealed class MimeType(
    val entries: Set<String>
) {
    constructor(type: String) : this(setOf(type))

    data class Single(
        private val type: String
    ) : MimeType(type) {
        val entry = type
    }

    data class Multiple(
        private val types: Set<String>
    ) : MimeType(types)

    companion object {
        val All = Single("*/*")

        val Txt = Single("text/plain")
        val Json = Single("application/json")
        val Csv = Single("text/csv")
        val Pdf = Single("application/pdf")
        val Zip = Single("application/zip")
        val Webp = Single("image/webp")
        val Gif = Single("image/gif")
        val Apng = Single("image/apng")
        val StaticPng = Single("image/png")
        val Png = Apng + StaticPng
        val Audio = Single("audio/*")
        val Jpg = Single("image/jpg")
        val Jpeg = Single("image/jpeg")
        val JpgAll = Jpg + Jpeg

        val UploadImage = Jpg + Jpeg + StaticPng + Gif + Single("image/webp") + Single("image/heif") + Single("image/heic") + Single("image/avif")
        val ImportText = Txt + Json
        val Font = Multiple(
            setOf(
                "font/ttf",
                "application/x-font-ttf",
                "font/otf"
            )
        )
        val Bmp = Single("image/bmp")
        val Jp2 = Single("image/jp2")
        val Tiff = Single("image/tiff")
        val Qoi = Single("image/qoi")
        val Ico = Single("image/x-icon")
        val Svg = Single("image/svg+xml")

        // 编程语言文件类型
        val Html = Single("text/html")
        val Brainfuck = Single("text/plain")
        val C = Single("text/x-c")
        val Cpp = Single("text/x-c++src")
        val CSharp = Single("text/x-csharp")
        val Css = Single("text/css")
        val Dart = Single("text/x-dart")
        val Go = Single("text/x-go")
        val Groovy = Single("text/x-groovy")
        val Java = Single("text/x-java")
        val JavaScript = Single("application/javascript")
        val TypeScript = Single("application/typescript")
        val Kotlin = Single("text/x-kotlin")
        val Latex = Single("application/x-latex")
        val Makefile = Single("text/x-makefile")
        val Markdown = Single("text/markdown")
        val Python = Single("text/x-python")
        val Scala = Single("text/x-scala")
        val Shell = Single("text/x-shellscript")
        val Sql = Single("text/x-sql")
        val Swift = Single("text/x-swift")
        val Yaml = Single("application/x-yaml")
        val Clojure = Single("text/x-clojure")
    }

    /**
     * 根据语言名称返回对应的SaveTarget对象
     * @param language 语言名称
     * @return 对应的SaveTarget对象，若无匹配则返回TEXT类型
     */
    fun fromLanguage(language: String): MimeType {
        return when (language.lowercase()) {
            "html" -> Html
            "brainfuck" -> Brainfuck
            "c" -> C
            "cpp", "c++" -> Cpp
            "csharp", "c#" -> CSharp
            "css", "scss" -> Css
            "dart" -> Dart
            "go", "golang" -> Go
            "groovy" -> Groovy
            "java" -> Java
            "javascript", "js", "jsx" -> JavaScript
            "typescript", "ts", "tsx" -> TypeScript
            "json" -> Json
            "kotlin", "kt" -> Kotlin
            "latex", "tex" -> Latex
            "makefile" -> Makefile
            "markdown", "md" -> Markdown
            "python", "py" -> Python
            "scala" -> Scala
            "shell", "bash", "zsh", "sh" -> Shell
            "sql" -> Sql
            "swift" -> Swift
            "yaml", "yml" -> Yaml
            "clojure", "clj" -> Clojure
            else -> Txt
        }
    }

    /**
     * 根据MimeType返回对应的文件扩展名
     * @return 文件扩展名(不带点),如果是Multiple类型则返回第一个匹配的扩展名
     */
    fun toFileExtension(): String {
        return when (this) {
            is Single -> when (entry) {
                "text/plain" -> "txt"
                "application/json" -> "json"
                "text/csv" -> "csv"
                "application/pdf" -> "pdf"
                "application/zip" -> "zip"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                "image/apng" -> "png"
                "image/png" -> "png"
                "audio/*" -> "mp3"
                "image/jpg" -> "jpg"
                "image/jpeg" -> "jpg"
                "image/bmp" -> "bmp"
                "image/jp2" -> "jp2"
                "image/tiff" -> "tiff"
                "image/qoi" -> "qoi"
                "image/x-icon" -> "ico"
                "image/svg+xml" -> "svg"
                "text/html" -> "html"
                "text/x-c" -> "c"
                "text/x-c++src" -> "cpp"
                "text/x-csharp" -> "cs"
                "text/css" -> "css"
                "text/x-dart" -> "dart"
                "text/x-go" -> "go"
                "text/x-groovy" -> "groovy"
                "text/x-java" -> "java"
                "application/javascript" -> "js"
                "application/typescript" -> "ts"
                "text/x-kotlin" -> "kt"
                "text/x-shellscript" -> "sh"
                "application/x-latex" -> "tex"
                "text/x-makefile" -> "makefile"
                "text/markdown" -> "md"
                "text/x-python" -> "py"
                "text/x-scala" -> "scala"
                "text/x-sql" -> "sql"
                "text/x-swift" -> "swift"
                "application/x-yaml" -> "yaml"
                "text/x-clojure" -> "clj"
                else -> "txt"
            }
            is Multiple -> {
                // 对于Multiple类型,取第一个entry并转换
                entries.firstOrNull()?.let {
                    Single(it).toFileExtension()
                } ?: "txt"
            }
        }
    }
}

fun mimeType(
    type: String
): MimeType.Single = MimeType.Single(type)

fun String.toMimeType() = mimeType(this)

fun mimeTypeOf(
    vararg types: String
): Multiple = Multiple(types.toSet())

fun mimeTypeOf(
    vararg types: MimeType
): Multiple = Multiple(types.flatMapTo(mutableSetOf()) { it.entries })

operator fun MimeType.plus(
    type: MimeType
): Multiple = Multiple(types = entries + type.entries)

operator fun Multiple.minus(
    type: MimeType
): Multiple = Multiple(types = entries - type.entries)