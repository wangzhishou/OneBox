package io.noties.markwon.prism4j.languages;

          import org.jetbrains.annotations.NotNull;

          import io.noties.prism4j.GrammarUtils;
          import io.noties.prism4j.Prism4j;
          import io.noties.prism4j.annotations.Modify;

          import static java.util.regex.Pattern.CASE_INSENSITIVE;
          import static java.util.regex.Pattern.compile;
          import static io.noties.prism4j.Prism4j.*;

          @Modify("markup")
          public abstract class Prism_html {

            @NotNull
            public static Prism4j.Grammar create(@NotNull Prism4j prism4j) {

              final Prism4j.Grammar grammar = grammar(
                "html",
                token("comment", pattern(compile("<!--[\\s\\S]*?-->"))),
                token(
                  "doctype",
                  pattern(compile("<!DOCTYPE[^>]+>", CASE_INSENSITIVE))
                ),
                // 优化后的 tag 规则
                token(
                  "tag",
                  pattern(
                    compile("<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s+[^\\s>\\/=]+(?:=(?:(\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1|[^\\s'\">=]+))?)*\\s*\\/?>"),
                    false,
                    true,
                    null,
                    grammar(
                      "inside",
                      token("tag-name", pattern(compile("^<\\/?[^\\s>\\/]+"))),
                      token("punctuation", pattern(compile("^<\\/?"))),  // 匹配 < 或 </
                      token("self-closing", pattern(compile("\\/+"))),   // 自闭合的 /
                      token("closing-bracket", pattern(compile(">$"))),  // 闭合的 >
                      token("attr-name", pattern(compile("\\b[^\\s>\\/=]+"))),
                      token(
                        "attr-value",
                        pattern(
                          compile("=\\s*(\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1|=\\s*[^\\s'\">=]+"),
                          false,
                          false,
                          null,
                          grammar(
                            "inside",
                            token("punctuation", pattern(compile("^=|(['\"])|\\1"))), // 匹配=和引号
                            token("string", pattern(compile("[^'\"]+")))
                          )
                        )
                      )
                    )
                  )
                ),
                token("entity", pattern(compile("&#?[\\da-z]{1,8};", CASE_INSENSITIVE)))
              );

              // 处理嵌入的 CSS/JavaScript
              final Prism4j.Grammar markup = prism4j.grammar("markup");
              if (markup != null) {
                // 处理 <style> 标签内的 CSS
                GrammarUtils.insertBeforeToken(markup, "tag",
                  token(
                    "style",
                    pattern(
                      compile("(<style[\\s\\S]*?>)[\\s\\S]*?(?=<\\/style>)", CASE_INSENSITIVE),
                      true,
                      true,
                      "language-css",
                      prism4j.grammar("css") // 引用 CSS 语法规则
                    )
                  )
                );

                // 处理 <script> 标签内的 JavaScript
                GrammarUtils.insertBeforeToken(markup, "tag",
                  token(
                    "script",
                    pattern(
                      compile("(<script[\\s\\S]*?>)[\\s\\S]*?(?=<\\/script>)", CASE_INSENSITIVE),
                      true,
                      true,
                      "language-javascript",
                      prism4j.grammar("javascript") // 引用 JS 语法规则
                    )
                  )
                );
              }

              return grammar;
            }

            private Prism_html() {}
          }