function togglePassword(inputId, button) {
    var input = document.getElementById(inputId);
    if (!input) return;

    if (input.type === "password") {
        input.type = "text";
        button.textContent = "🙈";
        button.setAttribute("aria-label", "Ẩn mật khẩu");
    } else {
        input.type = "password";
        button.textContent = "👁";
        button.setAttribute("aria-label", "Hiện mật khẩu");
    }
}

window.togglePassword = togglePassword;
