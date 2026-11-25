package com.voting.model;

public class Candidate {
    private int id;
    private String name;
    private String party;
    private int voteCount;

    public Candidate(int id, String name, String party, int voteCount) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.voteCount = voteCount;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }
    
    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }
}