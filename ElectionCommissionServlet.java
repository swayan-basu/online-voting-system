package com.voting.controller;

import com.voting.dao.VotingDao;
import com.voting.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@WebServlet("/commission/control")
public class ElectionCommissionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null || (!"admin".equalsIgnoreCase(user.getRole())
                && !"commission".equalsIgnoreCase(user.getRole()))) {
            response.sendRedirect("../index.jsp");
            return;
        }

        String action = request.getParameter("action");
        VotingDao dao = new VotingDao();
        if ("open".equals(action)) {
            String endTime = request.getParameter("endTime");
            if (endTime != null && !endTime.isBlank()) {
                try {
                    LocalDateTime selected = LocalDateTime.parse(endTime);
                    if (selected.isAfter(LocalDateTime.now())) {
                        dao.openElection(Timestamp.valueOf(selected));
                        response.sendRedirect("dashboard.jsp?event=reopened");
                        return;
                    }
                } catch (DateTimeParseException | IllegalArgumentException e) {
                    response.sendRedirect("dashboard.jsp?event=invalidTime");
                    return;
                }
            }
            response.sendRedirect("dashboard.jsp?event=invalidTime");
            return;
        } else if ("close".equals(action)) {
            dao.closeElection();
            response.sendRedirect("dashboard.jsp?event=closed");
            return;
        }
        response.sendRedirect("dashboard.jsp");
    }
}
