<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Check if user is logged in
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Model.User user = (Model.User) session.getAttribute("user");
%>
<html>
<head>
    <title>Welcome</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .welcome-message {
            margin-bottom: 20px;
            color: #333;
        }
        .user-info {
            margin-bottom: 20px;
            padding: 15px;
            background-color: #f8f9fa;
            border-radius: 4px;
            border: 1px solid #dee2e6;
        }
        .logout-form {
            margin-top: 20px;
            text-align: center;
        }
        .logout-button {
            padding: 10px 20px;
            background-color: #dc3545;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.2s;
        }
        .logout-button:hover {
            background-color: #c82333;
        }
        .user-info p {
            margin: 8px 0;
            color: #495057;
        }
        .user-info strong {
            color: #212529;
        }
    </style>
    <script>
        // Prevent browser back button from showing the page after logout
        window.onload = function() {
            window.history.pushState({ noBackExitsApp: true }, '');
        };
        window.onpageshow = function(event) {
            if (event.persisted) {
                window.location.href = '<%= request.getContextPath() %>/login';
            }
        };
    </script>
</head>
<body>
    <div class="container">
        <div class="welcome-message">
            <h2>Welcome, <%= user.getfName() %> <%= user.getlName() %>!</h2>
        </div>
        
        <div class="user-info">
            <h3>Your Information:</h3>
            <p><strong>First Name:</strong> <%= user.getfName() %></p>
            <p><strong>Last Name:</strong> <%= user.getlName() %></p>
            <p><strong>Phone Number:</strong> <%= user.getPhoneNumber() %></p>
            <p><strong>National ID:</strong> <%= user.getNationalID() %></p>
            <p><strong>Date of Birth:</strong> <%= user.getDOB() %></p>
        </div>

        <form action="<%= request.getContextPath() %>/logout" method="post" class="logout-form">
            <button type="submit" class="logout-button">Logout</button>
        </form>
    </div>
</body>
</html> 