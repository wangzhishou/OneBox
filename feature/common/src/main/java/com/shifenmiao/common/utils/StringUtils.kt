package com.shifenmiao.common.utils

object StringUtils {

    fun generateHtmlByJavascript(code:String): String {
        return """
            <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-size: 16px;
                            line-height: 1.5;
                        }
                    </style>
                </head>
                <body>
                    <script type="text/javascript">
                    $code
                    </script>
                </body>
            </html>
        """.trimIndent()
    }

}