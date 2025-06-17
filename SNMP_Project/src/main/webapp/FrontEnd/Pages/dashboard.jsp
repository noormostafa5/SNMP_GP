<%@ page import="java.util.List" %>
<%@ page import="Model.ServerNode" %>
<%@ page import="Database_Connection.ServerDatabaseOperation" %>
<%
    List<ServerNode> nodes = null;
    try {
        System.out.println("=== Fetching nodes from database ===");
        nodes = ServerDatabaseOperation.getAllServerNodes();
        System.out.println("Number of nodes retrieved: " + (nodes != null ? nodes.size() : "null"));
        if (nodes != null) {
            for (ServerNode node : nodes) {
                System.out.println("Node: " + node.getServerName() + ", " + node.getServerIp() + ", " + node.getServerPort());
            }
        }
    } catch (Exception e) {
        System.out.println("Error fetching nodes: " + e.getMessage());
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

    #nodesTable {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
      background-color: rgba(255, 255, 255, 0.251);
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    #nodesTable th, #nodesTable td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    #nodesTable th {
      background-color: rgba(63, 81, 181, 0.3);
      color: white;
    }

    #nodesTable tr:hover {
      background-color: #f5f5f546;
    }

    .action-btn {
      padding: 6px 12px;
      margin: 0 4px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .action-btn.edit {
      background-color: #346187;
      color: white;
    }

    .action-btn.delete {
      background-color: #942f27;
      color: white;
    }

    .action-btn:hover {
      opacity: 0.8;
    }

    .success-message {
      position: fixed;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%);
      background-color: #4CAF50;
      color: white;
      padding: 15px 25px;
      border-radius: 5px;
      z-index: 1000;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
      opacity: 0;
      transition: opacity 0.5s ease-in-out;
    }

    .success-message.show {
      opacity: 1;
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
      <a href="${pageContext.request.contextPath}/alarms" class="tab-button">Alarms</a>
      <a href="${pageContext.request.contextPath}/rules" class="tab-button">Action Rules</a>
    </div>

    <!-- Nodes Tab -->
    <div class="tab-content active">
      <div class="card">
        <form id="addNodeForm" method="post" action="${pageContext.request.contextPath}/addNode">
          <input name="serverName" id="nodeName" class="input" placeholder="Node Name" required />
          <input name="serverIp" id="nodeIP" class="input" placeholder="IP Address" required />
          <input name="serverPort" id="nodePort" class="input" type="number" min="1" max="65535" placeholder="Port" required />
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

      <div class="table-container">
        <h3>Server Nodes</h3>
        <% if (nodes == null) { %>
          <p>Error loading nodes. Please check the server logs.</p>
        <% } else if (nodes.isEmpty()) { %>
          <p>No nodes found in the database.</p>
        <% } else { %>
          <table id="nodesTable">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>IP Address</th>
                <th>Port</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <% for (ServerNode node : nodes) { %>
                <tr>
                  <td><%= node.getId() %></td>
                  <td><%= node.getServerName() %></td>
                  <td><%= node.getServerIp() %></td>
                  <td><%= node.getServerPort() %></td>
                  <td>
                    <button class="action-btn edit" onclick="editNode(<%= node.getId() %>, '<%= node.getServerName() %>', '<%= node.getServerIp() %>', <%= node.getServerPort() %>)">Edit</button>
                    <button class="action-btn delete" onclick="deleteNode('<%= node.getServerIp() %>')">Delete</button>
                  </td>
                </tr>
              <% } %>
            </tbody>
          </table>
        <% } %>
      </div>
    </div>
  </main>

  <!-- Edit Node Modal -->
  <div id="editModal" class="modal" style="display:none;">
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>Edit Node</h2>
      <form id="editNodeForm" method="post" action="${pageContext.request.contextPath}/updateNode">
        <input type="hidden" id="editNodeId" name="nodeId" />
        <input id="editNodeName" name="serverName" class="input" placeholder="Node Name" style="width: calc(100% - 24px); margin-bottom: 10px;"/>
        <input id="editNodeIP" name="serverIp" class="input" placeholder="IP Address" style="width: calc(100% - 24px); margin-bottom: 10px;"/>
        <input id="editNodePort" name="serverPort" class="input" type="number" min="1" max="65535" placeholder="Port" style="width: calc(100% - 24px); margin-bottom: 10px;"/>
        <br>
        <button type="submit" class="button">Save</button>
      </form>
    </div>
  </div>

  <!-- Delete Confirmation Modal -->
  <div id="deleteConfirmModal" class="modal" style="display: none;">
    <div class="modal-content">
      <h3>Are you sure you want to delete this node?</h3>
      <div style="margin-top: 20px; display: flex; justify-content: space-between;">
        <form id="deleteNodeForm" method="post" action="${pageContext.request.contextPath}/deleteNode" style="display: inline;">
          <input type="hidden" id="deleteNodeIp" name="serverIp" />
          <button type="submit" class="button">Yes, Delete</button>
        </form>
        <button class="button" onclick="closeDeleteModal()">Cancel</button>
      </div>
    </div>
  </div>

  <script>
    // Get modal elements
    const editModal = document.getElementById('editModal');
    const deleteModal = document.getElementById('deleteConfirmModal');
    const closeButtons = document.getElementsByClassName('close');

    // Close modal when clicking the X
    Array.from(closeButtons).forEach(button => {
      button.addEventListener('click', function() {
        editModal.style.display = 'none';
        deleteModal.style.display = 'none';
      });
    });

    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
      if (event.target === editModal) {
        editModal.style.display = 'none';
      }
      if (event.target === deleteModal) {
        deleteModal.style.display = 'none';
      }
    });

    function editNode(id, name, ip, port) {
      document.getElementById('editNodeId').value = id;
      document.getElementById('editNodeName').value = name;
      document.getElementById('editNodeIP').value = ip;
      document.getElementById('editNodePort').value = port;
      editModal.style.display = 'block';
    }

    function deleteNode(ip) {
      document.getElementById('deleteNodeIp').value = ip;
      deleteModal.style.display = 'block';
    }

    function closeDeleteModal() {
      deleteModal.style.display = 'none';
    }
  </script>
</body>
</html>