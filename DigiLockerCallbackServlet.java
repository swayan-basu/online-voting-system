package com.voting.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@WebServlet("/digilocker/callback")
public class DigiLockerCallbackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String error = request.getParameter("error");
        if (error != null) {
            response.sendRedirect("../register.jsp?error=IdentityVerificationFailed");
            return;
        }
        if (session == null) {
            response.sendRedirect("../register.jsp?error=IdentityVerificationUnavailable");
            return;
        }

        String expectedState = (String) session.getAttribute("digilockerState");
        String state = request.getParameter("state");
        if (expectedState == null || !expectedState.equals(state)) {
            response.sendRedirect("../register.jsp?error=IdentityVerificationFailed");
            return;
        }

        String code = request.getParameter("code");
        String verifier = (String) session.getAttribute("digilockerCodeVerifier");
        String clientId = System.getenv("DIGILOCKER_CLIENT_ID");
        String clientSecret = System.getenv("DIGILOCKER_CLIENT_SECRET");
        String redirectUri = System.getenv("DIGILOCKER_REDIRECT_URI");
        if (code == null || verifier == null || clientId == null
                || clientSecret == null || redirectUri == null) {
            response.sendRedirect("../register.jsp?error=IdentityVerificationUnavailable");
            return;
        }

        try {
            String form = "grant_type=" + enc("authorization_code")
                    + "&code=" + enc(code)
                    + "&redirect_uri=" + enc(redirectUri)
                    + "&code_verifier=" + enc(verifier)
                    + "&client_id=" + enc(clientId)
                    + "&client_secret=" + enc(clientSecret);
            String tokenUrl = env("DIGILOCKER_TOKEN_URL",
                    "https://dev-meripehchaan.dl6.in/oauth2/2/token");
            HttpRequest tokenRequest = HttpRequest.newBuilder(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> tokenResponse = HttpClient.newHttpClient()
                    .send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            if (tokenResponse.statusCode() < 200 || tokenResponse.statusCode() >= 300) {
                response.sendRedirect("../register.jsp?error=IdentityVerificationFailed");
                return;
            }

            String accessToken = jsonString(tokenResponse.body(), "access_token");
            if (accessToken.isEmpty()) {
                response.sendRedirect("../register.jsp?error=IdentityVerificationFailed");
                return;
            }

            String userUrl = env("DIGILOCKER_USER_URL",
                    "https://dev-meripehchaan.dl6.in/oauth2/1/user");
            HttpRequest userRequest = HttpRequest.newBuilder(URI.create(userUrl))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> userResponse = HttpClient.newHttpClient()
                    .send(userRequest, HttpResponse.BodyHandlers.ofString());
            if (userResponse.statusCode() < 200 || userResponse.statusCode() >= 300) {
                response.sendRedirect("../register.jsp?error=IdentityVerificationFailed");
                return;
            }

            String digilockerId = jsonString(userResponse.body(), "digilockerid");
            String name = jsonString(userResponse.body(), "name");
            if (digilockerId.isEmpty() || name.isEmpty()) {
                response.sendRedirect("../register.jsp?error=IdentityVerificationFailed");
                return;
            }
            session.setAttribute("digilockerVerified", true);
            session.setAttribute("digilockerId", digilockerId);
            session.setAttribute("digilockerName", name);
            session.removeAttribute("digilockerCodeVerifier");
            session.removeAttribute("digilockerState");
            response.sendRedirect("../register.jsp?verified=true");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendRedirect("../register.jsp?error=IdentityVerificationUnavailable");
        } catch (Exception e) {
            response.sendRedirect("../register.jsp?error=IdentityVerificationUnavailable");
        }
    }

    private String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String jsonString(String json, String key) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }
}
