<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SNMP Monitoring Dashboard</title>
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
      <a href="dashboard.jsp" class="tab-button active">Nodes</a>
      <a href="alarms.jsp" class="tab-button">Alarms</a>
      <a href="rules.jsp" class="tab-button">Action Rules</a>
    </div>

    <!-- Nodes Tab -->
    <div class="tab-content active">
      <div class="card">
        <input id="nodeName" class="input" placeholder="Node Name" />
        <input id="nodeIP" class="input" placeholder="IP Address" />
        <input id="nodePort" class="input" placeholder="Port" />
        <button class="button">Add Node</button>
      </div>

      <h3>Nodes List</h3>
      <table id="nodesTable">
        <thead>
          <tr><th>Name</th><th>IP</th><th>Port</th><th>Actions</th></tr>
        </thead>
        <tbody></tbody>
      </table>
    </div>
  </main>

  <!-- Edit Node Modal -->
  <div id="editModal" class="modal" style="display:none;">
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>Edit Node</h2>
      <input id="editNodeName" class="input" placeholder="Node Name" />
      <input id="editNodeIP" class="input" placeholder="IP Address" />
      <input id="editNodePort" class="input" placeholder="Port" />
      <br>
      <button class="button">Save</button>
    </div>
  </div>

  <!-- Edit Alarm Modal -->
  <div id="editAlarmModal" class="modal" style="display:none;">
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>Edit Alarm</h2>
      <input id="editAlarmNode" class="input" placeholder="Node" />
      <input id="editAlarmDesc" class="input" placeholder="Description" />
      <select id="editAlarmStatus" class="input">
        <option value="Active">Active</option>
        <option value="Clear">Clear</option>
      </select>
      <input id="editAlarmTimestamp" class="input" type="datetime-local" />
      <br>
      <button class="button">Save</button>
    </div>
  </div>

  <!-- Edit Rule Modal -->
  <div id="editRuleModal" class="modal" style="display:none;">
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>Edit Action Rule</h2>
      <input id="editRuleNodeId" class="input" placeholder="Node ID" />
      <input id="editRuleActionType" class="input" placeholder="Action Type" />
      <input id="editRuleTarget" class="input" placeholder="MSISDN / Email / Script Path" />
      <br>
      <button class="button">Save</button>
    </div>
  </div>

  <!-- Delete Confirmation Modal -->
  <div id="deleteConfirmModal" class="modal" style="display: none;">
    <div class="modal-content">
      <h3>Are you sure you want to delete this item?</h3>
      <div style="margin-top: 20px; display: flex; justify-content: space-between;">
        <button class="button">Yes, Delete</button>
        <button class="button">Cancel</button>
      </div>
    </div>
  </div>

</body>
</html>