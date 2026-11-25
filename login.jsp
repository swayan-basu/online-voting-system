<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Election Commission Official Login</title>
    <link rel="stylesheet" href="../css/style.css">
</head>
<body>
<main class="commission-login">
    <section class="commission-login-panel">
        <div class="brand-lockup"><span class="brand-mark">EC</span><span>Election Commission</span></div>
        <div class="commission-login-copy">
            <span class="eyebrow">AUTHORIZED OFFICIALS ONLY</span>
            <h1>Election control room</h1>
            <p>Manage voting hours, monitor live turnout, review candidate totals, and declare the official result.</p>
        </div>
        <div class="commission-login-footer">Secure administrative access &bull; Vote identities remain confidential</div>
    </section>
    <section class="commission-login-card">
        <span class="eyebrow">OFFICIAL SIGN IN</span>
        <h1>Welcome, official</h1>
        <p class="subtitle">Enter your Election Commission credentials.</p>
        <% if ("InvalidCredentials".equals(request.getParameter("error"))) { %>
            <div class="alert alert-error">Invalid official email or password.</div>
        <% } %>
        <form action="../login" method="post">
            <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
            <div class="form-group"><label for="email">Official email</label><input type="email" id="email" name="email" placeholder="commission@eci.gov.in" autocomplete="username" required></div>
            <div class="form-group"><label for="password">Password</label><input type="password" id="password" name="password" placeholder="Enter password" autocomplete="current-password" required></div>
            <button type="submit" class="btn">Enter control room <span>→</span></button>
        </form>
        <p class="form-footer"><a href="../index.jsp">Return to voter login</a></p>
    </section>
</main>
</body>
</html>
