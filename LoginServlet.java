package com.voting.controller;

import com.voting.dao.UserDao;
import com.voting.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import com.voting.security.LoginAttemptGuard;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String sourceKey = request.getRemoteAddr() + "|" + LoginAttemptGuard.normalize(email);
        if (LoginAttemptGuard.isBlocked(sourceKey)) {
            response.sendRedirect("index.jsp?error=TemporarilyLocked");
            return;
        }

        UserDao userDao = new UserDao();
        User user = userDao.loginUser(email, password);

        if (user != null) {
            LoginAttemptGuard.recordSuccess(sourceKey);
            request.getSession().invalidate();
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("currentUser", user);

            if ("admin".equalsIgnoreCase(user.getRole()) || "commission".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect("commission/dashboard.jsp");
            } else {
                response.sendRedirect("voter_dashboard.jsp");
            }
        } else {
            LoginAttemptGuard.recordFailure(sourceKey);
            String loginPage = request.getHeader("Referer");
            if (loginPage != null && loginPage.contains("/commission/login.jsp")) {
                response.sendRedirect("commission/login.jsp?error=InvalidCredentials");
            } else {
                response.sendRedirect("index.jsp?error=InvalidCredentials");
            }
        }
    }
}