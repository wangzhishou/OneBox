"use strict";

/*
 * PowerPoint 97-2003 (.ppt), as text.
 *
 * A .ppt keeps its slides in a binary "PowerPoint Document" stream inside the same
 * compound file a .doc uses, so the container reader prose-doc.js already has is reused
 * here for free. Inside the stream everything is records: a four-byte header (version
 * and instance, then a type) and a four-byte length, then the body. Containers have
 * version 15 and a body of more records, which is why walking the stream linearly still
 * sees every record in order, whatever its depth.
 *
 * The text lives in two atoms: TextCharsAtom (4000) holds UTF-16, TextBytesAtom (4008)
 * holds single-byte characters in the document's code page. Both are found mostly inside
 * SlideListWithText (4080), which is the outline: each slide's text is preceded by a
 * SlidePersistAtom (1011), so that one container is both the text and the slide breaks.
 *
 * What this cannot do is draw the slides. Their shapes, their positions and their
 * formatting are Escher drawing records, a second format again, and a reader for it is
 * a project of its own. So this page shows the deck's text, slide by slide, and says so.
 */

var PPT = {
  slideList: 4080,       // 0x0FF0, SlideListWithText
  slidePersist: 1011,    // 0x03F3, SlidePersistAtom
  textChars: 4000,       // 0x0FA0, TextCharsAtom (UTF-16LE)
  textBytes: 4008        // 0x0FA8, TextBytesAtom (single byte)
};

function u16(bytes, at) { return bytes[at] | (bytes[at + 1] << 8); }
function u32(bytes, at) {
  return (bytes[at] | (bytes[at + 1] << 8) | (bytes[at + 2] << 16)) + bytes[at + 3] * 0x1000000;
}

/*
 * Every record between [from] and [to], in the order they are stored, container bodies
 * included: a record whose version nibble is 15 holds more records, and the walk has to
 * step into it, because a container's length spans its children and a walk that skipped
 * it would step over them. A length that would run past the end stops the walk instead
 * of reading past it: a damaged file should give the text it can, not an exception.
 */
function walkRecords(bytes, from, to, visit) {
  var at = from;
  while (at + 8 <= to) {
    var version = u16(bytes, at) & 0xF;
    var type = u16(bytes, at + 2);
    var length = u32(bytes, at + 4);
    var body = at + 8;
    if (body + length > to) break;
    visit(type, body, length);
    if (version === 0xF) walkRecords(bytes, body, body + length, visit);
    at = body + length;
  }
}

function decodeChars(bytes, from, length) {
  var parts = [];
  for (var i = 0; i + 1 < length; i += 2) {
    parts.push(String.fromCharCode(bytes[from + i] | (bytes[from + i + 1] << 8)));
  }
  return parts.join("");
}

function decodeBytes(bytes, from, length) {
  var slice = bytes.subarray(from, from + length);
  try {
    return new TextDecoder("windows-1252").decode(slice);
  } catch (e) {
    var out = "";
    for (var i = 0; i < slice.length; i++) out += String.fromCharCode(slice[i]);
    return out;
  }
}

/*
 * The outline as slides of paragraphs. Text atoms hold a paragraph per \r (and a soft
 * break per vertical tab); a SlidePersistAtom opens a new slide. A deck whose outline
 * is empty, or was never written, falls back to one slide holding every text atom in
 * the stream in order.
 */
function outlineOf(stream) {
  var slides = [];
  var current = null;

  function pushText(text) {
    if (!current) {
      current = [];
      slides.push(current);
    }
    var parts = text.split(/[\r\u000B]/);
    for (var i = 0; i < parts.length; i++) {
      var line = parts[i].replace(/\u0000/g, "").trim();
      if (line) current.push(line);
    }
  }

  var found = false;
  walkRecords(stream, 0, stream.length, function (type, body, length) {
    if (type !== PPT.slideList) return;
    found = true;
    walkRecords(stream, body, body + length, function (inner, innerBody, innerLength) {
      if (inner === PPT.slidePersist) {
        current = [];
        slides.push(current);
      } else if (inner === PPT.textChars) {
        pushText(decodeChars(stream, innerBody, innerLength));
      } else if (inner === PPT.textBytes) {
        pushText(decodeBytes(stream, innerBody, innerLength));
      }
    });
  });

  if (!found) {
    current = [];
    slides.push(current);
    walkRecords(stream, 0, stream.length, function (type, body, length) {
      if (type === PPT.textChars) pushText(decodeChars(stream, body, length));
      else if (type === PPT.textBytes) pushText(decodeBytes(stream, body, length));
    });
  }

  return slides.filter(function (lines) { return lines.length > 0; });
}

function renderSlides(slides) {
  var deck = document.getElementById("deck");
  var note = document.createElement("div");
  note.className = "ppt-note";
  var title = document.createElement("strong");
  title.textContent = "Legacy PowerPoint file (.ppt)";
  note.appendChild(title);
  note.appendChild(document.createTextNode(
    "This format stores its slides as binary drawing records, which this viewer does not " +
    "draw. The text of the deck is shown below, slide by slide. Re-save it as .pptx to see " +
    "the slides themselves."
  ));
  deck.appendChild(note);

  for (var i = 0; i < slides.length; i++) {
    var card = document.createElement("section");
    card.className = "ppt-slide";
    var num = document.createElement("div");
    num.className = "ppt-num";
    num.textContent = "SLIDE " + (i + 1);
    card.appendChild(num);
    for (var j = 0; j < slides[i].length; j++) {
      var el = document.createElement(j === 0 ? "div" : "p");
      el.className = j === 0 ? "ppt-title" : "ppt-line";
      el.textContent = slides[i][j];
      card.appendChild(el);
    }
    deck.appendChild(card);
  }
}

/* A .ppt that is really a zip is a .pptx under the wrong name; let the slide page try. */
function looksZipped(bytes) {
  return bytes.length > 2 && bytes[0] === 0x50 && bytes[1] === 0x4B;
}

vwFetchDoc("buffer")
  .then(function (buf) {
    var bytes = new Uint8Array(buf);
    if (looksZipped(bytes)) {
      location.replace(
        "pptx.html?name=" + encodeURIComponent(vwName) + "&ext=pptx&length=" + bytes.length
      );
      return null;
    }
    var file = docCompound(bytes);
    var stream = file.stream("PowerPoint Document");
    if (!stream || !stream.length) throw new Error("This file has no presentation stream");
    var slides = outlineOf(stream);
    if (!slides.length) throw new Error("No text could be read from this presentation");
    renderSlides(slides);
    return slides;
  })
  .then(function (slides) {
    if (slides) vwStatusDone();
  })
  .catch(function (e) {
    vwError("Could not open this presentation", String(e));
  });
