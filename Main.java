package com.voting;

import java.util.List;

import com.voting.dao.UserDao;
import com.voting.dao.VotingDao;
import com.voting.model.Candidate;
import com.voting.model.User;

public class Main {
    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        System.out.println("--- Starting Voting System Test ---");

        User testUser = new User(0, "tom jerfling", "tom@example.com", "To7m}67@,J", "ABC}%1234F", "Aa!SecureP@ss123", "voter", false);
        
        boolean registered = userDao.registerUser(testUser);
        if (registered) {
            System.out.println("✅ SUCCESS: User registered successfully in MySQL!");
        } else {
            System.out.println("❌ FAILURE: User registration failed.");
        }

        System.out.println("\nAttempting login for tom@example.com...");
        User loggedInUser = userDao.loginUser("tom@example.com", "Aa!SecureP@ss123");

        if (loggedInUser != null) {
            System.out.println("✅ SUCCESS: Login successful!");
            System.out.println("   Welcome back, " + loggedInUser.getName() + "!");
            System.out.println("   Role: " + loggedInUser.getRole());
            System.out.println("   Has Voted?: " + loggedInUser.getHasvoted());

            // Voting and Duplicate Prevention Test
            VotingDao votingDao = new VotingDao();
            List<Candidate> candidates = votingDao.getAllCandidates();

            if (!candidates.isEmpty()) {
                int candidateId = candidates.get(0).getId();
                System.out.println("\nAttempting to cast vote for candidate: " + candidates.get(0).getName());
                
                boolean votedFirstTime = votingDao.castVote(loggedInUser.getId(), candidateId);
                if (votedFirstTime) {
                    System.out.println("✅ SUCCESS: Vote cast successfully!");
                } else {
                    System.out.println("❌ FAILURE: Could not cast vote.");
                }

                // Test duplicate voting prevention
                System.out.println("\nAttempting duplicate vote for the same user...");
                boolean votedSecondTime = votingDao.castVote(loggedInUser.getId(), candidateId);
                if (!votedSecondTime) {
                    System.out.println("✅ SUCCESS: Duplicate vote successfully prevented by transaction lock!");
                } else {
                    System.out.println("❌ FAILURE: Duplicate vote was allowed!");
                }
            } else {
                System.out.println("⚠️ WARNING: No candidates found in database. Please insert candidates into the 'candidates' table to test voting.");
            }

        } else {
            System.out.println("❌ FAILURE: Login failed. Check your email or password.");
        }
    }
}