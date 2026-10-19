"use strict";

/*
 * Word's own pages, issue #47.
 *
 * docx-preview starts a page only at a page or section break, so a document written as one
 * flow of text came out as a single sheet. Word records where each page began when it last
 * laid the file out, as w:lastRenderedPageBreak, and where a file carries that record its
 * pages are split there and numbered here. A file without it, as WPS, LibreOffice and
 * Google Docs write them, is drawn exactly as before.
 */
var vwPages = {
  /* Whether this document's pages are Word's */
  split: false,
  /* Whether the drawing on screen is the last, which the page counter and night mode wait for:
     a second drawing replaces the first's pages and everything marked on them */
  settled: false,
  /* Word's own count of its pages when it last saved the file, from docProps/app.xml */
  wordPages: 0,
  /* The places a second drawing starts a page Word left no record of; see vwFindMissingPages */
  turnAt: null,
  /* The section properties each page was made with, in order */
  props: [],
  /* Fields being read, innermost last; see trackField */
  fields: [],
  nextField: 0
};

function vwIsPageMark(node) {
  return node.type === "break" && node.break === "lastRenderedPageBreak";
}

function vwHasPageMark(node) {
  var kids = node.children || [];
  for (var i = 0; i < kids.length; i++) {
    if (vwIsPageMark(kids[i]) || vwHasPageMark(kids[i])) return true;
  }
  return false;
}

/* Whether anything would show: a paragraph that only holds a bookmark or a break is empty. */
var VW_BLANK = { "break": 1, bookmarkStart: 1, bookmarkEnd: 1 };
var VW_HOLDERS = { run: 1, hyperlink: 1, smartTag: 1, inserted: 1, paragraph: 1 };

function vwHasInk(node) {
  var kids = node.children || [];
  for (var i = 0; i < kids.length; i++) {
    var c = kids[i];
    if (VW_BLANK[c.type]) continue;
    if (VW_HOLDERS[c.type] ? vwHasInk(c) : true) return true;
  }
  return false;
}

function vwColumns(props) {
  return (props && props.columns && props.columns.numberOfColumns) || 1;
}

/*
 * Word records where each column began as well as each page, and the two look the same. So a
 * section set in columns loses every record, [all], and a section running on from one loses
 * the record at its top, which marks where those columns ended.
 */
function vwColumnMarksOut(elements, all) {
  var inked = false;
  function out(node) {
    if (!node.children) return node;
    var kept = [], changed = false;
    node.children.forEach(function (c) {
      if (vwIsPageMark(c) && (all || !inked)) { changed = true; return; }
      if (!c.children && !VW_BLANK[c.type]) inked = true;
      var d = out(c);
      if (d !== c) changed = true;
      kept.push(d);
    });
    return changed ? Object.assign({}, node, { children: kept }) : node;
  }
  return elements.map(out);
}

/*
 * A section with no header or footer of a kind of its own shows the one before it, as Word
 * does. docx-preview drew none there, which on a document of several sections left the page
 * numbers off every page after the first section.
 */
function vwInheritRefs(prev, props, key) {
  var own = props[key] || [];
  var inherited = (prev && prev[key] || []).filter(function (ref) {
    return !own.some(function (mine) { return mine.type === ref.type; });
  });
  if (inherited.length) props[key] = own.concat(inherited);
}

/*
 * Installed from the h hook in docx.js, before the first page is made. Replaces how
 * docx-preview groups the body into pages, for a document that records Word's own.
 */
function vwSplitAsWordDid(renderer) {
  var splitBySection = renderer.splitBySection;
  var groupByPageBreaks = renderer.groupByPageBreaks;
  var createPageElement = renderer.createPageElement;
  var renderHeaderFooter = renderer.renderHeaderFooter;
  var renderRun = renderer.renderRun;
  var renderParagraph = renderer.renderParagraph;
  var renderTableRow = renderer.renderTableRow;

  renderer.splitBySection = function (elements, bodyProps) {
    /* A second drawing counts its pages and fields afresh */
    vwPages.props = [];
    vwPages.fields = [];
    var app = this.document.extendedPropsPart;
    vwPages.wordPages = (app && app.props && app.props.pages) || 0;
    vwPages.split = elements.some(vwHasPageMark);
    if (!vwPages.split) return splitBySection.call(this, elements, bodyProps);
    return splitPieces(this, elements, bodyProps);
  };

  renderer.groupByPageBreaks = function (pieces) {
    if (!vwPages.split) return groupByPageBreaks.call(this, pieces);
    var pages = [], page = [];
    pieces.forEach(function (piece) {
      page.push(piece);
      if (piece.pageBreak) { pages.push(page); page = []; }
    });
    if (page.length) pages.push(page);
    return pages;
  };

  renderer.createPageElement = function (className, props, docStyle) {
    vwPages.props.push(props);
    return createPageElement.call(this, className, props, docStyle);
  };

  /* A header or footer is read on its own, so a field left open in one cannot reach another */
  renderer.renderHeaderFooter = function () {
    var outer = vwPages.fields;
    vwPages.fields = [];
    try {
      return renderHeaderFooter.apply(this, arguments);
    } finally {
      vwPages.fields = outer;
    }
  };

  renderer.renderRun = function (run) {
    if (run.fieldRun) trackField(run);
    var span = renderRun.call(this, run);
    var field = vwPages.fields[vwPages.fields.length - 1];
    if (span && field && field.showing && field.kind) {
      span.setAttribute("data-vw-field", field.id + " " + field.kind + " " + field.format);
    }
    return span;
  };

  /* The places splitPieces numbered, marked for vwFindMissingPages to measure */
  renderer.renderParagraph = function (para) {
    return vwMarkPlace(renderParagraph.call(this, para), para);
  };
  renderer.renderTableRow = function (row) {
    return vwMarkPlace(renderTableRow.call(this, row), row);
  };
}

function vwMarkPlace(el, node) {
  var spot = node.vwPlace;
  if (el && spot && spot.canTurn) el.setAttribute("data-vw-place", spot.n + " " + spot.stretch);
  return el;
}

/* A paragraph with no run: Word records where a page began in its first run, so here it cannot */
function vwRunless(node) {
  return !(node.children || []).some(function (c) {
    return c.type === "run" || !vwRunless(c);
  });
}

/* docx-preview reads Word's <w:tblHeader/>, which carries no value, as null, and a row
   without one has no isHeader */
function vwIsHeaderRow(row) {
  return "isHeader" in row && row.isHeader !== false;
}

function vwRowStartsRunless(row) {
  var cell = (row.children || [])[0];
  var first = cell && (cell.children || [])[0];
  return !!first && first.type === "paragraph" && vwRunless(first);
}

/*
 * A field is a code between its begin and separate marks, then the value it showed between
 * separate and end. docx-preview drops the code and draws that saved value, so the runs
 * showing a page number are marked here for vwNumberPages to write the right one into.
 */
var VW_FIELD = /^\s*(PAGE|NUMPAGES|SECTIONPAGES)\b(.*)$/i;
var VW_FIELD_FORMATS = {
  roman: "lowerRoman", ROMAN: "upperRoman", Roman: "upperRoman",
  alphabetic: "lowerLetter", ALPHABETIC: "upperLetter", Alphabetic: "upperLetter",
  arabic: "decimal", Arabic: "decimal", ARABIC: "decimal", ArabicDash: "numberInDash"
};

function trackField(run) {
  var fields = vwPages.fields;
  (run.children || []).forEach(function (part) {
    var top = fields[fields.length - 1];
    if (part.type === "instruction") {
      if (top && !top.showing) top.code += part.text || "";
    } else if (part.type === "complexField") {
      if (part.charType === "begin") {
        fields.push({ id: ++vwPages.nextField, code: "", showing: false });
      } else if (part.charType === "separate" && top) {
        top.showing = true;
        var said = VW_FIELD.exec(top.code);
        if (said) {
          var as = /\\\*\s*(\w+)/.exec(said[2]);
          top.kind = said[1].toUpperCase();
          top.format = (as && VW_FIELD_FORMATS[as[1]]) || "-";
        }
      } else if (part.charType === "end") {
        fields.pop();
      }
    }
  });
}

/*
 * The body as pages. A section break starts one unless the next section runs on, a page
 * break always does, and Word's record does once the page has something on it, so a record
 * that repeats a break already made adds no empty page.
 */
function splitPieces(renderer, elements, bodyProps) {
  var sections = [], held = [];
  elements.forEach(function (el) {
    held.push(el);
    if (el.type === "paragraph" && el.sectionProps) {
      sections.push({ props: el.sectionProps, elements: held });
      held = [];
    }
  });
  sections.push({ props: bodyProps, elements: held });

  var pieces = [], piece = null, inked = false;
  /* Places numbered, stretches between ink, and places with no ink after them yet */
  var places = 0, stretch = 0, open = [];
  function start(props) {
    piece = { sectProps: props, elements: [], pageBreak: false };
    pieces.push(piece);
  }
  /*
   * Numbers a place a page can begin without Word's record, the same on every drawing, and
   * says whether this drawing turns there. A turn makes a page only with ink above and below
   * it on the page, and places with no ink between them share a stretch, one turn to each.
   */
  function place(node, canTurn) {
    /* An object, so the copy of the paragraph that is drawn sees a verdict reached later */
    var spot = node.vwPlace = { n: places++, stretch: stretch, canTurn: canTurn };
    if (canTurn) open.push(spot);
    return !!(vwPages.turnAt && vwPages.turnAt[spot.n]);
  }
  function inkSeen() {
    stretch++;
    open = [];
  }
  function pageEnds() {
    open.forEach(function (spot) { spot.canTurn = false; });
    open = [];
  }
  function turn(hard) {
    if (!inked && !hard) return;
    pageEnds();
    piece.pageBreak = true;
    start(piece.sectProps);
    inked = false;
  }
  /* A table's ink is seen row by row as it is read, since its rows are added in slices */
  function add(el) {
    piece.elements.push(el);
    if (el.type === "paragraph" || el.type === "table" ? vwHasInk(el) : !VW_BLANK[el.type]) {
      inked = true;
      if (el.type !== "table") inkSeen();
    }
  }
  function rows(table, list) { return Object.assign({}, table, { children: list }); }

  sections.forEach(function (s, si) {
    var prev = si > 0 ? sections[si - 1].props : null;
    var runsOn = false;
    if (prev) {
      vwInheritRefs(prev, s.props, "headerRefs");
      vwInheritRefs(prev, s.props, "footerRefs");
      runsOn = (s.props.type === "continuous" || s.props.type === "nextColumn") &&
        !renderer.isPageBreakSection(prev, s.props);
      if (!runsOn) {
        pageEnds();
        piece.pageBreak = true;
        inked = false;
      }
    }
    start(s.props);

    var columned = vwColumns(s.props) > 1;
    var elements = columned || (runsOn && vwColumns(prev) > 1)
      ? vwColumnMarksOut(s.elements, columned) : s.elements;
    /* Columns fill side by side, so how far down a page a place sits says nothing in them */
    var placing = !columned;

    elements.forEach(function (el) {
      if (el.type === "table") {
        var all = el.children || [], heads = [], from = 0;
        while (heads.length < all.length && vwIsHeaderRow(all[heads.length])) heads.push(all[heads.length]);
        for (var r = 0; r < all.length; r++) {
          /* Whether rows of the body, or what came before the table, are above this row */
          var above = r > Math.max(from, heads.length) || (!from && inked);
          var turns = placing && !vwIsHeaderRow(all[r]) && vwRowStartsRunless(all[r]) &&
            place(all[r], above);
          if (r === from && (turns || vwHasPageMark(all[r]))) {
            turn();
          } else if (turns || vwHasPageMark(all[r])) {
            add(rows(el, (from >= heads.length && from ? heads : []).concat(all.slice(from, r))));
            turn();
            from = r;
          }
          if (vwHasInk(all[r])) inkSeen();
        }
        add(from ? rows(el, (from >= heads.length ? heads : []).concat(all.slice(from))) : el);
        return;
      }
      if (el.type !== "paragraph") { add(el); return; }
      if (placing && vwRunless(el) && place(el, inked)) turn();

      var style = renderer.findStyle(el.styleName);
      if (el.pageBreakBefore || (style && style.paragraphProps && style.paragraphProps.pageBreakBefore)) {
        turn();
      }

      var part = Object.assign({}, el, { children: [] });
      (el.children || []).forEach(function (run) {
        var rest = run;
        var at = run.type === "run" && run.children ? run.children.findIndex(isBreak) : -1;
        while (at >= 0) {
          if (at > 0) part.children.push(Object.assign({}, rest, { children: rest.children.slice(0, at) }));
          var hard = rest.children[at].break === "page";
          var before = vwHasInk(part);
          if (before || hard) {
            add(part);
            turn(hard);
            part = Object.assign({}, el, { children: [] });
            /* The rest of a paragraph a page cut in two: no second list number, no indent */
            if (before) part.className = [el.className, "vw-cont"].filter(Boolean).join(" ");
          } else {
            turn();
          }
          rest = Object.assign({}, rest, { children: rest.children.slice(at + 1) });
          at = rest.children.findIndex(isBreak);
        }
        if (rest === run || rest.children.length) part.children.push(rest);
      });
      add(part);
    });
  });

  pageEnds();

  /* An empty page left by a break at a section's end gives its break to the page before */
  var kept = [];
  pieces.forEach(function (p) {
    if (p.elements.length || !kept.length) kept.push(p);
    else if (p.pageBreak) kept[kept.length - 1].pageBreak = true;
  });
  return kept;
}

function isBreak(node) {
  return node.type === "break" && (node.break === "page" || node.break === "lastRenderedPageBreak");
}

/*
 * Word records no page that begins on a paragraph without a run, such as a blank line or an
 * empty first cell, so its own count of pages says how many went unrecorded. Resolves true
 * once vwPages.turnAt has places for them, for the document to be drawn again.
 */
function vwFindMissingPages() {
  var sheets = vwSheets(), missing = vwPages.wordPages - sheets.length;
  if (!vwPages.split || !(missing > 0)) return Promise.resolve(false);
  /* A layout first, so a font the document embeds is loading by the time fonts.ready is read */
  document.body.getBoundingClientRect();
  return document.fonts.ready.then(function () {
    vwPages.turnAt = vwPlaceTurns([].map.call(sheets, vwMeasureSheet), missing);
    return !!vwPages.turnAt;
  });
}

/* Over its paper by more than 2%: lines as Word sets them (docx-lines.js) run that close to Word's */
var VW_OVERFLOW = 1.02;

/*
 * Each missing page goes to the sheet that runs furthest over its paper, while one does and
 * has a place to turn for it.
 */
function vwPlaceTurns(plan, missing) {
  var given = 0, turnAt = null;
  while (given < missing) {
    var best = null, most = VW_OVERFLOW;
    plan.forEach(function (s) {
      var over = s.height / ((s.turns.length + 1) * s.room);
      if (s.room > 0 && !s.full && over > most) { best = s; most = over; }
    });
    if (!best) break;
    var turns = vwTurnsOn(best, best.turns.length + 2);
    if (turns) {
      best.turns = turns;
      given++;
    } else {
      best.full = true;
    }
  }
  plan.forEach(function (s) {
    s.turns.forEach(function (spot) { (turnAt = turnAt || {})[spot.n] = true; });
  });
  return turnAt;
}

/*
 * Where a sheet turns to make [pages] pages: at the place nearest each even share of it, one
 * to a stretch. Word turns at a blank line or a row only once the page above is full, so a
 * turn leaving less than half a page above it is not one Word made, and null says so.
 */
function vwTurnsOn(sheet, pages) {
  var turns = [], next = 0, above = 0;
  for (var j = 1; j < pages; j++) {
    var want = j * sheet.height / pages, pick = null, stretch = 0;
    for (var r = next; r < sheet.places.length; r++) {
      for (var q = 0; q < sheet.places[r].length; q++) {
        var spot = sheet.places[r][q];
        if (spot.at - above < sheet.room / 2) continue;
        if (!pick || Math.abs(spot.at - want) < Math.abs(pick.at - want)) {
          pick = spot;
          stretch = r;
        }
      }
    }
    if (!pick) return null;
    turns.push(pick);
    above = pick.at;
    next = stretch + 1;
  }
  return turns;
}

/*
 * How far down a sheet its body runs, the room its paper has for one, and the places a page
 * could begin on it, a list to each stretch, read before fitPageWidth zooms the sheets.
 */
function vwMeasureSheet(sheet) {
  var style = getComputedStyle(sheet);
  var padTop = parseFloat(style.paddingTop) || 0;
  var room = (parseFloat(style.minHeight) || 0) - padTop - (parseFloat(style.paddingBottom) || 0);
  var top = sheet.getBoundingClientRect().top + padTop, bottom = top;
  /* To the last ink, since Word lets a page's last space after fall below its margin */
  var blocks = sheet.querySelectorAll(":scope > article > *");
  for (var a = 0; a < blocks.length; a++) {
    bottom = Math.max(bottom, blocks[a].getBoundingClientRect().bottom);
  }
  var places = [], last = null;
  var marked = sheet.querySelectorAll(":scope > article [data-vw-place]");
  for (var m = 0; m < marked.length; m++) {
    var said = marked[m].getAttribute("data-vw-place").split(" ");
    var spot = { n: Number(said[0]), at: marked[m].getBoundingClientRect().top - top };
    if (said[1] === last) places[places.length - 1].push(spot);
    else places.push([spot]);
    last = said[1];
  }
  return { room: room, height: bottom - top, places: places, turns: [], full: false };
}

/*
 * Writes each page's number into the fields marked while it was drawn, counting as Word
 * does: on from the page before, unless a section restarts it, in the section's format
 * unless the field asks for its own.
 */
function vwNumberPages(sheets) {
  if (!vwPages.split || sheets.length !== vwPages.props.length) return;
  var numbers = [], sectionOf = [], sectionPages = [];
  for (var i = 0; i < sheets.length; i++) {
    var props = vwPages.props[i];
    var first = i === 0 || props !== vwPages.props[i - 1];
    var start = props.pageNumber && props.pageNumber.start;
    numbers.push(first && isFinite(start) && start !== null ? start : (i ? numbers[i - 1] + 1 : 1));
    if (first) sectionPages.push(0);
    sectionOf.push(sectionPages.length - 1);
    sectionPages[sectionPages.length - 1]++;
  }

  for (var p = 0; p < sheets.length; p++) {
    var own = vwPages.props[p].pageNumber;
    var seen = {};
    var marked = sheets[p].querySelectorAll("[data-vw-field]");
    for (var m = 0; m < marked.length; m++) {
      var said = marked[m].getAttribute("data-vw-field").split(" ");
      var text = "";
      if (!seen[said[0]]) {
        seen[said[0]] = true;
        var value = said[1] === "PAGE" ? numbers[p]
          : said[1] === "NUMPAGES" ? sheets.length : sectionPages[sectionOf[p]];
        var format = said[2] !== "-" ? said[2] : said[1] === "PAGE" && own ? own.format : "";
        text = vwFormatPageNumber(value, format);
      }
      writeText(marked[m], text);
    }
  }
}

/* The first text in [el] says [text] and the rest says nothing, so the run keeps its look */
function writeText(el, text) {
  var walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT);
  var node, done = false;
  while ((node = walker.nextNode())) {
    node.nodeValue = done ? "" : text;
    done = true;
  }
}

function vwFormatPageNumber(n, format) {
  switch (format) {
    case "lowerRoman": return roman(n).toLowerCase();
    case "upperRoman": return roman(n);
    case "lowerLetter": return letters(n).toLowerCase();
    case "upperLetter": return letters(n);
    case "numberInDash": return "- " + n + " -";
    default: return String(n);
  }
}

function roman(n) {
  if (n < 1 || n > 3999) return String(n);
  var out = "", marks = [[1000, "M"], [900, "CM"], [500, "D"], [400, "CD"], [100, "C"],
    [90, "XC"], [50, "L"], [40, "XL"], [10, "X"], [9, "IX"], [5, "V"], [4, "IV"], [1, "I"]];
  marks.forEach(function (m) {
    while (n >= m[0]) { out += m[1]; n -= m[0]; }
  });
  return out;
}

/* A to Z, then AA to ZZ, as Word counts in letters */
function letters(n) {
  if (n < 1) return String(n);
  var c = String.fromCharCode(65 + (n - 1) % 26);
  return new Array(Math.floor((n - 1) / 26) + 2).join(c);
}

/*
 * The page under the middle of the screen, told to the app for its page counter and Go to
 * page, as pdf.html does. A zoomed page's rect leaves the zoom out below about Chromium
 * 128 (see fitPageWidth in docx.js), so rects are read through vwPages.rectScale.
 */
var vwPageSent = 0;
var vwPageAskedFor = null;
var vwPageFrame = 0;

function vwSheets() {
  return document.querySelectorAll(".docx-wrapper > section.docx");
}

function vwSheetTop(sheet) {
  return sheet.getBoundingClientRect().top / (vwPages.rectScale || 1);
}

function vwPageOnScreen(sheets, vv) {
  if (vv.pageTop <= 1) return 1;
  if (vv.pageTop + vv.height >= document.documentElement.scrollHeight - 1) return sheets.length;
  var middle = vv.pageTop + vv.height / 2;
  var lo = 0, hi = sheets.length - 1, at = 0;
  while (lo <= hi) {
    var mid = (lo + hi) >> 1;
    if (vwSheetTop(sheets[mid]) + window.scrollY <= middle) { at = mid; lo = mid + 1; } else { hi = mid - 1; }
  }
  return at + 1;
}

function vwReportPage() {
  var sheets = vwSheets();
  var vv = window.visualViewport;
  if (!vwPages.settled || sheets.length < 2 || !vv || !vv.height) return;
  /* A page Go to page put on screen is named until the reader moves */
  if (vwPageAskedFor && Math.abs(vv.pageTop - vwPageAskedFor.top) > 1) vwPageAskedFor = null;
  var n = vwPageAskedFor ? vwPageAskedFor.n : vwPageOnScreen(sheets, vv);
  if (n !== vwPageSent && vwFind.post("page " + n + " " + sheets.length)) vwPageSent = n;
}

function vwSchedulePageReport() {
  if (vwPageFrame) return;
  vwPageFrame = requestAnimationFrame(function () { vwPageFrame = 0; vwReportPage(); });
}

/* At the top of the screen, or in the middle for a page too short for the middle to fall on */
function vwGoToPage(n) {
  var sheets = vwSheets();
  if (!(n >= 1 && n <= sheets.length)) return;
  var sheet = sheets[n - 1];
  var vv = window.visualViewport;
  var tall = sheet.getBoundingClientRect().height / (vwPages.rectScale || 1);
  sheet.scrollIntoView({ block: vv && tall <= vv.height / 2 ? "center" : "start" });
  vwPageAskedFor = vv ? { n: n, top: vv.pageTop } : null;
  vwReportPage();
}

function vwFollowPages() {
  if (vwSheets().length < 2) return;
  window.addEventListener("scroll", vwSchedulePageReport, { passive: true });
  if (window.visualViewport) {
    window.visualViewport.addEventListener("scroll", vwSchedulePageReport, { passive: true });
    window.visualViewport.addEventListener("resize", vwSchedulePageReport, { passive: true });
  }
  vwReportPage();
}
