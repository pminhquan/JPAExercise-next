/*
 * Six-digit OTP visual for verify-otp and forgot-password. Presentation
 * only: the six boxes mirror the single real <input name="otp">. The real
 * input keeps native caret, backspace, and paste behavior and remains the
 * only submitted field named `otp`. Without JavaScript the plain input is
 * shown and the boxes stay hidden. Extracted verbatim from the former
 * inline blocks (Phase 6).
 */
(function () {
    "use strict";
    var field = document.querySelector("[data-otp]");
    if (!field) {
        return;
    }
    var input = field.querySelector(".otp-real");
    var digits = field.querySelectorAll(".otp-digit");
    if (!input || digits.length === 0) {
        return;
    }

    field.classList.add("otp-field--enhanced");

    function render() {
        var value = input.value;
        var caret = typeof input.selectionStart === "number" ? input.selectionStart : value.length;
        var focused = document.activeElement === input;
        for (var i = 0; i < digits.length; i++) {
            digits[i].textContent = i < value.length ? value.charAt(i) : "";
            digits[i].classList.toggle("otp-digit--filled", i < value.length);
            digits[i].classList.toggle("otp-digit--active", focused && i === caret);
        }
    }

    input.addEventListener("input", render);
    input.addEventListener("click", render);
    input.addEventListener("keyup", render);
    input.addEventListener("focus", function () {
        if (typeof input.setSelectionRange === "function") {
            input.setSelectionRange(input.value.length, input.value.length);
        }
        render();
    });
    input.addEventListener("blur", render);
    render();
})();
