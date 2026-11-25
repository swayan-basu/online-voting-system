package com.voting.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter("/*")
public class CsrfFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("Referrer-Policy", "no-referrer");
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; base-uri 'self'; form-action 'self'; object-src 'none'; "
                        + "script-src 'self'; style-src 'self'; font-src 'self'; img-src 'self' data:; "
                        + "connect-src 'self'; frame-src 'none'; worker-src 'self'; frame-ancestors 'none'");

        String uri = http.getRequestURI();
        if (uri.endsWith("/index.jsp")
                || uri.endsWith("/register.jsp")
                || uri.endsWith("/commission/login.jsp")
                || uri.endsWith("/commission/dashboard.jsp")
                || uri.endsWith("/voter_dashboard.jsp")
                || uri.endsWith("/login")) {
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
        }

        HttpSession session = http.getSession(true);
        String token = (String) session.getAttribute("csrfToken");
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute("csrfToken", token);
        }
        http.setAttribute("csrfToken", token);

        if ("POST".equalsIgnoreCase(http.getMethod())) {
            String submitted = http.getParameter("csrfToken");
            if (submitted == null || !submitted.equals(token)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
