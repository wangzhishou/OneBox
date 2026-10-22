document.getElementById("fname").textContent = vwName;
// 没有补充说明：这张卡片的标题与按钮文案由 i18n.js 按界面语言翻译
document.getElementById("hint").style.display = "none";

document.getElementById("asText").addEventListener("click", function () {
  location.replace(
    "text.html?name=" + encodeURIComponent(vwName) + "&ext=" + encodeURIComponent(vwExt)
  );
});
