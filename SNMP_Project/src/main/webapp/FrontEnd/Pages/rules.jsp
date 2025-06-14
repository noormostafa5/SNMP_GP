<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SNMP Action Rules</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/FrontEnd/Styles/index.css">
  <style>
    body {
      background-image: url('${pageContext.request.contextPath}/FrontEnd/background/bckg1.jpg');
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
      <a href="${pageContext.request.contextPath}/logout" class="button">Logout</a>
    </div>

    <div class="tabs">
      <a href="${pageContext.request.contextPath}/dashboard" class="tab-button">Nodes</a>
      <a href="${pageContext.request.contextPath}/alarms" class="tab-button">Errors</a>
      <a href="${pageContext.request.contextPath}/rules" class="tab-button active">Action Rules</a>
    </div>

    <div class="tab-content active">
      <div class="card">
        <input id="ruleNodeId" class="input" placeholder="Node ID" />
        <input id="ruleActionType" class="input" placeholder="Action Type (SMS, Email, RunScript)" />
        <input id="ruleTarget" class="input" placeholder="MSISDN / Email / Script Path" />
        <button class="button">Add Rule</button>
      </div>

      <h3>Action Rules List</h3>
      <table id="rulesTable">
        <thead>
          <tr><th>Node ID</th><th>Action Type</th><th>Target</th><th>Actions</th></tr>
        </thead>
        <tbody></tbody>
      </table>
    </div>
  </main>
</body>
</html>