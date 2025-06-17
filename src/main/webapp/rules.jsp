<select id="ruleNodeId">
  <option value="">Select Node</option>
</select>

let nodeMap = {};

fetch('${pageContext.request.contextPath}/ActionServlet?action=getNodes')
  .then(response => response.json())
  .then(data => {
    const dropdown = document.getElementById('ruleNodeId');
    data.forEach(node => {
      nodeMap[node.nodeId] = node.nodeName;
      console.log('Caching node:', node.nodeId, node.nodeName);
      const option = document.createElement('option');
      option.value = node.nodeId;
      option.textContent = node.nodeName;
      option.style.backgroundColor = 'rgb(143, 142, 142)';
      dropdown.appendChild(option);
    });
  })
  .catch(() => showMessage('Failed to load nodes.', 'error'));

fetch('AddRule', {
// ... existing code ...
})
.then(res => {
  if (res.success) {
    const nodeName = res.nodeName;
    addRuleToTable({
      id: res.insertId || '',
      nodeName: nodeName,
      actionType: actionType,
      rootEmail: rootEmail
    });
    document.getElementById('ruleActionType').value = '';
    document.getElementById('ruleTarget').value = '';
    document.getElementById('ruleNodeId').selectedIndex = 0;
    showMessage('Rule added successfully!', 'success');
  } else {
    showMessage('Failed to add rule.', 'error');
  }
});

/* -------- Add Rule button (NOW POSTS TO BACKEND) -------- */
document.getElementById('addRuleButton').addEventListener('click', evt => {
  evt.preventDefault();

  const nodeId     = document.getElementById('ruleNodeId').value;
  const actionType = document.getElementById('ruleActionType').value.trim();
  const target     = document.getElementById('ruleTarget').value.trim();

  if (!nodeId || !actionType || !target) {
    showMessage('Please fill in all rule details.', 'error');
    return;
  }
  if (actionType.toLowerCase() === 'email') {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!re.test(target)) {
      showMessage('Invalid email format.', 'error');
      return;
    }
  }

  /* post to backend */
  fetch('${pageContext.request.contextPath}/AddRule', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      serverId:  nodeId,
      actionType: actionType,
      rootEmail:  target
    })
  })
  .then(r => r.json())
  .then(res => {
    if (res.success) {
      const nodeName = res.nodeName;
      addRuleToTable({
        id: res.insertId || '',
        nodeName: nodeName,
        actionType: actionType,
        rootEmail: target
      });
      document.getElementById('ruleActionType').value = '';
      document.getElementById('ruleTarget').value = '';
      document.getElementById('ruleNodeId').selectedIndex = 0;

      showMessage('Rule added successfully!', 'success');
    } else {
      showMessage('Failed to add rule: ' + (res.message || 'DB error'), 'error');
    }
  })
  .catch(err => {
    console.error('Error adding rule:', err);
    showMessage('Server error while adding rule.', 'error');
  });
});

document.getElementById("saveRuleBtn").addEventListener("click", function () {
    const serverId = document.getElementById("serverSelect").value;
    const actionType = document.getElementById("actionTypeInput").value.trim();
    const rootEmail = document.getElementById("rootEmailInput").value.trim();

    if (!serverId || !actionType || !rootEmail) {
        alert("Please fill all fields");
        return;
    }

    fetch("AddRule", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            serverId: parseInt(serverId),
            actionType: actionType,
            rootEmail: rootEmail
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Add the new row directly using the response's nodeName
            const tbody = document.querySelector("#rulesTable tbody");
            const newRow = document.createElement("tr");
            newRow.innerHTML = `
                <td>${data.insertId}</td>
                <td>${data.nodeName}</td>
                <td>${actionType}</td>
                <td>${rootEmail}</td>
                <td>
                    <button class="edit-btn btn btn-sm btn-warning" data-id="${data.insertId}">Edit</button>
                    <button class="delete-btn btn btn-sm btn-danger" data-id="${data.insertId}">Delete</button>
                </td>
            `;
            tbody.appendChild(newRow);

            // Clear modal inputs
            document.getElementById("actionTypeInput").value = "";
            document.getElementById("rootEmailInput").value = "";
            document.getElementById("serverSelect").selectedIndex = 0;
            // Close modal
            bootstrap.Modal.getInstance(document.getElementById("addRuleModal")).hide();
        } else {
            alert("Failed to add rule: " + (data.message || "Unknown error"));
        }
    })
    .catch(error => {
        console.error("Error:", error);
        alert("Error adding rule.");
    });
});

// ... existing code ... 