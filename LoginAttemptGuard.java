package com.voting.security;

import com.voting.dao.DBConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public final class LoginAttemptGuard {
    private static final int MAX_FAILURES = 5;

    private LoginAttemptGuard() {}

    public static boolean isBlocked(String key) {
        try (Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT failed_attempts, locked_until FROM login_attempts WHERE attempt_key=?")) {
            stmt.setString(1, normalize(key));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return false;
                Timestamp lockedUntil = rs.getTimestamp("locked_until");
                return lockedUntil != null && lockedUntil.getTime() > System.currentTimeMillis();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static void recordFailure(String key) {
        try (Connection conn = DBConn.getConnection();
             PreparedStatement select = conn.prepareStatement(
                     "SELECT failed_attempts FROM login_attempts WHERE attempt_key=?");
             PreparedStatement upsert = conn.prepareStatement(
                     "INSERT INTO login_attempts(attempt_key,failed_attempts,locked_until) "
                     + "VALUES (?,?,?) ON DUPLICATE KEY UPDATE "
                     + "failed_attempts=VALUES(failed_attempts), locked_until=VALUES(locked_until)");
             PreparedStatement reset = conn.prepareStatement(
                     "UPDATE login_attempts SET failed_attempts=0,locked_until=NULL WHERE attempt_key=?")) {
            String normalized = normalize(key);
            select.setString(1, normalized);
            int failures = 0;
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) failures = rs.getInt(1);
            }
            failures++;
            if (failures >= MAX_FAILURES) {
                upsert.setString(1, normalized);
                upsert.setInt(2, failures);
                upsert.setTimestamp(3, new Timestamp(System.currentTimeMillis() + 15L * 60L * 1000L));
                upsert.executeUpdate();
            } else {
                upsert.setString(1, normalized);
                upsert.setInt(2, failures);
                upsert.setNull(3, java.sql.Types.TIMESTAMP);
                upsert.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void recordSuccess(String key) {
        try (Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM login_attempts WHERE attempt_key=?")) {
            stmt.setString(1, normalize(key));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String normalize(String key) {
        return key == null ? "unknown" : key.trim().toLowerCase();
    }

}
