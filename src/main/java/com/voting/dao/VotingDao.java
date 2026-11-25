package com.voting.dao;

import com.voting.model.Candidate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VotingDao {

    public static class ElectionStatus {
        public final String status;
        public final java.sql.Timestamp endTime;
        public ElectionStatus(String status, java.sql.Timestamp endTime) {
            this.status = status;
            this.endTime = endTime;
        }
    }

    public ElectionStatus getElectionStatus() {
        String sql = "SELECT status, end_time FROM election_settings WHERE id = 1";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                java.sql.Timestamp end = rs.getTimestamp("end_time");
                if ("OPEN".equals(rs.getString("status")) && end != null
                        && end.before(new java.sql.Timestamp(System.currentTimeMillis()))) {
                    closeElection(conn);
                    return new ElectionStatus("CLOSED", end);
                }
                return new ElectionStatus(rs.getString("status"), end);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ElectionStatus("CLOSED", null);
    }

    public void openElection(java.sql.Timestamp endTime) {
        String sql = "UPDATE election_settings SET status='OPEN', end_time=? WHERE id=1";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, endTime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeElection() {
        try (Connection conn = DBConn.getConnection()) {
            closeElection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeElection(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE election_settings SET status='CLOSED' WHERE id=1")) {
            stmt.executeUpdate();
        }
    }

    public int getTotalVotes() {
        String sql = "SELECT COUNT(*) FROM votes";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Candidate> getAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates";

        try (Connection conn = DBConn.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                candidates.add(new Candidate(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("party"),
                    rs.getInt("vote_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return candidates;
    }

    public boolean castVote(int userId, int candidateId) {
        Connection conn = null;
        try {
            conn = DBConn.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement electionStmt = conn.prepareStatement(
                    "SELECT status, end_time FROM election_settings WHERE id=1 FOR UPDATE");
                 ResultSet election = electionStmt.executeQuery()) {
                if (!election.next() || !"OPEN".equals(election.getString("status"))) {
                    conn.rollback();
                    return false;
                }
                java.sql.Timestamp endTime = election.getTimestamp("end_time");
                if (endTime == null || endTime.before(new java.sql.Timestamp(System.currentTimeMillis()))) {
                    closeElection(conn);
                    conn.commit();
                    return false;
                }
            }

            // 1. Double-check if the user has already voted
            String checkSql = "SELECT has_voted FROM users WHERE id = ? FOR UPDATE";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getBoolean("has_voted")) {
                        conn.rollback();
                        return false; // Already voted! Prevent duplicate
                    }
                }
            }

            String updateCandidateSql = "UPDATE candidates SET vote_count = vote_count + 1 WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateCandidateSql)) {
                updateStmt.setInt(1, candidateId);
                if (updateStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // The anonymous ballot intentionally contains no voter ID or account ID.
            String insertBallotSql = "INSERT INTO votes (candidate_id) VALUES (?)";
            try (PreparedStatement ballotStmt = conn.prepareStatement(insertBallotSql)) {
                ballotStmt.setInt(1, candidateId);
                ballotStmt.executeUpdate();
            }

            String updateUserSql = "UPDATE users SET has_voted = true WHERE id = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(updateUserSql)) {
                userStmt.setInt(1, userId);
                userStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction successfully
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
