"use strict";

/*
 * Lines as Word sets them. Word spaces lines as a multiple of the font's own single line, where
 * docx-preview multiplies the size alone, and Android has none of Word's fonts, so each is drawn
 * in a phone font sized to set text as wide as Word's. A page then holds what it held in Word.
 */

/*
 * Per Word font: its single line over its size, the phone font drawn for it, the size that font
 * needs to set Latin and Cyrillic text as wide, and its ascent, descent and line gap over its
 * size, as Windows gives them. From the fonts or their metric twins, over real documents' text.
 */
var VW_WORD_FONTS = {
  "calibri": [1.2207, "sans", 0.920, 0.895, 0.952, 0.269, 0],
  "calibri light": [1.2207, "sans", 0.920, 0.895, 0.952, 0.269, 0],
  "cambria": [1.1720, "serif", 0.910, 0.910, 0.950, 0.222, 0],
  "times new roman": [1.1499, "serif", 0.871, 0.824, 0.891, 0.216, 0.042],
  "times": [1.1499, "serif", 0.871, 0.824, 0.891, 0.216, 0.042],
  "liberation serif": [1.1499, "serif", 0.871, 0.824, 0.891, 0.216, 0.042],
  "arial": [1.1499, "sans", 1.014, 0.975, 0.905, 0.212, 0.033],
  "liberation sans": [1.1499, "sans", 1.014, 0.975, 0.905, 0.212, 0.033],
  "arial narrow": [1.1475, "sans", 0.831, 0.800, 0.922, 0.210, 0.015],
  "courier new": [1.1328, "mono", 0.991, 0.991, 0.833, 0.300, 0],
  "liberation mono": [1.1328, "mono", 0.991, 0.991, 0.833, 0.300, 0],
  "georgia": [1.1362, "serif", 0.945, 0.913, 0.917, 0.219, 0],
  "tahoma": [1.2070, "sans", 1.019, 0.955, 1.000, 0.207, 0],
  "verdana": [1.2153, "sans", 1.171, 1.077, 1.005, 0.210, 0],
  "trebuchet ms": [1.1611, "sans", 1.026, 0.960, 0.939, 0.222, 0],
  "segoe ui": [1.3301, "sans", 1.008, 0.970, 1.079, 0.251, 0],
  "aptos": [1.2847, "sans", 0.965, 0.954, 1.010, 0.275, 0],
  "aptos display": [1.2847, "sans", 0.910, 0.893, 1.010, 0.275, 0],
  "aptos light": [1.2847, "sans", 0.945, 0.926, 1.010, 0.275, 0],
  "aptos narrow": [1.2847, "sans", 0.893, 0.875, 1.010, 0.275, 0],
  "aptos serif": [1.2847, "serif", 0.954, 0.935, 1.010, 0.275, 0]
};

/* A font not listed gets about the middle of Word's common ones; docx.html falls back to the same */
var VW_LINE = 1.2;

function vwFirstFamily(value) {
  return String(value).split(",")[0].trim().replace(/^["']|["']$/g, "").toLowerCase();
}

/* The single line of a font-family value, through the theme's own fonts for a theme font */
function vwLineFactor(value) {
  var first = vwFirstFamily(value);
  var theme = /^var\(--docx-(\w+)-font\)$/.exec(first);
  if (theme) return "var(--vw-f-" + theme[1] + ", " + VW_LINE + ")";
  var font = VW_WORD_FONTS[first];
  return String(font ? font[0] : VW_LINE);
}

/*
 * A copy of docx-preview's style values with its line spacing moved into the properties the rule
 * in docx.html draws each line from: --vw-m for "auto", --vw-lh for "exact" and "atLeast", and
 * --vw-f, the line of the font in use. "atLeast" came out as the size plus the minimum.
 */
function vwWordValues(values) {
  var out = {}, k, at;
  for (k in values) out[k] = values[k];
  var lh = out["line-height"];
  if (lh !== undefined) {
    delete out["line-height"];
    if ((at = /^calc\(100% \+ (.+)\)$/.exec(lh))) {
      out["--vw-lh"] = "max(" + at[1] + ", calc(var(--vw-f, " + VW_LINE + ") * 1em))";
    } else if (/^\d*\.?\d+$/.test(lh)) {
      out["--vw-m"] = lh;
      out["--vw-lh"] = "initial";
    } else {
      out["--vw-lh"] = lh;
      if (out["min-height"] === lh) delete out["min-height"];
    }
  }
  if (out["font-family"] !== undefined) out["--vw-f"] = vwLineFactor(out["font-family"]);
  for (k in values) {
    var theme = /^--docx-(\w+)-font$/.exec(k);
    if (theme) out["--vw-f-" + theme[1].toLowerCase()] = vwLineFactor(values[k]);
  }
  return out;
}

/* The paragraphs a rule for runs is about: their blank lines and struts take its font, as a mark does */
function vwMarkSelector(selector) {
  var marks = [];
  String(selector).split(",").forEach(function (one) {
    var m = /^(.*\S)\s+span$/.exec(one.trim());
    if (m) marks.push(/(^|\s)p(\.[\w-]+)*$/.test(m[1]) ? m[1] : m[1] + " p");
  });
  return marks.join(", ");
}

/*
 * Installed from the h hook in docx.js. Every style rule passes through styleToString, so its
 * spacing moves to the properties above, and a rule for a style's runs gives its paragraphs the
 * same font.
 */
function vwWordLines(renderer) {
  var styleToString = renderer.styleToString;
  var renderParagraph = renderer.renderParagraph;
  renderer.styleToString = function (selector, values, extra) {
    if (!values || String(selector).charAt(0) === "@") return styleToString.apply(this, arguments);
    var v = vwWordValues(values);
    var css = styleToString.call(this, selector, v, extra);
    var mark = vwMarkSelector(selector), font = null;
    ["font-family", "font-size", "--vw-f"].forEach(function (k) {
      if (v[k] !== undefined) (font = font || {})[k] = v[k];
    });
    return mark && font ? css + styleToString.call(this, mark, font) : css;
  };
  renderer.renderParagraph = function (para) {
    var el = renderParagraph.call(this, para);
    if (el) vwStrutAsText(el, para);
    return el;
  };
}

/* A paragraph's or run's own formatting, as the h hook builds it; returns the custom properties
   to set once the element exists, since docx-preview assigns its style object property by property */
function vwWordElement(spec) {
  if (!spec || (spec.tagName !== "p" && spec.tagName !== "span") || !spec.style || typeof spec.style !== "object") {
    return null;
  }
  var v = vwWordValues(spec.style), custom = null;
  for (var k in v) {
    if (k.lastIndexOf("--", 0) === 0) {
      (custom = custom || {})[k] = v[k];
      delete v[k];
    }
  }
  spec.style = v;
  return custom;
}

/*
 * A browser starts each line with an empty box in the paragraph's own font, which docx-preview
 * leaves at the page's default, so a line of 11pt text stood 12pt tall. Word has no such box, so
 * the paragraph takes its first run's font, or for a blank line the size Word gave its mark.
 */
function vwStrutAsText(el, para) {
  var spans = el.getElementsByTagName("span");
  for (var i = 0; i < spans.length; i++) {
    var run = spans[i];
    if (!run.textContent || run.getElementsByTagName("span").length) continue;
    for (var at = run; at && at !== el; at = at.parentNode) {
      if (!at.style) continue;
      if (at.style.fontFamily && !el.style.fontFamily) el.style.fontFamily = at.style.fontFamily;
      if (at.style.fontSize && !el.style.fontSize) el.style.fontSize = at.style.fontSize;
      var f = at.style.getPropertyValue("--vw-f");
      if (f && !el.style.getPropertyValue("--vw-f")) el.style.setProperty("--vw-f", f);
    }
    return;
  }
  if (para.runProps && para.runProps.fontSize) el.style.fontSize = para.runProps.fontSize;
}

/*
 * Word sets no space between paragraphs of a style that asks for none (w:contextualSpacing, as
 * List Paragraph does), and docx-preview reads no such setting, so it is read here, from the
 * styles' XML that vwDrawWord has docx-preview keep. Then every part's XML is let go before the
 * document is drawn, so a long one is not held twice.
 */
function vwContextualSpacing(doc) {
  var styles = doc.stylesPart && doc.stylesPart._xmlDocument;
  var css = styles ? vwContextualCss(styles) : "";
  (doc.parts || []).forEach(function (part) { part._xmlDocument = null; });
  /* A second drawing of the same file needs the same rules, which are already in */
  if (!css || document.getElementById("vw-contextual")) return;
  var style = document.createElement("style");
  style.id = "vw-contextual";
  style.textContent = css;
  document.head.appendChild(style);
}

var VW_W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

function vwContextualCss(doc) {
  /* Most files set it nowhere, and walking each of a long template's styles costs the phone milliseconds */
  if (!doc.getElementsByTagNameNS(VW_W, "contextualSpacing").length) return "";
  var own = {}, basedOn = {}, done = {}, css = "";
  [].forEach.call(doc.getElementsByTagNameNS(VW_W, "style"), function (style) {
    if (style.getAttributeNS(VW_W, "type") !== "paragraph") return;
    var id = style.getAttributeNS(VW_W, "styleId");
    var base = style.getElementsByTagNameNS(VW_W, "basedOn")[0];
    if (base) basedOn[id] = base.getAttributeNS(VW_W, "val");
    var set = style.getElementsByTagNameNS(VW_W, "contextualSpacing")[0];
    if (set) own[id] = !/^(0|false|off)$/.test(set.getAttributeNS(VW_W, "val"));
  });
  function on(id, depth) {
    if (id in own) return own[id];
    return depth < 20 && id in basedOn ? on(basedOn[id], depth + 1) : false;
  }
  Object.keys(basedOn).concat(Object.keys(own)).forEach(function (id) {
    if (done[id] || !on(id, 0)) return;
    done[id] = true;
    /* docx-preview's class for the style, as its processStyleName makes it */
    var p = "p." + CSS.escape("docx_" + id.replace(/[ .]+/g, "-").replace(/[&]+/g, "and").toLowerCase());
    css += ".docx-wrapper " + p + " + " + p + " { margin-top: 0 !important; }\n" +
      ".docx-wrapper " + p + ":has(+ " + p + ") { margin-bottom: 0 !important; }\n";
  });
  return css;
}

/* The phone's fonts by the names Android gives them; its Roboto is one variable font, so its italic is an axis */
var VW_STAND_INS = {
  sans: [
    ["local('Roboto'), local('Roboto-Regular')", "100 900", "normal", ""],
    ["local('Roboto-Italic'), local('Roboto')", "100 900", "italic", "font-variation-settings: 'ital' 1; "]
  ],
  serif: [
    ["local('Noto Serif'), local('NotoSerif')", "400", "normal", ""],
    ["local('Noto Serif Bold'), local('NotoSerif-Bold')", "700", "normal", ""],
    ["local('Noto Serif Italic'), local('NotoSerif-Italic')", "400", "italic", ""],
    ["local('Noto Serif Bold Italic'), local('NotoSerif-BoldItalic')", "700", "italic", ""]
  ],
  mono: [["local('Cutive Mono Regular'), local('CutiveMono-Regular')", "400", "normal", ""]]
};

var VW_CYRILLIC = "U+0400-052F, U+1C80-1C8F, U+2DE0-2DFF, U+A640-A69F";

/* A face for each Word font in the table under its own name, Cyrillic after Latin so it wins there */
function vwStandInCss() {
  var css = "";
  function pct(x) { return (100 * x).toFixed(2) + "%"; }
  Object.keys(VW_WORD_FONTS).forEach(function (name) {
    var f = VW_WORD_FONTS[name];
    [[f[2], ""], [f[3], "unicode-range: " + VW_CYRILLIC + "; "]].forEach(function (script) {
      var size = script[0];
      VW_STAND_INS[f[1]].forEach(function (face) {
        css += "@font-face { font-family: \"" + name + "\"; src: " + face[0] + "; font-weight: " + face[1] +
          "; font-style: " + face[2] + "; " + face[3] + script[1] + "size-adjust: " + pct(size) +
          "; ascent-override: " + pct(f[4] / size) + "; descent-override: " + pct(f[5] / size) +
          "; line-gap-override: " + pct(f[6] / size) + "; }\n";
      });
    });
  });
  return css;
}

/* The stand-ins are Android's fonts; elsewhere a Word font is drawn as the system has it */
if (/Android/.test(navigator.userAgent)) {
  (function () {
    var style = document.createElement("style");
    style.textContent = vwStandInCss();
    document.head.appendChild(style);
  })();
}
