package com.voting.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String identityNumber;
    private String voterId;
    private String password;
    private String role;
    private boolean hasvoted;

    public User(int id, String name, String email, String password, String role, boolean hasVoted) {
        this(id, name, email, null, null, password, role, hasVoted);
    }

    public User(int id, String name, String email, String identityNumber, String password, String role, boolean hasVoted) {
        this(id, name, email, null, identityNumber, password, role, hasVoted);
    }

    public User(int id, String name, String email, String voterId, String identityNumber, String password, String role, boolean hasVoted) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.voterId = voterId;
        this.identityNumber = identityNumber;
        this.password = password;
        this.role = role;
        this.hasvoted = hasVoted;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIdentityNumber() { return identityNumber; }
    public void setIdentityNumber(String identityNumber) { this.identityNumber = identityNumber; }
    public String getVoterId() { return voterId; }
    public void setVoterId(String voterId) { this.voterId = voterId; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean getHasvoted() { return hasvoted; }
    public void setHasvoted(boolean hasvoted) { this.hasvoted = hasvoted; }
}