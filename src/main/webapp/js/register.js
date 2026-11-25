(function () {
    "use strict";

    const type = document.getElementById("documentType");
    const number = document.getElementById("documentNumber");
    const label = document.getElementById("documentNumberLabel");
    const status = document.getElementById("identityStatus");
    const password = document.getElementById("password");
    const passwordStatus = document.getElementById("passwordStatus");
    const strengthBar = document.getElementById("strengthBar");
    const generatePassword = document.getElementById("generatePassword");

    if (!type || !number || !password || !generatePassword) return;

    function updateDocumentField() {
        const pan = type.value === "PAN";
        label.textContent = pan ? "PAN card number" : "Aadhaar card number";
        number.value = pan
            ? number.value.toUpperCase()
            : number.value.replace(/\D/g, "");
        number.maxLength = pan ? 10 : 12;
        number.inputMode = pan ? "text" : "numeric";
        number.pattern = pan ? "[A-Z]{5}[0-9]{4}[A-Z]" : "[0-9]{12}";
        number.placeholder = pan ? "ABCDE1234F" : "Enter 12-digit Aadhaar number";
        validateIdentity();
    }

    function scorePassword(value) {
        let score = 0;
        if (value.length >= 16) score++;
        if (value.length >= 24) score++;
        if (/[a-z]/.test(value)) score++;
        if (/[A-Z]/.test(value)) score++;
        if (/[0-9]/.test(value)) score++;
        if (/[^A-Za-z0-9]/.test(value)) score++;
        return score;
    }

    function validatePassword() {
        const value = password.value;
        const score = scorePassword(value);
        strengthBar.style.width = Math.max(12, score * 20) + "%";
        strengthBar.className = score <= 2 ? "weak" : score <= 4 ? "good" : "strong";
        const valid = value.length >= 16
            && /[a-z]/.test(value)
            && /[A-Z]/.test(value)
            && /[0-9]/.test(value)
            && /[^A-Za-z0-9]/.test(value);

        if (!value) {
            passwordStatus.textContent = "Use at least 16 characters: uppercase, lowercase, number, and symbol.";
            passwordStatus.style.color = "";
        } else if (!valid) {
            passwordStatus.textContent = "Choose a stronger password: add a capital letter, number, and symbol.";
            passwordStatus.style.color = "#a63d3d";
        } else {
            passwordStatus.textContent = "Strong password — at least 16 characters and all character types included.";
            passwordStatus.style.color = "#16835d";
        }
        password.setCustomValidity(valid
            ? ""
            : "Password must have at least 16 characters with uppercase, lowercase, number, and symbol");
    }

    function cryptoText(length) {
        const upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        const lower = "abcdefghijkmnopqrstuvwxyz";
        const digits = "23456789";
        const symbols = "!@#$%&*_-";
        const all = upper + lower + digits + symbols;
        const chosen = [];

        [upper, lower, digits, symbols].forEach(function (set) {
            const random = new Uint32Array(1);
            crypto.getRandomValues(random);
            chosen.push(set[random[0] % set.length]);
        });

        const remaining = new Uint32Array(length - chosen.length);
        crypto.getRandomValues(remaining);
        remaining.forEach(function (value) {
            chosen.push(all[value % all.length]);
        });

        for (let i = chosen.length - 1; i > 0; i--) {
            const random = new Uint32Array(1);
            crypto.getRandomValues(random);
            const j = random[0] % (i + 1);
            [chosen[i], chosen[j]] = [chosen[j], chosen[i]];
        }
        return chosen.join("");
    }

    password.addEventListener("input", validatePassword);
    generatePassword.addEventListener("click", function () {
        password.value = cryptoText(16);
        password.type = "text";
        validatePassword();
        password.focus();
        passwordStatus.textContent = "Strong password generated and added to the password field.";
        passwordStatus.style.color = "#16835d";
        window.setTimeout(function () {
            password.type = "password";
        }, 2200);
    });

    function validateIdentity() {
        const value = number.value.trim();
        if (!value) {
            status.textContent = "Enter a valid Aadhaar or PAN number.";
            status.style.color = "";
            number.setCustomValidity("");
            return;
        }
        const validPan = /^[A-Z]{5}[0-9]{4}[A-Z]$/.test(value);
        const validAadhaar = /^[0-9]{12}$/.test(value);
        const valid = type.value === "PAN" ? validPan : validAadhaar;
        status.textContent = valid
            ? "Format verified — ownership is not verified in demo mode"
            : "Invalid " + (type.value === "PAN" ? "PAN" : "Aadhaar") + " number. Enter correct details.";
        status.style.color = valid ? "#16835d" : "#a63d3d";
        number.setCustomValidity(valid
            ? ""
            : "Enter a valid " + (type.value === "PAN" ? "PAN" : "Aadhaar") + " number");
    }

    type.addEventListener("change", updateDocumentField);
    number.addEventListener("input", function () {
        if (type.value === "PAN") {
            number.value = number.value.toUpperCase();
        } else {
            number.value = number.value.replace(/\D/g, "");
        }
        validateIdentity();
    });

    validatePassword();
    updateDocumentField();
})();
