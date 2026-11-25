package com.voting.service;

import com.voting.dao.DBConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DemoIdentityVerificationService implements IdentityVerificationService {
    @Override
    public boolean verify(String documentType, String documentNumber, String voterId, String name) {
        if (documentNumber == null || voterId == null || name == null || name.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM approved_voters identity_record "
                + "JOIN approved_voters voter ON voter.full_name=identity_record.full_name "
                + "WHERE identity_record.document_type=? AND identity_record.document_number=? "
                + "AND identity_record.full_name=? AND voter.document_type='VOTER_ID' "
                + "AND voter.document_number=?";
        try (Connection conn = DBConn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documentType);
            stmt.setString(2, documentNumber.trim().toUpperCase());
            stmt.setString(3, name.trim());
            stmt.setString(4, voterId.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
