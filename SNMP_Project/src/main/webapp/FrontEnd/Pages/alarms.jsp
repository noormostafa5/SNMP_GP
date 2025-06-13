<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SNMP Alarms</title>
  <link rel="stylesheet" type="text/css" href="../Styles/index.css">
  <style>
    body {
      background-image: url('../background/bckg1.jpg');
      background-size: cover;
      background-position: center;
      background-repeat: no-repeat;
      min-height: 100vh;
      margin: 0;
      padding: 0;
    }
  </style>
</head>
<body>
  <main id="dashboard">
    <div class="header">
      <h1 class="title">SNMP Monitoring Dashboard</h1>
      <a href="../Pages/welcome.jsp" class="button">Logout</a>
    </div>

    <div class="tabs">
      <a href="dashboard.jsp" class="tab-button">Nodes</a>
      <a href="alarms.jsp" class="tab-button active">Alarms</a>
      <a href="rules.jsp" class="tab-button">Action Rules</a>
    </div>

    <div class="tab-content active">
      <div class="card" style="display: flex; flex-wrap: nowrap; width: 100%;">
        <input id="alarmNode" class="input" placeholder="Node" />
        <input id="alarmDesc" class="input" placeholder="Description" />
        <select id="alarmStatus" class="input" style="background-color: rgba(143, 142, 142, 0.201); cursor: pointer;">
          <option style="color:black; font-size: 18px;" value="Active">Active</option>
          <option style="color:black; font-size: 18px;" value="Clear">Clear</option>
        </select>
        <input id="alarmTimestamp" class="input" type="datetime-local" />
        <button class="button">Add Alarm</button>
      </div>

      <h3>Alarms List</h3>
      <table id="alarmsTable">
        <thead>
          <tr><th>Node</th><th>Description</th><th>Status</th><th>Timestamp</th><th>Actions</th></tr>
        </thead>
        <tbody></tbody>
      </table>
    </div>
  </main>
</body>
</html>