/*
 * Shared password show/hide toggle for the auth screens (login, register,
 * reset password). Progressive enhancement only: the toggle button stays
 * hidden until this script reveals it, so no-JS submission is intact.
 * Extracted verbatim from the former inline blocks (Phase 6).
 */
(function () {
    "use strict";
    var wrappers = document.querySelectorAll("[data-password-field]");
    for (var i = 0; i < wrappers.length; i++) {
        (function (wrapper) {
            var input = wrapper.querySelector("input");
            var toggle = wrapper.querySelector("[data-password-toggle]");
            if (!input || !toggle) {
                return;
            }
            toggle.hidden = false;
            toggle.addEventListener("click", function () {
                var show = input.type === "password";
                input.type = show ? "text" : "password";
                toggle.textContent = show ? "Hide" : "Show";
                toggle.setAttribute("aria-pressed", show ? "true" : "false");
                input.focus();
            });
        })(wrappers[i]);
    }
})();
