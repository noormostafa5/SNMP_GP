<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SNMP Monitoring Dashboard</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/FrontEnd/Styles/style.css">
</head>
<body>
  <main id="dashboard">
    <div class="header">
      <h1 class="title">SNMP Monitoring Dashboard</h1>
      <button class="button" onclick="window.location.href='${pageContext.request.contextPath}/logout'">Logout</button>
    </div>

    <div class="tabs">
      <button class="tab-button active" data-tab="nodes">Nodes</button>
      <button class="tab-button" data-tab="alarms">Alarms</button>
      <button class="tab-button" data-tab="rules">Action Rules</button>
    </div>

    <!-- Nodes Tab -->
    <div id="nodes" class="tab-content active">
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

    <!-- Alarms Tab -->
    <div id="alarms" class="tab-content">
      <div class="card">
        <input id="alarmNode" class="input" placeholder="Node" />
        <input id="alarmDesc" class="input" placeholder="Description" />
        <select id="alarmStatus" class="input">
          <option value="Active">Active</option>
          <option value="Clear">Clear</option>
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

    <!-- Rules Tab -->
    <div id="rules" class="tab-content">
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