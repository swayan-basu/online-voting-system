package com.voting.controller;

import com.voting.dao.DBConn;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/identity/check")
public class IdentityLookupServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String type = request.getParameter("type");
        String number = request.getParameter("number");
        if (type == null || number == null
                || (!"PAN".equalsIgnoreCase(type) && !"AADHAAR".equalsIgnoreCase(type))) {
            response.getWriter().write("{\"valid\":false,\"message\":\"Invalid identity details\"}");
            return;
        }
        number = number.trim().toUpperCase();

        String sql = "SELECT full_name FROM approved_voters "
                + "WHERE document_type=? AND document_number=?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type.toUpperCase());
            stmt.setString(2, number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("full_name").replace("\\", "\\\\").replace("\"", "\\\"");
                    response.getWriter().write("{\"valid\":true,\"name\":\"" + name + "\"}");
                } else {
                    response.getWriter().write("{\"valid\":false,\"message\":\"Identity number is invalid or not approved\"}");
                }
            }
        } catch (Exception e) {
            response.getWriter().write("{\"valid\":false,\"message\":\"Verification unavailable\"}");
        }
    }
}
