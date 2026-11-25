package com.voting.dao;

import com.voting.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.voting.security.PasswordUtil;

public class UserDao {

    public boolean registerUser(User user) {
        boolean success = false;
        String sql = "INSERT INTO users (name, email, voter_id, identity_number, password, role, has_voted) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getVoterId());
            pstmt.setString(4, user.getIdentityNumber());
            pstmt.setString(5, PasswordUtil.hash(user.getPassword()));
            pstmt.setString(6, user.getRole());
            pstmt.setBoolean(7, user.getHasvoted());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public User loginUser(String email, String password) {
        User user = null;
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConn.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.startsWith("pbkdf2$")) {
                        if (!PasswordUtil.verify(password, storedPassword)) return null;
                    } else if (!password.equals(storedPassword)) {
                        return null;
                    } else {
                        try (Connection upgradeConn = DBConn.getConnection();
                             PreparedStatement upgrade = upgradeConn.prepareStatement(
                                     "UPDATE users SET password=? WHERE id=?")) {
                            upgrade.setString(1, PasswordUtil.hash(password));
                            upgrade.setInt(2, rs.getInt("id"));
                            upgrade.executeUpdate();
                        }
                    }
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String dbEmail = rs.getString("email");
                    String identityNumber = rs.getString("identity_number");
                    String dbPassword = rs.getString("password");
                    String role = rs.getString("role");
                    boolean hasVoted = rs.getBoolean("has_voted");

                    String voterId = rs.getString("voter_id");
                    user = new User(id, name, dbEmail, voterId, identityNumber, dbPassword, role, hasVoted);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}