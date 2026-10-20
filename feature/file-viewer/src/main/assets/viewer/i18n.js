/*
 * 阅读器外壳的本地化。
 *
 * 页面里的按钮/弹窗文案来自 epub.html，运行时的状态文字（复制按钮的 Copied、
 * 打不开时的错误标题）来自 epub.js —— 都是随页面一起打包的英文。这里不改那两个
 * 文件，而是由宿主把"英文原文 → 当前语言译文"的对照表放进 URL 的 ui 参数：
 *
 *   https://appassets.androidplatform.net/assets/viewer/epub.html?...&ui=<对照表>
 *
 * 对照表只由 FileViewerComponent 用 Android 字符串资源生成，所以 14 种语言跟
 * 设置里的语言走。表里没有的英文原样保留（页面升级后失配也不会坏）。
 *
 * 只翻译外壳节点，正文（#book）绝不动：书里的文字不能被当成 UI 文案替换掉。
 */
"use strict";

(function () {
    var raw = (new URLSearchParams(location.search)).get("ui");
    if (!raw) return;

    // 对照表格式：key\u001evalue，条目之间用 \u001f（两个字符都不会出现在文案里）
    var dict = {};
    raw.split("\u001f").forEach(function (pair) {
        var at = pair.indexOf("\u001e");
        if (at > 0) dict[pair.slice(0, at)] = pair.slice(at + 1);
    });
    var hasWords = Object.keys(dict).length > 0;
    if (!hasWords) return;

    // 外壳容器：只观察这些节点，书页正文与目录条目不在其中
    var CHROME = [
        "#vw-status",
        "#epub-bar",
        "#epub-turn",
        "#epub-contents .epub-contents-head",
        "#epub-link .epub-link-box"
    ];

    function translateNode(root) {
        var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null);
        var node = walker.nextNode();
        while (node) {
            var text = node.nodeValue;
            var key = text ? text.trim() : "";
            if (key && Object.prototype.hasOwnProperty.call(dict, key)) {
                node.nodeValue = text.replace(key, dict[key]);
            }
            node = walker.nextNode();
        }
    }

    function translateAll() {
        for (var i = 0; i < CHROME.length; i++) {
            var nodes = document.querySelectorAll(CHROME[i]);
            for (var j = 0; j < nodes.length; j++) translateNode(nodes[j]);
        }
    }

    function watch() {
        if (!window.MutationObserver) return;
        var observer = new MutationObserver(function () {
            translateAll();
        });
        for (var i = 0; i < CHROME.length; i++) {
            var nodes = document.querySelectorAll(CHROME[i]);
            for (var j = 0; j < nodes.length; j++) {
                observer.observe(nodes[j], {childList: true, characterData: true, subtree: true});
            }
        }
    }

    // 给页面脚本留一个口子（epub.js 目前用不到，后续自定义文案可以直接调）
    window.vwT = function (text) {
        return Object.prototype.hasOwnProperty.call(dict, text) ? dict[text] : text;
    };

    // 脚本排在所有外壳节点之后：先翻一遍静态文案，立刻挂上观察器接住
    // epub.js 运行时写进去的文案（复制按钮、错误标题）
    translateAll();
    watch();
    document.addEventListener("DOMContentLoaded", translateAll);
})();
