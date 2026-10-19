/* Nothing is read below marked's floor. The card is already up, and the library in the
   head either did not parse, below Chromium 80, or would fail on its first document,
   below 92: issue #31, where this said "marked is not defined" instead. See
   vwWebViewTooOld in app.js. */
if (!vwWebViewTooOld("Markdown files")) {
  vwFetchDoc("text")
    .then(function (txt) {
      var html = marked.parse(txt, { gfm: true, breaks: false });
      document.getElementById("content").innerHTML = DOMPurify.sanitize(html);
      vwStatusDone();
    })
    .catch(function (e) { vwError("Could not render this Markdown file", String(e)); });
}
