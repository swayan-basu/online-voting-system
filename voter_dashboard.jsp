<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.voting.model.User" %>
<%@ page import="com.voting.model.Candidate" %>
<%@ page import="com.voting.dao.VotingDao" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    VotingDao votingDao = new VotingDao();
    List<Candidate> candidates = votingDao.getAllCandidates();
    VotingDao.ElectionStatus election = votingDao.getElectionStatus();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VoteSecure | Ballot</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <main class="dashboard-shell">
        <header class="dashboard-header"><div class="brand-lockup"><span class="brand-mark">✓</span><span>VoteSecure</span></div><div class="user-menu"><span>Hi, <strong><%= currentUser.getName() %></strong></span><a href="logout.jsp" class="logout-link">Sign out</a></div></header>

        <% String success = request.getParameter("success"); %>
        <% String error = request.getParameter("error"); %>

        <% if ("VotedSuccessfully".equals(success)) { %>
            <div class="alert alert-success"><strong>Thank you for voting!</strong><br>Thank you for being a responsible citizen of India.</div>
        <% } else if (error != null) { %>
            <div class="alert alert-error">Unable to submit vote or duplicate vote detected.</div>
        <% } %>

        <% if (currentUser.getHasvoted()) { %>
            <div class="page-intro"><span class="eyebrow">BALLOT RECEIVED</span><h1>Thank you for your vote</h1><p class="subtitle">Your vote is securely recorded and remains anonymous. Election results are available only to the Election Commission.</p></div>
        <% } else if (!"OPEN".equals(election.status)) { %>
            <div class="page-intro"><span class="eyebrow">ELECTION STATUS</span><h1>Voting is currently closed</h1><p class="subtitle">Please return when the Election Commission opens voting.</p></div>
        <% } else { %>
            <div class="page-intro"><span class="eyebrow">2026 COMMUNITY ELECTION</span><h1>Make your choice</h1><p class="subtitle">Select one candidate below. Your ballot can only be submitted once.</p></div>

            <form action="vote" method="post">
                <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
                <% for (Candidate c : candidates) { %>
                    <label class="candidate-card"><input type="radio" name="candidateId" value="<%= c.getId() %>" required>
                        <div class="candidate-info">
                            <span class="candidate-avatar"><%= c.getName().substring(0, 1).toUpperCase() %></span><div>
                                <div class="candidate-name"><%= c.getName() %></div>
                                <div class="candidate-party"><%= c.getParty() %></div>
                            </div>
                        </div><span class="select-label">Select</span>
                    </label>
                <% } %>

                <button type="submit" class="btn ballot-submit">Submit my vote <span>→</span></button>
            </form>
        <% } %>
    </main>
</body>
</html>