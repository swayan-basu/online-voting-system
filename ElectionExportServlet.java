package com.voting.controller;

import com.voting.dao.DBConn;
import com.voting.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/commission/export")
public class ElectionExportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null || (!"admin".equalsIgnoreCase(user.getRole())
                && !"commission".equalsIgnoreCase(user.getRole()))) {
            response.sendRedirect("../index.jsp");
            return;
        }

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"election-results.csv\"");
        try (PrintWriter out = response.getWriter();
             Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.name, c.party, c.vote_count FROM candidates c ORDER BY c.vote_count DESC");
             ResultSet rs = stmt.executeQuery()) {
            out.println("Candidate,Party,Vote Count");
            while (rs.next()) {
                out.print(csv(rs.getString("name")));
                out.print(",");
                out.print(csv(rs.getString("party")));
                out.print(",");
                out.println(rs.getInt("vote_count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
