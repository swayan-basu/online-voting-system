package com.voting.service;

public final class IdentityVerificationFactory {
    private IdentityVerificationFactory() {}

    public static IdentityVerificationService create() {
        String mode = System.getenv("IDENTITY_VERIFICATION_MODE");
        if ("DIGILOCKER".equalsIgnoreCase(mode)) {
            return new DigiLockerIdentityVerificationService();
        }
        return new DemoIdentityVerificationService();
    }
}
