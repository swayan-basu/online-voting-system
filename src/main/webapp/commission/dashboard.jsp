<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.voting.model.User" %>
<%@ page import="com.voting.model.Candidate" %>
<%@ page import="com.voting.dao.VotingDao" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    User user = (User) session.getAttribute("currentUser");
    if (user == null || (!"admin".equalsIgnoreCase(user.getRole()) && !"commission".equalsIgnoreCase(user.getRole()))) {
        response.sendRedirect("../index.jsp");
        return;
    }
    VotingDao dao = new VotingDao();
    List<Candidate> candidates = dao.getAllCandidates();
    VotingDao.ElectionStatus status = dao.getElectionStatus();
    int totalVotes = dao.getTotalVotes();
    Candidate winner = null;
    String defaultEndTime = LocalDateTime.now().plusHours(24).withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    String minimumEndTime = LocalDateTime.now().plusMinutes(5).withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    int highest = -1;
    for (Candidate candidate : candidates) {
        if (candidate.getVoteCount() > highest) {
            highest = candidate.getVoteCount();
            winner = candidate;
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="refresh" content="15">
    <title>Election Commission Dashboard</title>
    <link rel="stylesheet" href="../css/style.css">
</head>
<body>
<main class="commission-shell">
    <header class="dashboard-header">
        <div class="brand-lockup"><span class="brand-mark">EC</span><span>Election Commission</span></div>
        <div class="user-menu"><span>Official: <strong><%= user.getName() %></strong></span><a href="../logout.jsp" class="logout-link">Sign out</a></div>
    </header>
    <div class="page-intro">
        <span class="eyebrow">ELECTION CONTROL ROOM</span>
        <h1><%= "OPEN".equals(status.status) ? "Voting live" : "Election closed" %></h1>
        <p class="subtitle">Live totals refresh automatically every 15 seconds.</p>
    </div>
    <% if ("reopened".equals(request.getParameter("event"))) { %>
        <div class="alert alert-success election-action-alert">Election reopened. Existing votes and totals were preserved.</div>
    <% } else if ("closed".equals(request.getParameter("event"))) { %>
        <div class="alert alert-success election-action-alert">Election closed successfully.</div>
    <% } else if ("invalidTime".equals(request.getParameter("event"))) { %>
        <div class="alert alert-error election-action-alert">Choose a voting end time at least five minutes in the future.</div>
    <% } %>
    <div class="commission-grid">
        <div class="commission-stat"><span>Total ballots</span><strong><%= totalVotes %></strong></div>
        <div class="commission-stat"><span>Election status</span><strong><%= "OPEN".equals(status.status) ? "Live" : "Closed" %></strong></div>
        <div class="commission-stat"><span>Winner</span><strong><%= "CLOSED".equals(status.status) && winner != null ? winner.getName() : "Pending" %></strong></div>
    </div>
    <section class="commission-panel"><h2>Candidate results</h2><table>
        <thead><tr><th>Candidate</th><th>Party</th><th>Votes</th></tr></thead>
        <tbody>
        <% for (Candidate candidate : candidates) { %>
            <tr><td><strong><%= candidate.getName() %></strong></td><td><%= candidate.getParty() %></td><td><%= candidate.getVoteCount() %></td></tr>
        <% } %>
        </tbody>
    </table></section>
    <% if ("CLOSED".equals(status.status) && winner != null) { %>
        <div class="alert alert-success winner-alert"><strong>Winner: <%= winner.getName() %></strong> — <%= winner.getParty() %> (<%= winner.getVoteCount() %> votes)</div>
    <% } %>
    <form action="control" method="post" class="commission-control-form">
        <input type="hidden" name="csrfToken" value="<%= request.getAttribute("csrfToken") %>">
        <% if (!"OPEN".equals(status.status)) { %>
            <div class="form-group">
                <label for="endTime">New voting end time</label>
                <input type="datetime-local" id="endTime" name="endTime" min="<%= minimumEndTime %>" value="<%= defaultEndTime %>" max="2099-12-31T23:59" required>
                <small class="field-help">Existing ballots and candidate totals will be preserved.</small>
            </div>
            <button class="btn" type="submit" name="action" value="open">Reopen election</button>
        <% } else { %>
            <button class="btn" type="submit" name="action" value="close">Close voting and declare winner</button>
        <% } %>
    </form>
    <a class="btn commission-export-link" href="export">Download Excel-compatible CSV report</a>
</main>
</body>
</html>
