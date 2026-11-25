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

@WebServlet("/vote")
public class VoteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        // Security check: ensure user is logged in
        if (currentUser == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        if (!"voter".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("index.jsp?error=InvalidCredentials");
            return;
        }

        String candidateIdStr = request.getParameter("candidateId");
        if (candidateIdStr != null) {
            int candidateId;
            try {
                candidateId = Integer.parseInt(candidateIdStr);
            } catch (NumberFormatException e) {
                response.sendRedirect("voter_dashboard.jsp?error=NoCandidateSelected");
                return;
            }

            VotingDao votingDao = new VotingDao();
            boolean success = votingDao.castVote(currentUser.getId(), candidateId);

            if (success) {
                // Update session state so UI locks the ballot
                currentUser.setHasvoted(true);
                session.setAttribute("currentUser", currentUser);
                response.sendRedirect("voter_dashboard.jsp?success=VotedSuccessfully");
            } else {
                // Duplicate vote prevented or database error
                response.sendRedirect("voter_dashboard.jsp?error=VoteFailedOrDuplicate");
            }
        } else {
            response.sendRedirect("voter_dashboard.jsp?error=NoCandidateSelected");
        }
    }
}
