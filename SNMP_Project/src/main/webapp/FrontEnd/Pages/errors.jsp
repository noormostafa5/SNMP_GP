<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SNMP Error Reports</title>
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
      <h1 class="title">SNMP Error Reports</h1>
      <a href="${pageContext.request.contextPath}/logout" class="button">Logout</a>
    </div>

    <div class="tabs">
      <a href="${pageContext.request.contextPath}/dashboard" class="tab-button">Nodes</a>
      <a href="${pageContext.request.contextPath}/alarms" class="tab-button active">Error Reports</a>
      <a href="${pageContext.request.contextPath}/rules" class="tab-button">Action Rules</a>
    </div>

    <div class="tab-content active">
      <div id="messageContainer"></div>
      
      <div class="card" style="display: flex; flex-wrap: nowrap; width: 100%;">
        <input id="serverName" class="input" placeholder="Server Name" />
        <input id="serverIp" class="input" placeholder="Server IP" />
        <input id="description" class="input" placeholder="Error Description" />
        <button onclick="addError()" class="button">Add Error Report</button>
      </div>

      <h3>Error Reports List</h3>
      <div id="loading" class="loading">Loading error reports...</div>
      <table id="errorTable" style="display: none;">
        <thead>
          <tr>
            <th>ID</th>
            <th>Server Name</th>
            <th>Server IP</th>
            <th>Description</th>
            <th>Report Time</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody id="errorTableBody"></tbody>
      </table>
    </div>
  </main>

  <script>
    // Load error reports when page loads
    document.addEventListener('DOMContentLoaded', function() {
      loadErrorReports();
    });

    function loadErrorReports() {
      fetch('${pageContext.request.contextPath}/api/alarms')
        .then(response => response.json())
        .then(data => {
          document.getElementById('loading').style.display = 'none';
          document.getElementById('errorTable').style.display = 'table';
          
          const tbody = document.getElementById('errorTableBody');
          tbody.innerHTML = '';
          
          if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No error reports found</td></tr>';
            return;
          }
          
          data.forEach(error => {
            const row = document.createElement('tr');
            row.innerHTML = `
              <td>\${error.id}</td>
              <td>\${error.serverName || 'N/A'}</td>
              <td>\${error.serverIp || 'N/A'}</td>
              <td>\${error.description || 'N/A'}</td>
              <td>\${formatDateTime(error.reportTime)}</td>
              <td>
                <button class="delete-btn" onclick="deleteError(\${error.id})">Delete</button>
              </td>
            `;
            tbody.appendChild(row);
          });
        })
        .catch(error => {
          console.error('Error loading error reports:', error);
          document.getElementById('loading').innerHTML = 'Error loading error reports';
          showMessage('Error loading error reports: ' + error.message, 'error');
        });
    }

    function addError() {
      const serverName = document.getElementById('serverName').value.trim();
      const serverIp = document.getElementById('serverIp').value.trim();
      const description = document.getElementById('description').value.trim();
      
      if (!serverName || !serverIp || !description) {
        showMessage('Please fill in all fields', 'error');
        return;
      }
      
      const params = new URLSearchParams();
      params.append('serverName', serverName);
      params.append('serverIp', serverIp);
      params.append('description', description);
      
      fetch('${pageContext.request.contextPath}/api/alarms', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          showMessage('Error report added successfully', 'success');
          // Clear form
          document.getElementById('serverName').value = '';
          document.getElementById('serverIp').value = '';
          document.getElementById('description').value = '';
          // Reload table
          loadErrorReports();
        } else {
          showMessage('Error: ' + (data.error || 'Failed to add error report'), 'error');
        }
      })
      .catch(error => {
        console.error('Error adding error report:', error);
        showMessage('Error adding error report: ' + error.message, 'error');
      });
    }

    function deleteError(id) {
      if (!confirm('Are you sure you want to delete this error report?')) {
        return;
      }
      
      fetch('${pageContext.request.contextPath}/api/alarms?id=' + id, {
        method: 'DELETE'
      })
      .then(response => {
        if (response.ok) {
          showMessage('Error report deleted successfully', 'success');
          loadErrorReports();
        } else {
          showMessage('Failed to delete error report', 'error');
        }
      })
      .catch(error => {
        console.error('Error deleting error report:', error);
        showMessage('Error deleting error report: ' + error.message, 'error');
      });
    }

    function formatDateTime(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      const date = new Date(dateTimeString);
      return date.toLocaleString();
    }

    function showMessage(message, type) {
      const container = document.getElementById('messageContainer');
      const messageDiv = document.createElement('div');
      messageDiv.className = type === 'error' ? 'error-message' : 'success-message';
      messageDiv.textContent = message;
      
      container.innerHTML = '';
      container.appendChild(messageDiv);
      
      // Auto-hide after 5 seconds
      setTimeout(() => {
        if (messageDiv.parentNode) {
          messageDiv.parentNode.removeChild(messageDiv);
        }
      }, 5000);
    }
  </script>
</body>
</html>