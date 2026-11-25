<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VoteSecure | Create account</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <main class="auth-shell">
        <section class="brand-panel">
            <div class="brand-lockup"><span class="brand-mark">✓</span><span>VoteSecure</span></div>
            <div class="brand-copy"><span class="eyebrow">JOIN THE ELECTION</span><h1>Your vote starts with you.</h1><p>Create your secure voter account and take part in the decision.</p></div>
            <img class="brand-illustration" src="assets/voter-ballot.svg" alt="Voter placing a ballot into a secure ballot box">
            <div class="trust-row"><span class="trust-icon">◈</span><span>Private and protected</span><span class="trust-icon">◈</span><span>Built for everyone</span></div>
        </section>
        <section class="auth-card">
            <div class="mobile-brand"><div class="brand-lockup"><span class="brand-mark">✓</span><span>VoteSecure</span></div></div>
            <span class="eyebrow">CREATE ACCOUNT</span><h2>Register to vote</h2><p class="subtitle">It only takes a minute to get started.</p>
            <% if ("RegistrationFailed".equals(request.getParameter("error"))) { %><div class="alert alert-error">Registration failed. That identity number or email may already be registered.</div><% } %>
            <% if ("InvalidIdentityFormat".equals(request.getParameter("error"))) { %><div class="alert alert-error">Enter a correctly formatted PAN or Aadhaar number.</div><% } %>
            <% if ("IdentityVerificationFailed".equals(request.getParameter("error"))) { %><div class="alert alert-error">The demo identity is not in the approved synthetic voter registry.</div><% } %>
            <% if ("InvalidVoterId".equals(request.getParameter("error"))) { %><div class="alert alert-error">Enter a valid voter card number using 6–20 letters, numbers, or hyphens.</div><% } %>
            <% if ("WeakPassword".equals(request.getParameter("error"))) { %><div class="alert alert-error">Use at least 16 characters with uppercase, lowercase, a number, and a symbol.</div><% } %>
            <form action="register" method="post">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <div class="form-group"><label for="name">Full name</label><input type="text" id="name" name="name" placeholder="Alice Smith" autocomplete="name" required></div>
                <div class="form-group"><label for="voterId">Voter card number <span class="required-label">Required</span></label><input type="text" id="voterId" name="voterId" placeholder="e.g. ABC1234567" autocomplete="off" minlength="6" maxlength="20" pattern="[A-Za-z0-9\x2d]{6,20}" required><small class="field-help">This ID will be used only once for one voter account.</small></div>
                <div class="form-group"><label for="documentType">Identity document</label><select id="documentType" name="documentType" required><option value="AADHAAR">Aadhaar card</option><option value="PAN">PAN card</option></select></div>
                <div class="form-group"><label id="documentNumberLabel" for="documentNumber">Aadhaar card number</label><input type="text" id="documentNumber" name="documentNumber" placeholder="Enter 12-digit Aadhaar number" inputmode="numeric" maxlength="12" pattern="[0-9]{12}" autocomplete="off" required><small id="identityStatus" class="field-help">Format is checked locally. Ownership is not verified in demo mode.</small></div>
                <div class="form-group"><label for="email">Email address</label><input type="email" id="email" name="email" placeholder="you@example.com" autocomplete="email" required></div>
                <div class="form-group password-group"><label for="password">Create strong password</label><input type="password" id="password" name="password" placeholder="Minimum 16 characters" autocomplete="new-password" minlength="16" required><div class="password-meter"><span id="strengthBar"></span></div><small id="passwordStatus" class="field-help">Use at least 16 characters: uppercase, lowercase, number, and symbol.</small><button type="button" id="generatePassword" class="suggest-button">Suggest a strong password</button></div>
                <button type="submit" class="btn">Create voter account <span>→</span></button>
            </form>
            <p class="form-footer">Already registered? <a href="index.jsp">Sign in here</a></p>
        </section>
    </main>
    <script src="js/register.js"></script>
</body>
</html>