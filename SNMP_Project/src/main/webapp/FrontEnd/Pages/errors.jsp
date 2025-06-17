<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SNMP Alarms</title>
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

    /* Existing styles for alarmsTable - adjust if needed for consistency */
    #alarmsTable {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
      background-color: rgba(255, 255, 255, 0.251);
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    #alarmsTable th, #alarmsTable td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    #alarmsTable th {
      background-color: #443b69;
      color: white;
    }

    #alarmsTable tr:hover {
      background-color: #f5f5f546;
    }

    /* Styles for alarm status */
    #alarmsTable tr.status-active {
      background-color: rgba(255, 0, 0, 0.2);
    }

    #alarmsTable tr.status-clear {
      background-color: rgba(0, 255, 0, 0.2);
    }

    /* Action buttons styles */
    .action-btn {
      padding: 6px 12px;
      margin: 0 4px;
      border: 1px solid #443b69; /* Darker border */
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

    /* New style for history button */
    .action-btn.history {
      background-color: #6a5acd; /* A purple tone */
    }

    .action-btn:hover {
      opacity: 0.8;
    }

    /* Style for the Add Alarm button in the card */
    .card .button {
      background-color: #443b69;
      border: 1px solid #443b69;
      color: white;
    }

    .card .button:hover {
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
      <h1 class="title">SNMP Error Reports</h1>
      <a href="${pageContext.request.contextPath}/" class="button">Logout</a>
    </div>

    <div class="tabs">
      <a href="${pageContext.request.contextPath}/dashboard" class="tab-button">Nodes</a>
      <a href="${pageContext.request.contextPath}/alarms" class="tab-button active">Alarms</a>
      <a href="${pageContext.request.contextPath}/rules" class="tab-button">Action Rules</a>
    </div>

    <div class="tab-content active">
      <div class="card" style="display: flex; flex-wrap: nowrap; width: 100%;">
        <input id="alarmNode" class="input" placeholder="Server Name" />
        <input id="alarmDesc" class="input" placeholder="Description" />
        <select id="alarmStatus" class="input" style="background-color: rgba(143, 142, 142, 0.201); cursor: pointer;">
          <option style="color:black; font-size: 18px;" value="Active">Active</option>
          <option style="color:black; font-size: 18px;" value="Clear">Clear</option>
        </select>
        <input id="alarmTimestamp" class="input" type="datetime-local" />
        <button id="addAlarmButton" class="button">Add Alarm</button>
      </div>

      <h3>Alarms List</h3>
      <table id="alarmsTable">
        <thead>
          <tr><th>Server Name</th><th>Description</th><th>Status</th><th>Timestamp</th><th>Actions</th></tr>
        </thead>
        <tbody>
          <!-- Static alarm data (optional, for testing) -->
          <tr class="status-active">
            <td>Router-01</td>
            <td>High CPU usage</td>
            <td>Active</td>
            <td>2023-10-27T10:00</td>
            <td>
              <button class="action-btn edit">Edit</button>
              <button class="action-btn delete">Delete</button>
              <button class="action-btn history">History</button>
            </td>
          </tr>
          <tr class="status-active">
            <td>Server-02</td>
            <td>Disk space low</td>
            <td>Active</td>
            <td>2023-10-27T09:30</td>
            <td>
              <button class="action-btn edit">Edit</button>
              <button class="action-btn delete">Delete</button>
              <button class="action-btn history">History</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </main>

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

  <!-- History Modal -->
  <div id="alarmHistoryModal" class="modal" style="display:none;">
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>Alarm History</h2>
      <div id="alarmHistoryContent" style="max-height: 300px; overflow-y: auto; padding-right: 10px;">
        <!-- History will be loaded here -->
      </div>
      <br>
      <button class="button">Close</button>
    </div>
  </div>

  <!-- Success Message -->
  <div id="successMessage" class="success-message" style="display:none;">
    Alarm action successful!
  </div>

  <script>
    const alarmsTableBody = document.querySelector('#alarmsTable tbody');
    const editAlarmModal = document.getElementById('editAlarmModal');
    const deleteConfirmModal = document.getElementById('deleteConfirmModal');
    const alarmHistoryModal = document.getElementById('alarmHistoryModal');
    const alarmHistoryContent = document.getElementById('alarmHistoryContent');
    const successMessage = document.getElementById('successMessage');
    let currentAlarmRow = null;

    // Function to add a single alarm to the table
    function addAlarmToTable(alarm) {
      const newRow = alarmsTableBody.insertRow();
      newRow.insertCell(0).textContent = alarm.node;
      newRow.insertCell(1).textContent = alarm.description;
      newRow.insertCell(2).textContent = alarm.status;
      newRow.insertCell(3).textContent = alarm.timestamp;
      const actionsCell = newRow.insertCell(4);
      actionsCell.innerHTML = 
        '<button class="action-btn edit">Edit</button>' +
        '<button class="action-btn delete">Delete</button>' +
        '<button class="action-btn history">History</button>';

      // Add status class
      if (alarm.status === 'Active') {
        newRow.classList.add('status-active');
      } else if (alarm.status === 'Clear') {
        newRow.classList.add('status-clear');
      }
    }

    // Load pending alarms from localStorage on page load
    window.addEventListener('load', () => {
      let pendingAlarms = JSON.parse(localStorage.getItem('pendingAlarms') || '[]');
      if (pendingAlarms.length > 0) {
        pendingAlarms.forEach(alarm => addAlarmToTable(alarm));
        localStorage.removeItem('pendingAlarms'); // Clear once processed
      }
    });

    // Event delegation for Edit, Delete, and History buttons on alarmsTable
    alarmsTableBody.addEventListener('click', function(event) {
      if (event.target.classList.contains('action-btn')) {
        const button = event.target;
        const row = button.closest('tr');
        currentAlarmRow = row;

        if (button.classList.contains('edit')) {
          document.getElementById('editAlarmNode').value = row.cells[0].textContent;
          document.getElementById('editAlarmDesc').value = row.cells[1].textContent;
          document.getElementById('editAlarmStatus').value = row.cells[2].textContent;
          document.getElementById('editAlarmTimestamp').value = row.cells[3].textContent;
          editAlarmModal.style.display = 'block';
        } else if (button.classList.contains('delete')) {
          deleteConfirmModal.dataset.rowIndex = Array.from(row.parentNode.children).indexOf(row);
          deleteConfirmModal.style.display = 'block';
        } else if (button.classList.contains('history')) {
          const alarmNode = row.cells[0].textContent;
          const alarmDesc = row.cells[1].textContent;
          
          // Using static placeholder timestamps to avoid any JSP EL parsing issues
          const history = [
            '[Static Timestamp 1] Alarm for ' + alarmNode + ' (' + alarmDesc + ') status changed to Clear.',
            '[Static Timestamp 2] Alarm for ' + alarmNode + ' (' + alarmDesc + ') status changed to Active.',
            '[Static Timestamp 3] Alarm for ' + alarmNode + ' (' + alarmDesc + ') created.'
          ];
          alarmHistoryContent.innerHTML = history.map(item => '<p>' + item + '</p>').join('');
          alarmHistoryModal.style.display = 'block';
        }
      }
    });

    // Close modals using X button
    document.querySelectorAll('.close').forEach(button => {
      button.addEventListener('click', () => {
        editAlarmModal.style.display = 'none';
        deleteConfirmModal.style.display = 'none';
        alarmHistoryModal.style.display = 'none'; // Close history modal
      });
    });

    // Close modals when clicking outside
    window.addEventListener('click', (event) => {
      if (event.target === editAlarmModal) {
        editAlarmModal.style.display = 'none';
      }
      if (event.target === deleteConfirmModal) {
        deleteConfirmModal.style.display = 'none';
      }
      if (event.target === alarmHistoryModal) { // Close history modal
        alarmHistoryModal.style.display = 'none';
      }
    });

    // Handle Edit Alarm Save button
    editAlarmModal.querySelector('.button').addEventListener('click', () => {
      if (currentAlarmRow) {
        currentAlarmRow.cells[0].textContent = document.getElementById('editAlarmNode').value;
        currentAlarmRow.cells[1].textContent = document.getElementById('editAlarmDesc').value;
        currentAlarmRow.cells[2].textContent = document.getElementById('editAlarmStatus').value;
        currentAlarmRow.cells[3].textContent = document.getElementById('editAlarmTimestamp').value;

        // Update status class
        currentAlarmRow.classList.remove('status-active', 'status-clear'); // Remove existing
        const newStatus = document.getElementById('editAlarmStatus').value;
        if (newStatus === 'Active') {
          currentAlarmRow.classList.add('status-active');
        } else if (newStatus === 'Clear') {
          currentAlarmRow.classList.add('status-clear');
        }

        showSuccessMessage('Alarm updated successfully!');
        editAlarmModal.style.display = 'none';
      }
    });

    // Handle Delete Alarm Yes button
    deleteConfirmModal.querySelector('.button').addEventListener('click', () => {
      const rowIndex = deleteConfirmModal.dataset.rowIndex;
      if (rowIndex !== undefined && alarmsTableBody.children[rowIndex]) {
        alarmsTableBody.removeChild(alarmsTableBody.children[rowIndex]);
        showSuccessMessage('Alarm deleted successfully!');
      }
      deleteConfirmModal.style.display = 'none';
    });

    // Handle Delete Alarm Cancel button
    deleteConfirmModal.querySelectorAll('.button')[1].addEventListener('click', () => {
      deleteConfirmModal.style.display = 'none';
    });

    // Handle History Modal Close button
    alarmHistoryModal.querySelector('.button').addEventListener('click', function() {
      alarmHistoryModal.style.display = 'none';
    });

    // Function to display success message
    function showSuccessMessage(message) {
      successMessage.textContent = message;
      successMessage.classList.add('show');
      setTimeout(() => {
        successMessage.classList.remove('show');
      }, 3000);
    }

    // Handle Add Alarm button
    document.getElementById('addAlarmButton').addEventListener('click', (event) => {
      event.preventDefault();

      const alarmNode = document.getElementById('alarmNode').value;
      const alarmDesc = document.getElementById('alarmDesc').value;
      const alarmStatus = document.getElementById('alarmStatus').value;
      const alarmTimestamp = document.getElementById('alarmTimestamp').value;

      if (alarmNode && alarmDesc && alarmStatus && alarmTimestamp) {
        const newAlarm = {
          node: alarmNode,
          description: alarmDesc,
          status: alarmStatus,
          timestamp: alarmTimestamp
        };
        addAlarmToTable(newAlarm);

        // Clear input fields
        document.getElementById('alarmNode').value = '';
        document.getElementById('alarmDesc').value = '';
        document.getElementById('alarmTimestamp').value = '';

        showSuccessMessage('Alarm added successfully!');
        console.log('Alarm added:', newAlarm);
      } else {
        alert('Please fill in all alarm details.');
      }
    });
  </script>
</body>
</html>