"use strict";

/*
 * OpenDocument Presentation (.odp / .otp), drawn as slides.
 *
 * An .odp is a zip holding content.xml and styles.xml. content.xml is the presentation
 * itself: one draw:page per slide, each a canvas of absolutely positioned frames. Every
 * frame carries its place and size in svg:x/y/width/height, and its look in a style
 * named by draw:style-name or text:style-name, which is where the fill, the font size
 * and the colour live. So the reader is mostly one job done twice: read the style, then
 * place the thing the style belongs to.
 *
 * What is drawn: page backgrounds, frames' fills and strokes, text (paragraphs, spans,
 * lists, line breaks), and pictures. What is not: embedded objects (charts, embedded
 * spreadsheets), animations, transitions, and 3D. A shape group is drawn flat, where
 * its members say they are.
 *
 * Sizes are the deck's own, in centimetres or points, turned into CSS px against 96 dpi:
 * a page laid out at 28 x 21 cm is 1058 x 794 px. Each slide is then zoomed to the width
 * of the screen, which is why this page declares width=device-width rather than living
 * at the WebView's 980 px default. Zoom and not transform, as docx.html explains: zoom
 * scales the layout, so a frame's place inside the slide is still the place it was put.
 */

var NS = {
  office: "urn:oasis:names:tc:opendocument:xmlns:office:1.0",
  style: "urn:oasis:names:tc:opendocument:xmlns:style:1.0",
  text: "urn:oasis:names:tc:opendocument:xmlns:text:1.0",
  draw: "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0",
  fo: "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0",
  svg: "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0",
  xlink: "http://www.w3.org/1999/xlink"
};

function byTag(root, ns, name) {
  return root.getElementsByTagNameNS(ns, name);
}

function attr(el, ns, name) {
  return el ? el.getAttributeNS(ns, name) : null;
}

/* A length as the format writes it (2cm, 18pt, 0.5in) in CSS px, or null. */
function px(value) {
  if (!value) return null;
  var m = /^(-?[0-9]*\.?[0-9]+)(cm|mm|in|pt|pc|px)$/.exec(String(value).trim());
  if (!m) return null;
  var n = parseFloat(m[1]);
  switch (m[2]) {
    case "cm": return n * 37.795276;
    case "mm": return n * 3.7795276;
    case "in": return n * 96;
    case "pt": return n * (4 / 3);
    case "pc": return n * 16;
    default: return n;
  }
}

/* Every named style in [doc], by style:name, with which family it belongs to. */
function collectStyles(doc, into) {
  var styles = byTag(doc, NS.style, "style");
  for (var i = 0; i < styles.length; i++) {
    var name = attr(styles[i], NS.style, "name");
    if (!name) continue;
    into[name] = { family: attr(styles[i], NS.style, "family"), el: styles[i] };
  }
  return into;
}

/*
 * A style's drawing properties: what fills a shape.
 *
 * draw:fill tells whether the fill is a colour, none or a bitmap; draw:fill-color is
 * that colour; the stroke pair draws the border. transparent means no fill.
 */
function graphic(styles, name) {
  var style = styles[name];
  var el = style && byTag(style.el, NS.style, "graphic-properties")[0];
  if (!el) return {};
  var fill = attr(el, NS.draw, "fill");
  return {
    fill: fill === "solid" ? attr(el, NS.draw, "fill-color") : null,
    stroke: attr(el, NS.draw, "stroke") === "solid" ? attr(el, NS.draw, "stroke-color") : null,
    strokeWidth: px(attr(el, NS.draw, "stroke-width")),
    padding: px(attr(el, NS.fo, "padding")),
    opacity: attr(el, NS.draw, "opacity")
  };
}

/*
 * A page's own background lives in a drawing-page style, not in a graphic one:
 * draw:style-name on draw:page names a style:drawing-page-properties, where draw:fill
 * and draw:fill-color say the colour the slide is painted. Without this a deck with a
 * dark master came out white, and its white title went with it.
 */
function pageFill(styles, name) {
  var style = styles[name];
  var el = style && byTag(style.el, NS.style, "drawing-page-properties")[0];
  if (!el) return null;
  return attr(el, NS.draw, "fill") === "solid" ? attr(el, NS.draw, "fill-color") : null;
}

/* A style's text properties, inherited down a chain of styles when asked for. */
function textOf(styles, name) {
  var style = styles[name];
  var el = style && byTag(style.el, NS.style, "text-properties")[0];
  if (!el) return {};
  return {
    size: px(attr(el, NS.fo, "font-size")),
    color: attr(el, NS.fo, "color"),
    weight: attr(el, NS.fo, "font-weight"),
    italic: attr(el, NS.fo, "font-style") === "italic",
    background: attr(el, NS.fo, "background-color")
  };
}

function paragraphOf(styles, name) {
  var style = styles[name];
  var el = style && byTag(style.el, NS.style, "paragraph-properties")[0];
  if (!el) return {};
  return {
    align: attr(el, NS.fo, "text-align"),
    marginLeft: px(attr(el, NS.fo, "margin-left")),
    marginTop: px(attr(el, NS.fo, "margin-top")),
    marginBottom: px(attr(el, NS.fo, "margin-bottom")),
    lineHeight: attr(el, NS.fo, "line-height")
  };
}

function paintText(el, props) {
  if (props.size) el.style.fontSize = props.size + "px";
  if (props.color) el.style.color = props.color;
  if (props.weight === "bold") el.style.fontWeight = "700";
  if (props.italic) el.style.fontStyle = "italic";
  if (props.background) el.style.backgroundColor = props.background;
}

function alignOf(value) {
  if (value === "center") return "center";
  if (value === "end" || value === "right") return "right";
  if (value === "justify") return "justify";
  return "left";
}

/* The text of a paragraph, with each span's own character style kept. */
function appendRuns(target, node, styles) {
  for (var n = node.firstChild; n; n = n.nextSibling) {
    if (n.nodeType === 3) {
      target.appendChild(document.createTextNode(n.nodeValue));
      continue;
    }
    if (n.nodeType !== 1) continue;
    var name = n.localName;
    if (name === "s") {
      var count = parseInt(attr(n, NS.text, "c") || "1", 10) || 1;
      target.appendChild(document.createTextNode(new Array(count + 1).join(" ")));
    } else if (name === "tab") {
      target.appendChild(document.createTextNode("\t"));
    } else if (name === "line-break") {
      target.appendChild(document.createElement("br"));
    } else if (name === "span") {
      var span = document.createElement("span");
      paintText(span, textOf(styles, attr(n, NS.text, "style-name")));
      appendRuns(span, n, styles);
      target.appendChild(span);
    } else if (name === "a") {
      // A link keeps its text; the target is not followed, as every page here does
      appendRuns(target, n, styles);
    } else {
      appendRuns(target, n, styles);
    }
  }
}

/*
 * The paragraphs of a text box. text:h is a heading and text:p a paragraph, and both
 * carry a paragraph style by name; a paragraph inside a list item is marked as one.
 */
function paragraphsOf(box, styles, base) {
  var out = [];
  var nodes = box.getElementsByTagNameNS(NS.text, "p");
  var heads = box.getElementsByTagNameNS(NS.text, "h");
  var all = [];
  var i;
  for (i = 0; i < nodes.length; i++) all.push(nodes[i]);
  for (i = 0; i < heads.length; i++) all.push(heads[i]);
  all.sort(function (a, b) {
    return a.compareDocumentPosition(b) & Node.DOCUMENT_POSITION_FOLLOWING ? -1 : 1;
  });

  for (i = 0; i < all.length; i++) {
    var node = all[i];
    var name = attr(node, NS.text, "style-name");
    var p = document.createElement("p");
    var para = paragraphOf(styles, name);
    var text = textOf(styles, name);
    // A paragraph style often carries only character properties; the box's are the base
    paintText(p, {
      size: text.size || base.size,
      color: text.color || base.color,
      weight: text.weight,
      italic: text.italic,
      background: text.background
    });
    p.style.textAlign = alignOf(para.align);
    if (para.marginLeft) p.style.marginLeft = para.marginLeft + "px";
    if (para.marginTop) p.style.marginTop = para.marginTop + "px";
    if (para.marginBottom) p.style.marginBottom = para.marginBottom + "px";
    if (para.lineHeight && para.lineHeight !== "normal") p.style.lineHeight = para.lineHeight;
    var inList = false;
    for (var up = node.parentNode; up && up !== box; up = up.parentNode) {
      if (up.localName === "list-item") { inList = true; break; }
    }
    if (inList) p.insertBefore(document.createTextNode("• "), p.firstChild);
    appendRuns(p, node, styles);
    out.push(p);
  }
  return out;
}

function slideButtonless(slide) {
  slide.setAttribute("data-odp-slide", "");
}

function renderShape(el, styles, images) {
  var name = el.localName;
  if (name === "g") return null;

  var box = {
    x: px(attr(el, NS.svg, "x")) || 0,
    y: px(attr(el, NS.svg, "y")) || 0,
    w: px(attr(el, NS.svg, "width")),
    h: px(attr(el, NS.svg, "height"))
  };
  var styleName = attr(el, NS.draw, "style-name");
  var look = graphic(styles, styleName);
  var shape = document.createElement("div");
  shape.className = "odp-shape";
  shape.style.left = box.x + "px";
  shape.style.top = box.y + "px";
  if (box.w) shape.style.width = box.w + "px";
  if (box.h) shape.style.height = box.h + "px";
  if (look.fill) shape.style.backgroundColor = look.fill;
  if (look.padding) shape.style.padding = look.padding + "px";
  if (look.stroke) {
    shape.style.border = (look.strokeWidth || 1) + "px solid " + look.stroke;
  }
  if (name === "ellipse") shape.style.borderRadius = "50%";
  if (name === "frame" || name === "caption") shape.style.overflow = "visible";

  var image = byTag(el, NS.draw, "image")[0];
  if (image) {
    var href = attr(image, NS.xlink, "href");
    var url = href && images[href];
    if (url) {
      var img = document.createElement("img");
      img.src = url;
      img.alt = "";
      shape.appendChild(img);
      return shape;
    }
  }

  var textBox = byTag(el, NS.draw, "text-box")[0];
  if (textBox) {
    var baseText = textOf(styles, attr(textBox, NS.text, "style-name"));
    var base = {
      size: baseText.size || 24,      // 18pt, the presentation default
      color: baseText.color || "#1E1B15"
    };
    var list = paragraphsOf(textBox, styles, base);
    for (var i = 0; i < list.length; i++) shape.appendChild(list[i]);
  }
  return shape;
}

/*
 * The slide's size, from the page layout its master page names.
 *
 * Not the first page layout in styles.xml: a deck keeps one layout per paper it might be
 * printed on, and a 16:9 deck's file lists an A4 portrait one before the 16:9 landscape
 * one it actually uses. The master page is what says which.
 */
function pageSize(stylesDoc, masterName) {
  var layoutName = null;
  var masters = byTag(stylesDoc, NS.style, "master-page");
  for (var m = 0; m < masters.length; m++) {
    if (attr(masters[m], NS.style, "name") !== masterName) continue;
    layoutName = attr(masters[m], NS.style, "page-layout-name");
  }
  var layouts = byTag(stylesDoc, NS.style, "page-layout");
  for (var i = 0; i < layouts.length; i++) {
    if (layoutName && attr(layouts[i], NS.style, "name") !== layoutName) continue;
    var props = byTag(layouts[i], NS.style, "page-layout-properties")[0];
    var w = props && px(attr(props, NS.fo, "page-width"));
    var h = props && px(attr(props, NS.fo, "page-height"));
    if (w && h) return { w: w, h: h };
  }
  var all = byTag(stylesDoc, NS.style, "page-layout-properties");
  for (var k = 0; k < all.length; k++) {
    var aw = px(attr(all[k], NS.fo, "page-width"));
    var ah = px(attr(all[k], NS.fo, "page-height"));
    if (aw && ah) return { w: aw, h: ah };
  }
  return { w: 1058, h: 794 };   // 28 x 21 cm, the 4:3 default
}

/* The drawing-page style a master page paints its slides with, if it paints one. */
function masterStyle(stylesDoc, masterName) {
  var masters = byTag(stylesDoc, NS.style, "master-page");
  for (var i = 0; i < masters.length; i++) {
    if (attr(masters[i], NS.style, "name") === masterName) {
      return attr(masters[i], NS.draw, "style-name");
    }
  }
  return null;
}

/* Pictures, as blob URLs by their zip path. */
function collectImages(zip) {
  var names = [];
  zip.forEach(function (path) {
    if (/^(Pictures|ObjectReplacements)\//i.test(path) && !/\/$/.test(path)) names.push(path);
  });
  return Promise.all(names.map(function (path) {
    return zip.file(path).async("blob").then(function (blob) {
      return { path: path, url: URL.createObjectURL(blob) };
    });
  })).then(function (list) {
    var map = {};
    for (var i = 0; i < list.length; i++) map[list[i].path] = list[i].url;
    return map;
  });
}

function readPresentation(bytes) {
  return JSZip.loadAsync(bytes).then(function (zip) {
    var contentFile = zip.file("content.xml");
    if (!contentFile) throw new Error("This file has no content.xml");
    return Promise.all([
      contentFile.async("string"),
      zip.file("styles.xml") ? zip.file("styles.xml").async("string") : Promise.resolve(""),
      collectImages(zip)
    ]);
  }).then(function (parts) {
    var parser = new DOMParser();
    var content = parser.parseFromString(parts[0], "text/xml");
    var stylesDoc = parts[1] ? parser.parseFromString(parts[1], "text/xml") : content;
    var styles = collectStyles(content, {});
    collectStyles(stylesDoc, styles);
    var pages = byTag(content, NS.draw, "page");
    var firstMaster = pages.length ? attr(pages[0], NS.draw, "master-page-name") : null;
    var size = pageSize(stylesDoc, firstMaster);
    var deck = document.getElementById("deck");
    if (!pages.length) throw new Error("This presentation has no slides");

    var slides = [];
    for (var i = 0; i < pages.length; i++) {
      var page = pages[i];
      var slide = document.createElement("div");
      slide.className = "odp-slide";
      slide.style.width = size.w + "px";
      slide.style.height = size.h + "px";
      // A page may paint its own background; when it does not, its master page's is the
      // one the deck is written against
      var background = pageFill(styles, attr(page, NS.draw, "style-name")) ||
        pageFill(styles, masterStyle(stylesDoc, attr(page, NS.draw, "master-page-name")));
      if (background) slide.style.backgroundColor = background;
      slideButtonless(slide);

      for (var child = page.firstElementChild; child; child = child.nextElementSibling) {
        if (child.namespaceURI !== NS.draw) continue;
        var shape = renderShape(child, styles, parts[2]);
        if (shape) slide.appendChild(shape);
      }
      deck.appendChild(slide);
      slides.push(slide);
    }
    fit(slides, size.w);
    return slides;
  });
}

/*
 * Every slide zoomed to the width of the deck. One scale for all of them, since they
 * share a page size; a deck whose pages differ in size is rare and would need a scale
 * each, which is a line to add when one turns up.
 */
var fitted = { slides: [], width: 0 };

function fit(slides, width) {
  fitted.slides = slides || fitted.slides;
  fitted.width = width || fitted.width;
  var deck = document.getElementById("deck");
  var room = deck.clientWidth - 16;      // its padding either side
  if (!fitted.width || !(room > 0)) return;
  var zoom = room / fitted.width;
  for (var i = 0; i < fitted.slides.length; i++) {
    fitted.slides[i].style.zoom = String(zoom);
  }
}

addEventListener("resize", function () { fit(); });

vwFetchDoc("buffer")
  .then(function (buf) {
    return readPresentation(new Uint8Array(buf));
  })
  .then(function () {
    vwStatusDone();
  })
  .catch(function (e) {
    vwError("Could not open this presentation", String(e));
  });
