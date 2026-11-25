package com.voting.service;

public class DigiLockerIdentityVerificationService implements IdentityVerificationService {
    @Override
    public boolean verify(String documentType, String documentNumber, String voterId, String name) {
        throw new IllegalStateException(
                "Use the DigiLocker OAuth callback; direct document verification is not an API token exchange");
    }
}
