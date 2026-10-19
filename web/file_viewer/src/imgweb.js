var img = document.getElementById("img");
// An empty alt hides the image from screen readers entirely; the filename is
// the only description we have
img.alt = vwName;
if (vwExt === "svg") img.className = "svg";
img.onerror = function () {
  vwError("Could not display this image", vwName);
};
img.src = "/doc/file" + (vwExt ? "." + vwExt : "");
