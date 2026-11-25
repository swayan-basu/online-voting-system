package com.voting.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@WebServlet("/digilocker/authorize")
public class DigiLockerAuthorizeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String clientId = System.getenv("DIGILOCKER_CLIENT_ID");
        String redirectUri = System.getenv("DIGILOCKER_REDIRECT_URI");
        if (clientId == null || redirectUri == null) {
            response.sendRedirect("../register.jsp?error=IdentityVerificationUnavailable");
            return;
        }

        byte[] verifierBytes = new byte[32];
        new SecureRandom().nextBytes(verifierBytes);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes);
        String state = Long.toHexString(new SecureRandom().nextLong());
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String challenge = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(verifier.getBytes(StandardCharsets.US_ASCII)));
            HttpSession session = request.getSession();
            session.setAttribute("digilockerCodeVerifier", verifier);
            session.setAttribute("digilockerState", state);

            String authUrl = env("DIGILOCKER_AUTH_URL",
                    "https://dev-meripehchaan.dl6.in/oauth2/1/authorize");
            String scope = env("DIGILOCKER_SCOPE", "openid");
            String query = "client_id=" + enc(clientId)
                    + "&response_type=code"
                    + "&redirect_uri=" + enc(redirectUri)
                    + "&state=" + enc(state)
                    + "&code_challenge=" + enc(challenge)
                    + "&code_challenge_method=S256"
                    + "&scope=" + enc(scope);
            response.sendRedirect(authUrl + "?" + query);
        } catch (Exception e) {
            throw new ServletException("Unable to start DigiLocker authorization", e);
        }
    }

    private String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
