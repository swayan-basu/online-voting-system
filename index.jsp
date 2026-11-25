<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Online Voting System - Login</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <main class="auth-shell">
        <section class="brand-panel">
            <div class="brand-lockup"><span class="brand-mark">✓</span><span>VoteSecure</span></div>
            <div class="brand-copy">
                <span class="eyebrow">YOUR VOICE MATTERS</span>
                <h1>A simpler way to make your voice heard.</h1>
                <p>Cast your vote with confidence through a secure, transparent digital ballot.</p>
            </div>
            <img class="brand-illustration" src="assets/voter-ballot.svg" alt="Voter placing a ballot into a secure ballot box">
            <div class="trust-row"><span class="trust-icon">◈</span><span>One person, one vote</span><span class="trust-icon">◈</span><span>Results you can trust</span></div>
        </section>
        <section class="auth-card">
            <div class="mobile-brand"><div class="brand-lockup"><span class="brand-mark">✓</span><span>VoteSecure</span></div></div>
            <span class="eyebrow">VOTER PORTAL</span>
            <h2>Welcome back</h2>
            <p class="subtitle">Sign in to access your ballot.</p>
            <% String error = request.getParameter("error"); %>
            <% if ("InvalidCredentials".equals(error)) { %>
                <div class="alert alert-error">Invalid email or password. Please try again.</div>
            <% } %>
            <% if ("true".equals(request.getParameter("registered"))) { %>
                <div class="alert alert-success">Account created. You can now sign in.</div>
            <% } %>
            <form action="login" method="post">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <div class="form-group"><label for="email">Email address</label><input type="email" id="email" name="email" placeholder="you@example.com" autocomplete="username" required></div>
                <div class="form-group"><label for="password">Password</label><input type="password" id="password" name="password" placeholder="Enter your password" autocomplete="current-password" required></div>
                <button type="submit" class="btn">Continue to ballot <span>→</span></button>
            </form>
            <p class="form-footer">New to VoteSecure? <a href="register.jsp">Create an account</a></p>
            <p class="form-footer"><a href="commission/login.jsp">Election Commission official login</a></p>
        </section>
    </main>
</body>
</html>