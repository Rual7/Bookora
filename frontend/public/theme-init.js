(function () {
  var theme = "light";
  try {
    var stored = localStorage.getItem("bookora-theme");
    if (stored === "light" || stored === "dark") {
      theme = stored;
    } else if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
      theme = "dark";
    }
  } catch {
    if (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) {
      theme = "dark";
    }
  }
  document.documentElement.dataset.theme = theme;

  // If the app fails before it can draw anything, say so on the page instead of leaving it empty.
  window.addEventListener("error", function (event) {
    var message = document.getElementById("boot-message");
    if (message) {
      message.textContent = "Bookora could not start: " + event.message;
    }
  });
})();
