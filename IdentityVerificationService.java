package com.voting.service;

public interface IdentityVerificationService {
    boolean verify(String documentType, String documentNumber, String voterId, String name);
}
