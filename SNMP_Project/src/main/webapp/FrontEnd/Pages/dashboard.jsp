<%@ page import="java.util.List" %>
<%@ page import="Model.ServerNode" %>
<%@ page import="Database_Connection.ServerDatabaseOperation" %>
<%
    List<ServerNode> nodes = null;
    try {
        nodes = ServerDatabaseOperation.getAllServerNodes();
    } catch (Exception e) {
        e.printStackTrace();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SNMP Monitoring Dashboard</title>
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
      <a href="${pageContext.request.contextPath}/dashboard" class="tab-button active">Nodes</a>
      <a href="${pageContext.request.contextPath}/alarms" class="tab-button">Errors</a>
      <a href="${pageContext.request.contextPath}/rules" class="tab-button">Action Rules</a>
    </div>

    <!-- Nodes Tab -->
    <div class="tab-content active">
      <div class="card">
        <form id="addNodeForm" method="post" action="${pageContext.request.contextPath}/addNode">
          <input name="serverName" id="nodeName" class="input" placeholder="Node Name" required />
          <input name="serverIp" id="nodeIP" class="input" placeholder="IP Address" required />
          <input name="cpuUsage" id="cpuUsage" class="input" type="number" step="0.01" min="0" max="100" placeholder="CPU Usage (%)" />
          <input name="memoryUsage" id="memoryUsage" class="input" type="number" step="0.01" min="0" max="100" placeholder="Memory Usage (%)" />
          <input name="diskUsage" id="diskUsage" class="input" type="number" step="0.01" min="0" max="100" placeholder="Disk Usage (%)" />
          <input name="networkUsage" id="networkUsage" class="input" type="number" step="0.01" min="0" max="100" placeholder="Network Usage (%)" />
          <select name="status" id="status" class="input">
            <option value="Active">Active</option>
            <option value="Inactive">Inactive</option>
            <option value="Warning">Warning</option>
            <option value="Critical">Critical</option>
          </select>
          <button type="submit" class="button">Add Node</button>
        </form>
      </div>

      <!-- Display success/error messages -->
      <% if (request.getAttribute("success") != null) { %>
        <div style="color: green; margin: 10px 0; padding: 10px; background: #d4edda; border: 1px solid #c3e6cb; border-radius: 4px;">
          <%= request.getAttribute("success") %>
        </div>
      <% } %>
      <% if (request.getAttribute("error") != null) { %>
        <div style="color: red; margin: 10px 0; padding: 10px; background: #f8d7da; border: 1px solid #f5c6cb; border-radius: 4px;">
          <%= request.getAttribute("error") %>
        </div>
      <% } %>

      <h3>Server Reports</h3>
      <table id="nodesTable">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>IP</th>
            <th>Report Time</th>
            <th>CPU Usage</th>
            <th>Memory Usage</th>
            <th>Disk Usage</th>
            <th>Network Usage</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
        <% if (nodes != null) {
            for (ServerNode node : nodes) { %>
          <tr>
            <td><%= node.getId() %></td>
            <td><%= node.getServerName() %></td>
            <td><%= node.getServerIp() %></td>
            <td><%= node.getReportTime() %></td>
            <td><%= node.getCpuUsage() %></td>
            <td><%= node.getMemoryUsage() %></td>
            <td><%= node.getDiskUsage() %></td>
            <td><%= node.getNetworkUsage() %></td>
            <td><%= node.getStatus() %></td>
          </tr>
        <%   }
           } %>
        </tbody>
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

  <!-- Edit Error Modal -->
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