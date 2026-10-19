"use strict";

/*
 * Word 97-2003, the binary .doc, read into the pages prose-draw.js draws. Discussion
 * #27 and the README's oldest caveat.
 *
 * A .doc is a small filesystem (the "compound file": a FAT and a directory of streams)
 * holding three streams that matter. WordDocument starts with the FIB, a table of
 * offsets into the other two, and holds the text. The table stream (0Table or 1Table,
 * the FIB says which) holds everything about the text: which bytes of WordDocument are
 * text and in what order (the "piece table", since a fast-saved file keeps its text in
 * the order it was typed and not the order it is read), the character and paragraph
 * formatting in 512-byte pages keyed by file offset, the styles, the lists, and where
 * the footnotes and headers are. Data holds the pictures.
 *
 * Positions come in two kinds and every bug in a reader of this format is confusing
 * them. A CP is a character's index in the text as read; an FC is a byte offset in
 * WordDocument. The piece table maps one to the other, and the formatting pages are
 * keyed by FC, so to format a character its CP is turned into an FC through the piece
 * it lies in. Nothing here holds an FC longer than it must.
 *
 * Formatting is a run of "sprms", each a two-byte code and an operand, applied in order
 * over a base: a paragraph is its style's properties then its own sprms, a character is
 * its paragraph style's then its character style's then its own. The whole reader is
 * the plumbing to find the right sprms for each character and paragraph, and the two
 * functions that apply them.
 *
 * What is drawn: text in any script, character and paragraph formatting, headings,
 * lists with their numbers counted the way Word counts them, tables with merged cells,
 * borders and shading, pictures whether in the text or floating beside it, text boxes,
 * footnotes and endnotes, headers and footers, page size and margins, page and section
 * breaks. Word 6 and Word 95 files, which format their text with a different set of
 * codes, are read as text with their paragraphs. What is not: drawn shapes without a
 * picture, comments, tracked changes (the text is shown as last saved), columns, and
 * a document protected by a password, which is refused.
 */

/* ------------------------------------------------------------------------------------
 * Bytes
 * ---------------------------------------------------------------------------------- */

function docU8(b, at) { return b[at]; }
function docU16(b, at) { return b[at] | (b[at + 1] << 8); }
function docI16(b, at) { var v = docU16(b, at); return v & 0x8000 ? v - 0x10000 : v; }
function docU32(b, at) { return (b[at] | (b[at + 1] << 8) | (b[at + 2] << 16)) + b[at + 3] * 0x1000000; }
function docI32(b, at) { return b[at] | (b[at + 1] << 8) | (b[at + 2] << 16) | (b[at + 3] << 24); }

function docFail(why) {
  throw new Error(why);
}

/* ------------------------------------------------------------------------------------
 * The compound file
 * ---------------------------------------------------------------------------------- */

/*
 * Reads the streams out of a compound file: the FAT from the header's list of FAT
 * sectors, the directory from its chain, and each stream from its own chain, or from
 * the mini stream when it is small.
 *
 * Each of the file's numbers is held to what a file of its length could mean: no more
 * FAT sectors than it has sectors, no stream longer than it is, and no chain through
 * the same sector twice, since a damaged FAT can point at itself for ever. Believed,
 * an eight-kilobyte file held the reader for seconds, and a directory that led back
 * to itself was read sixty-five thousand times over into hundreds of megabytes.
 */
function docCompound(bytes) {
  if (bytes.length < 512 || bytes[0] !== 0xD0 || bytes[1] !== 0xCF || bytes[2] !== 0x11 || bytes[3] !== 0xE0) {
    docFail("This is not a Word document.");
  }
  var sectorSize = 1 << docU16(bytes, 0x1E);
  var miniSize = 1 << docU16(bytes, 0x20);
  var fatCount = docU32(bytes, 0x2C);
  var firstDir = docU32(bytes, 0x30);
  var cutoff = docU32(bytes, 0x38);
  var firstMiniFat = docU32(bytes, 0x3C);
  var firstDifat = docU32(bytes, 0x44);
  // The format's sectors are 512 bytes, or 4,096 in its fourth version, and no other size
  if ((sectorSize !== 512 && sectorSize !== 4096) || miniSize > sectorSize) docFail("This Word document is damaged.");
  var perSector = sectorSize / 4;
  var sectorCount = Math.max(0, Math.floor((bytes.length - sectorSize) / sectorSize));
  var room = sectorCount * sectorSize;

  function sector(n) {
    var at = (n + 1) * sectorSize;
    return at + sectorSize <= bytes.length ? at : -1;
  }

  // The FAT, from the sector list in the header and in the DIFAT chain after it
  fatCount = Math.min(fatCount, sectorCount);
  var fatSectors = [];
  var i;
  for (i = 0; i < 109 && fatSectors.length < fatCount; i++) fatSectors.push(docU32(bytes, 0x4C + i * 4));
  var difat = firstDifat;
  var walked = new Uint8Array(sectorCount);
  while (difat < sectorCount && !walked[difat] && fatSectors.length < fatCount) {
    walked[difat] = 1;
    var at = sector(difat);
    if (at < 0) break;
    for (i = 0; i < perSector - 1 && fatSectors.length < fatCount; i++) fatSectors.push(docU32(bytes, at + i * 4));
    difat = docU32(bytes, at + (perSector - 1) * 4);
  }
  var fat = new Uint32Array(sectorCount + 1);
  fat.fill(0xFFFFFFFE);
  for (i = 0; i < fatSectors.length; i++) {
    var fs = sector(fatSectors[i]);
    if (fs < 0) continue;
    for (var j = 0; j < perSector; j++) {
      var n = i * perSector + j;
      if (n <= sectorCount) fat[n] = docU32(bytes, fs + j * 4);
    }
  }

  // A chain ends at its end mark, at a sector the table does not reach, or at a sector
  // it has already passed through
  function chain(start, table, limit) {
    var out = [];
    var seen = new Uint8Array(table.length);
    var n = start;
    while (n < table.length && !seen[n] && out.length < limit) {
      seen[n] = 1;
      out.push(n);
      n = table[n];
    }
    return out;
  }

  function readChain(start, size) {
    var out = new Uint8Array(size);
    var sectors = chain(start, fat, Math.ceil(size / sectorSize) + 1);
    var done = 0;
    for (var s = 0; s < sectors.length && done < size; s++) {
      var at = sector(sectors[s]);
      if (at < 0) break;
      var take = Math.min(sectorSize, size - done);
      out.set(bytes.subarray(at, at + take), done);
      done += take;
    }
    return out;
  }

  // The directory: 128-byte entries, the root first
  var dirSectors = chain(firstDir, fat, sectorCount);
  var entries = [];
  for (i = 0; i < dirSectors.length; i++) {
    var ds = sector(dirSectors[i]);
    if (ds < 0) break;
    for (var e = 0; e + 128 <= sectorSize; e += 128) {
      var at2 = ds + e;
      var nameLen = docU16(bytes, at2 + 64);
      var type = bytes[at2 + 66];
      if (!type || nameLen < 2 || nameLen > 64) continue;
      var name = "";
      for (var c = 0; c < nameLen - 2; c += 2) name += String.fromCharCode(docU16(bytes, at2 + c));
      entries.push({ name: name, type: type, start: docU32(bytes, at2 + 116), size: docU32(bytes, at2 + 120) });
    }
  }
  if (!entries.length) docFail("This Word document is damaged.");

  // Small streams live inside the root entry's stream, in mini sectors
  var root = entries[0];
  var mini = null;
  var miniFat = null;
  function miniStream() {
    if (mini) return;
    mini = readChain(root.start, Math.min(root.size, room));
    var raw = readChain(firstMiniFat, Math.ceil(mini.length / miniSize) * 4);
    miniFat = new Uint32Array(raw.length / 4);
    for (var k = 0; k < miniFat.length; k++) miniFat[k] = docU32(raw, k * 4);
  }

  return {
    stream: function (name) {
      for (var k = 1; k < entries.length; k++) {
        if (entries[k].type !== 2 || entries[k].name !== name) continue;
        var s = entries[k];
        if (s.size > 256 * 1024 * 1024) docFail("This Word document is too large to open here.");
        if (s.size >= cutoff) return readChain(s.start, Math.min(s.size, room));
        miniStream();
        var size = Math.min(s.size, mini.length);
        var out = new Uint8Array(size);
        var sectors = chain(s.start, miniFat, Math.ceil(size / miniSize) + 1);
        var done = 0;
        for (var m = 0; m < sectors.length && done < size; m++) {
          var at = sectors[m] * miniSize;
          var take = Math.min(miniSize, size - done);
          if (at + take > mini.length) break;
          out.set(mini.subarray(at, at + take), done);
          done += take;
        }
        return out;
      }
      return null;
    }
  };
}

/* ------------------------------------------------------------------------------------
 * Sprms
 * ---------------------------------------------------------------------------------- */

/*
 * Walks a run of sprms, calling fn(code, operandOffset, operandLength, bytes) for each.
 * The operand's length is in the code's top three bits, except for the variable ones,
 * where the first operand byte says, and two table sprms where the first two do, and
 * count one more than the bytes that follow them.
 */
function docSprms(b, at, end, fn) {
  while (at + 2 <= end) {
    var code = docU16(b, at);
    var spra = code >> 13;
    var len;
    at += 2;
    if (spra === 0 || spra === 1) len = 1;
    else if (spra === 2 || spra === 4 || spra === 5) len = 2;
    else if (spra === 3) len = 4;
    else if (spra === 7) len = 3;
    else if (code === 0xD608 || code === 0xD606) { len = docU16(b, at) - 1; at += 2; }   // counts itself
    else { len = b[at]; at += 1; }
    if (at + len > end) break;
    fn(code, at, len, b);
    at += len;
  }
}

/* The sixteen colours the format numbers. 0 is "automatic", which is the ink. */
var DOC_ICO = [
  null, "#000000", "#0000ff", "#00ffff", "#00ff00", "#ff00ff", "#ff0000", "#ffff00",
  "#ffffff", "#000080", "#008080", "#008000", "#800080", "#800000", "#808000", "#808080",
  "#c0c0c0"
];

/* A COLORREF: red, green, blue and a byte that says "automatic" when it is 0xFF. */
function docCv(b, at) {
  return b[at + 3] === 0xFF ? null : vwProseRgb(b[at], b[at + 1], b[at + 2]);
}

var DOC_BRC_STYLES = {
  1: "solid", 2: "solid", 3: "double", 5: "solid", 6: "dotted", 7: "dashed", 8: "dashed",
  9: "dotted", 10: "double", 11: "double", 12: "double", 13: "double", 14: "double",
  15: "double", 16: "double", 17: "double", 18: "double", 19: "double", 20: "solid",
  21: "double", 22: "dashed", 23: "ridge", 24: "groove", 25: "inset", 26: "outset"
};

/* A border in the old four-byte form: width in eighths of a point, kind, colour. */
function docBrc80(b, at) {
  var width = b[at];
  var kind = b[at + 1];
  if (!kind || width === 0xFF) return { width: 0 };
  return { width: width / 8, style: DOC_BRC_STYLES[kind] || "solid", color: DOC_ICO[b[at + 2] & 31] || "#000000" };
}

/* And the eight-byte form, colour first. */
function docBrc(b, at) {
  var width = b[at + 4];
  var kind = b[at + 5];
  if (!kind || kind === 0xFF) return { width: 0 };
  return { width: width / 8, style: DOC_BRC_STYLES[kind] || "solid", color: docCv(b, at) || "#000000" };
}

/* Shading: the old two-byte form, ten bits of colour indexes and a pattern. */
function docShd80(b, at) {
  var v = docU16(b, at);
  var fore = DOC_ICO[v & 31];
  var back = DOC_ICO[(v >> 5) & 31];
  var pattern = v >> 10;
  if (pattern === 0 || v === 0xFFFF) return back || null;
  // A solid pattern is the foreground colour; a hatched one is read as its ground
  return pattern === 1 ? (fore || "#000000") : (back || null);
}

/* And the ten-byte form: two colours and a pattern. */
function docShd(b, at) {
  var fore = docCv(b, at);
  var back = docCv(b, at + 4);
  var pattern = docU16(b, at + 8);
  if (pattern === 1) return fore || "#000000";
  if (pattern >= 2 && pattern <= 13 && fore && back) return docMix(back, fore, [5, 10, 20, 25, 30, 40, 50, 60, 70, 75, 80, 90][pattern - 2] / 100);
  return back;
}

function docMix(a, b, t) {
  function ch(c, i) { return parseInt(c.substr(1 + i * 2, 2), 16); }
  var out = [];
  for (var i = 0; i < 3; i++) out.push(Math.round(ch(a, i) * (1 - t) + ch(b, i) * t));
  return vwProseRgb(out[0], out[1], out[2]);
}

var DOC_KUL = { 0: false, 1: true, 2: true, 3: "double", 4: "dotted", 6: "solid", 7: "dashed", 9: "dashed", 10: "dotted", 11: "wavy", 20: "dotted", 23: "dashed", 27: "wavy", 39: "dashed", 43: "wavy" };

/*
 * Applies a character sprm to chp. base is the formatting the sprm's toggles are
 * relative to: 128 means "as the style has it" and 129 the opposite, and the style is
 * whatever base holds when the paragraph's or character style's sprms have been
 * applied and the character's own begin.
 */
function docApplyChp(chp, base, code, at, len, b) {
  function toggle(key) {
    var v = b[at];
    chp[key] = v === 1 ? true : v === 0 ? false : v === 129 ? !base[key] : !!base[key];
  }
  switch (code) {
    case 0x0835: toggle("bold"); break;
    case 0x0836: toggle("italic"); break;
    case 0x0837: toggle("strike"); break;
    case 0x083A: toggle("smallCaps"); break;
    case 0x083B: toggle("caps"); break;
    case 0x083C: toggle("hidden"); break;
    case 0x2A53: chp.strike = b[at] ? "double" : false; break;
    case 0x0855: chp.special = !!b[at]; break;
    case 0x0806: chp.data = !!b[at]; break;
    case 0x080A: chp.ole = !!b[at]; break;
    case 0x2A3E: chp.underline = DOC_KUL[b[at]] !== undefined ? DOC_KUL[b[at]] : true; break;
    case 0x2A42: chp.color = DOC_ICO[b[at] & 31] || null; break;
    case 0x6870: chp.color = docCv(b, at); break;
    case 0x4A43: chp.size = docU16(b, at) / 2; break;
    case 0x4845: chp.rise = docI16(b, at); break;
    case 0x2A48: chp.position = b[at] === 1 ? "super" : b[at] === 2 ? "sub" : null; break;
    case 0x4A4F: chp.ftc = docU16(b, at); break;
    case 0x4A50: chp.ftcFE = docU16(b, at); break;
    case 0x4A51: chp.ftcOther = docU16(b, at); break;
    case 0x8840: chp.spacing = docI16(b, at) / 20; break;
    case 0x2A0C: chp.highlight = DOC_ICO[b[at] & 31] || null; break;
    case 0x4866: chp.background = docShd80(b, at); break;
    case 0xCA71: if (len >= 10) chp.background = docShd(b, at); break;
    case 0x6A03: chp.picture = docU32(b, at); break;
    case 0x6A09: chp.symbolFont = docU16(b, at); chp.symbol = docU16(b, at + 2); break;
    case 0x4A30: chp.istd = docU16(b, at); break;
    case 0x2A33: break;   // plain: handled by the caller, which resets to the style
    case 0x0800: chp.deleted = !!b[at]; break;
    case 0x0802: chp.fieldHidden = !!b[at]; break;
  }
}

/*
 * Applies a paragraph sprm to pap. Table sprms arrive here too, since a row's
 * properties are the sprms of the paragraph that ends it, and go into pap.tap.
 */
function docApplyPap(pap, code, at, len, b) {
  switch (code) {
    case 0x4600: pap.istd = docU16(b, at); break;
    case 0x2403: pap.align = ["left", "center", "right", "justify"][b[at]] || null; break;
    case 0x2461: pap.align = ["left", "center", "right", "justify", "justify", "justify"][b[at]] || null; break;
    case 0x2407: pap.pageBreakBefore = !!b[at]; break;
    case 0x260A: pap.ilvl = b[at]; break;
    case 0x460B: pap.ilfo = docU16(b, at); break;
    case 0x840E: case 0x845D: pap.right = docI16(b, at) / 20; break;
    case 0x840F: case 0x845E: pap.left = docI16(b, at) / 20; break;
    case 0x8411: case 0x8460: pap.first = docI16(b, at) / 20; break;
    case 0x6412:
      var dya = docI16(b, at);
      pap.line = null; pap.lineExact = null; pap.lineAtLeast = null;
      if (docU16(b, at + 2)) pap.line = dya / 240;
      else if (dya < 0) pap.lineExact = -dya / 20;
      else if (dya > 0) pap.lineAtLeast = dya / 20;
      break;
    case 0xA413: pap.before = docU16(b, at) / 20; break;
    case 0xA414: pap.after = docU16(b, at) / 20; break;
    case 0x2416: pap.inTable = !!b[at]; break;
    case 0x2417: pap.rowEnd = !!b[at]; break;
    case 0x6649: pap.itap = docI32(b, at); break;
    case 0x244B: pap.innerCell = !!b[at]; break;
    case 0x244C: pap.innerRowEnd = !!b[at]; break;
    case 0x6424: pap.borderTop = docBrc80(b, at); break;
    case 0x6425: pap.borderLeft = docBrc80(b, at); break;
    case 0x6426: pap.borderBottom = docBrc80(b, at); break;
    case 0x6427: pap.borderRight = docBrc80(b, at); break;
    case 0xC64E: if (len >= 8) pap.borderTop = docBrc(b, at); break;
    case 0xC64F: if (len >= 8) pap.borderLeft = docBrc(b, at); break;
    case 0xC650: if (len >= 8) pap.borderBottom = docBrc(b, at); break;
    case 0xC651: if (len >= 8) pap.borderRight = docBrc(b, at); break;
    case 0x442D: pap.background = docShd80(b, at); break;
    case 0xC64D: if (len >= 10) pap.background = docShd(b, at); break;
    case 0x2640: pap.outline = b[at]; break;
    case 0x2441: pap.rtl = !!b[at]; break;
    case 0x246D: pap.contextual = !!b[at]; break;
    default:
      if ((code >> 10 & 7) === 5) docApplyTap(pap, code, at, len, b);
  }
}

/*
 * The table properties of a row, from the sprms on its end mark. sprmTDefTable is the
 * one that matters: the column edges and a description of each cell. The rest adjust
 * cells by index, and are applied over whatever the definition said.
 */
function docApplyTap(pap, code, at, len, b) {
  // 108 twips either side of a cell's text is Word's own default, used when a row
  // says nothing, which a row Word itself wrote often does
  var tap = pap.tap || (pap.tap = { edges: [], cells: [], left: 0, gap: 5.4, pad: [null, null, null, null] });
  var i, n, cell;
  switch (code) {
    case 0x5400: case 0x548A: tap.align = ["left", "center", "right"][docU16(b, at)] || null; break;
    case 0x9601: tap.left = docI16(b, at) / 20; break;
    case 0x9602: tap.gap = docI16(b, at) / 20; break;
    case 0x9407: tap.height = docI16(b, at) / 20; break;
    case 0x3404: tap.header = !!b[at]; break;
    case 0xD608: case 0xD606:
      n = b[at];
      if (n > 63 || len < 1 + (n + 1) * 2) break;
      tap.edges = [];
      tap.cells = [];
      for (i = 0; i <= n; i++) tap.edges.push(docI16(b, at + 1 + i * 2) / 20);
      var tcAt = at + 1 + (n + 1) * 2;
      var tcSize = (len - (tcAt - at)) >= n * 20 ? 20 : 10;
      for (i = 0; i < n; i++) {
        var t = tcAt + i * tcSize;
        if (t + tcSize > at + len) { tap.cells.push({}); continue; }
        var grf = docU16(b, t);
        cell = {
          mergedAcross: !!(grf & 2), mergesAcross: !!(grf & 1),
          vertMerge: !!(grf & 0x20), vertRestart: !!(grf & 0x40),
          valign: ["top", "middle", "bottom"][(grf >> 7) & 3] || "top"
        };
        var brcAt = tcSize === 20 ? t + 4 : t + 2;
        cell.borderTop = docBrc80(b, brcAt);
        cell.borderLeft = docBrc80(b, brcAt + 4);
        cell.borderBottom = docBrc80(b, brcAt + 8);
        cell.borderRight = docBrc80(b, brcAt + 12);
        tap.cells.push(cell);
      }
      break;
    case 0xD609:
      for (i = 0; i * 2 + 1 < len && i < tap.cells.length; i++) tap.cells[i].background = docShd80(b, at + i * 2);
      break;
    case 0xD612: case 0xD616: case 0xD60C: case 0xD670: case 0xD671: case 0xD672:
      var from = (code === 0xD612 || code === 0xD670) ? 0 : (code === 0xD616 || code === 0xD671) ? 22 : 44;
      for (i = 0; i * 10 + 9 < len && from + i < tap.cells.length; i++) tap.cells[from + i].background = docShd(b, at + i * 10);
      break;
    case 0xD62D: case 0xD62E:
      if (len >= 4) for (i = b[at]; i < b[at + 1] && i < tap.cells.length; i++) tap.cells[i].background = docShd80(b, at + 2);
      break;
    case 0xD62B:
      if (len >= 2 && b[at] < tap.cells.length) {
        tap.cells[b[at]].vertMerge = !!(b[at + 1] & 1);
        tap.cells[b[at]].vertRestart = !!(b[at + 1] & 2);
      }
      break;
    case 0xD62C:
      if (len >= 3) for (i = b[at]; i < b[at + 1] && i < tap.cells.length; i++) {
        tap.cells[i].valign = ["top", "middle", "bottom"][b[at + 2]] || "top";
      }
      break;
    case 0xD605:
      if (len >= 24) tap.borders = [docBrc80(b, at), docBrc80(b, at + 4), docBrc80(b, at + 8), docBrc80(b, at + 12), docBrc80(b, at + 16), docBrc80(b, at + 20)];
      break;
    case 0xD613:
      if (len >= 48) tap.borders = [docBrc(b, at), docBrc(b, at + 8), docBrc(b, at + 16), docBrc(b, at + 24), docBrc(b, at + 32), docBrc(b, at + 40)];
      break;
    case 0xD620:
      // itcFirst, itcLim, which sides, then a border for them
      if (len >= 7) for (i = b[at]; i < b[at + 1] && i < tap.cells.length; i++) {
        var sides = b[at + 2];
        var brc = docBrc80(b, at + 3);
        if (sides & 1) tap.cells[i].borderTop = brc;
        if (sides & 2) tap.cells[i].borderLeft = brc;
        if (sides & 4) tap.cells[i].borderBottom = brc;
        if (sides & 8) tap.cells[i].borderRight = brc;
      }
      break;
    case 0xD62F:
      if (len >= 11) for (i = b[at]; i < b[at + 1] && i < tap.cells.length; i++) {
        var which = b[at + 2];
        var line = docBrc(b, at + 3);
        if (which & 1) tap.cells[i].borderTop = line;
        if (which & 2) tap.cells[i].borderLeft = line;
        if (which & 4) tap.cells[i].borderBottom = line;
        if (which & 8) tap.cells[i].borderRight = line;
      }
      break;
    case 0xD634: case 0xD632:
      // itcFirst, itcLim, which sides, unit, width: the space inside the cells
      if (len >= 6 && b[at + 3] === 3) {
        var pad = docU16(b, at + 4) / 20;
        var mask = b[at + 2];
        var lo = code === 0xD634 ? 0 : b[at];
        var hi = code === 0xD634 ? 64 : b[at + 1];
        for (i = lo; i < hi && i < 64; i++) {
          if (code === 0xD634) {
            if (mask & 1) tap.pad[0] = pad;
            if (mask & 2) tap.pad[3] = pad;
            if (mask & 4) tap.pad[2] = pad;
            if (mask & 8) tap.pad[1] = pad;
            break;
          }
          cell = tap.cells[i];
          if (!cell) continue;
          if (mask & 1) cell.padTop = pad;
          if (mask & 2) cell.padLeft = pad;
          if (mask & 4) cell.padBottom = pad;
          if (mask & 8) cell.padRight = pad;
        }
      }
      break;
  }
}

/* ------------------------------------------------------------------------------------
 * The document
 * ---------------------------------------------------------------------------------- */

var DOC_LID_CODEPAGES = {
  0x0419: "windows-1251", 0x0422: "windows-1251", 0x0423: "windows-1251", 0x0402: "windows-1251",
  0x042F: "windows-1251", 0x0408: "windows-1253", 0x041F: "windows-1254", 0x040D: "windows-1255",
  0x0401: "windows-1256", 0x0405: "windows-1250", 0x040E: "windows-1250", 0x0415: "windows-1250",
  0x041B: "windows-1250", 0x0424: "windows-1250", 0x041A: "windows-1250", 0x0418: "windows-1250",
  0x0425: "windows-1257", 0x0426: "windows-1257", 0x0427: "windows-1257", 0x042A: "windows-1258",
  0x041E: "windows-874", 0x0411: "shift_jis", 0x0412: "euc-kr", 0x0804: "gbk", 0x0404: "big5"
};

/*
 * Opens the file: the streams, the FIB, and the text as one string in reading order
 * together with the pieces that say where each character's bytes are.
 */
function docOpen(bytes) {
  var file = docCompound(bytes);
  var main = file.stream("WordDocument");
  if (!main || main.length < 0x200) docFail("This is not a Word document: it has no text inside it.");
  if (docU16(main, 0) !== 0xA5EC && docU16(main, 0) !== 0xA5DC) docFail("This is not a Word document.");

  var nFib = docU16(main, 2);
  var flags = docU16(main, 0x0A);
  if (flags & 0x100) {
    docFail("This document is protected by a password, and Gander cannot open password-protected Word documents.");
  }
  var old = nFib < 0xC1;
  var doc = {
    main: main,
    table: file.stream(flags & 0x200 ? "1Table" : "0Table"),
    data: file.stream("Data"),
    old: old,
    nFib: nFib,
    lid: docU16(main, 6),
    fcMin: docU32(main, 0x18),
    fcMac: docU32(main, 0x1C)
  };
  if (!doc.table) {
    if (old) doc.table = main;   // a Word 6 file keeps its tables in the one stream
    else docFail("This Word document is damaged: its table stream is missing.");
  }

  // The FIB's table of offsets: where each structure lies in the table stream
  var entries = {};
  var names = ["stshOrig", "stsh", "fndRef", "fndTxt", "andRef", "andTxt", "sed", "pad", "phe", "glsy",
    "plcfGlsy", "hdd", "bteChpx", "btePapx", "sea", "ffn", "fldMom", "fldHdr", "fldFtn", "fldAtn", "fldMcr",
    "bkmk", "bkf", "bkl", "cmds", "u1", "sttbMcr", "prDrvr", "prEnvPort", "prEnvLand", "wss", "dop", "assoc",
    "clx", "pgdFtn", "autosave", "grpXst", "atnBkmk", "u2", "u3", "spaMom", "spaHdr", "atnBkf", "atnBkl",
    "pms", "formFld", "endRef", "endTxt", "fldEdn", "pgdEdn", "dggInfo", "rmark", "caption", "autoCaption",
    "wkb", "spl", "txbxTxt", "fldTxbx", "hdrTxbxTxt", "fldHdrTxbx", "stwUser", "ttmbd", "cookie", "pgdMother",
    "bkdMother", "pgdFtn2", "bkdFtn", "pgdEdn2", "bkdEdn", "intlFld", "routeSlip", "savedBy", "fnm", "lst",
    "lfo", "txbxBkd", "txbxHdrBkd"];
  var i;
  if (old) {
    for (i = 0; i < 34 && 0x58 + i * 6 + 6 <= main.length; i++) {
      entries[names[i]] = { fc: docU32(main, 0x58 + i * 6), lcb: docU16(main, 0x5C + i * 6) };
    }
    doc.ccp = { text: docI32(main, 0x34), ftn: docI32(main, 0x38), hdd: docI32(main, 0x3C), mcr: docI32(main, 0x40),
      atn: docI32(main, 0x44), edn: 0, txbx: 0, hdrTxbx: 0 };
  } else {
    var count = docU16(main, 0x98);
    for (i = 0; i < names.length && i < count && 0x9A + i * 8 + 8 <= main.length; i++) {
      entries[names[i]] = { fc: docU32(main, 0x9A + i * 8), lcb: docU32(main, 0x9E + i * 8) };
    }
    doc.ccp = { text: docI32(main, 0x4C), ftn: docI32(main, 0x50), hdd: docI32(main, 0x54), mcr: docI32(main, 0x58),
      atn: docI32(main, 0x5C), edn: docI32(main, 0x60), txbx: docI32(main, 0x64), hdrTxbx: docI32(main, 0x68) };
  }
  doc.at = entries;
  doc.ccp.all = doc.ccp.text + doc.ccp.ftn + doc.ccp.hdd + doc.ccp.mcr + doc.ccp.atn + doc.ccp.edn + doc.ccp.txbx + doc.ccp.hdrTxbx;
  if (doc.ccp.all < 0 || doc.ccp.all > 64 * 1024 * 1024) docFail("This Word document is damaged.");

  docPieces(doc);
  return doc;
}

/*
 * The piece table: which bytes hold the text, in reading order. Each piece is a run of
 * characters, held either as one byte each in Windows-1252 or as two in UTF-16, and
 * the text is decoded here in one pass. A file with no piece table, which a Word 6 file
 * that was never fast-saved is, keeps its text in one run from fcMin.
 */
function docPieces(doc) {
  var b = doc.table;
  var clx = doc.at.clx;
  var pieces = [];
  var at = clx ? clx.fc : 0;
  var end = clx ? Math.min(b.length, clx.fc + clx.lcb) : 0;
  doc.prcs = [];

  while (clx && at < end) {
    var kind = b[at];
    if (kind === 1) {
      // A run of sprms a piece can refer to
      var cb = docU16(b, at + 1);
      doc.prcs.push({ at: at + 3, end: Math.min(end, at + 3 + cb) });
      at += 3 + cb;
    } else if (kind === 2) {
      var lcb = docU32(b, at + 1);
      var plc = at + 5;
      var n = Math.floor((lcb - 4) / 12);
      for (var i = 0; i < n; i++) {
        var cpStart = docU32(b, plc + i * 4);
        var cpEnd = docU32(b, plc + (i + 1) * 4);
        var pcd = plc + (n + 1) * 4 + i * 8;
        if (pcd + 8 > end) break;
        var fc = docU32(b, pcd + 2);
        var prm = docU16(b, pcd + 6);
        var wide = true;
        if (!doc.old && (fc & 0x40000000)) { wide = false; fc = (fc & 0x3FFFFFFF) / 2; }
        if (doc.old) wide = false;
        if (cpEnd > cpStart) pieces.push({ cp: cpStart, end: cpEnd, fc: fc, wide: wide, prm: prm });
      }
      break;
    } else {
      break;
    }
  }
  if (!pieces.length) {
    pieces.push({ cp: 0, end: doc.ccp.all, fc: doc.fcMin, wide: false, prm: 0 });
  }

  var decoder;
  try {
    decoder = new TextDecoder(doc.old && DOC_LID_CODEPAGES[doc.lid] ? DOC_LID_CODEPAGES[doc.lid] : "windows-1252");
  } catch (e) {
    decoder = new TextDecoder("windows-1252");
  }
  var parts = [];
  var main = doc.main;
  var total = 0;
  for (var p = 0; p < pieces.length; p++) {
    var piece = pieces[p];
    var chars = piece.end - piece.cp;
    var bytes = chars * (piece.wide ? 2 : 1);
    if (piece.fc + bytes > main.length) {
      chars = Math.max(0, Math.floor((main.length - piece.fc) / (piece.wide ? 2 : 1)));
      bytes = chars * (piece.wide ? 2 : 1);
      piece.end = piece.cp + chars;
    }
    var text;
    if (piece.wide) {
      var codes = new Uint16Array(chars);
      for (var c = 0; c < chars; c++) codes[c] = docU16(main, piece.fc + c * 2);
      text = "";
      for (var s = 0; s < chars; s += 8192) text += String.fromCharCode.apply(null, codes.subarray(s, Math.min(s + 8192, chars)));
    } else {
      text = decoder.decode(main.subarray(piece.fc, piece.fc + bytes));
      // A single-byte piece decodes one character per byte, or the positions drift
      if (text.length !== chars) {
        text = "";
        for (var k = 0; k < chars; k++) text += String.fromCharCode(main[piece.fc + k]);
      }
    }
    piece.text = text;
    piece.cp = total;
    piece.end = total + chars;
    total += chars;
    parts.push(text);
  }
  doc.pieces = pieces;
  doc.text = parts.join("");
  doc.ccp.all = Math.min(doc.ccp.all, doc.text.length);
}

/* The piece a CP lies in, by bisection, and the FC of that CP. */
function docPieceAt(doc, cp) {
  var pieces = doc.pieces;
  var lo = 0;
  var hi = pieces.length - 1;
  while (lo < hi) {
    var mid = (lo + hi) >> 1;
    if (cp < pieces[mid].end) hi = mid; else lo = mid + 1;
  }
  return pieces[lo];
}

function docFcOf(doc, cp) {
  var piece = docPieceAt(doc, cp);
  return piece.fc + (cp - piece.cp) * (piece.wide ? 2 : 1);
}

/* ------------------------------------------------------------------------------------
 * Formatting pages
 * ---------------------------------------------------------------------------------- */

/*
 * The 512-byte pages that hold character and paragraph formatting, found through a
 * "bin table": a PLC of FC ranges, each naming the page for that range. A page holds
 * up to 255 FC runs and, for each, an offset to its sprms.
 */
function docBinTable(doc, entry) {
  var b = doc.table;
  if (!entry || entry.lcb < 8 || entry.fc + entry.lcb > b.length) return { fcs: [], pages: [] };
  var n = Math.floor((entry.lcb - 4) / 8);
  var fcs = [];
  var pages = [];
  for (var i = 0; i <= n; i++) fcs.push(docU32(b, entry.fc + i * 4));
  for (var j = 0; j < n; j++) pages.push(docU32(b, entry.fc + (n + 1) * 4 + j * 4));
  return { fcs: fcs, pages: pages };
}

/* Every page whose FC range meets [fcFrom, fcTo). */
function docPagesFor(bin, fcFrom, fcTo) {
  var out = [];
  for (var i = 0; i < bin.pages.length; i++) {
    if (bin.fcs[i + 1] > fcFrom && bin.fcs[i] < fcTo) out.push(bin.pages[i]);
  }
  return out;
}

/*
 * The formatting runs of one piece, as [{ cp, end, at, len }] in CP order, at and len
 * being where the run's sprms are in the WordDocument stream. kind is "chp" or "pap":
 * the pages differ in what follows the FC list, and in how a paragraph's sprms begin.
 *
 * They are read once for each piece and kept on it, under kind. A file Word saved whole
 * is one piece, and when its pages were read again for every paragraph, the time a
 * document took to open went as the square of its length.
 */
function docRuns(doc, bin, piece, kind) {
  if (piece[kind]) return piece[kind];
  var main = doc.main;
  var runs = [];
  var width = piece.wide ? 2 : 1;
  var fcFrom = piece.fc;
  var fcTo = piece.fc + (piece.end - piece.cp) * width;
  var pages = docPagesFor(bin, fcFrom, fcTo);

  for (var p = 0; p < pages.length; p++) {
    var page = pages[p] * 512;
    if (page + 512 > main.length) continue;
    var crun = main[page + 511];
    if (!crun || crun > 0x7F) continue;
    for (var r = 0; r < crun; r++) {
      var runFrom = docU32(main, page + r * 4);
      var runTo = docU32(main, page + (r + 1) * 4);
      if (runTo <= fcFrom || runFrom >= fcTo) continue;
      var at, len;
      if (kind === "chp") {
        var off = main[page + (crun + 1) * 4 + r] * 2;
        if (!off) { at = 0; len = 0; }
        else { len = main[page + off]; at = page + off + 1; }
      } else {
        var bx = page + (crun + 1) * 4 + r * (doc.old ? 7 : 13);
        var off2 = main[bx] * 2;
        if (!off2) { at = 0; len = 0; }
        else {
          var cb = main[page + off2];
          if (cb) { len = cb * 2 - 1; at = page + off2 + 1; }
          else { len = main[page + off2 + 1] * 2; at = page + off2 + 2; }
        }
      }
      var cpFrom = piece.cp + Math.floor((Math.max(runFrom, fcFrom) - fcFrom) / width);
      var cpTo = piece.cp + Math.ceil((Math.min(runTo, fcTo) - fcFrom) / width);
      if (cpTo > cpFrom) runs.push({ cp: cpFrom, end: cpTo, at: at, len: len });
    }
  }
  runs.sort(function (x, y) { return x.cp - y.cp; });
  piece[kind] = runs;
  return runs;
}

/*
 * The first of a piece's runs to end after cp, found by bisection, or runs.length. Runs
 * do not overlap, so their ends are in order as their starts are.
 */
function docRunFrom(runs, cp) {
  var lo = 0;
  var hi = runs.length;
  while (lo < hi) {
    var mid = (lo + hi) >> 1;
    if (runs[mid].end <= cp) lo = mid + 1; else hi = mid;
  }
  return lo;
}

/* ------------------------------------------------------------------------------------
 * Styles, fonts, lists
 * ---------------------------------------------------------------------------------- */

/*
 * The stylesheet. Each style is its base style's formatting with its own sprms over
 * it, so a style is settled once, on demand, and kept. What is kept is the pap and the
 * chp a paragraph in that style starts from.
 */
function docStyles(doc) {
  var b = doc.table;
  var entry = doc.at.stsh;
  var styles = { list: [], done: {}, headings: {} };
  if (!entry || entry.lcb < 4 || entry.fc + entry.lcb > b.length) return styles;

  var cbStshi = docU16(b, entry.fc);
  var stshi = entry.fc + 2;
  var cstd = docU16(b, stshi);
  var cbStd = docU16(b, stshi + 2);
  var at = stshi + cbStshi;
  var end = entry.fc + entry.lcb;

  for (var i = 0; i < cstd && at + 2 <= end; i++) {
    var cb = docU16(b, at);
    var std = at + 2;
    at += 2 + cb;
    if (!cb) { styles.list.push(null); continue; }
    var sti = docU16(b, std) & 0x0FFF;
    var stk = docU16(b, std + 2) & 0x0F;
    var istdBase = docU16(b, std + 2) >> 4;
    var cupx = docU16(b, std + 4) & 0x0F;
    var nameAt = std + cbStd;
    var name = "";
    var upxAt;
    if (doc.old) {
      var cch = b[nameAt];
      for (var c = 0; c < cch; c++) name += String.fromCharCode(b[nameAt + 1 + c]);
      upxAt = nameAt + 1 + cch + 1;
    } else {
      var cch2 = docU16(b, nameAt);
      for (var c2 = 0; c2 < cch2 && c2 < 128; c2++) name += String.fromCharCode(docU16(b, nameAt + 2 + c2 * 2));
      upxAt = nameAt + 2 + cch2 * 2 + 2;
    }
    if (upxAt & 1) upxAt++;
    var style = { sti: sti, stk: stk, base: istdBase === 0x0FFF ? null : istdBase, name: name, papx: null, chpx: null };
    var stop = std + cb;
    for (var u = 0; u < cupx && upxAt + 2 <= stop; u++) {
      var cbUpx = docU16(b, upxAt);
      var body = upxAt + 2;
      var isPap = (stk === 1 && u === 0) || (stk === 3 && u === 1) || (stk === 4 && u === 0);
      var isChp = (stk === 1 && u === 1) || stk === 2 || (stk === 3 && u === 2);
      if (isPap && cbUpx >= 2) style.papx = { at: body + 2, end: Math.min(stop, body + cbUpx) };
      else if (isChp) style.chpx = { at: body, end: Math.min(stop, body + cbUpx) };
      upxAt = body + cbUpx;
      if (upxAt & 1) upxAt++;
    }
    if (sti >= 1 && sti <= 9) style.heading = sti;
    styles.list.push(style);
  }
  return styles;
}

function docNewPap() {
  return { istd: 0, itap: 0 };
}

function docNewChp() {
  return { size: 10, ftc: 0 };
}

function docCopy(o) {
  var out = {};
  for (var k in o) out[k] = o[k];
  return out;
}

/* The settled formatting a style gives: { pap, chp }, bases included. */
function docStyle(doc, istd) {
  var styles = doc.styles;
  if (styles.done[istd]) return styles.done[istd];
  var style = styles.list[istd];
  var out;
  if (!style || istd > 4095) {
    out = { pap: docNewPap(), chp: docNewChp(), heading: 0 };
  } else {
    var base = style.base != null && style.base !== istd ? docStyle(doc, style.base) : { pap: docNewPap(), chp: docNewChp(), heading: 0 };
    out = { pap: docCopy(base.pap), chp: docCopy(base.chp), heading: style.heading || 0 };
    // The stylesheet's own default font, when the style names none of its own
    if (out.chp.ftc === 0 && doc.defaultFtc) out.chp.ftc = doc.defaultFtc;
    if (style.papx) docSprms(doc.table, style.papx.at, style.papx.end, function (code, at, len, b) {
      docApplyPap(out.pap, code, at, len, b);
    });
    if (style.chpx) docSprms(doc.table, style.chpx.at, style.chpx.end, function (code, at, len, b) {
      docApplyChp(out.chp, out.chp, code, at, len, b);
    });
  }
  out.pap.istd = istd;
  styles.done[istd] = out;
  return out;
}

/* The font table: name, kind and character set for each font number. */
function docFonts(doc) {
  var b = doc.table;
  var entry = doc.at.ffn;
  var fonts = [];
  if (!entry || entry.lcb < 4 || entry.fc + entry.lcb > b.length) return fonts;
  var at = entry.fc;
  var end = entry.fc + entry.lcb;
  // A count and the size of what follows each name, after a marker some writers add
  var count;
  if (docU16(b, at) === 0xFFFF) { count = docU16(b, at + 2); at += 6; }
  else { count = docU16(b, at); at += 4; }

  for (var i = 0; i < count && at + 2 <= end; i++) {
    var cb = b[at] + 1;
    var kind = ["nil", "roman", "swiss", "modern", "script", "decor"][(b[at + 1] >> 4) & 7] || null;
    var chs = b[at + 4];
    var name = "";
    if (doc.old) {
      for (var c = at + 6; c < at + cb && b[c]; c++) name += String.fromCharCode(b[c]);
    } else {
      for (var w = at + 40; w + 1 < at + cb; w += 2) {
        var code = docU16(b, w);
        if (!code) break;
        name += String.fromCharCode(code);
      }
    }
    fonts.push({ name: name, css: vwProseFont(name, kind), symbol: chs === 2 ? (vwProseSymbolFont(name) || "symbol") : vwProseSymbolFont(name) });
    at += cb;
  }
  return fonts;
}

var DOC_NFC = { 0: "decimal", 1: "upperRoman", 2: "lowerRoman", 3: "upperLetter", 4: "lowerLetter", 22: "zero" };

/*
 * The lists: for each, nine levels of how to number and how to indent, and the
 * overrides that a document's paragraphs actually point at. A paragraph says which
 * override and which level; the override says which list, and may replace levels.
 */
function docLists(doc) {
  var b = doc.table;
  var lists = { lst: [], lfo: [] };
  var entry = doc.at.lst;
  if (!entry || entry.lcb < 2 || entry.fc + entry.lcb > b.length) return lists;

  function level(at, end) {
    if (at + 28 > end) return null;
    var lvl = {
      start: docI32(b, at), nfc: b[at + 4], align: ["left", "center", "right"][b[at + 5] & 3] || "left",
      legal: !!(b[at + 5] & 4), noRestart: !!(b[at + 5] & 8), follow: b[at + 15],
      papx: null, chpx: null, text: "", places: []
    };
    var cbChpx = b[at + 24];
    var cbPapx = b[at + 25];
    var p = at + 28;
    if (cbPapx) lvl.papx = { at: p, end: Math.min(end, p + cbPapx) };
    p += cbPapx;
    if (cbChpx) lvl.chpx = { at: p, end: Math.min(end, p + cbChpx) };
    p += cbChpx;
    var cch = docU16(b, p);
    p += 2;
    for (var c = 0; c < cch && p + 1 < end; c++, p += 2) {
      var code = docU16(b, p);
      if (code < 9) { lvl.places.push({ at: lvl.text.length, level: code }); lvl.text += "\u0000"; }
      else lvl.text += String.fromCharCode(code);
    }
    lvl.end = p;
    return lvl;
  }

  var count = docU16(b, entry.fc);
  var at = entry.fc + 2;
  var i, l;
  for (i = 0; i < count && at + 28 <= b.length; i++) {
    lists.lst.push({ id: docI32(b, at), simple: !!(b[at + 26] & 1), levels: [] });
    at += 28;
  }
  for (i = 0; i < lists.lst.length; i++) {
    var n = lists.lst[i].simple ? 1 : 9;
    for (l = 0; l < n; l++) {
      var lvl = level(at, b.length);
      if (!lvl) break;
      lists.lst[i].levels.push(lvl);
      at = lvl.end;
    }
  }

  var lfoEntry = doc.at.lfo;
  if (!lfoEntry || lfoEntry.lcb < 4 || lfoEntry.fc + lfoEntry.lcb > b.length) return lists;
  var lfoCount = docU32(b, lfoEntry.fc);
  at = lfoEntry.fc + 4;
  for (i = 0; i < lfoCount && at + 16 <= b.length; i++) {
    lists.lfo.push({ id: docI32(b, at), count: b[at + 12], levels: {} });
    at += 16;
  }
  for (i = 0; i < lists.lfo.length; i++) {
    var lfo = lists.lfo[i];
    for (l = 0; l < lfo.count && at + 8 <= b.length; l++) {
      var startAt = docI32(b, at);
      var f = docU32(b, at + 4);
      var ilvl = f & 15;
      var over = { start: (f & 16) ? startAt : null, level: null };
      at += 8;
      if (f & 32) {
        var lvl2 = level(at, b.length);
        if (!lvl2) break;
        over.level = lvl2;
        at = lvl2.end;
      }
      lfo.levels[ilvl] = over;
    }
    for (var k = 0; k < lists.lst.length; k++) if (lists.lst[k].id === lfo.id) lfo.lst = lists.lst[k];
  }
  return lists;
}

/*
 * The level a paragraph is numbered at: the list's, unless its override replaces it.
 * ilfo is one-based; a zero means no list.
 */
function docLevel(doc, ilfo, ilvl) {
  var lfo = doc.lists.lfo[ilfo - 1];
  if (!lfo || !lfo.lst) return null;
  var over = lfo.levels[ilvl];
  var lvl = (over && over.level) || lfo.lst.levels[lfo.lst.simple ? 0 : ilvl];
  if (!lvl) return null;
  return { lvl: lvl, start: over && over.start != null ? over.start : lvl.start, lfo: lfo };
}

/* 1st, 2nd, 3rd, 4th, and 11th to 13th. */
function docOrdinal(n) {
  var tens = n % 100;
  var suffix = (tens >= 11 && tens <= 13) ? "th" : ["th", "st", "nd", "rd"][n % 10] || "th";
  return n + suffix;
}

/*
 * The label of a numbered paragraph, counted. counts is kept per list, so two
 * paragraphs pointing at the same list continue each other's numbers however far
 * apart they are, and a deeper level starts again when a shallower one moves on.
 */
function docLabel(doc, pap) {
  var found = docLevel(doc, pap.ilfo, pap.ilvl || 0);
  if (!found) return null;
  var lvl = found.lvl;
  var key = found.lfo.id;
  var counts = doc.counts[key] || (doc.counts[key] = []);
  var level = pap.ilvl || 0;

  if (lvl.nfc !== 23 && lvl.nfc !== 255) {
    counts[level] = counts[level] == null ? found.start : counts[level] + 1;
    for (var deeper = level + 1; deeper < 9; deeper++) counts[deeper] = null;
  }
  var text = "";
  var last = 0;
  for (var i = 0; i < lvl.places.length; i++) {
    var place = lvl.places[i];
    var shown = docLevel(doc, pap.ilfo, place.level);
    var n = counts[place.level];
    if (n == null) n = shown ? shown.start : 1;
    var format = shown ? shown.lvl.nfc : 0;
    var number = format === 22 ? (n < 10 ? "0" + n : String(n))
      : format === 5 ? docOrdinal(n)
      : vwProseNumber(n, DOC_NFC[format] || "decimal");
    text += lvl.text.slice(last, place.at) + number;
    last = place.at + 1;
  }
  text += lvl.text.slice(last);
  if (lvl.nfc === 255) text = "";

  return { text: text, lvl: lvl, follow: lvl.follow };
}

/* ------------------------------------------------------------------------------------
 * PLCs: the tables that place things in the text
 * ---------------------------------------------------------------------------------- */

/* A PLC: n+1 CPs followed by n entries of size bytes. */
function docPlc(doc, entry, size) {
  var b = doc.table;
  if (!entry || entry.lcb < 4 || entry.fc + entry.lcb > b.length) return { cps: [], at: 0, size: size };
  var n = Math.floor((entry.lcb - 4) / (4 + size));
  var cps = [];
  for (var i = 0; i <= n; i++) cps.push(docU32(b, entry.fc + i * 4));
  return { cps: cps, at: entry.fc + (n + 1) * 4, size: size, n: n };
}

/* ------------------------------------------------------------------------------------
 * Pictures
 * ---------------------------------------------------------------------------------- */

/*
 * The picture bytes in an Office Art blip record, whichever of the six kinds it is.
 * A metafile's bytes are usually deflated and are left that way: the page cannot draw
 * one, so all that is needed is enough of them for vwProsePicture to say so.
 */
function docBlip(b, at, end) {
  if (at + 8 > end) return null;
  var head = docU16(b, at);
  var type = docU16(b, at + 2);
  var len = docU32(b, at + 4);
  var inst = head >> 4;
  var body = at + 8;
  var stop = Math.min(end, body + len);
  if (type < 0xF018 || type > 0xF117) return null;
  var uids = 1;
  if (type === 0xF01A && inst === 0x3D5) uids = 2;
  else if (type === 0xF01B && inst === 0x217) uids = 2;
  else if (type === 0xF01C && inst === 0x543) uids = 2;
  else if (type === 0xF01D && (inst === 0x46B || inst === 0x6E3)) uids = 2;
  else if (type === 0xF01E && inst === 0x6E1) uids = 2;
  else if (type === 0xF01F && inst === 0x7A9) uids = 2;
  else if (type === 0xF029 && inst === 0x6E5) uids = 2;
  var dataAt = body + 16 * uids;
  if (type === 0xF01A || type === 0xF01B || type === 0xF01C) dataAt += 34;   // metafile header
  else dataAt += 1;                                                            // tag byte
  if (dataAt >= stop) return null;
  var bytes = b.subarray(dataAt, stop);
  if (type === 0xF01F) return docDib(bytes);
  return bytes;
}

/* A device-independent bitmap, made a BMP by putting the file header back on. */
function docDib(dib) {
  if (dib.length < 40) return null;
  var out = new Uint8Array(dib.length + 14);
  out[0] = 0x42; out[1] = 0x4D;
  var size = out.length;
  out[2] = size & 255; out[3] = (size >> 8) & 255; out[4] = (size >> 16) & 255; out[5] = (size >> 24) & 255;
  var headerSize = docU32(dib, 0);
  var bits = docU16(dib, 14);
  var colours = docU32(dib, 32) || (bits <= 8 ? 1 << bits : 0);
  var offset = 14 + headerSize + colours * 4;
  out[10] = offset & 255; out[11] = (offset >> 8) & 255; out[12] = (offset >> 16) & 255; out[13] = (offset >> 24) & 255;
  out.set(dib, 14);
  return out;
}

/*
 * A picture in the text: at fcPic in the Data stream is a PICF header, with the size
 * the picture is to be drawn at, and after it the Office Art container that holds the
 * blip. The blip is found by walking the container's records for one of the blip kinds.
 */
function docInlinePicture(doc, fcPic) {
  var b = doc.data;
  if (!b || fcPic + 0x44 > b.length) return null;
  var lcb = docU32(b, fcPic);
  var cbHeader = docU16(b, fcPic + 4);
  var mm = docU16(b, fcPic + 6);
  var end = Math.min(b.length, fcPic + lcb);
  var pic = {
    width: docU16(b, fcPic + 0x1C) / 20 * (docU16(b, fcPic + 0x20) || 1000) / 1000,
    height: docU16(b, fcPic + 0x1E) / 20 * (docU16(b, fcPic + 0x22) || 1000) / 1000,
    bytes: null
  };
  var at = fcPic + cbHeader;
  if (mm === 0x66) at += 1 + b[at];   // a file name, for a linked picture
  if (mm !== 0x64 && mm !== 0x66) return pic;

  // Records nest: containers are walked into, blips are taken, everything else skipped
  function walk(from, to, depth) {
    while (from + 8 <= to && depth < 8) {
      var head = docU16(b, from);
      var type = docU16(b, from + 2);
      var len = docU32(b, from + 4);
      var stop = Math.min(to, from + 8 + len);
      var found = docBlipRecord(doc, b, from, type, stop);
      if (found) return found;
      if ((head & 15) === 15 && type !== 0xF007) {
        var inner = walk(from + 8, stop, depth + 1);
        if (inner) return inner;
      }
      from = stop;
    }
    return null;
  }
  pic.bytes = walk(at, end, 0);
  return pic;
}

/*
 * The picture in a record, if it is one: a blip, or a blip store entry, which is a
 * 36-byte description followed by the blip itself, or by nothing when the blip is kept
 * in the WordDocument stream instead, at the offset the description gives. That is
 * where the drawings' store keeps its pictures, and not the Data stream, which holds
 * the pictures in the text.
 */
function docBlipRecord(doc, b, from, type, stop) {
  if (type >= 0xF018 && type <= 0xF117) return docBlip(b, from, stop);
  if (type !== 0xF007) return null;
  var blip = docBlip(b, from + 8 + 36, stop);
  if (!blip && b !== doc.data) {
    var foDelay = docU32(b, from + 8 + 28);
    if (foDelay < doc.main.length) blip = docBlip(doc.main, foDelay, doc.main.length);
  }
  return blip;
}

/*
 * The drawings: floating pictures and text boxes are shapes, anchored to a character
 * in the text and described in the Office Art content in the table stream. Each
 * shape's properties name the picture it shows by index into the blip store, and the
 * text box it holds by index into the text box stories.
 */
function docDrawings(doc) {
  var out = { shapes: {}, blips: [], anchors: {} };
  var entry = doc.at.dggInfo;
  var b = doc.table;
  if (!entry || entry.lcb < 8 || entry.fc + entry.lcb > b.length) return out;
  var end = entry.fc + entry.lcb;

  function walk(from, to, depth) {
    while (from + 8 <= to && depth < 12) {
      var head = docU16(b, from);
      var type = docU16(b, from + 2);
      var len = docU32(b, from + 4);
      var stop = Math.min(to, from + 8 + len);
      if (type === 0xF007) {
        out.blips.push(docBlipRecord(doc, b, from, type, stop));
      } else if (type === 0xF004) {
        var shape = { pib: 0, txid: 0 };
        var spid = null;
        var inner = from + 8;
        while (inner + 8 <= stop) {
          var t = docU16(b, inner + 2);
          var l = docU32(b, inner + 4);
          if (t === 0xF00A) spid = docU32(b, inner + 8);
          else if (t === 0xF00B || t === 0xF121 || t === 0xF122) {
            var count = docU16(b, inner) >> 4;
            for (var p = 0; p < count && inner + 8 + p * 6 + 6 <= stop; p++) {
              var pid = docU16(b, inner + 8 + p * 6) & 0x3FFF;
              var value = docU32(b, inner + 8 + p * 6 + 2);
              if (pid === 0x0104) shape.pib = value;
              else if (pid === 0x0080) shape.txid = value;
            }
          }
          inner += 8 + l;
        }
        if (spid != null) out.shapes[spid] = shape;
      } else if ((head & 15) === 15) {
        walk(from + 8, stop, depth + 1);
      }
      from = stop;
    }
  }
  // The drawing group's container comes first, holding the blip store, and then each
  // drawing: a byte saying whether it is the main text's or the headers', and the
  // drawing's own container. Read as one run of records, that byte would begin a
  // record, and the drawing with every shape in it would be skipped as its body
  var record = entry.fc;
  var lead = 0;
  while (record + lead + 8 <= end) {
    var next = Math.min(end, record + lead + 8 + docU32(b, record + lead + 4));
    walk(record + lead, next, 0);
    record = next;
    lead = 1;
  }

  // Which shape sits at which anchor character
  var spa = docPlc(doc, doc.at.spaMom, 26);
  for (var i = 0; i < spa.n; i++) {
    var at = spa.at + i * 26;
    out.anchors[spa.cps[i]] = {
      spid: docU32(b, at),
      width: (docI32(b, at + 12) - docI32(b, at + 4)) / 20,
      height: (docI32(b, at + 16) - docI32(b, at + 8)) / 20
    };
  }
  return out;
}

/* ------------------------------------------------------------------------------------
 * Reading a story
 * ---------------------------------------------------------------------------------- */

/*
 * The paragraphs of a range of the text, each with its formatting settled and its
 * characters cut into runs of one formatting each. A paragraph ends at a paragraph
 * mark, a cell mark or the end of the range, and its properties are those of the run
 * that holds its mark, which is where the format keeps them.
 */
function docParagraphs(doc, cpFrom, cpTo) {
  var text = doc.text;
  var paras = [];
  var cp = cpFrom;
  cpTo = Math.min(cpTo, text.length);

  while (cp < cpTo) {
    var end = cp;
    while (end < cpTo) {
      var ch = text.charCodeAt(end);
      end++;
      if (ch === 13 || ch === 7 || ch === 12 || ch === 14) break;
    }
    paras.push(docParagraph(doc, cp, end));
    cp = end;
  }
  return paras;
}

/* The sprms a piece adds to everything in it, from the Clx, when it has any. */
function docPiecePrm(doc, piece) {
  if (!piece.prm || !(piece.prm & 1)) return null;
  var prc = doc.prcs[piece.prm >> 1];
  return prc || null;
}

function docParagraph(doc, cp, end) {
  var markCp = end - 1;
  var piece = docPieceAt(doc, markCp);
  var runs = docRuns(doc, doc.papBin, piece, "pap");
  var holder = runs[docRunFrom(runs, markCp)];
  var papx = holder && holder.cp <= markCp ? holder : null;

  var istd = papx && papx.len >= 2 ? docU16(doc.main, papx.at) : 0;
  var style = docStyle(doc, istd);
  var pap = docCopy(style.pap);
  var ownSprms = [];
  function collect(code, at, len, b) {
    // A paragraph whose sprms outgrow the page, a table row's usually, keeps them in
    // the Data stream and leaves a pointer to them here: sprmPHugePapx, which MS-DOC
    // numbers 0x6646 and Word 97's own documentation 0x6645. One found in the Data
    // stream is not followed, since a damaged file's could point at itself
    if ((code === 0x6646 || code === 0x6645) && doc.data && b !== doc.data) {
      var fc = docU32(b, at);
      if (fc + 2 <= doc.data.length) {
        var cb = docU16(doc.data, fc);
        docSprms(doc.data, fc + 2, Math.min(doc.data.length, fc + 2 + cb), collect);
      }
      return;
    }
    ownSprms.push([code, at, len, b]);
  }
  if (papx && papx.len > 2) docSprms(doc.main, papx.at + 2, papx.at + papx.len, collect);
  var prc = docPiecePrm(doc, piece);
  if (prc) docSprms(doc.table, prc.at, prc.end, collect);

  // The list level's indent comes between the style and the paragraph's own sprms
  var i;
  for (i = 0; i < ownSprms.length; i++) {
    if (ownSprms[i][0] === 0x460B) pap.ilfo = docU16(ownSprms[i][3], ownSprms[i][1]);
    if (ownSprms[i][0] === 0x260A) pap.ilvl = ownSprms[i][3][ownSprms[i][1]];
  }
  var label = null;
  if (pap.ilfo > 0 && pap.ilfo < 0xF800) {
    var found = docLevel(doc, pap.ilfo, pap.ilvl || 0);
    if (found && found.lvl.papx) docSprms(doc.table, found.lvl.papx.at, found.lvl.papx.end, function (code, at, len, b) {
      docApplyPap(pap, code, at, len, b);
    });
  }
  for (i = 0; i < ownSprms.length; i++) docApplyPap(pap, ownSprms[i][0], ownSprms[i][1], ownSprms[i][2], ownSprms[i][3]);
  if (pap.ilfo > 0 && pap.ilfo < 0xF800) label = docLabel(doc, pap);

  var para = {
    cp: cp, end: end, pap: pap, style: style, label: label,
    mark: doc.text.charCodeAt(end - 1),
    runs: docCharRuns(doc, cp, end - 1, style)
  };
  return para;
}

/*
 * The characters of a paragraph cut into runs, each with its chp settled: the
 * paragraph style's characters, the character style named in the run, then the
 * run's own sprms, with the piece's sprms, if any, first among them.
 */
function docCharRuns(doc, cp, end, style) {
  var out = [];
  var pos = cp;
  while (pos < end) {
    var piece = docPieceAt(doc, pos);
    var stop = Math.min(end, piece.end);
    var runs = docRuns(doc, doc.chpBin, piece, "chp");
    var prc = docPiecePrm(doc, piece);
    var covered = pos;
    for (var r = docRunFrom(runs, covered); r < runs.length && covered < stop; r++) {
      var run = runs[r];
      if (run.end <= covered) continue;
      if (run.cp > covered) {
        out.push({ cp: covered, end: Math.min(run.cp, stop), chp: docChp(doc, style, null, prc) });
        covered = Math.min(run.cp, stop);
      }
      if (covered >= stop) break;
      var to = Math.min(run.end, stop);
      out.push({ cp: covered, end: to, chp: docChp(doc, style, run, prc) });
      covered = to;
    }
    if (covered < stop) out.push({ cp: covered, end: stop, chp: docChp(doc, style, null, prc) });
    pos = stop;
  }
  return out;
}

function docChp(doc, style, run, prc) {
  var chp = docCopy(style.chp);
  var sprms = [];
  function collect(code, at, len, b) { sprms.push([code, at, len, b]); }
  if (prc) docSprms(doc.table, prc.at, prc.end, collect);
  if (run && run.len) docSprms(doc.main, run.at, run.at + run.len, collect);

  // A character style first, then the run's own sprms over the pair
  var i;
  for (i = 0; i < sprms.length; i++) {
    if (sprms[i][0] === 0x4A30) {
      var cs = docStyle(doc, docU16(sprms[i][3], sprms[i][1]));
      var over = docCopy(cs.chp);
      for (var k in over) if (k !== "istd" && over[k] !== docNewChp()[k]) chp[k] = over[k];
    }
  }
  var base = docCopy(chp);
  for (i = 0; i < sprms.length; i++) {
    if (sprms[i][0] === 0x2A33) { chp = docCopy(base); continue; }
    docApplyChp(chp, base, sprms[i][0], sprms[i][1], sprms[i][2], sprms[i][3]);
  }
  return chp;
}

/* ------------------------------------------------------------------------------------
 * Drawing
 * ---------------------------------------------------------------------------------- */

function docChpBag(doc, chp) {
  var bag = {};
  if (chp.bold) bag.bold = true;
  if (chp.italic) bag.italic = true;
  if (chp.underline) bag.underline = chp.underline;
  if (chp.strike) bag.strike = chp.strike;
  if (chp.caps) bag.caps = true;
  if (chp.smallCaps) bag.smallCaps = true;
  if (chp.hidden) bag.hidden = true;
  if (chp.spacing) bag.spacing = chp.spacing;
  if (chp.size && chp.size !== 12) bag.size = chp.size;
  if (chp.color) bag.color = chp.color;
  if (chp.highlight || chp.background) bag.background = chp.highlight || chp.background;
  if (chp.position) bag.position = chp.position;
  else if (chp.rise) bag.position = chp.rise > 0 ? "super" : "sub";
  var font = doc.fonts[chp.ftc];
  if (font && !font.symbol && font.name) bag.font = font.css;
  return bag;
}

function docPapBag(pap, para) {
  var bag = {};
  if (pap.align) bag.align = pap.align;
  if (pap.left) bag.left = pap.left;
  if (pap.right) bag.right = pap.right;
  if (pap.first) bag.first = pap.first;
  if (pap.before) bag.before = pap.before;
  if (pap.after) bag.after = pap.after;
  if (pap.line) bag.line = pap.line;
  if (pap.lineExact) bag.lineExact = pap.lineExact;
  if (pap.lineAtLeast) bag.lineAtLeast = pap.lineAtLeast;
  if (pap.background) bag.background = pap.background;
  if (pap.rtl) bag.rtl = true;
  var sides = ["Top", "Right", "Bottom", "Left"];
  for (var i = 0; i < 4; i++) {
    var b = pap["border" + sides[i]];
    if (b && b.width) { bag["border" + sides[i]] = b; bag["pad" + sides[i]] = 1; }
  }
  if (para && para.label && para.label.text && pap.first > 0) bag.first = 0;
  return bag;
}

/*
 * One paragraph onto the page. flow is where it goes and what it may draw into:
 * the state a story keeps while its paragraphs are drawn.
 */
function docDrawParagraph(state, para, into) {
  var doc = state.doc;
  var prose = state.prose;
  var pap = para.pap;
  var text = doc.text;

  var level = para.style.heading || (pap.outline != null && pap.outline < 9 ? pap.outline + 1 : 0);
  var el = document.createElement(level ? "h" + Math.min(level, 6) : "p");
  el.className = vwProseClass(prose, docPapBag(pap, para));
  into.appendChild(el);

  var last = state.lastParagraph;
  if (pap.contextual && last && last.el === el.previousElementSibling && last.istd === pap.istd) {
    last.el.style.marginBottom = "0";
    el.style.marginTop = "0";
  }
  state.lastParagraph = { el: el, istd: pap.istd };

  var link = null;
  var fieldDepth = state.fieldDepth || 0;
  var hiddenDepth = state.hiddenDepth || 0;

  function target() { return link || el; }

  for (var r = 0; r < para.runs.length; r++) {
    var run = para.runs[r];
    var chp = run.chp;
    var name = vwProseClass(prose, docChpBag(doc, chp));
    var font = doc.fonts[chp.ftc];
    var symbol = font ? font.symbol : null;
    var buffer = "";

    function flush() {
      if (!buffer) return;
      var out = symbol || /[\uF020-\uF0FF]/.test(buffer) ? vwProseSymbols(buffer, symbol) : buffer;
      var node = document.createTextNode(out);
      if (name) {
        var span = document.createElement("span");
        span.className = name;
        span.appendChild(node);
        target().appendChild(span);
      } else {
        target().appendChild(node);
      }
      buffer = "";
    }

    for (var cp = run.cp; cp < run.end; cp++) {
      var ch = text.charCodeAt(cp);

      // Fields: code between 0x13 and 0x14 is hidden, the result after 0x14 is shown
      if (ch === 0x13) { flush(); fieldDepth++; hiddenDepth++; state.fieldCode = ""; continue; }
      if (ch === 0x14) {
        flush();
        if (hiddenDepth > 0) hiddenDepth--;
        if (/^\s*HYPERLINK\b/i.test(state.fieldCode || "") && !link) {
          link = document.createElement("a");
          el.appendChild(link);
        }
        continue;
      }
      if (ch === 0x15) {
        flush();
        if (fieldDepth > 0) fieldDepth--;
        if (hiddenDepth > fieldDepth) hiddenDepth = fieldDepth;
        if (!fieldDepth) link = null;
        continue;
      }
      if (hiddenDepth > 0) { if (ch >= 0x20) state.fieldCode = (state.fieldCode || "") + String.fromCharCode(ch); continue; }
      if (chp.hidden || chp.deleted) continue;

      if (chp.special) {
        if (ch === 0x01 && chp.picture != null && !chp.data) {
          flush();
          var pic = docInlinePicture(doc, chp.picture);
          if (pic) target().appendChild(vwProsePicture(prose, pic.bytes, pic.width, pic.height, ""));
          continue;
        }
        if (ch === 0x08) { flush(); docDrawShape(state, cp, target()); continue; }
        if (ch === 0x02) { flush(); docDrawNoteRef(state, cp, target()); continue; }
        if (ch === 0x28 && chp.symbol) {
          var sf = doc.fonts[chp.symbolFont];
          buffer += vwProseSymbols(String.fromCharCode(chp.symbol), sf ? sf.symbol : "symbol");
          continue;
        }
        if (ch < 0x20) continue;
      }
      if (ch === 0x0B) { flush(); target().appendChild(document.createElement("br")); continue; }
      if (ch === 0x1E) { buffer += "\u2011"; continue; }
      if (ch === 0x1F) { buffer += "\u00AD"; continue; }
      if (ch === 0x09) { buffer += "\t"; continue; }
      if (ch < 0x20 && ch !== 0x09) continue;
      buffer += text.charAt(cp);
    }
    flush();
  }
  state.fieldDepth = fieldDepth;
  state.hiddenDepth = hiddenDepth;

  if (para.label && para.label.text) {
    var lvl = para.label.lvl;
    var labelChp = docCopy(para.style.chp);
    if (lvl.chpx) {
      docSprms(doc.table, lvl.chpx.at, lvl.chpx.end, function (code, at, len, b) {
        docApplyChp(labelChp, labelChp, code, at, len, b);
      });
    }
    var labelFont = doc.fonts[labelChp.ftc];
    var labelText = vwProseSymbols(para.label.text, labelFont ? labelFont.symbol : null);
    // What follows the number: a tab, which the hanging indent stands in for when
    // there is one, a space, or nothing
    var hang = pap.first < 0 ? -pap.first : 0;
    if (para.label.follow === 1) { labelText += " "; hang = 0; }
    else if (para.label.follow === 0 && !hang) labelText += "\t";
    else if (para.label.follow !== 0) hang = 0;
    vwProseLabel(prose, el, labelText, docChpBag(doc, labelChp), hang);
  }
  return el;
}

/* A footnote or endnote reference: its number, and the note itself kept for the end. */
function docDrawNoteRef(state, cp, into) {
  var doc = state.doc;
  var which = null;
  var index = -1;
  var i;
  for (i = 0; i < doc.fnRef.n; i++) if (doc.fnRef.cps[i] === cp) { which = "fn"; index = i; }
  if (which == null) for (i = 0; i < doc.enRef.n; i++) if (doc.enRef.cps[i] === cp) { which = "en"; index = i; }
  if (which == null) return;
  var mark = document.createElement("sup");
  mark.className = "vw-noteref";
  mark.textContent = which === "fn" ? String(++state.fnCount) : vwProseNumber(++state.enCount, "lowerRoman");
  into.appendChild(mark);

  var txt = which === "fn" ? doc.fnTxt : doc.enTxt;
  var base = which === "fn" ? doc.ccp.text : doc.ccp.text + doc.ccp.ftn + doc.ccp.hdd + doc.ccp.mcr + doc.ccp.atn;
  if (index + 1 < txt.cps.length) {
    state.notes.push({ mark: mark.textContent, from: base + txt.cps[index], to: base + txt.cps[index + 1] });
  }
}

/* A shape anchored here: its picture, its text box, or nothing. */
function docDrawShape(state, cp, into) {
  var doc = state.doc;
  var anchor = doc.drawings.anchors[cp];
  if (!anchor) return;
  var shape = doc.drawings.shapes[anchor.spid];
  if (!shape) return;
  if (shape.pib && doc.drawings.blips[shape.pib - 1]) {
    var img = vwProsePicture(state.prose, doc.drawings.blips[shape.pib - 1], anchor.width, anchor.height, "");
    img.style.display = "block";
    into.appendChild(img);
  } else if (shape.txid && doc.ccp.txbx) {
    var story = (shape.txid >> 16) - 1;
    var txbx = doc.txbx;
    if (story >= 0 && story + 1 < txbx.cps.length) {
      var base = doc.ccp.text + doc.ccp.ftn + doc.ccp.hdd + doc.ccp.mcr + doc.ccp.atn + doc.ccp.edn;
      var box = document.createElement("div");
      box.style.display = "inline-block";
      box.style.verticalAlign = "top";
      box.style.maxWidth = "100%";
      if (anchor.width > 0) box.style.width = vwProsePt(anchor.width);
      if (state.depth < 4) {
        state.depth++;
        docDrawStory(state, base + txbx.cps[story], base + txbx.cps[story + 1], box);
        state.depth--;
      }
      into.appendChild(box);
    }
  }
}

/*
 * A story, which is a range of the text with its own paragraphs and tables: the body,
 * a header, a footnote, a text box. Table rows are gathered as they come and built
 * when something that is not a row follows them.
 */
function docDrawStory(state, cpFrom, cpTo, into) {
  var doc = state.doc;
  var paras = docParagraphs(doc, cpFrom, cpTo);
  var rows = [];
  var cells = [];
  var cell = null;
  var i;

  function closeTable() {
    if (cell) { cells.push(cell); cell = null; }
    if (cells.length) { rows.push({ cells: cells, tap: null }); cells = []; }
    if (rows.length) docDrawTable(state, rows, into);
    rows = [];
  }

  for (i = 0; i < paras.length; i++) {
    var para = paras[i];
    var pap = para.pap;
    var inTable = pap.inTable || pap.itap > 0;

    if (state.top && into === state.prose.body) {
      var breaks = (pap.pageBreakBefore && into.firstChild) || state.pendingBreak;
      state.pendingBreak = false;
      if (breaks && !inTable) { closeTable(); into = state.prose.body = vwProseSheet(state.prose); }
      else if (breaks) { closeTable(); into = state.prose.body = vwProseSheet(state.prose); }
    }

    if (inTable) {
      if (pap.rowEnd && !pap.innerRowEnd || (pap.itap <= 1 && pap.rowEnd)) {
        if (cell) { cells.push(cell); cell = null; }
        rows.push({ cells: cells, tap: pap.tap });
        cells = [];
        continue;
      }
      if (!cell) cell = document.createDocumentFragment();
      docDrawParagraph(state, para, cell);
      // A cell mark ends the cell, unless it is an inner table's, whose cells run on
      if (para.mark === 7 && !pap.innerCell && !pap.innerRowEnd) { cells.push(cell); cell = null; }
      continue;
    }
    closeTable();
    docDrawParagraph(state, para, into);

    // A page or section break at the mark
    if (state.top && into === state.prose.body) {
      if (para.mark === 12) state.pendingBreak = true;
      if (para.mark === 12 && doc.sectionAt[para.end - 1] != null) {
        state.section = doc.sectionAt[para.end - 1] + 1;
        state.pendingLayout = docLayout(doc, state.section);
      }
    }
  }
  closeTable();
}

/*
 * A table from its rows. As in Rich Text, each row brings its own column edges and
 * the rows are laid on one grid, each cell spanning the grid columns between its own
 * two edges.
 */
function docDrawTable(state, rows, into) {
  var prose = state.prose;
  var edges = [];
  var r, c, i;
  function edge(x) {
    for (var k = 0; k < edges.length; k++) if (Math.abs(edges[k] - x) <= 0.75) return;
    edges.push(x);
  }
  for (r = 0; r < rows.length; r++) {
    var tap = rows[r].tap;
    if (!tap || !tap.edges.length) continue;
    for (c = 0; c < tap.edges.length; c++) edge(tap.edges[c]);
  }
  edges.sort(function (a, b) { return a - b; });
  function column(x) {
    var best = 0;
    for (var k = 0; k < edges.length; k++) if (Math.abs(edges[k] - x) < Math.abs(edges[best] - x)) best = k;
    return best;
  }

  var table = document.createElement("table");
  var body = document.createElement("tbody");
  var first = rows[0].tap || {};
  if (edges.length > 1) {
    var group = document.createElement("colgroup");
    for (i = 1; i < edges.length; i++) {
      var col = document.createElement("col");
      col.style.width = vwProsePt(edges[i] - edges[i - 1]);
      group.appendChild(col);
    }
    table.appendChild(group);
    table.style.tableLayout = "fixed";
    table.style.width = vwProsePt(edges[edges.length - 1] - edges[0]);
    if (first.align === "center") { table.style.marginLeft = "auto"; table.style.marginRight = "auto"; }
    else if (first.align === "right") table.style.marginLeft = "auto";
    else if (edges[0] + (first.gap || 0) < 0) table.style.marginLeft = vwProsePt(Math.max(-72, edges[0] + (first.gap || 0)));
  }
  table.appendChild(body);

  for (r = 0; r < rows.length; r++) {
    var row = rows[r];
    var tap = row.tap || { edges: [], cells: [], pad: [], gap: 5.4 };
    row.spans = [];
    for (c = 0; c < row.cells.length; c++) {
      var def = tap.cells[c] || {};
      var from = tap.edges.length > c ? column(tap.edges[c]) : c;
      var to = tap.edges.length > c + 1 ? column(tap.edges[c + 1]) : from + 1;
      row.spans.push({ from: from, to: Math.max(to, from + 1), def: def, cell: row.cells[c] });
    }
    for (c = row.spans.length - 1; c > 0; c--) {
      if (row.spans[c].def.mergedAcross) {
        row.spans[c - 1].to = Math.max(row.spans[c - 1].to, row.spans[c].to);
        row.spans.splice(c, 1);
      }
    }
  }

  for (r = 0; r < rows.length; r++) {
    var tr = document.createElement("tr");
    var rowTap = rows[r].tap || { pad: [], borders: null, gap: 5.4 };
    if (rowTap.height) tr.style.height = vwProsePt(Math.abs(rowTap.height));
    for (c = 0; c < rows[r].spans.length; c++) {
      var span = rows[r].spans[c];
      if (span.def.vertMerge && !span.def.vertRestart) continue;
      var td = document.createElement("td");
      if (span.to - span.from > 1) td.colSpan = span.to - span.from;
      if (span.def.vertRestart) {
        var down = 1;
        for (var below = r + 1; below < rows.length; below++) {
          var under = null;
          for (var k = 0; k < rows[below].spans.length; k++) if (rows[below].spans[k].from === span.from) under = rows[below].spans[k];
          if (!under || !under.def.vertMerge || under.def.vertRestart) break;
          down++;
        }
        if (down > 1) td.rowSpan = down;
      }
      var bag = {};
      var sides = ["Top", "Right", "Bottom", "Left"];
      var tableBorders = rowTap.borders;
      for (var s = 0; s < 4; s++) {
        var pad = span.def["pad" + sides[s]];
        if (pad == null) pad = rowTap.pad[s];
        if (pad == null && (s === 1 || s === 3)) pad = rowTap.gap;
        if (pad == null) pad = 0;
        bag["pad" + sides[s]] = pad;
        var line = span.def["border" + sides[s]];
        if ((!line || !line.width) && tableBorders) {
          // The table's own: outside edges on the outside, inside lines between cells
          var outside = (s === 0 && r === 0) || (s === 2 && r === rows.length - 1) ||
            (s === 3 && c === 0) || (s === 1 && c === rows[r].spans.length - 1);
          line = outside ? tableBorders[s === 0 ? 0 : s === 3 ? 1 : s === 2 ? 2 : 3] : tableBorders[s === 0 || s === 2 ? 4 : 5];
        }
        if (line && line.width) bag["border" + sides[s]] = line;
      }
      if (span.def.background) bag.background = span.def.background;
      if (span.def.valign && span.def.valign !== "top") bag.valign = span.def.valign;
      td.className = vwProseClass(prose, bag);
      td.appendChild(span.cell);
      tr.appendChild(td);
    }
    body.appendChild(tr);
  }
  into.appendChild(table);
}

/* ------------------------------------------------------------------------------------
 * Sections, headers, notes
 * ---------------------------------------------------------------------------------- */

/* The page a section is printed on, from the section's sprms. */
function docLayout(doc, section) {
  var sed = doc.sed;
  var layout = { width: 612, height: 792, top: 72, bottom: 72, left: 90, right: 90 };
  if (!sed || section >= sed.n) return docWithHeaders(doc, layout, section);
  var fcSepx = docU32(doc.table, sed.at + section * 12 + 2);
  var main = doc.main;
  if (fcSepx !== 0xFFFFFFFF && fcSepx + 2 <= main.length) {
    var cb = docU16(main, fcSepx);
    docSprms(main, fcSepx + 2, Math.min(main.length, fcSepx + 2 + cb), function (code, at, len, b) {
      switch (code) {
        case 0xB01F: layout.width = docU16(b, at) / 20; break;
        case 0xB020: layout.height = docU16(b, at) / 20; break;
        case 0xB021: layout.left = docU16(b, at) / 20; break;
        case 0xB022: layout.right = docU16(b, at) / 20; break;
        case 0x9023: layout.top = Math.abs(docI16(b, at)) / 20; break;
        case 0x9024: layout.bottom = Math.abs(docI16(b, at)) / 20; break;
        case 0x301D: if (b[at] === 2) { var w = layout.width; layout.width = layout.height; layout.height = w; } break;
      }
    });
  }
  return docWithHeaders(doc, layout, section);
}

/*
 * The header and footer of a section: the odd-page ones, which are the ones every page
 * has in a document that does not ask for different even pages. The header story holds
 * six per section after six the whole document shares.
 */
function docWithHeaders(doc, layout, section) {
  var hdd = doc.hdd;
  if (!hdd || doc.old) return layout;
  var base = doc.ccp.text + doc.ccp.ftn;
  function story(index) {
    if (index + 1 >= hdd.cps.length) return null;
    var from = base + hdd.cps[index];
    var to = base + hdd.cps[index + 1];
    if (to - from < 2) return null;
    return function (into) {
      var sub = { doc: doc, prose: layout.prose, top: false, notes: [], fnCount: 0, enCount: 0, depth: 1 };
      docDrawStory(sub, from, to, into);
    };
  }
  layout.header = story(6 + section * 6 + 1);
  layout.footer = story(6 + section * 6 + 3);
  return layout;
}

/* ------------------------------------------------------------------------------------
 * Opening the file
 * ---------------------------------------------------------------------------------- */

/*
 * Draws the document in buffer into container and answers the prose it drew, the way
 * vwReadOdt does.
 */
function vwReadDoc(buffer, container) {
  var doc = docOpen(new Uint8Array(buffer));
  var prose = vwProseOpen(container);

  doc.styles = docStyles(doc);
  doc.fonts = docFonts(doc);
  doc.lists = docLists(doc);
  doc.counts = {};
  doc.chpBin = docBinTable(doc, doc.at.bteChpx);
  doc.papBin = docBinTable(doc, doc.at.btePapx);
  doc.sed = docPlc(doc, doc.at.sed, 12);
  doc.hdd = docPlc(doc, doc.at.hdd, 0);
  doc.fnRef = docPlc(doc, doc.at.fndRef, 2);
  doc.fnTxt = docPlc(doc, doc.at.fndTxt, 0);
  doc.enRef = docPlc(doc, doc.at.endRef, 2);
  doc.enTxt = docPlc(doc, doc.at.endTxt, 0);
  doc.txbx = docPlc(doc, doc.at.txbxTxt, 22);
  doc.drawings = doc.old ? { shapes: {}, blips: [], anchors: {} } : docDrawings(doc);

  // The stylesheet's default font is the first one the font table names
  var stshi = doc.at.stsh && doc.at.stsh.lcb > 20 ? doc.at.stsh.fc + 2 : 0;
  doc.defaultFtc = stshi && !doc.old ? docU16(doc.table, stshi + 12) : 0;

  // Which paragraph mark is a section's end
  doc.sectionAt = {};
  for (var s = 0; s < doc.sed.n; s++) doc.sectionAt[doc.sed.cps[s + 1] - 1] = s;

  var state = { doc: doc, prose: prose, top: true, notes: [], fnCount: 0, enCount: 0, depth: 0, pendingBreak: false, section: 0 };
  var normal = docStyle(doc, 0);
  var baseFont = doc.fonts[normal.chp.ftc];
  prose.base = vwProseClass(prose, {
    font: baseFont && !baseFont.symbol ? baseFont.css : null,
    size: normal.chp.size || 10,
    color: normal.chp.color || null
  });

  var layout = docLayout(doc, 0);
  layout.prose = prose;
  vwProseSheet(prose, layout);

  if (doc.old) {
    docDrawOld(state, container);
  } else {
    docDrawStory(state, 0, doc.ccp.text, prose.body);
    if (state.notes.length) {
      var area = vwProseNotes(prose);
      for (var n = 0; n < state.notes.length; n++) {
        var note = state.notes[n];
        var holder = document.createElement("div");
        area.appendChild(holder);
        var sub = { doc: doc, prose: prose, top: false, notes: [], fnCount: 0, enCount: 0, depth: 1 };
        docDrawStory(sub, note.from, note.to, holder);
        var firstPara = holder.querySelector("p, h1, h2, h3, h4, h5, h6");
        if (firstPara) {
          var mark = document.createElement("sup");
          mark.textContent = note.mark;
          firstPara.insertBefore(document.createTextNode(" "), firstPara.firstChild);
          firstPara.insertBefore(mark, firstPara.firstChild);
        }
      }
    }
  }
  return Promise.resolve(prose);
}

/*
 * A Word 6 or Word 95 file: its text with its paragraphs, and nothing else. Those
 * files format their text with a different, one-byte set of codes, and a phone that
 * meets one every few years is better served by its words than by a second reader.
 */
function docDrawOld(state, container) {
  var doc = state.doc;
  var text = doc.text.slice(0, doc.ccp.text);
  var into = state.prose.body;
  var start = 0;
  for (var i = 0; i <= text.length; i++) {
    var ch = i < text.length ? text.charCodeAt(i) : 13;
    if (ch !== 13 && ch !== 12 && ch !== 7 && ch !== 14) continue;
    var el = document.createElement("p");
    var line = text.slice(start, i).replace(/[\u0000-\u0008\u000B\u000E-\u001F]/g, "");
    el.textContent = line;
    el.style.marginBottom = "6pt";
    into.appendChild(el);
    if (ch === 12 && i < text.length - 1) into = vwProseSheet(state.prose);
    start = i + 1;
  }
}
