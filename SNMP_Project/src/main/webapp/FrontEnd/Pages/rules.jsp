<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ page import="Database_Connection.ActionDatabaseOperation" %>
<%@ page import="Model.Action" %>
<%@ page import="Model.Node" %>
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

    /* Table Styles */
    #rulesTable {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
      background-color: rgba(255, 255, 255, 0.251);
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    #rulesTable th, #rulesTable td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    #rulesTable th {
      background-color: #443b69;
      color: white;
    }

    #rulesTable tr:hover {
      background-color: #f5f5f546;
    }

    /* Action buttons styles */
    .action-btn {
      padding: 6px 12px;
      margin: 0 4px;
      border: 1px solid #443b69;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      color: white;
    }

    .action-btn.edit {
      background-color: #346187;
    }

    .action-btn.delete {
      background-color: #942f27;
    }

    .action-btn:hover {
      opacity: 0.8;
    }

    /* Style for the Add Rule button in the card */
    .card .button {
      background-color: #443b69;
      border: 1px solid #443b69;
      color: white;
    }

    .card .button:hover {
      opacity: 0.8;
    }

    /* Message Styles */
    .message-container {
      margin: 10px 0;
      padding: 10px;
      border-radius: 4px;
      display: none; /* Hidden by default */
      position: fixed;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 1000;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
      opacity: 0;
      transition: opacity 0.5s ease-in-out;
    }

    .message-container.show {
      opacity: 1;
      display: block;
    }

    .message-container.success {
      background-color: #d4edda;
      color: #155724;
      border: 1px solid #c3e6cb;
    }

    .message-container.error {
      background-color: #f8d7da;
      color: #721c24;
      border: 1px solid #f5c6cb;
    }

    /* Modal Styles */
    .modal {
      display: none;
      position: fixed;
      z-index: 1;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      overflow: auto;
      background-color: rgba(0,0,0,0.4);
      padding-top: 60px;
    }

    .modal-content {
      background-color: #1a1a2e;
      margin: 5% auto;
      padding: 30px;
      border: 1px solid #888;
      width: 80%;
      max-width: 500px;
      border-radius: 8px;
      position: relative;
      color: #e0e0e0;
      box-shadow: 0 5px 15px rgba(0,0,0,0.3);
    }

    .modal-content h2 {
      color: #e0e0e0;
      margin-bottom: 20px;
      text-align: center;
    }

    .modal-content .input {
      width: calc(100% - 24px); /* Full width for stacked inputs */
      padding: 10px;
      margin-bottom: 10px;
      border: 1px solid #443b69;
      border-radius: 4px;
      background-color: #0e0e1a;
      color: #e0e0e0;
    }

    .modal-content .input::placeholder {
      color: #a0a0a0;
    }

    .modal-content .input:focus {
      outline: none;
      border-color: #6a5acd;
      box-shadow: 0 0 0 2px rgba(106, 90, 205, 0.5);
    }

    .modal-content .button {
      background-color: #6a5acd;
      color: white;
      padding: 10px 20px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 16px;
      margin-top: 10px;
    }

    .modal-content .button:hover {
      background-color: #5544b3;
    }

    .close {
      color: #aaaaaa;
      font-size: 28px;
      font-weight: bold;
      position: absolute;
      top: 10px;
      right: 20px;
      cursor: pointer;
    }

    .close:hover,
    .close:focus {
      color: #e0e0e0;
      text-decoration: none;
      cursor: pointer;
    }
  </style>
</head>
<body>
  <main id="dashboard">
    <div class="header">
      <h1 class="title">SNMP Monitoring Dashboard</h1>
      <a href="${pageContext.request.contextPath}/" class="button">Logout</a>
    </div>

    <div class="tabs">
      <a href="${pageContext.request.contextPath}/dashboard" class="tab-button">Nodes</a>
      <a href="${pageContext.request.contextPath}/alarms" class="tab-button">Alarms</a>
      <a href="${pageContext.request.contextPath}/rules" class="tab-button active">Action Rules</a>
    </div>

    <div class="tab-content active">
      <div class="card" style="display: flex; flex-wrap: nowrap; width: 100%;">
        <select id="ruleNodeId" class="input" style="background-color: rgba(143, 142, 142, 0.247); cursor: pointer;">
          <option value="">Select Node</option>
        </select>

        <input id="ruleActionType" class="input" placeholder="Action Type (Email)" />
        <input id="ruleTarget" class="input" placeholder="Enter Your Email" />
        <button id="addRuleButton" class="button">Add Rule</button>
      </div>

        <h3>Action Rules List</h3>
          <table id="rulesTable">
            <thead>
              <tr><th>Node Name</th><th>Action Type</th><th>Target</th><th>Actions</th></tr>
            </thead>
            <tbody>
              <%
                List<Action> actionRules = ActionDatabaseOperation.getAllActionRulesWithNodeNames();
                for (Action rule : actionRules) {
              %>
                <tr data-id="<%= rule.getId() %>">
                  <td><%= rule.getNodeName() %></td>
                  <td><%= rule.getActionType() %></td>
                  <td><%= rule.getRootEmail() %></td>
                  <td>
                    <button class="action-btn edit">Edit</button>
                    <button class="action-btn delete">Delete</button>
                  </td>
                </tr>
              <%
                }
              %>
            </tbody>
          </table>
    </div>
  </main>

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

  <!-- Message Container (for success/error messages) -->
  <div id="messageContainer" class="message-container"></div>

 <script>
    /* ────────────────────────────
       GLOBALS & UTILITIES
    ──────────────────────────── */
    const rulesTableBody     = document.querySelector('#rulesTable tbody');
    const editRuleModal      = document.getElementById('editRuleModal');
    const deleteConfirmModal = document.getElementById('deleteConfirmModal');
    const messageContainer   = document.getElementById('messageContainer');

    let currentRuleRow = null;
    let currentRuleId  = null;

    /* ❶  NEW: nodeMap keeps a local cache of id ➜ name pairs
          so we always know a node’s name even if the dropdown
          hasn’t been refreshed yet.                                */
    const nodeMap = {};

    /* ────────────────────────────
       TABLE HELPERS
    ──────────────────────────── */
    function addRuleToTable(rule) {
      const newRow = rulesTableBody.insertRow();
      newRow.dataset.id = rule.id || '';
      newRow.insertCell(0).textContent = rule.nodeName;
      newRow.insertCell(1).textContent = rule.actionType;
      newRow.insertCell(2).textContent = rule.rootEmail;
      const actionsCell = newRow.insertCell(3);
      actionsCell.innerHTML = `
        <button class="action-btn edit">Edit</button>
        <button class="action-btn delete">Delete</button>`;
    }

    /* ────────────────────────────
       EDIT & DELETE LISTENERS
    ──────────────────────────── */
    rulesTableBody.addEventListener('click', event => {
      if (!event.target.classList.contains('action-btn')) return;

      const button = event.target;
      const row    = button.closest('tr');
      currentRuleRow = row;
      currentRuleId  = row.dataset.id;

      if (button.classList.contains('edit')) {
        document.getElementById('editRuleActionType').value = row.cells[1].textContent;
        document.getElementById('editRuleTarget').value     = row.cells[2].textContent;
        editRuleModal.style.display = 'block';
      } else if (button.classList.contains('delete')) {
        deleteConfirmModal.style.display = 'block';
      }
    });

    /* Close modals */
    document.querySelectorAll('.close').forEach(btn => {
      btn.addEventListener('click', () => {
        editRuleModal.style.display    = 'none';
        deleteConfirmModal.style.display = 'none';
      });
    });

    window.addEventListener('click', e => {
      if (e.target === editRuleModal)      editRuleModal.style.display      = 'none';
      if (e.target === deleteConfirmModal) deleteConfirmModal.style.display = 'none';
    });

    /* ────────────────────────────
       SAVE EDITS (unchanged)
    ──────────────────────────── */
    editRuleModal.querySelector('.button').addEventListener('click', () => {
      const newActionType = document.getElementById('editRuleActionType').value;
      const newRootEmail  = document.getElementById('editRuleTarget').value;

      if (newActionType.toLowerCase() === 'email') {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!re.test(newRootEmail)) {
          showMessage('Invalid email format.', 'error');
          return;
        }
      }

      fetch('${pageContext.request.contextPath}/editRule', {
        method : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body   : JSON.stringify({ id: currentRuleId, actionType: newActionType, rootEmail: newRootEmail })
      })
      .then(res => res.json())
      .then(json => {
        if (json.success) {
          currentRuleRow.cells[1].textContent = newActionType;
          currentRuleRow.cells[2].textContent = newRootEmail;
          showMessage('Rule updated successfully.', 'success');
          editRuleModal.style.display = 'none';
        } else {
          showMessage('Failed to update rule.', 'error');
        }
      })
      .catch(() => showMessage('Server error during update.', 'error'));
    });

    /* ────────────────────────────
       CONFIRM DELETE (unchanged)
    ──────────────────────────── */
    deleteConfirmModal.querySelector('.button').addEventListener('click', () => {
      fetch('${pageContext.request.contextPath}/deleteRule', {
        method : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body   : JSON.stringify({ id: currentRuleId })
      })
      .then(res => res.json())
      .then(json => {
        if (json.success) {
          currentRuleRow.remove();
          showMessage('Rule deleted successfully.', 'success');
        } else {
          showMessage('Failed to delete rule.', 'error');
        }
        deleteConfirmModal.style.display = 'none';
      })
      .catch(() => showMessage('Server error during deletion.', 'error'));
    });

    /* Cancel delete */
    deleteConfirmModal.querySelectorAll('.button')[1]
      .addEventListener('click', () => deleteConfirmModal.style.display = 'none');

    /* Message helper */
    function showMessage(message, type) {
      messageContainer.textContent = message;
      messageContainer.className   = `message ${type} show`;
      setTimeout(() => messageContainer.classList.remove('show'), 3000);
    }

    /* ────────────────────────────
       ADD RULE  (❷  MODIFIED)
    ──────────────────────────── */
    document.getElementById('addRuleButton').addEventListener('click', evt => {
      evt.preventDefault();

      const nodeId     = document.getElementById('ruleNodeId').value;
      const actionType = document.getElementById('ruleActionType').value.trim();
      const rootEmail  = document.getElementById('ruleTarget').value.trim();

      if (!nodeId || !actionType || !rootEmail) {
        showMessage('Please fill in all rule details.', 'error');
        return;
      }

      if (actionType.toLowerCase() === 'email') {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!re.test(rootEmail)) {
          showMessage('Invalid email format.', 'error');
          return;
        }
      }

      fetch('${pageContext.request.contextPath}/AddRule', {
        method : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body   : JSON.stringify({ serverId: nodeId, actionType: actionType, rootEmail: rootEmail })
      })
      .then(res => res.json())
      .then(res => {
        if (res.success) {
          /* ❷  KEY CHANGE ────────────────
             Prefer nodeName from backend ➜ fallback to nodeMap ➜
             final fallback to dropdown option text.              */
          const nodeName =
            (res.nodeName && res.nodeName !== 'Unknown')      ? res.nodeName
          : (nodeMap[nodeId])                                 ? nodeMap[nodeId]
          : (document.querySelector(`#ruleNodeId option[value="${nodeId}"]`)
               ?.textContent || 'Unknown');

          addRuleToTable({
            id        : res.insertId || '',
            nodeName  : nodeName,
            actionType: actionType,
            rootEmail : rootEmail
          });

          /* Reset form */
          document.getElementById('ruleActionType').value = '';
          document.getElementById('ruleTarget').value     = '';
          document.getElementById('ruleNodeId').selectedIndex = 0;

          showMessage('Rule added successfully!', 'success');
        } else {
          showMessage('Failed to add rule.', 'error');
        }
      })
      .catch(() => showMessage('Server error while adding rule.', 'error'));
    });

    /* ────────────────────────────
       INITIAL NODE LIST LOAD (❸  MODIFIED)
    ──────────────────────────── */
    fetch('${pageContext.request.contextPath}/ActionServlet?action=getNodes')
      .then(response => response.json())
      .then(data => {
        const dropdown = document.getElementById('ruleNodeId');
        data.forEach(node => {
          /* ❸  Store in nodeMap so we always know the name */
          nodeMap[node.nodeId] = node.nodeName;

          const option   = document.createElement('option');
          option.value   = node.nodeId;
          option.textContent        = node.nodeName;
          option.style.backgroundColor = 'rgb(143, 142, 142)';
          dropdown.appendChild(option);
        });
      })
      .catch(() => showMessage('Failed to load nodes.', 'error'));
 </script>



</body>
</html>